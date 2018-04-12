package edu.cmu.cs.vbc.analysis

import edu.cmu.cs.vbc.vbytecode.instructions.{Instruction, JumpInstruction}
import edu.cmu.cs.vbc.vbytecode.{Block, VBCMethodNode, VBlock, VMethodEnv}

import scala.collection.mutable

/**
  * Detect loops at the VBlock level
  */
class LoopDector(env: VMethodEnv) {
  val mn: VBCMethodNode = env.method
  val instructions = env.instructions
  /**
    * Number of instructions
    */
  val n: Int = instructions.length

  /**
    * Perform the loop detection analysis
    * @return Nothing, results are stored as a field in each VBlock
    */
  def go(): Unit = {
    // init
    val queue: mutable.Queue[Int] = mutable.Queue()

    def getVBlock(b: Block): VBlock = env.getVBlock(b)

    def isDifferentVBlocks(from: Block, to: Block): Boolean = {
      val fromV = env.getVBlock(from)
      val toV = env.getVBlock(to)
      if (fromV != toV) {
        assert(toV.firstBlock == to, "Jumping to a block that is not a VBlock head")
      }
      fromV == toV
    }

    def updateBlock(targetBlockIdx: Int, inVBs: Set[Block]): Unit = {
      val targetBlock = env.getBlock(targetBlockIdx)
      var changed = false
      if (targetBlock.dominators.isEmpty) {
        targetBlock.dominators = inVBs
        changed = true
      }
      else if (targetBlock.dominators.intersect(inVBs) != targetBlock.dominators) {
        targetBlock.dominators = targetBlock.dominators.intersect(inVBs)
        changed = true
      }

      if (changed && !(queue contains targetBlockIdx)) {
        queue.enqueue(targetBlockIdx)
      }
    }

    queue.enqueue(0)
    // data flow analysis
    while (queue.nonEmpty) {
      val blockIdx: Int = queue.dequeue()
      val block = env.getBlock(blockIdx)
      val instr: Instruction = block.instr.last
      instr match {
        case jump: JumpInstruction =>
          val (uncond, cond) = jump.getSuccessor()
          if (cond.isDefined) updateBlock(cond.get, block.dominators + block)
          if (uncond.isDefined)
            updateBlock(uncond.get, block.dominators + block)
          else
            updateBlock(blockIdx + 1, block.dominators + block)
        case i if !i.isReturnInstr =>
          updateBlock(blockIdx + 1, block.dominators + block)
        case _ =>
      }
    }
  }

  def hasLoop: Boolean = {
    env.method.body.blocks.exists {b =>
      (env.getSuccessors(b) intersect b.dominators).exists(x => !env.isSameVBlock(env.getVBlock(x), env.getVBlock(b))) ||
        env.getSuccessors(b).contains(b)  // self loop
    }
  }

  lazy val hasComplexLoop: Boolean = computeComplexLoop()

  def computeComplexLoop(): Boolean = {
    if (env.method.name == "nextToken" && env.clazz.name.contains("GeneratedJavaLexer"))
      return true // this method is taking too much time
    val backtrackingBlocks = env.method.body.blocks.filter {b =>
      (env.getSuccessors(b) intersect b.dominators).exists(x => !env.isSameVBlock(env.getVBlock(x), env.getVBlock(b))) ||
        env.getSuccessors(b).contains(b)  // self loop
    }
    backtrackingBlocks.exists(b => isComplexLoop(b, b, Set()))
  }

  def isComplexLoop(start: Block, x: Block, loopBody: Set[Block]): Boolean = {
    if (loopBody contains x) {
      if (start == x) {
        val hasTwoSuccessors = loopBody.filter(env.getSuccessors(_).size > 1)
        val hasTwoPredecessors = loopBody.filter(env.getPredecessors(_).size > 1)
        !(hasTwoPredecessors.size == 1 && hasTwoSuccessors.size == 1)
      }
      else {
        // we have entered another loop
        false
      }
    }
    else {
      val successors = env.getSuccessors(x)
      assert(successors.isEmpty || successors.size == 1 || successors.size == 2, "successor number is not 0, 1 or 2")
      if (successors.size == 2) {
        val path1 = isComplexLoop(start, successors.head, loopBody + x)
        val path2 = isComplexLoop(start, successors.last, loopBody + x)
        path1 || path2
      }
      else if (successors.size == 1) {
        isComplexLoop(start, successors.head, loopBody + x)
      }
      else {
        false // end of program
      }
    }
  }


}
