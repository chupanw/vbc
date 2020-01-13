package edu.cmu.cs.vbc.analysis

import com.typesafe.scalalogging.LazyLogging
import edu.cmu.cs.vbc.config.Settings
import edu.cmu.cs.vbc.vbytecode.{MethodDesc, TypeDesc}
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._
import org.objectweb.asm.tree.analysis.{Analyzer, BasicInterpreter, BasicValue, Frame}

import scala.collection.mutable

/**
  * Chop a method into a few smaller ones to avoid method too large problem.
  *
  * This step happens before transformation of variational bytecode
  *
  * @todo For performance comparison, we might want to save the chopped version and use it instead of the original one
  */
class MethodChopper(val owner: String, val mn: MethodNode) extends LazyLogging {

  /**
    * Maximum number of instructions we allow in the original method
    *
    * todo: need to experiment on a reasonable value
    * todo: should this go into [[Settings]]?
    */
  val limit = 2000  // triangle had 5259 and failed


  def reverseGraph(blocks: List[BasicBlock]): List[BasicBlock] = {
    def findBlock(i: Int, bs: List[BasicBlock]): BasicBlock = bs.find(_.i == i).get
    val reversed = blocks.map(x => BasicBlock(x.i))
    blocks.foreach {x =>
      x.successors.foreach(y => findBlock(y.i, reversed).successors.append(findBlock(x.i, reversed)))
    }
    reversed
  }

  def shouldChop(): Boolean = mn.instructions.size() > limit

  /**
    * Split the original method into a few smaller methods
    */
  def chop(): List[MethodNode] = {
    //todo: avoid exception handling regions and don't move handler blocks to the next methods
    if (shouldChop()) {
      val chopPoint = findChopPoint()
      if (chopPoint.isDefined) {
        val chopPointIndex = chopPoint.get
        logger.info(s"chopping method ${mn.name} of class $owner at index $chopPointIndex")
        val instructions: Array[AbstractInsnNode] = mn.instructions.toArray
        val (headInsns, tailInsns) = instructions.splitAt(chopPointIndex + 1) // both sequence should include the label
        val oldLabel = headInsns.last.asInstanceOf[LabelNode]
        val newLabel = new LabelNode()
        tailInsns.foreach {
          case node: JumpInsnNode if node.label == oldLabel => node.label = newLabel
          case _ =>
        }
        val tailInsnsWithStartingLabel = newLabel +: tailInsns
        assert(instructions(chopPointIndex).isInstanceOf[LabelNode], "Chop point not a label, might affect jumping")
        val isStatic = (mn.access & Opcodes.ACC_STATIC) > 0
        // load local variables
        val loadSeq = generateLoadSequence(headInsns)
        // create new method
        val originalDesc = MethodDesc(mn.desc)
        val newDesc = generateDescriptorFromLoadSeq(loadSeq, if(originalDesc.isReturnVoid) "V" else originalDesc.getReturnType.get.desc)
        val newName = mn.name + System.nanoTime()
        val newMethod = new MethodNode(mn.access, newName, newDesc, null, Array())
        mn.maxStack = math.max(mn.maxStack, loadSeq.length + (if (isStatic) 0 else 1))
        newMethod.maxLocals = mn.maxLocals
        newMethod.maxStack = mn.maxStack
        tailInsnsWithStartingLabel.foreach(newMethod.instructions.add(_))
        // modify original method
        mn.instructions.clear()
        headInsns.foreach(mn.instructions.add(_))
        val invokeType: Int = if (isStatic) Opcodes.INVOKESTATIC else Opcodes.INVOKEVIRTUAL // todo: might be unsafe if this is a long interface method
        if (!isStatic)
          mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
        loadSeq.foreach(mn.instructions.add(_))
        mn.instructions.add(new MethodInsnNode(invokeType, owner, newName, newDesc, false))
        mn.instructions.add(generateReturnInsn(mn.desc))
        val returning = mn :: new MethodChopper(owner, newMethod).chop()
        returning
      } else {
        logger.info("failed to find a chop point for method " + mn.name + " in class " + owner)
        List(mn)
      }
    } else {
      List(mn)
    }
  }

