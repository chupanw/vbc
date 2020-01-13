package edu.cmu.cs.vbc.vbytecode

import com.typesafe.scalalogging.LazyLogging
import edu.cmu.cs.vbc.analysis.{LoopDector, VBCAnalyzer, VBCFrame}
import edu.cmu.cs.vbc.config.Settings
import edu.cmu.cs.vbc.utils.{LiftUtils, Statistics}
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm.{Label, Type}

/**
  * Environment used during generation of the byte code and variational
  * byte code
  *
  * While initialized, VMethodEnv runs the stack analysis (@see [[VBCAnalyzer]]) and collect
  * essential information to:
  *
  * 1. decide whether or not to lift each instruction (see tagV section)
  * 1. handle unbalanced stack (see unbalanced stack section)
  */
class VMethodEnv(clazz: VBCClassNode, method: VBCMethodNode)
  extends MethodEnv(clazz, method)
  with VBlockAnalysis
  with LazyLogging{

  val exceptionVar: LocalVar = freshLocalVar(name = "$exceptionVar", desc = LiftUtils.vclasstype, LocalVar.initOneNull)
  val ctxParameter: Parameter = new Parameter(-1, "ctx", TypeDesc(LiftUtils.fexprclasstype))

  //////////////////////////////////////////////////
  // tagV
  //////////////////////////////////////////////////

  // Note: long and double are wrapped into objects, so no need to consider the fact that
  // each long or double takes two slots.
  override def parameterCount: Int = Type.getArgumentTypes(method.desc).size + (if (method.isStatic) 0 else 1)

  val blockTags = new Array[Boolean](blocks.length)

  /**
    * For each instruction, mark whether or not we need to lift it
    *
    * However, different instructions have different needs for tags. For example, for GETFIELD,
    * since we assume that all fields are Vs, the only concern is whether GETFIELD is performed on
    * a V. But for INVOKESPECIAL, we need more information, such as whether or not wrapping
    * method return value into a V, whether current method is invoked on a V, etc.
    *
    * Using an Int to represent instruction tag, we could have 32 different tags.
    */
  val instructionTags = new Array[Int](instructions.length)
  def resetAllTags(): Unit = {
    instructionTags.indices.foreach(i => instructionTags(i) = 0)
  }
  /**
    * Lift or not lift? Lifting means differently for different instructions.
    */
  val TAG_LIFT = 1
  /**
    * Method instructions only, whether we are having V arguments
    *
    * For some library methods that could not be lifted (e.g. java/lang/xxx), we try to invoke methods
    * with non-V arguments. But there might be cases where having V arguments is unavoidable (e.g. print
    * the value stored in local variable). For those cases, we use this tag to switch to model classes.
    */
  val TAG_HAS_VARG = 2
  /**
    * Whether we need to wrap value into a V.
    *
    * We could always wrap all the return values into Vs, but that is not efficient and hard to debug from
    * reading bytecode.
    */
  val TAG_NEED_V = 4
  /**
    * INVOKESPECIAL only. Handle special case like NEW DUP INVOKESPECIAL sequence.
    */
  val TAG_WRAP_DUPLICATE = 8

  /**
    * For instructions that we need to consider wrapping more than one values
    */
  val TAG_NEED_V2 = 16

  def setTag(instr: Instruction, tag: Int): Unit = {
    instructionTags(getInsnIdx(instr)) |= tag
  }

  def getTag(instr: Instruction, tag: Int): Boolean = {
    (instructionTags(getInsnIdx(instr)) & tag) != 0
  }

  // by default all elements are false
  def getInsnIdx(instr: Instruction) = instructions.indexWhere(_ eq instr)

  def shouldLiftInstr(instr: Instruction): Boolean = (instructionTags(getInsnIdx(instr)) & TAG_LIFT) != 0

  def getOrderedSuccessorsIndexes(b: Block): List[Int] = {
    var idxSet = Set[Int]()
    var visited = Set[Block]()
    val succ: Set[Block] = getSuccessors(b)
    var queue = List[Block]()
    queue  = queue ++ succ
    while (queue.nonEmpty) {
      val h = queue.head
      visited += h
      if (!idxSet.contains(getBlockIdx(h))) {
        idxSet += getBlockIdx(h)
        val succ = getSuccessors(h)
        queue = queue ++ succ
      }
      queue = queue.tail
    }
    idxSet.toList sortWith (_ < _)
  }

  def getMergePoint(b1: Block, b2: Block): Int = {
    val l1: List[Int] = getOrderedSuccessorsIndexes(b1)
    val l2: List[Int] = getOrderedSuccessorsIndexes(b2)
    l1.find(l2.contains(_)).get
  }

  def setLift(instr: Instruction): Unit = {
    instructionTags(getInsnIdx(instr)) |= TAG_LIFT
  }

  def isBlockUnderCtx(i: Instruction): Boolean = {
    val blockIdx = blocks.indexWhere((bb) => bb.instr.exists((insn) => insn eq i))
    blockTags(blockIdx)
  }

  //todo: refine this to consider store operations in different VBlocks, but the same method context
  def isLVStoredAcrossVBlocks(v: Variable): Boolean = {
    blocks.filter{
      b => b.instr.exists {
        case s: StoreInstruction => s.v == v
        case l: LoadInstruction => l.v == v
        case i: InstrIINC => i.variable == v
        case _ => false
      }
    }.map(getVBlock).distinct.size > 1
  }


  def getBlockForInstruction(i: Instruction): Block = blocks.find(_.instr contains i).get

  def isNonStaticL0(variable: Variable): Boolean = {
    if (method.isStatic) false
    else getVarIdxNoCtx(variable) == 0
  }

  /**
    * Track which local variables need to be lifted.
    *
    * This information is used in the initialization of local variables. We give each LV a
    * default initializer based on types in [[edu.cmu.cs.vbc.loader.Loader]], before our DFA. During the DFA,
    * we might realize that some LVs must be V type, and the default initializer might store values of
    * incorrect types from the perspective of JVM verifier.
    *
    * This can only be changed by store instructions.
    */
  private val liftingLVs: collection.mutable.Set[Variable] = collection.mutable.Set[Variable]()
  def liftLV(v: Variable): Unit = liftingLVs add v
  def isLiftingLV(v: Variable): Boolean = liftingLVs contains v
  def getLiftingLVSize = liftingLVs.size

  /**
    * determines whether a CFJ edge between two blocks could be executed
    * in a stricter context than the context in which `fromBlock` was executed.
    *
    * This is the case when the last instruction of the `fromBlock` is
    * a variational jump (eg if condition on V value) or a method call that
    * could throw a variational exception. In contrast, GOTO or a normal
    * exception is a nonvariational jump and the `toBlock` will continue
    * in the same condition as the `fromBlock`
    */
  def isVariationalJump(fromBlock: Block, toBlock: Block): Boolean = {
    //TODO this should be informed by results of the tagV analysis
    //for now it's simply returning true for all IF statements and method
    //invocations

    val lastInstr = fromBlock.instr.last
    val fromBlockIdx = getBlockIdx(fromBlock)
    val toBlockIdx = getBlockIdx(toBlock)

    lastInstr match {
      case InstrGOTO(t) => false // GOTO does not change the context
      //      case method: MethodInstruction => true // all methods can have conditional exceptions
      case jump: JumpInstruction =>
        if (hasTagVInfo && !shouldLiftInstr(jump))
          false
        else {
          // all possible jump targets are variational, exception edges are not
          val succ = jump.getSuccessor()
          toBlockIdx == succ._1.getOrElse(fromBlockIdx + 1) || succ._2.exists(_ == toBlockIdx)
        }
      case _ => false // exceptions do not change the context
    }
  }

  def hasTagVInfo: Boolean = instructionTags != null && instructionTags.exists(_ != 0)

  var nVBlockAnalysis: Int = 1
  val analyzer = new VBCAnalyzer(this)
  val framesBefore: Array[VBCFrame] = tagVWithVBlocksUpdate()
  val framesAfter: Array[VBCFrame] = analyzer.computeAfterFrames(framesBefore)
  val expectingVars: Map[Block, List[Variable]] = blocks.map(computeExpectingVars(_)).toMap

  // loop detection
  val loopDetector = new LoopDector(this)
  if (Settings.detectComplexLoop) {
    loopDetector.go()
    if (loopDetector.hasComplexLoop) {
      logger.warn("Loop detected: " + clazz.name + " " + method.name + method.desc)
      Statistics.nComplex += 1
    } else {
      Statistics.nSimple += 1
    }
  }

  def tagVWithVBlocksUpdate(): Array[VBCFrame] = {
    // at this point we have a rough version of vblocks
    var frames: Array[VBCFrame] = analyzer.computeBeforeFrames
    // recompute vblocks
    var vblocks: List[VBlock] = computeVBlocks()
    nVBlockAnalysis += 1
    // repeat until we reach a fixed point
    while (vblocks != this.vblocks) {
//      logger.info(s"\t\t ${this.vblocks.size} VBlocks, ${blocks.size} basic blocks")
//      logger.info("\t\t (Updating VBlocks)")
      this.vblocks = vblocks
      frames = analyzer.computeBeforeFrames
      vblocks = computeVBlocks()
      nVBlockAnalysis += 1
    }
    if (vblocks.size < blocks.size)
      logger.info(s"\t\t ${vblocks.size} VBlocks, ${blocks.size} basic blocks")
    frames
  }

  def getLeftVars(block: Block): List[Set[Variable]] = {
    val afterFrame = framesAfter(getInsnIdx(block.instr.last))
    val (succ1, succ2) = getJumpTargets(block)
    getVarSetList(Nil, succ1, succ2, afterFrame.getStackSize)
  }

  def getVarSetList(l: List[Set[Variable]], succ1: Option[Block], succ2: Option[Block], n: Int): List[Set[Variable]] =
    if (n == 0) l else getVarSetList(getVarSet(succ1, succ2, n) ::: l, succ1, succ2, n - 1)

  def getVarSet(succ1: Option[Block], succ2: Option[Block], n: Int): List[Set[Variable]] = {
    var set = Set[Variable]()
    if (succ1.isDefined) {
      val exp1 = getExpectingVars(succ1.get)
      if (exp1.nonEmpty) {
        set = set + exp1(n - 1)
      }
    }
    if (succ2.isDefined) {
      val exp2 = getExpectingVars(succ2.get)
      if (exp2.nonEmpty) {
        set = set + exp2(n - 1)
      }
    }
    List(set)
  }

  def getExpectingVars(block: Block): List[Variable] = expectingVars(block)

  def computeExpectingVars(block: Block): (Block, List[Variable]) = {
    val beforeFrame = framesBefore(getInsnIdx(block.instr.head))
    val newVars: List[Variable] = createNewVars(Nil, beforeFrame.getStackSize)
    (block, newVars)
  }

  //////////////////////////////////////////////////
  // Block management
  //////////////////////////////////////////////////

  // allocate a variable for each VBlock, except for the first, which can reuse the parameter slot
  // EBlocks have variables that do not need to be initialized (we cannot jump there directly)
  val vblockVars: Map[VBlock, Variable] =
  (for ((vblock, vblockidx) <- vblocks zip vblocks.indices) yield
    vblock -> freshLocalVar("$blockctx" + vblockidx, LiftUtils.fexprclasstype, LocalVar.initFalse)).toMap
//    (vblocks.head -> ctxParameter) //TODO should not reuse ctxParameter if we want to access that value later on, e.g., in VInstrRETURN

  def getVBlockVar(vblock: VBlock): Variable = vblockVars(vblock)

  def getVBlockVar(block: Block): Variable = {
    val vblock = vblocks.filter(_.allBlocks contains block)
    assert(vblock.size == 1, "expected the block to be in exactly one VBlock")
    vblockVars(vblock.head)
  }

  def getVBlockLabel(vblock: VBlock) = getBlockLabel(vblock.firstBlock)

  //////////////////////////////////////////////////
  // Utilities
  //////////////////////////////////////////////////

  def createNewVars(l: List[Variable], n: Int): List[Variable] =
    if (n == 0) l else createNewVars(List[Variable](freshLocalVar("$unbalancedstack" + n, LiftUtils.vclasstype, init = LocalVar.initOneNull)) ::: l, n - 1)

  def getBlockVarVIdx(block: Block): Int = getVarIdx(getVBlockVar(block))

  def getLocalVariables() = localVars

  def isConditionalField(owner: String, name: String, desc: String): Boolean = {
    val filter = (f: VBCFieldNode) => {
      owner == clazz.name && name == f.name && f.desc == desc && f.hasConditionalAnnotation()
    }
    clazz.fields.exists(filter)
  }

  def getVarIdxNoCtx(variable: Variable) = super.getVarIdx(variable)

  /**
    * all values shifted by 1 by the ctx parameter
    */
  override def getVarIdx(variable: Variable): Int =
    if (variable eq ctxParameter) parameterCount
    else {
      val idx = super.getVarIdx(variable)
      variable match {
        case p: Parameter =>
          // need to shift because we use V to represent long and double
          if (!method.isStatic && idx == 0)
            idx
          else
            MethodDesc(method.desc).getParameterIndex(idx, method.isStatic) + (if (method.isStatic) 0 else 1)
        case _: LocalVar =>
          if (idx >= parameterCount) idx + 1
          else idx
      }
    }

  val blockEndLabel: List[Label] = for (i <- blocks.indices.toList) yield new Label(s"END_OF_BLOCK_$i")
  val allHandlerBlocks: List[Block] = blocks.flatMap(_.exceptionHandlers).distinct.map(handler => getBlock(handler.handlerBlockIdx))
  def isHandlerBlock(b: Block): Boolean = allHandlerBlocks.contains(b)
  def getBlockEndLabel(b: Block): Label = blockEndLabel(blocks.indexOf(b))

  //////////////////////////////////////////////////
  // Statistics
  //////////////////////////////////////////////////
  val filteredInstructionTags: Array[Int] = instructionTags.indices.toArray.filter {i =>
    instructions(i) match {
      case _: InstrLINENUMBER => false
      case _: InstrNOP => false
      case _: InstrINIT_CONDITIONAL_FIELDS => false
      case _ => true
    }
  }.map(i => instructionTags(i))
  val filteredInstruction: Array[Instruction] = instructionTags.indices.toArray.filter {i =>
    instructions(i) match {
      case _: InstrLINENUMBER => false
      case _: InstrNOP => false
      case _: InstrINIT_CONDITIONAL_FIELDS => false
      case _ => instructionTags(i) == 0
    }
  }.map(i => instructions(i))
  Statistics.collectLiftingRatio(clazz.name, method.name, filteredInstructionTags.count(_ != 0), filteredInstructionTags.length)

  /** graphviz graph for debugging purposes */
  def toDot: String = {
    def blockname(b: Block) = "\"B" + getBlockIdx(b) + "\""
    def blocklabel(b: Block) = "B" + getBlockIdx(b) + ": v" + vblocks.indexOf(getVBlock(b)) + "\\n" +
      getExpectingVars(b).mkString("stack_load ", ", ", "\\n") +
      b.instr.mkString("\\n") + "\\n" +
      getLeftVars(b).mkString("stack_store ", ", ", "\\n")

    var result = "digraph G {\n"
    for (b <- blocks)
      result += s"  ${blockname(b)} [ shape=box label = " + "\"" + blocklabel(b) + "\" " + (if (isExceptionHandlerBlock(b)) " color=\"blue\"" else "") + "];\n"
    for (b <- blocks;
         succ <- getSuccessors(b))
      result += s"  ${blockname(b)} -> ${blockname(succ)}" +
        (if (isVariationalJump(b, succ)) "[ color=\"red\" label=\"V\" ]" else "") + ";\n"
    for (b <- blocks;
         (ex, handler) <- getExceptionHandlers(b))
      result += s"  ${blockname(b)} -> ${blockname(handler)}" +
        " [ color=\"blue\" label=\"" + ex + "\" ];\n"

    result + "}"
  }
}
