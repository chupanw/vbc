package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.{VBCFrame, V_TYPE}
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode.{Block, MethodEnv, Owner, VMethodEnv}
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

case class InstrMONITORENTER() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(MONITORENTER)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
//    System.err.println("WARNING: MONITOR is used in: " + env.clazz.name + "#" + env.method.name)
    loadCurrentCtx(mv, env, block)
    mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "monitorVerify", s"($vclasstype$fexprclasstype)Ljava/lang/Object;", false)
    mv.visitInsn(MONITORENTER)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, frame) = s.pop()
    if (v != V_TYPE(false))
      (frame, prev)
    else
      (frame, Set())
  }
}

case class InstrMONITOREXIT() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(MONITOREXIT)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    loadCurrentCtx(mv, env, block)
    mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "monitorVerify", s"($vclasstype$fexprclasstype)Ljava/lang/Object;", false)
    mv.visitInsn(MONITOREXIT)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, frame) = s.pop()
    if (v != V_TYPE(false))
      (frame, prev)
    else
      (frame, Set())
  }
}