  def generateLoadSequence(insns: Array[AbstractInsnNode]): Array[AbstractInsnNode] = {
    val loadMap = mutable.Map[Int, AbstractInsnNode]()
    val offset = if ((mn.access & Opcodes.ACC_STATIC) > 0) 0 else 1
    // method parameters
    MethodDesc(mn.desc).getArgs.zipWithIndex.foreach(p => p._1.desc match {
      case "Z" | "B" | "C" | "S" | "I" => loadMap.put(p._2 + offset, new VarInsnNode(Opcodes.ILOAD, p._2 + offset))
      case "F" => loadMap.put(p._2 + offset, new VarInsnNode(Opcodes.FLOAD, p._2 + offset))
      case "J" => loadMap.put(p._2 + offset, new VarInsnNode(Opcodes.LLOAD, p._2 + offset))
      case "D" => loadMap.put(p._2 + offset, new VarInsnNode(Opcodes.DLOAD, p._2 + offset))
      case _ => loadMap.put(p._2 + offset, new VarInsnNode(Opcodes.ALOAD, p._2 + offset))
    })
    // scan store instructions
    def store2Load(storeOp: Int): Int = storeOp match {
      case Opcodes.ISTORE => Opcodes.ILOAD
      case Opcodes.FSTORE => Opcodes.FLOAD
      case Opcodes.LSTORE => Opcodes.LLOAD
      case Opcodes.DSTORE => Opcodes.DLOAD
      case Opcodes.ASTORE => Opcodes.ALOAD
      case _ => throw new RuntimeException("Not a store: " + storeOp)
    }
    insns.filter(x => x.getOpcode >= Opcodes.ISTORE && x.getOpcode <= Opcodes.ASTORE).foreach {x =>
      val lv = x.asInstanceOf[VarInsnNode].`var`
      if (loadMap.contains(lv)) {
        assert(loadMap(lv).getOpcode == store2Load(x.getOpcode), s"type mismatch at local variable $lv in method $mn of class $owner")
      } else {
        loadMap.put(lv, new VarInsnNode(store2Load(x.getOpcode), lv))
      }
    }
    // sanity check that local variable numbers are consecutive
    val lvs = loadMap.keySet.toList.sorted
    for (i <- 0 until lvs.size - 1) assert(lvs(i) - lvs(i+1) == -1, s"missing a local variable when chopping method $mn of class $owner")
    // return ordered value set
    loadMap.toList.sortWith((p1, p2) => p1._1 < p2._1).map(_._2).toArray
  }

  def generateDescriptorFromLoadSeq(loadSeq: Array[AbstractInsnNode], returnType: String): String = {
    loadSeq.map(_.getOpcode).map {
      case Opcodes.ILOAD => "I"
      case Opcodes.FLOAD => "F"
      case Opcodes.LLOAD => "J"
      case Opcodes.DLOAD => "D"
      case Opcodes.ALOAD => "Ljava/lang/Object;"
    } mkString("(", "", s")$returnType")
  }

  def generateReturnInsn(desc: String): AbstractInsnNode = MethodDesc(desc).getReturnType match {
    case None => new InsnNode(Opcodes.RETURN)
    case Some(TypeDesc("I")) | Some(TypeDesc("Z")) | Some(TypeDesc("B")) | Some(TypeDesc("C")) | Some(TypeDesc("S")) => new InsnNode(Opcodes.IRETURN)
    case Some(TypeDesc("J")) => new InsnNode(Opcodes.LRETURN)
    case Some(TypeDesc("F")) => new InsnNode(Opcodes.FRETURN)
    case Some(TypeDesc("D")) => new InsnNode(Opcodes.DRETURN)
    case Some(x) => new InsnNode(Opcodes.ARETURN)
  }

  /**
    *  Find a post-dominator block that is closest to the limit, which might not exist in rare cases
    *
    *  @return the index of the instruction that starts going into the next method
    */
  def findChopPoint(): Option[Int] = {
    val blocks: List[BasicBlock] = new BasicBlockAnalyzer(owner, mn).analyze()
    val postDoms = computePostDominators(blocks)
    println(toDot(blocks, postDoms))
    None
//    val b = postDoms.sorted.find(x => x.i > limit && x.i < mn.instructions.size())
//    if (b.isDefined) Some(b.get.i) else None
  }

