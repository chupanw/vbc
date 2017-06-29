package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis.{INT_TYPE, VBCFrame, VInt_TYPE}
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._


/**
  * DUP instruction
  */
case class InstrDUP() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    //TODO, when applied to LONG, use the int one instead of the 2byte one
    mv.visitInsn(DUP)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v, prev, frame1) = s.pop()
    val frame2 = frame1.push(v, prev)
    val frame3 = frame2.push(v, prev)
    (frame3, Set())
  }
}


/**
  * POP instruction
  */
case class InstrPOP() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(POP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(POP)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v, prev, newFrame) = s.pop()
    (newFrame, Set())
  }
}


/**
  * Push byte
  *
  * @param value
  */
case class InstrBIPUSH(value: Int) extends Instruction {
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      pushConstant(mv, value)
      callVintCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else
      toByteCode(mv, env, block)
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitIntInsn(BIPUSH, value)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(VInt_TYPE(), Set(this))
      else
        s.push(INT_TYPE(), Set(this))
    (newFrame, Set())
  }
}


/**
  * SIPUSH: Push short
  *
  * @param value
  */
case class InstrSIPUSH(value: Int) extends Instruction {
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitIntInsn(SIPUSH, value)
      callVintCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else
      toByteCode(mv, env, block)
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitIntInsn(SIPUSH, value)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(VInt_TYPE(), Set(this))
      else
        s.push(INT_TYPE(), Set(this))
    (newFrame, Set())
  }
}

/**
  * Duplicate the top operand stack value and insert two values down
  *
  * Operand stack: ..., value2, value1 -> ..., value1, value2, value1
  */
case class InstrDUP_X1() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP_X1)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    val frame3 = frame2.push(v1, prev1)
    val frame4 = frame3.push(v2, prev1)
    val frame5 = frame4.push(v1, prev1)
    (frame5, Set())
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP_X1)
  }
}
