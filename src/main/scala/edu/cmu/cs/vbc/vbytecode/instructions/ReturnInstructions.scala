package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis.{VBCFrame, VInt_TYPE, V_TYPE}
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

trait ReturnInstruction extends Instruction {
  override def isReturnInstr: Boolean = true
}

/**
  * RETURN instruction
  */
case class InstrRETURN() extends ReturnInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(RETURN)

  /** Return $exceptionVar
    *
    * Even if the return type is void, there might be suspended exceptions and we need to
    * return them here.
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.method.isInit)
      mv.visitInsn(RETURN)  //cpwtodo: handle exceptions in <init>
    else {
      mv.visitVarInsn(ALOAD, env.getVarIdx(env.exceptionVar))
      mv.visitInsn(ARETURN)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set())

  override def isRETURN: Boolean = true
}


case class InstrIRETURN() extends ReturnInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IRETURN)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    // Instead of returning an Integer, we return a reference
    mv.visitInsn(ARETURN)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    env.setLift(this)
    val (v, prev, newFrame) = s.pop()
    val backtrack =
      if (v != VInt_TYPE()) prev
      else Set[Instruction]()
    (newFrame, backtrack)
  }
}


case class InstrARETURN() extends ReturnInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(ARETURN)

  /** Return $exceptionVar and $result
    *
    * $exceptionVar represents exceptions that are thrown while calling lifted methods, and
    * $result stands for normal return values as well as exceptions thrown by ATHROW.
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    // $result should be on top of operand stack already
    // For ARETURN, exceptions are stored into $result, so $exceptionVar is useless.
    mv.visitInsn(ARETURN)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    env.setLift(this)
    val (v, prev, newFrame) = s.pop()
    val backtrack =
      if (v != V_TYPE()) prev
      else Set[Instruction]()
    (newFrame, backtrack)
  }
}

/** Throw exception or error
  *
  * In lifted bytecode, all ATHROWs will be replaced with STOREs, and handled while methods return.
  */
case class InstrATHROW() extends Instruction {
  override def isATHROW: Boolean = true
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(ATHROW)
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit =
    throw new RuntimeException("ATHROW should not appear in lifted bytecode")
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) =
    throw new RuntimeException("ATHROW should not appear in lifted bytecode")
}