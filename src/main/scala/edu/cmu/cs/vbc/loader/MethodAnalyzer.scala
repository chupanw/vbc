package edu.cmu.cs.vbc.loader

import org.objectweb.asm.tree.analysis.{Analyzer, BasicInterpreter, BasicValue, Frame}
import org.objectweb.asm.tree.{JumpInsnNode, LabelNode, MethodNode}

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

  /**
    * LabelNode -> Block Index
    */
  var label2BlockIdx = Map[LabelNode, Int]()

  def analyze(): Array[Frame[BasicValue]] = {
    //    println("Method: " + mn.name)
    analyze(owner, mn)
  }

  override protected def newControlFlowEdge(insn: Int, successor: Int): Unit = {
    if (blocks.isEmpty) {
      blocks = blocks + insn // first instruction
      edges += (ENTRY -> Set(insn))
    }
    else {
      mn.instructions.get(insn) match {
        case jump: JumpInsnNode =>
          blocks = blocks + successor
          //          if (jump.getOpcode != GOTO)
            blocks = blocks + (insn + 1) // the instruction after the jump

          edges += (insn -> Set(successor, insn + 1))
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
      val (blockEnd, succ) =
        if (edges.size > 1)
          edges.reduce((closestEdge, edge) =>
            if (edge._1 > blockStart && edge._1 < closestEdge._1) edge else closestEdge)
        else
          (mn.instructions.size() - 1, Set.empty[Int])
      val pred = edges.filter(edge => edge._2.contains(blockStart) && edge._1 != ENTRY).keySet
      BasicBlock(blockStart, blockEnd, pred, succ)
    }).toSet
  }
  lazy val blockStarts: Map[Int, BasicBlock] =
    bBlocks.map(bb => bb.startLine -> bb).toMap
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

      search(bBlocks.find(b => b.predecessors == Set.empty[Int]).get)
      depthFirstNums
    }

    edgesByBlock.map(e => e._1 -> e._2.filter(succ => dfns(e._1) >= dfns(succ))).filter(e => e._2.nonEmpty)
  }

  lazy val loops: Set[Loop] = {
    //   ,--------------------------.
    // start -> ... body ... -> retreatFrom
    def blocksBetween(start: BasicBlock, retreatFrom: BasicBlock): Set[BasicBlock] = {
      if (start == retreatFrom ||                  // reached end of body
          start.startLine > retreatFrom.startLine) // broke out of the loop somehow
        Set()
      else {
        val edges = edgesByBlock(start)
        edges.map(e => blocksBetween(e, retreatFrom)).reduce((collected, blocks) => collected union blocks)
      }
    }

    retreatingEdges.flatMap(edge => {
      val from = edge._1
      edge._2.map(to => Loop(to, blocksBetween(to, from)))
    }).toSet
  }
}

case class BasicBlock(startLine: Int, endLine: Int, predecessors: Set[Int], successors: Set[Int])
case class Loop(entry: BasicBlock, body: Set[BasicBlock])
