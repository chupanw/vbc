package edu.cmu.cs.vbc.vbytecode

import edu.cmu.cs.vbc.utils.LiftUtils
import edu.cmu.cs.vbc.vbytecode.instructions._

/**
  * rewrites of methods as part of variational lifting.
  * note that ASTs are immutable; returns a new (rewritten) method.
  *
  * rewrites happen before toVByteCode is called on a method;
  * the MethodEnv can be used for the transformation
  */
object Rewrite {

  def rewrite(m: VBCMethodNode, cls: VBCClassNode): VBCMethodNode =
    initializeConditionalFields(removeOurHandlers(m), cls)

  def rewriteV(m: VBCMethodNode, cls: VBCClassNode): VBCMethodNode = {
    if (m.body.blocks.nonEmpty) {
      initializeConditionalFields(
        addFakeHanlderBlocks(
          appendGOTO(
            ensureUniqueReturnInstr(
              replaceAthrowWithAreturn(
                recordHandledExceptions(m)
              )
            )
          )
        ),
        cls
      )
    } else {
      m
    }
  }

  var idCount = 0
  private def profiling(m: VBCMethodNode, cls: VBCClassNode): VBCMethodNode = {
    val id = cls.name + "#" + m.name + "#" + idCount
    idCount += 1
    val newBlocks = m.body.blocks.map(
      b =>
        if (b == m.body.blocks.head)
          Block(InstrStartTimer(id) +: b.instr, b.exceptionHandlers, b.exceptions)
        else if (b == m.body.blocks.last)
          Block(b.instr.flatMap(
                  i =>
                    if (i.isReturnInstr)
                      List(InstrStopTimer(id), i)
                    else
                      List(i)),
                b.exceptionHandlers,
                b.exceptions)
        else
        b)
    m.copy(body = CFG(newBlocks))
  }

  private def appendGOTO(m: VBCMethodNode): VBCMethodNode = {
    val rewrittenBlocks = m.body.blocks.map(b =>
      if (b != m.body.blocks.last && b.instr.nonEmpty && !b.instr.last.isJumpInstr && !b.instr.last.isATHROW) {
        Block(b.instr :+ InstrGOTO(m.body.blocks.indexOf(b) + 1), b.exceptionHandlers, b.exceptions)
      } else
      b)
    m.copy(body = CFG(rewrittenBlocks))
  }

  private def replaceAthrowWithAreturn(m: VBCMethodNode): VBCMethodNode = {
    val rewrittenBlocks = m.body.blocks.map(b =>
      if (b == m.body.blocks.last) {
        Block(b.instr.flatMap(i =>
                if (i.isATHROW) List(InstrCheckThrow(), InstrARETURN()) else List(i)),
              b.exceptionHandlers,
              b.exceptions)
      } else b)
    m.copy(body = CFG(rewrittenBlocks))
  }

  private def ensureUniqueReturnInstr(m: VBCMethodNode): VBCMethodNode = {
    //if the last instruction in the last block is the only return statement, we are happy
    val returnInstr = for (block <- m.body.blocks; instr <- block.instr if instr.isReturnInstr)
      yield instr
    assert(returnInstr.nonEmpty, "no return instruction found in method")
    // RETURN and ARETURN should not happen together, otherwise code could not compile in the first place.
    // For all other kinds of return instructions that take argument, there is no need to worry about exact return type
    // because they are all Vs anyway.
//    assert(returnInstr.map(_.getClass).distinct.size == 1, "inconsistency: different kinds of return instructions found in method")
    if (returnInstr.size == 1 && returnInstr.head == m.body.blocks.last.instr.last)
      m
    else {
      unifyReturnInstr(m: VBCMethodNode, if (m.name == "<init>") InstrRETURN() else InstrARETURN())
    }
  }