  /**
    * Simple algorithm for finding all dominators that dominate the exit node
    *
    * We assume that the head and last represent entrance and exit of the method, respectively
    *
    * fixme: revert back to calculate ONLY postdominators
    *
    * @param blocks  all basic blocks and edges
    * @return A list of basic blocks that dominates the exit node
    */
  def computePostDominators(blocks: List[BasicBlock]): List[BasicBlock] = {
    val reverseBlocks: List[BasicBlock] = reverseGraph(blocks)
    val cache = mutable.Map[BasicBlock, List[BasicBlock]]()
    def isTerminal(b: BasicBlock): Boolean = b.successors.isEmpty || (b.successors.size == 1 && b.successors.head.successors.isEmpty)
    def go(b: BasicBlock): List[BasicBlock] = {
      cache.getOrElseUpdate(b, {
        if (isTerminal(b) || !b.successors.exists(x => x.i < b.i)) {
          List(b)
        }
        else {
          val paths = b.successors.filter(x => x.i < b.i).map(x => go(x)).filter(x => isTerminal(x.last))
          val pathGroups = paths.groupBy(_.last)
          val intersectWithDiffEnds = pathGroups.toList.map(g => g._2.foldLeft(g._2.head)(_ intersect _))
          b :: intersectWithDiffEnds.foldLeft(List[BasicBlock]())(_ union _)
//          b :: paths.foldLeft(paths.head)(_ intersect _)
        }
      })
    }
    go(reverseBlocks.last)
  }

  def toDot(blocks: List[BasicBlock], postDoms: List[BasicBlock]): String = {
    val stmts = blocks.zipWithIndex.flatMap(p => {
      val b = p._1
      val index = p._2
      val node = if (postDoms.contains(b)) s"$index [color = red]" else s"$index"
      val edges = b.successors.toList.map(x => {
        s"$index -> ${blocks.indexWhere(_.i == x.i)}"
      })
      (node :: edges).filterNot(_ == "")
    })
    s"digraph ${mn.name} {\n" + stmts.mkString("\t", "\n\t", "\n") + "}"
  }
}


case class BasicBlock(i: Int) extends Ordered[BasicBlock] {
  val successors: mutable.ArrayBuffer[BasicBlock] = mutable.ArrayBuffer[BasicBlock]()
  override def compare(that: BasicBlock): Int = this.i - that.i
}

/**
  * Construct basic blocks from a method
  *
  */
class BasicBlockAnalyzer(owner: String, mn: MethodNode) extends Analyzer[BasicValue](new BasicInterpreter()) {
  /**
    * Each element of the list represents the first instruction of a block
    */
  private val blocks = mutable.TreeSet[BasicBlock]()
  private val edges = mutable.ArrayBuffer[(Int, Int)]()

  def analyze(): List[BasicBlock] = {
    analyze(owner, mn)
    addJumps()
    addEndNode()
    assert(sanityCheck(blocks), "basic block analysis failed sanity check...")
    blocks.toList
  }

  /**
    * One call of this describes only one edge
    *
    * @param insn
    *            an instruction index.
    * @param successor
    */
  override protected def newControlFlowEdge(insn: Int, successor: Int): Unit = {
    edges.append((insn, successor))
    if (blocks.isEmpty)
      blocks += BasicBlock(insn)
    else if (successor != insn + 1 || mn.instructions.get(insn).isInstanceOf[JumpInsnNode]) {
      val next = BasicBlock(insn + 1)
      val jump = BasicBlock(successor)
      blocks += next
      blocks += jump
    }
  }

  def addJumps(): Unit = {
    for ((insn, successor) <- edges) {
      val current = findCurrentBlock(insn, blocks)
      val isGOTO = mn.instructions.get(insn).getOpcode == Opcodes.GOTO
      findBlock(successor).foreach(x => current.successors.append(x))
    }
  }

  /**
    * Explicitly add unconditional jumps between consecutive blocks
    */
  def addEndNode(): Unit = {
    val sorted = blocks.toArray
    val end = BasicBlock(Int.MaxValue)
    blocks += end
    sorted.foreach(x => if (x.successors.isEmpty) x.successors.append(end))
  }

  /**
    * Given an instruction index, return the basic block that contains this instruction
    */
  def findCurrentBlock(i: Int, blocks: mutable.TreeSet[BasicBlock]): BasicBlock = blocks.filter(_.i <= i).last

  def findBlock(i: Int): Option[BasicBlock] = blocks.find(_.i == i)

  def printBlocksIdx = blocks.foreach(println)

  def sanityCheck(blocks: mutable.TreeSet[BasicBlock]): Boolean = {
    val nonEmptySuccessor = blocks.init.isEmpty || blocks.init.forall(_.successors.nonEmpty)
    val emptyLast = blocks.last.successors.isEmpty
    nonEmptySuccessor && emptyLast
  }
}
