package edu.cmu.cs.vbc.loader

import edu.cmu.cs.vbc.vbytecode.VBCHandler
import org.objectweb.asm.tree._
import org.objectweb.asm.tree.analysis.{Analyzer, BasicInterpreter, BasicValue}

import scala.collection.JavaConversions._
import scala.collection.immutable.SortedSet


/**
  * Recognize CFG blocks in a method
  *
  * All instructions within a block are executed in the same context.
  * There are no jumps within a block. A block may be terminated with an
  * exception, but all instructions within a block share the same exception
  * handlers.
  *
  * Works as follows:
  *
  * * Iterates over all instructions to find the the instructions that separate blocks
  * (jump instructions terminate a block; method calls terminate a block; labels
  * that are jump targets start a block; beginnings and ends of exception handling sections
  * as well as the labels indicating the handling code start and end blocks)
  *
  * * All blocks that are jump targets should start with a label. In `label2BlockIdx`
  * we store all the blocks that have known start labels.
  * There might be labels within blocks for line numbers etc, but we won't care about those.
  *
  * * We produce a map that associates each block with a list of exception handlers
  * for that block.
  *
  *
  * Method calls terminate a block due to variational exception handling. [Ignored for now]
  *
  * @param mn method under analysis
  */
class MethodCFGAnalyzer(owner: String, mn: MethodNode) extends Analyzer[BasicValue](new BasicInterpreter()) {
  /**
    * Each element of the list represents the first instruction of a block
    */
  var blocks: SortedSet[Int] = SortedSet()
  /**
    * LabelNode -> Block Index
    */
  var label2BlockIdx = Map[LabelNode, Int]()

  def instructions = mn.instructions


  def analyze(): Unit = {
    //    println("Method: " + mn.name)
    analyze(owner, mn)
//    splitForMethodInvocations() // ignored for now
    analyzeExceptions()
    if (blocks.nonEmpty && blocks.last >= instructions.size()) {
      blocks = blocks.init
    }
  }

  override protected def newControlFlowEdge(insn: Int, successor: Int): Unit = {
    if (blocks.isEmpty)
      blocks = blocks + insn // first instruction
    //        else if (successor != insn + 1) {
    else if (successor != insn + 1 || mn.instructions.get(insn).isInstanceOf[JumpInsnNode]) {
      blocks = blocks + (insn + 1) // the instruction after the jump
      blocks = blocks + successor // there is a jump
    }
  }

  /**
    * split into additional blocks for exception handlers
    */
  def analyzeExceptions(): Unit = {
    for (tryCatchBlock <- mn.tryCatchBlocks) {
      blocks += instructions.indexOf(tryCatchBlock.start)
      blocks += instructions.indexOf(tryCatchBlock.end)
      blocks += instructions.indexOf(tryCatchBlock.handler)
    }
  }

  /**
    * since every variational method may throw variational
    * exceptions, there is a potential split after every method call
    */
  def splitForMethodInvocations() = {
    for (idx <- 0 until instructions.size())
      instructions.get(idx) match {
        case m: MethodInsnNode => blocks += idx + 1
        case _ =>
      }

  }


  /**
    * If the start of a block is a label, construct a map between LabelNode and block index.
    *
    */
  def validate() = blocks.foreach((x: Int) => {
    val i = mn.instructions.get(x)
    i match {
      case node: LabelNode =>
        label2BlockIdx += (node -> instructionIdxToBlockIdx(x))
      case _ =>
    }
  })

  /**
    * only defined for the instrIdx at the beginning of each block
    */
  def instructionIdxToBlockIdx(instrIdx: Int): Int = blocks.toVector.indexOf(instrIdx)


  /**
    * by construction all instructions in a block have the same
    * exception handlers. An exception handler is described in terms of
    * a type and a block index of the handling code.
    */
  def getBlockException(instrIdx: Int): List[VBCHandler] = {
    val handlers = getHandlers(instrIdx)
    if (handlers == null) Nil
    else {
      val hs: List[VBCHandler] = handlers.toList.map(h =>
        VBCHandler(h.`type`,
          instructionIdxToBlockIdx(instructions.indexOf(h.handler)),
          if (h.visibleTypeAnnotations == null) Nil else h.visibleTypeAnnotations.toList,
          if (h.invisibleTypeAnnotations == null) Nil else h.invisibleTypeAnnotations.toList))
      assume(hs.last.exceptionType == "java/lang/Throwable")
      val vExp = VBCHandler("edu/cmu/cs/vbc/VException", -1, Nil, Nil)  // this will get translated in Rewrite.addFakeHandlerBlocks()
      // assuming the last one is the one we added to catch all exceptions
      // null is possible if using synchronization or finally
      if (hs.size > 1 && !hs.exists(x => x.exceptionType == null))
        hs.init ::: vExp :: hs.last :: Nil
      else
        hs
    }
  }
}