  private def unifyReturnInstr(method: VBCMethodNode, returnInstr: Instruction): VBCMethodNode = {
    //TODO technically, all methods will always return type V, so we should not have
    //to worry really about what kind of store/load/return instruction we generate here
    val returnVariable = new LocalVar("$result", LiftUtils.vclasstype)

    var newReturnBlockInstr = List(returnInstr)
    if (!returnInstr.isRETURN)
      newReturnBlockInstr ::= InstrALOAD(returnVariable)
    val newReturnBlock    = Block(newReturnBlockInstr, Nil, Nil)
    val newReturnBlockIdx = method.body.blocks.size

    def getStoreInstr(retInstr: Instruction): Instruction = retInstr match {
      case _: InstrIRETURN => InstrISTORE(returnVariable)
      case _: InstrLRETURN => InstrLSTORE(returnVariable)
      case _: InstrFRETURN => InstrFSTORE(returnVariable)
      case _: InstrDRETURN => InstrDSTORE(returnVariable)
      case _: InstrARETURN => InstrASTORE(returnVariable)
      case _               => throw new RuntimeException("Not a return instruction: " + retInstr)
    }
    def storeAndGotoSeq(retInstr: Instruction): List[Instruction] =
      List(getStoreInstr(retInstr), InstrGOTO(newReturnBlockIdx))
    def storeNullAndGotoSeq: List[Instruction] =
      List(InstrACONST_NULL(), InstrASTORE(returnVariable), InstrGOTO(newReturnBlockIdx))

    val rewrittenBlocks = method.body.blocks.map(
      block =>
        Block(
          block.instr.flatMap(instr =>
            if (instr.isReturnInstr) {
              if (instr.isRETURN) storeNullAndGotoSeq else storeAndGotoSeq(instr)
            } else
              List(instr)),
          block.exceptionHandlers,
          block.exceptions
      ))

    method.copy(
      body = CFG(
        rewrittenBlocks :+ newReturnBlock
      ))

  }

  /** Insert INIT_CONDITIONAL_FIELDS in init
    *
    * Current rewriting assumes following sequence is in the first block of init:
    *
    * ALOAD 0
    * {load arguments}
    * INVOKESPECIAL superclass's init
    *
    */
  private def initializeConditionalFields(m: VBCMethodNode, cls: VBCClassNode): VBCMethodNode =
    if (m.isInit) {
      val firstBlockInstructions = m.body.blocks.head.instr
      val newBlocks = Block(InstrINIT_CONDITIONAL_FIELDS() +: firstBlockInstructions,
                            m.body.blocks.head.exceptionHandlers,
                            m.body.blocks.head.exceptions) +: m.body.blocks.drop(1)
      m.copy(body = CFG(newBlocks))
    } else m

