package edu.cmu.cs.vbc.vbytecode

import scala.collection.mutable

case class LoopAnalysis(method: VBCMethodNode) extends CFGAnalysis {
  lazy val edges: Map[Block, Set[Block]] = blocks.map(b => b -> getSuccessors(b)).toMap
  lazy val retreatingEdges: Map[Block, Set[Block]] = {
    val dfns = {
      var blockVisited = blocks.map(_ -> false).toMap
      var dfstEdges = Map[Block, Set[Block]]()
      var depthFirstNums = Map[Block, Int]()
      var c = blocks.size

      def search(block: Block): Unit = {
        blockVisited = blockVisited.updated(block, true)

        for { succ <- getSuccessors(block) } yield {
          if (!blockVisited(succ)) {
            dfstEdges = dfstEdges.updated(block, dfstEdges.getOrElse(block, Set()) + succ)
            search(succ)
          }
        }
        depthFirstNums += (block -> c)
        c -= 1
      }

      if (blocks.nonEmpty)
        search(blocks.find(getPredecessors(_).isEmpty).get)
      depthFirstNums
    }

    edges.map(e => e._1 -> e._2.filter(succ => dfns.getOrElse(e._1, -1) >= dfns.getOrElse(succ, -2)))
      .filter(e => e._2.nonEmpty)
  }
  lazy val loops: Set[Loop] = {
    val descendantsOf = reachableFrom(_: Block, getSuccessors)
    val ancestorsOf = reachableFrom(_: Block, getPredecessors)

    // Loop nodes: get nodes of body + retreatFrom
    //   ,--------------------------.
    // start -> ... body ... -> retreatFrom
    def blocksBetween(start: Block, retreatFrom: Block): Set[Block] = {
      (descendantsOf(start) intersect ancestorsOf(retreatFrom)) + retreatFrom
    }

    retreatingEdges.flatMap(edge => {
      val from = edge._1
      edge._2.map(to => Loop(to, blocksBetween(to, from) - to))
    }).toSet
  }
  // Collect all blocks reachable from BLOCK - where NEIGHBORSOF defines the blocks "next to" a given block
  private def reachableFrom(block: Block, neighborsOf: (Block => Iterable[Block])): Set[Block] = {
    var collected = Set.empty[Block]
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
}

case class Loop(entry: Block, body: Set[Block]) {
  override def toString: String = s"Loop {\nentry: $entry\nbody: $body\n}"
}
