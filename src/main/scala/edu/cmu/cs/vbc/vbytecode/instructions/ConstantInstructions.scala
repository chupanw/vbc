package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis._
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.{MethodVisitor, Type}

/**
  * @author chupanw
  */

/**
  * ICONST_n instruction
  *
  * @param v
  */
case class InstrICONST(v: Int) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    pushConstant(mv, v)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      pushConstant(mv, v)
      callVintCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else {
      pushConstant(mv, v)
    }
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


case class InstrLDC(o: Object) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitLdcInsn(o)
    o match {
      case s: String => wrapString(mv)
      case _ => // do nothing
    }
  }

  def wrapString(mv: MethodVisitor) = {
    val stringOwner = Owner("java/lang/String")
    if (stringOwner != stringOwner.toModel)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner("java/lang/String").toModel,
        MethodName("valueOf"),
        MethodDesc(s"(Ljava/lang/String;)${Owner("java/lang/String").toModel.getTypeDesc}"),
        false
      )
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, blockA: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      var createdOne = false
      o match {
        case s: String => mv.visitLdcInsn(o); wrapString(mv)
        case i: Integer => {
          mv.visitLdcInsn(o)
          callVintCreateOne(mv, (m) => loadCurrentCtx(m, env, blockA))
          createdOne = true
        }
        case l: java.lang.Long => mv.visitLdcInsn(o); long2Long(mv)
        case t: Type =>
          if (t.getSort == Type.ARRAY) {
            val t = Type.getObjectType(s"[$vclasstype")
            mv.visitLdcInsn(t)
          }
          else
            mv.visitLdcInsn(o)
        case _ => throw new UnsupportedOperationException("Unsupported LDC type")
      }

      if (!createdOne) {
        callVCreateOne(mv, (m) => loadCurrentCtx(m, env, blockA))
      }
    }
    else {
      mv.visitLdcInsn(o)
      o match {
        case s: String => wrapString(mv)
        case i: Integer =>  // do nothing
        case _ => throw new UnsupportedOperationException("Unsupported LDC type")
      }
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(), Set(this))
      else
        o match {
          case i: java.lang.Integer => s.push(INT_TYPE(), Set(this))
          case str: java.lang.String => s.push(VBCType(Type.getObjectType("java/lang/String")), Set(this))
          case l: java.lang.Long => s.push(LONG_TYPE(), Set(this))
          case t: Type => s.push(REF_TYPE(), Set(this))
          case _ => throw new RuntimeException("Incomplete support for LDC")
        }
    (newFrame, Set())
  }
}

case class InstrACONST_NULL() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(ACONST_NULL)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitInsn(ACONST_NULL)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else {
      mv.visitInsn(ACONST_NULL)
    }

  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    if (env.shouldLiftInstr(this))
      (s.push(V_TYPE(), Set(this)), Set())
    else
      (s.push(VBCType(Type.getObjectType("null")), Set(this)), Set())
  }
}

case class InstrLCONST(v: Int) extends Instruction {
  require(v == 0 || v == 1)

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    if (v == 0) mv.visitInsn(LCONST_0) else mv.visitInsn(LCONST_1)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (v == 0) mv.visitInsn(LCONST_0) else mv.visitInsn(LCONST_1)
    if (env.shouldLiftInstr(this)) {
      long2Long(mv)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    if (env.shouldLiftInstr(this))
      (s.push(V_TYPE(), Set(this)), Set())
    else
      (s.push(LONG_TYPE(), Set(this)), Set())
  }
}