  /**
    * Attach one or more fake handler blocks to each Block.
    *
    * This way we ensure that each fake handler block is part of only one VBlock, so that the context is available
    * in handler block. Each fake handler block is simply one GOTO instruction that jumps to the original handler
    * block.
    *
    * Note that we add all fake blocks to the end of method, so that we do not need to change indexes of existing
    * jump instructions
    *
    * If there are multiple catch phrases, we have multiple handler for VExceptions, and thus the first VException
    * handler will mask the rest. To avoid this behavior, we need to combine all VException handlers and jump to
    * actual handlers depending on the inner exception types.
    */
  def addFakeHanlderBlocks(m: VBCMethodNode): VBCMethodNode = {
    var currentIdx: Int = m.body.blocks.size
    val pairs: List[(Block, List[Block])] = m.body.blocks.map(b => {
      if (b.exceptionHandlers.isEmpty) (b, Nil)
      else {
        // replace exceptionHandlers in current block to point to new fake blocks
        val fakePairs: List[(VBCHandler, List[Block])] = b.exceptionHandlers.toList.map {
          h =>
            if (h.exceptionType != "edu/cmu/cs/vbc/VException") {
              val fakeHandler = new VBCHandler(
                h.exceptionType,
                currentIdx,
                h.visibleTypeAnnotations,
                h.invisibleTypeAnnotations
              )
              // shouldJumpBack is true because fake handler blocks are always behind actual handler blocks
              val fakeBlock = Block(
                List(InstrUpdateCtxFromVException(), InstrWrapOne(), InstrGOTO(h.handlerBlockIdx)),
                Nil,
                Nil,
                shouldJumpBack = true)
              currentIdx = currentIdx + 1
              (fakeHandler, List(fakeBlock))
            } else {
              // Replace placebo handler with jumps to actual handler
              // First, we generate a block to update context and wrap VException into V.
              // Then, if there are $n$ exception handlers in the original code, we generate $n-1$ blocks ending with IFNE
              //  to jump to actual handler.
              // Finally, we generate a block with a GOTO that jumps to the last exception handler, because actual handler
              //  can handle unexpected exceptions (i.e., throw it again and catch by our outer try-catch).
              val handler = VBCHandler("edu/cmu/cs/vbc/VException", currentIdx, Nil, Nil)
              val firstBlock =
                Block(InstrUpdateCtxFromVException(), InstrWrapOne(), InstrGOTO(currentIdx + 1))
              val originHandlers = b.exceptionHandlers.toList
              // remove the last one, which should be Throwable and the VException, the rest should be the original exceptions being handled
              val originHandlersWithExceptions =
                originHandlers.init.filter(_.exceptionType != "edu/cmu/cs/vbc/VException")
              // leave one actual handler for the last GOTO
              val jumpBlocks: List[Block] = originHandlersWithExceptions.init.map(x => {
                Block(
                  List(
                    InstrDUP(),
                    // null is possible when finally or synchronized is used, which means catching all possible exceptions
                    // (see JVM Spec SE 8 Chap. 3.13)
                    InstrLDC(if (x.exceptionType == null) "java/lang/Throwable" else x.exceptionType),
                    InstrINVOKESTATIC(
                      Owner.getVOps,
                      MethodName("isTypeOf"),
                      MethodDesc("(Ledu/cmu/cs/vbc/VException;Ljava/lang/String;)Z"),
                      itf = false),
                    InstrIFNE(x.handlerBlockIdx)
                  ),
                  Nil,
                  Nil,
                  shouldJumpBack = true
                )
              }) ::: Block(List(InstrGOTO(originHandlersWithExceptions.last.handlerBlockIdx)),
                           Nil,
                           Nil,
                           shouldJumpBack = true) :: Nil
              currentIdx += originHandlersWithExceptions.size + 1
              (handler, firstBlock :: jumpBlocks)
            }
        }
        val (fakeExceptionHandlers, fakeBlocks) = fakePairs.unzip
        val newBlock: Block                     = b.copy(exceptionHandlers = fakeExceptionHandlers)
        (newBlock, fakeBlocks.flatten)
      }
    })
    val newBlocks: List[Block]  = pairs.unzip._1
    val fakeBlocks: List[Block] = pairs.unzip._2.flatten
    m.copy(body = new CFG(newBlocks ::: fakeBlocks))
  }

  private def recordHandledExceptions(m: VBCMethodNode): VBCMethodNode = {
    import scala.collection.mutable.ArrayBuffer
    val eb: Array[ArrayBuffer[String]] = Array.fill(m.body.blocks.size)(ArrayBuffer())
    for (b <- m.body.blocks if b.exceptionHandlers.nonEmpty) {
      val hs = b.exceptionHandlers
      assume(hs.last.exceptionType == "java/lang/Throwable",
             "last exception type should be Throwable")
      val fhs = b.exceptionHandlers.init.filter(_.exceptionType != "edu/cmu/cs/vbc/VException")
      fhs foreach { x =>
        eb(x.handlerBlockIdx) += x.exceptionType
      }
    }
    val newBlocks = m.body.blocks.zipWithIndex.map(x =>
      Block(x._1.instr, x._1.exceptionHandlers, eb(x._2).toList.distinct))
    m.copy(body = CFG(newBlocks))
  }

  /**
    * Remove placeholder VException handler and our last Throwable handler for unlifted code, used in diff. testing
    */
  private def removeOurHandlers(m: VBCMethodNode): VBCMethodNode = {
    val blocks = m.body.blocks.map(
      b =>
        b.copy(
          exceptionHandlers =
            if (b.exceptionHandlers.nonEmpty)
              b.exceptionHandlers.init.filterNot(_.exceptionType == "edu/cmu/cs/vbc/VException")
            else
              b.exceptionHandlers))
    m.copy(body = CFG(blocks))
  }
}
