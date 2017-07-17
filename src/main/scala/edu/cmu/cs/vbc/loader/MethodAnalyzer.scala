package edu.cmu.cs.vbc.loader

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.analysis.{Analyzer, BasicInterpreter, BasicValue, Frame}
import org.objectweb.asm.tree.{AbstractInsnNode, JumpInsnNode, LabelNode, MethodNode}

import scala.collection.mutable

/**
  * Recognize CFG blocks in a method
  *
  * @param mn method under analysis
  */
class MethodAnalyzer(owner: String, mn: MethodNode) extends Analyzer[BasicValue](new BasicInterpreter()) {
  /**
    * Each element of the list represents the first instruction of a block
    */
  var blocks: scala.collection.mutable.SortedSet[Int] = scala.collection.mutable.SortedSet()
  var edges: Map[Int, Set[Int]] = Map()
  val ENTRY: Int = -1
  val EXIT: Int = mn.instructions.size() - 1

  /**
    * LabelNode -> Block Index
    */
  var label2BlockIdx = Map[LabelNode, Int]()

  def analyze(): Array[Frame[BasicValue]] = {
    //    println("Method: " + mn.name)
    analyze(owner, mn)
  }

  // called on each insn in the insn list
  override protected def newControlFlowEdge(insn: Int, successor: Int): Unit = {
    if (blocks.isEmpty) {
      blocks = blocks + insn // first instruction
      edges += (ENTRY -> Set(insn))
    }
    else {
      mn.instructions.get(insn) match {
        case jump: JumpInsnNode => {
          blocks = blocks + successor
          blocks = blocks + (insn + 1) // the instruction after the jump

          val jumpIsUnconditional = jump.getOpcode == Opcodes.GOTO
          val edgesTo = if (jumpIsUnconditional) Set(successor) else Set(successor, insn + 1)
          edges += (insn -> edgesTo)

          /*
          Handle fall-thru edges in if statements. E.g:
               if (x)
               then: ... goto(END); // jump to END
               else: ...            // fall thru to END -- this won't be detected by just finding jumps
          END: ...
           */
          mn.instructions.get(successor - 1) match {
            case _: JumpInsnNode => // nothing to do - this jump will be handled normally
            case _ => {
              // The insn before the successor of this jump is not a jump: this means that it is
              // a fall-thru to the successor block. This fall-thru must be added as an edge
              edges += ((successor - 1) -> Set(successor))
            }
          }
        }
        case _ => // do nothing
      }
    }
  }

  def printBlocksIdx = blocks.foreach((x: Int) => {
    println(x)
  })

  /**
    * If the start of a block is a label, construct a map between LabelNode and block index.
    *
    */
  def validate() = blocks.foreach((x: Int) => {
    val i = mn.instructions.get(x)
    if (i.isInstanceOf[LabelNode]) {
      label2BlockIdx += (i.asInstanceOf[LabelNode] -> blocks.toVector.indexOf(x))
    }
  })

  lazy val bBlocks: Set[BasicBlock] = {
    blocks.map(blockStart => {
      val (foundBlockEnd, foundSucc) =
        if (edges.size > 1)
          edges.filter(e => e._1 != ENTRY)
               .reduce((closestEdge, edge) => {
                 val edgeIsCloserDescendent =
                   (edge._1 > blockStart && edge._1 < closestEdge._1 || closestEdge._1 < blockStart)

                 if (edgeIsCloserDescendent) edge else closestEdge
               })
        else
          (mn.instructions.size() - 1, Set.empty[Int])
      val (blockEnd, succ) =
        if (foundBlockEnd <= blockStart) (EXIT, Set.empty[Int])
        else                             (foundBlockEnd, foundSucc)

      val pred = edges.filter(edge => edge._2.contains(blockStart) && edge._1 != ENTRY).keySet

      BasicBlock(blockStart, blockEnd, pred, succ, mn.instructions.toArray.slice(blockStart, blockEnd + 1).toList)
    }).toSet
  }
  lazy val blockStarts: Map[Int, BasicBlock] =
    bBlocks.map(bb => bb.startLine -> bb).toMap
  lazy val blockEnds: Map[Int, BasicBlock] =
    bBlocks.map(bb => bb.endLine -> bb).toMap
  lazy val edgesByBlock: Map[BasicBlock, Set[BasicBlock]] =
    edges.collect {
      case e if e._1 != ENTRY => bBlocks.find(b => b.endLine == e._1).get -> e._2.map(blockStarts(_))
    }

  lazy val retreatingEdges: Map[BasicBlock, Set[BasicBlock]] = {
    val dfns = {
      var blockVisited = bBlocks.map(_ -> false).toMap
      var dfstEdges = Map[BasicBlock, Set[BasicBlock]]()
      var depthFirstNums = Map[BasicBlock, Int]()
      var c = bBlocks.size

      def search(block: BasicBlock): Unit = {
        blockVisited = blockVisited.updated(block, true)
        block.successors.map(line => blockStarts(line)).foreach(succ =>
          if (!blockVisited(succ)) {
            dfstEdges = dfstEdges.updated(block, dfstEdges.getOrElse(block, Set()) + succ)
            search(succ)
          })
        depthFirstNums += (block -> c)
        c -= 1
      }

      if (bBlocks.nonEmpty)
        search(bBlocks.find(b => b.predecessors == Set.empty[Int]).get)
      depthFirstNums
    }

    edgesByBlock.map(e => e._1 -> e._2.filter(succ => dfns(e._1) >= dfns(succ))).filter(e => e._2.nonEmpty)
  }

  lazy val loops: Set[Loop] = {
    // Collect all blocks reachable from BLOCK - where NEIGHBORSOF defines the blocks "next to" a given block
    def reachableFrom(block: BasicBlock, neighborsOf: (BasicBlock => Iterable[BasicBlock])) ={
      var collected = Set.empty[BasicBlock]
      var blockQ = mutable.Queue(block)
      while (blockQ.nonEmpty) {
        val b = blockQ.dequeue
        if (!collected.contains(b)) {
          blockQ ++= neighborsOf(b)
        }
        collected += b
      }
      collected
    }
    val descendantsOf = reachableFrom(_: BasicBlock, b => b.successors.map(blockStarts(_)))
    val ancestorsOf = reachableFrom(_: BasicBlock, b => b.predecessors.map(blockEnds(_)))

    // Loop nodes: get nodes of body + retreatFrom
    //   ,--------------------------.
    // start -> ... body ... -> retreatFrom
    def blocksBetween(start: BasicBlock, retreatFrom: BasicBlock): Set[BasicBlock] = {
      (descendantsOf(start) intersect ancestorsOf(retreatFrom)) + retreatFrom
    }

    retreatingEdges.flatMap(edge => {
      val from = edge._1
      edge._2.map(to => Loop(to, blocksBetween(to, from) - to))
    }).toSet
  }

  def insertInsns(after: AbstractInsnNode, insns: List[AbstractInsnNode]): Unit = {
    val insert = mn.instructions.insert(after, _: AbstractInsnNode)
    insns.reverse.foreach(insn => insert(insn))
  }
}

case class BasicBlock(startLine: Int, endLine: Int, predecessors: Set[Int], successors: Set[Int], instructions: List[AbstractInsnNode])
case class Loop(entry: BasicBlock, body: Set[BasicBlock])
