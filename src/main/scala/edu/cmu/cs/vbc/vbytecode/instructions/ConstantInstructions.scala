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
      mv.visitMethodInsn(INVOKESTATIC, IntClass, "valueOf", s"(I)$IntType", false)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else {
      pushConstant(mv, v)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(false), Set(this))
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

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, blockA: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      o match {
        case s: String => mv.visitLdcInsn(o); wrapString(mv)
        case i: Integer => mv.visitLdcInsn(o); int2Integer(mv)
        case l: java.lang.Long => mv.visitLdcInsn(o); long2Long(mv)
        case _: java.lang.Double => mv.visitLdcInsn(o); double2Double(mv)
        case _: java.lang.Float => mv.visitLdcInsn(o); float2Float(mv)
        case t: Type =>
          if (t.getSort == Type.ARRAY) {
            val t = Type.getObjectType(s"[$vclasstype")
            mv.visitLdcInsn(t)
          }
          else
            mv.visitLdcInsn(Type.getType(Owner(t.getInternalName).toModel.getTypeDesc))
        case _ => throw new UnsupportedOperationException("Unsupported LDC type: " + o.getClass)
      }
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, blockA))
    }
    else {
      o match {
        case t: Type =>
          if (t.getSort == Type.ARRAY)
            mv.visitLdcInsn(t)
          else
            mv.visitLdcInsn(Type.getType(Owner(t.getInternalName).toModel.getTypeDesc))
        case _ => mv.visitLdcInsn(o)
      }
      o match {
        case s: String => wrapString(mv)
        case i: Integer =>  // do nothing
        case l: java.lang.Long => // do nothing
        case _: java.lang.Double => // do nothing
        case t: Type => // do nothing
        case _ => throw new UnsupportedOperationException("Unsupported LDC type: " + o.getClass)
      }
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

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        o match {
          case _: java.lang.Long => s.push(V_TYPE(true), Set(this))
          case _: java.lang.Double => s.push(V_TYPE(true), Set(this))
          case _ => s.push(V_TYPE(false), Set(this))
        }
      else
        o match {
          case i: java.lang.Integer => s.push(INT_TYPE(), Set(this))
          case str: java.lang.String => s.push(VBCType(Type.getObjectType("java/lang/String")), Set(this))
          case l: java.lang.Long => s.push(LONG_TYPE(), Set(this))
          case _: java.lang.Double => s.push(DOUBLE_TYPE(), Set(this))
          case t: Type => s.push(REF_TYPE(), Set(this))
          case _: java.lang.Float => s.push(FLOAT_TYPE(), Set(this))
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
      (s.push(V_TYPE(false), Set(this)), Set())
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
      (s.push(V_TYPE(true), Set(this)), Set())
    else
      (s.push(LONG_TYPE(), Set(this)), Set())
  }
}

/**
  * Push double 0
  */
case class InstrDCONST_0() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DCONST_0)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitInsn(DCONST_0)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getDouble, "valueOf", s"(D)${TypeDesc.getDouble}", false)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else {
      mv.visitInsn(DCONST_0)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(true), Set(this))
      else
        s.push(DOUBLE_TYPE(), Set(this))
    (newFrame, Set())
  }
}

/**
  * Push double 1
  */
case class InstrDCONST_1() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DCONST_1)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitInsn(DCONST_1)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getDouble, "valueOf", s"(D)${TypeDesc.getDouble}", false)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else {
      mv.visitInsn(DCONST_1)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(true), Set(this))
      else
        s.push(DOUBLE_TYPE(), Set(this))
    (newFrame, Set())
  }
}

/**
  * Push the float constant 0.0 onto the operand stack
  */
case class InstrFCONST_0() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FCONST_0)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitInsn(FCONST_0)
      float2Float(mv)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else {
      mv.visitInsn(FCONST_0)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(false), Set(this))
      else
        s.push(FLOAT_TYPE(), Set(this))
    (newFrame, Set())
  }
}

/**
  * Push the float constant 1.0 onto the operand stack
  */
case class InstrFCONST_1() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FCONST_1)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitInsn(FCONST_1)
      float2Float(mv)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else {
      mv.visitInsn(FCONST_1)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(false), Set(this))
      else
        s.push(FLOAT_TYPE(), Set(this))
    (newFrame, Set())
  }
}
