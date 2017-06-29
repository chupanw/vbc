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
    initializeConditionalFields(m, cls)

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
      val firstBlock = m.body.blocks.head
      val firstBlockInstructions = firstBlock.instr

      val nopPrefix = firstBlockInstructions.takeWhile(_.isInstanceOf[EmptyInstruction])
      val restInstrs = firstBlockInstructions.drop(nopPrefix.length).toList
      // this is a stronger assumption
      assert(restInstrs.head.isALOAD0, "first instruction in <init> is NOT ALOAD 0")
      val (initSeq, nonInitSeq) = extractSuperInit(restInstrs, cls)

      val newInstrs = nopPrefix ++ (InstrINIT_CONDITIONAL_FIELDS() :: initSeq ::: nonInitSeq)
      val newBlocks = Block(newInstrs, Nil) +: m.body.blocks.drop(1)
      m.copy(body = CFG(newBlocks))
    } else m

  private def extractSuperInit(instrs: List[Instruction], cls: VBCClassNode): (List[Instruction], List[Instruction]) = {
    val invokeSpecialInit = instrs.filter(isInvokeSpecialInit(_, cls))
    assert(invokeSpecialInit.size == 1, "Suspicious number of <init> call: " + invokeSpecialInit.size)
    val (prefix, postfix) = instrs.splitAt(instrs.indexOf(invokeSpecialInit.head))
    val aload0 = prefix.reverse.find(_.isALOAD0)
    assert(aload0.nonEmpty, "No ALOAD 0 before calling <init>")
    val (beforeALOAD0, afterALOAD0) = prefix.splitAt(prefix.indexOf(aload0.get))
    val initSeq: List[Instruction] = afterALOAD0 ::: invokeSpecialInit.head :: Nil
    val nonInitSeq: List[Instruction] = beforeALOAD0 ::: postfix.tail
    (initSeq, nonInitSeq)
  }

  private def isInvokeSpecialInit(i: Instruction, cls: VBCClassNode): Boolean = i match {
    case invokespecial: InstrINVOKESPECIAL =>
      invokespecial.name.contentEquals("<init>") &&
        (invokespecial.owner.contentEquals(cls.superName) || invokespecial.owner.contentEquals(cls.name))
    case _ => false
  }

  def rewriteV(m: VBCMethodNode, cls: VBCClassNode): VBCMethodNode = {
    if (m.body.blocks.nonEmpty) {
      initializeConditionalFields(
        appendGOTO(
          ensureUniqueReturnInstr(
            replaceAthrowWithAreturn(m)
          )
        ), cls
      )
    }
    else {
      m
    }
  }

  private def appendGOTO(m: VBCMethodNode): VBCMethodNode = {
    val rewrittenBlocks = m.body.blocks.map(b =>
      if (b != m.body.blocks.last && b.instr.nonEmpty && !b.instr.last.isJumpInstr) {
        Block(b.instr :+ InstrGOTO(m.body.blocks.indexOf(b) + 1), b.exceptionHandlers)
      } else
        b
    )
    m.copy(body = CFG(rewrittenBlocks))
  }

  private def replaceAthrowWithAreturn(m: VBCMethodNode): VBCMethodNode = {
    val rewrittenBlocks = m.body.blocks.map(b =>
      Block(b.instr.flatMap(i => List(
        if (i.isATHROW) InstrARETURN() else i)
      ), b.exceptionHandlers)
    )
    m.copy(body = CFG(rewrittenBlocks))
  }

  private def ensureUniqueReturnInstr(m: VBCMethodNode): VBCMethodNode = {
    //if the last instruction in the last block is the only return statement, we are happy
    val returnInstr = for (block <- m.body.blocks; instr <- block.instr if instr.isReturnInstr) yield instr
    assert(returnInstr.nonEmpty, "no return instruction found in method")
    // RETURN and ARETURN should not happen together, otherwise code could not compile in the first place.
    // For all other kinds of return instructions that take argument, there is no need to worry about exact return type
    // because they are all Vs anyway.
//    assert(returnInstr.map(_.getClass).distinct.size == 1, "inconsistency: different kinds of return instructions found in method")
    if (returnInstr.size == 1 && returnInstr.head == m.body.blocks.last.instr.last)
      m
    else {
      val isReturningInt = MethodDesc(m.desc).isReturnPrimWithV
      val returnInst = if (m.name == "<init>") InstrRETURN() else if (isReturningInt) InstrIRETURN() else InstrARETURN()
      unifyReturnInstr(m: VBCMethodNode, returnInst)
    }
  }

  private def unifyReturnInstr(method: VBCMethodNode, returnInstr: Instruction): VBCMethodNode = {
    // Need to distinguish V and VInt (and potentiall VFloat, VDouble...)
    val isReturningInt = returnInstr match {
      case _: InstrIRETURN => true
      case _ => false
    }
    val returnVariable = new LocalVar(
      name = "$result",
      desc = if (isReturningInt) LiftUtils.vintclasstype else LiftUtils.vclasstype,
      vinitialize = if (isReturningInt) LocalVar.initOneintZero else LocalVar.initOneNull
    )

    var newReturnBlockInstrs = List(returnInstr)
    val newReturnInstr = if (isReturningInt) InstrILOAD(returnVariable) else InstrALOAD(returnVariable)
    if (!returnInstr.isRETURN)
      newReturnBlockInstrs ::= newReturnInstr
    val newReturnBlock = Block(newReturnBlockInstrs, Nil)
    val newReturnBlockIdx = method.body.blocks.size

    def storeAndGoto: List[Instruction] = List(
      if (isReturningInt) InstrISTORE(returnVariable) else InstrASTORE(returnVariable),
      InstrGOTO(newReturnBlockIdx)
    )
    def storeNullAndGoto: List[Instruction] = InstrACONST_NULL() :: storeAndGoto

    val rewrittenBlocks = method.body.blocks.map(block =>
      Block(block.instr.flatMap(instr =>
        if (instr.isReturnInstr) {
          if (instr.isRETURN) storeNullAndGoto else storeAndGoto
        }
        else
          List(instr)
      ), block.exceptionHandlers))

    method.copy(body =
      CFG(
        rewrittenBlocks :+ newReturnBlock
      )
    )

  }
}
