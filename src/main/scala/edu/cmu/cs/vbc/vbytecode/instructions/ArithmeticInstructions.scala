package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis._
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.utils.{InvokeDynamicUtils, VCall}
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

trait BinOpInstruction extends Instruction {

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    if (s.stack.take(2).exists(_._1 == V_TYPE(false)))
      env.setLift(this)
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame2.push(V_TYPE(false), Set(this))
      else {
        frame2.push(INT_TYPE(), Set(this))
      }
    val backtrack: Set[Instruction] =
      if (env.shouldLiftInstr(this)) {
        if (v1 != V_TYPE(false)) prev1
        else if (v2 != V_TYPE(false)) prev2
        else Set()
      }
      else
        Set()
    (newFrame, backtrack)
  }
}

trait BinOpNonIntInstruction extends Instruction {

  def updateStackWithReturnType(s: VBCFrame, env: VMethodEnv, retType: VBCType): UpdatedFrame = {
    val retVType: VBCType = retType match {
      case _: LONG_TYPE => V_TYPE(true)
      case _: DOUBLE_TYPE => V_TYPE(true)
      case _ => V_TYPE(false)
    }
    if (s.stack.take(2).exists(_._1.isInstanceOf[V_TYPE]))
      env.setLift(this)
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame2.push(retVType, Set(this))
      else {
        frame2.push(retType, Set(this))
      }
    val backtrack: Set[Instruction] =
      if (env.shouldLiftInstr(this)) {
        if (!v1.isInstanceOf[V_TYPE]) prev1
        else if (!v2.isInstanceOf[V_TYPE]) prev2
        else Set()
      }
      else
        Set()
    (newFrame, backtrack)
  }
}

/**
  * IADD instruction
  */
case class InstrIADD() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IADD)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "IADD", s"(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;$fexprclasstype)Ledu/cmu/cs/varex/V;", false)
    }
    else
      mv.visitInsn(IADD)
  }
}


case class InstrISUB() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(ISUB)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "ISUB", s"(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;$fexprclasstype)Ledu/cmu/cs/varex/V;", false)
    }
    else
      mv.visitInsn(ISUB)
  }
}


case class InstrIMUL() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IMUL)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "IMUL", s"(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;$fexprclasstype)Ledu/cmu/cs/varex/V;", false)
    }
    else
      mv.visitInsn(IMUL)
  }
}


case class InstrIDIV() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IDIV)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "IDIV", s"(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;$fexprclasstype)Ledu/cmu/cs/varex/V;", false)
    } else
      mv.visitInsn(IDIV)
  }
}

/**
  * Negate int.
  *
  * Operand stack: ..., value -> ..., result
  */
case class InstrINEG() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(INEG)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, frame) = s.pop()
    if (v == V_TYPE(false))
      env.setLift(this)
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame.push(V_TYPE(false), Set(this))
      else {
        frame.push(INT_TYPE(), Set(this))
      }
    if (env.shouldLiftInstr(this) && v != V_TYPE(is64Bit = false))
        return (s, prev)
    (newFrame, Set())
  }

  /**
    * Lifting means performing operations on a V object
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "ineg",
        s"($vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else {
      mv.visitInsn(INEG)
    }
  }
}

/** Shift left int
  *
  * ..., value1(int), value2(int) -> ..., result
  */
case class InstrISHL() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(ISHL)
  }

  /** Lifting means invoking ISHL on V.
    *
    * If lifting, assume that value2 is V
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      InvokeDynamicUtils.invoke(
        VCall.sflatMap,
        mv,
        env,
        loadCtx = loadCurrentCtx(_, env, block),
        lambdaName = "ISHL",
        desc = TypeDesc.getInt + "(" + TypeDesc.getInt + ")" + vclasstype,
        nExplodeArgs = 1
      ) {
        (mv: MethodVisitor) => {
          mv.visitVarInsn(ALOAD, 0) // Integer
          Integer2int(mv)  // int
          mv.visitVarInsn(ALOAD, 2) // Integer
          Integer2int(mv) // int
          mv.visitInsn(ISHL)
          int2Integer(mv)
          callVCreateOne(mv, m => m.visitVarInsn(ALOAD, 1))
          mv.visitInsn(ARETURN)
        }
      }
    }
    else
      mv.visitInsn(ISHL)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (argType, argBacktrack, frame1) = s.pop()
    val (receiverType, receiverBacktrack, frame2) = frame1.pop()
    val hasV = argType == V_TYPE(false) || receiverType == V_TYPE(false)
    if (hasV) env.setLift(this)
    val newFrame = frame2.push(if (hasV) V_TYPE(false) else INT_TYPE(), Set(this))
    val backtrack: Set[Instruction] = (argType, receiverType) match {
      case (INT_TYPE(), _) => argBacktrack
      case (_, INT_TYPE()) => receiverBacktrack
      case _ => Set()
    }
    (newFrame, backtrack)
  }
}

/** Compare long
  *
  * ..., value1(long), value2(long) -> ..., result(int)
  */
case class InstrLCMP() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LCMP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      InvokeDynamicUtils.invoke(
        VCall.sflatMap,
        mv,
        env,
        loadCtx = loadCurrentCtx(_, env, block),
        lambdaName = "lcmp",
        desc = TypeDesc.getLong + s"(${TypeDesc.getLong})" + vclasstype,
        nExplodeArgs = 1
      ) {
        (mv: MethodVisitor) => {
          mv.visitVarInsn(ALOAD, 0) //value1
          Long2long(mv)
          mv.visitVarInsn(ALOAD, 2) //value2
          Long2long(mv)
          mv.visitInsn(LCMP)
          int2Integer(mv)
          callVCreateOne(mv, (m) => m.visitVarInsn(ALOAD, 1))
          mv.visitInsn(ARETURN)
        }
      }
    }
    else
      mv.visitInsn(LCMP)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (value2Type, value2Backtrack, frame2) = s.pop()
    val (value1Type, value1Backtrack, frame1) = frame2.pop()
    ((value1Type, value2Type): @unchecked) match {
      case (LONG_TYPE(), LONG_TYPE()) => (frame1.push(INT_TYPE(), Set(this)), Set())
      case (LONG_TYPE(), V_TYPE(true)) => (frame1, value1Backtrack) // backtrack, frame1 will be discarded
      case (V_TYPE(true), LONG_TYPE()) => (frame1, value2Backtrack) // backtrack, frame2 will be discarded
      case (V_TYPE(true), V_TYPE(true)) =>
        env.setLift(this)
        (frame1.push(V_TYPE(false), Set(this)), Set())
    }
  }
}

/** Negate long
  *
  * ..., value(long) -> ..., result(long)
  */
case class InstrLNEG() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LNEG)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      InvokeDynamicUtils.invoke(
        VCall.smap,
        mv,
        env,
        loadCtx = loadCurrentCtx(_, env, block),
        lambdaName = "lneg",
        desc = TypeDesc.getLong + "()" + TypeDesc.getLong
      ) {
        (mv: MethodVisitor) => {
          mv.visitVarInsn(ALOAD, 1)
          Long2long(mv)
          mv.visitInsn(LNEG)
          long2Long(mv)
          mv.visitInsn(ARETURN)
        }
      }
    }
    else
      mv.visitInsn(LNEG)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (vType, _, frame) = s.pop()
    if (vType == V_TYPE(true)) {
      env.setLift(this)
      (frame.push(V_TYPE(true), Set(this)), Set())
    }
    else
      (frame.push(LONG_TYPE(), Set(this)), Set())
  }
}

/** Arithmetic shift right int
  *
  * ..., value1(int), value2(int) -> ..., result(int)
  */
case class InstrISHR() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(ISHR)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)){
      InvokeDynamicUtils.invoke(
        VCall.sflatMap,
        mv,
        env,
        loadCtx = loadCurrentCtx(_, env, block),
        lambdaName = "ishr",
        desc = TypeDesc.getInt + s"(${TypeDesc.getInt})" + vclasstype,
        nExplodeArgs = 1
      ) {
        (mv: MethodVisitor) => {
          mv.visitVarInsn(ALOAD, 0) // value1
          Integer2int(mv)
          mv.visitVarInsn(ALOAD, 2) // value2
          Integer2int(mv)
          mv.visitInsn(ISHR)
          int2Integer(mv)
          callVCreateOne(mv, (m) => m.visitVarInsn(ALOAD, 1))
          mv.visitInsn(ARETURN)
        }
      }
    }
    else
      mv.visitInsn(ISHR)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (value2Type, value2Backtrack, frame2) = s.pop()
    val (value1Type, value1Backtrack, frame1) = frame2.pop()
    ((value1Type, value2Type): @unchecked) match {
      case (INT_TYPE(), INT_TYPE()) => (frame1.push(INT_TYPE(), Set(this)), Set())
      case (INT_TYPE(), V_TYPE(false)) => (frame1, value1Backtrack)
      case (V_TYPE(false), INT_TYPE()) => (frame1, value2Backtrack)
      case (V_TYPE(false), V_TYPE(false)) =>
        env.setLift(this)
        (frame1.push(V_TYPE(false), Set(this)), Set())
    }
  }
}

/**
  * Boolean AND int
  */
case class InstrIAND() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IAND)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    loadCurrentCtx(mv, env, block)
    mv.visitMethodInsn(
      INVOKESTATIC,
      Owner.getVOps,
      "iand",
      MethodDesc(s"($vclasstype$vclasstype$fexprclasstype)$vclasstype"),
      false
    )
  }
}

case class InstrLAND() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(LAND)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "land",
        s"($vclasstype$vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else
      mv.visitInsn(LAND)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

/** Boolean OR int
  *
  * ..., value1(int), value2(int) -> result(int)
  */
case class InstrIOR() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IOR)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, MethodName("ior"), MethodDesc(s"($vclasstype$vclasstype$fexprclasstype)$vclasstype"), false)
    }
    else
      mv.visitInsn(IOR)
}
}

case class InstrLSUB() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LSUB)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "lsub",
        s"($vclasstype$vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else
      mv.visitInsn(LSUB)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

/** Logical shift right int
  *
  * ..., value1(int), value2(int) -> ..., result(int)
  */
case class InstrIUSHR() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(IUSHR)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit =
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, MethodName("iushr"), MethodDesc(s"($vclasstype$vclasstype$fexprclasstype)$vclasstype"), false)
    }
    else
      mv.visitInsn(IUSHR)
}

/** Remainder int
  *
  * ..., value1(int), value2(int) -> ..., result(int)
  */
case class InstrIREM() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(IREM)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit =
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, MethodName("irem"), MethodDesc(s"($vclasstype$vclasstype)$vclasstype"), false)
    else
      mv.visitInsn(IREM)
}

/** Boolean XOR int
  *
  * ..., value1, value2 -> ..., result
  */
case class InstrIXOR() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(IXOR)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    loadCurrentCtx(mv, env, block)
    mv.visitMethodInsn(
      INVOKESTATIC,
      Owner.getVOps,
      "ixor",
      MethodDesc(s"($vclasstype$vclasstype$fexprclasstype)$vclasstype"),
      false
    )
  }
}


/** Convert long to int
  *
  * ..., value(long) -> ..., result(int)
  */
case class InstrL2I() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(L2I)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "l2i",
        s"($vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else
      mv.visitInsn(L2I)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, frame) = s.pop()
    if (v == V_TYPE(true))
      env.setLift(this)
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame.push(V_TYPE(false), Set(this))
      else {
        frame.push(INT_TYPE(), Set(this))
      }
    if (env.shouldLiftInstr(this) && v != V_TYPE(true))
      return (s, prev)
    (newFrame, Set())
  }
}

/** Convert int to short
  *
  * ..., value (int) -> ..., result (int)
  */
case class InstrI2S() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(I2S)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "i2s",
        s"($vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else
      mv.visitInsn(I2S)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, frame) = s.pop()
    if (v == V_TYPE(false))
      env.setLift(this)
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame.push(V_TYPE(false), Set(this))
      else {
        frame.push(INT_TYPE(), Set(this))
      }
    if (env.shouldLiftInstr(this) && v != V_TYPE(false))
      return (s, prev)
    (newFrame, Set())

  }
}

/** Logical shift right long
  *
  * ..., value1(long), value2(int) -> ..., result(long)
  */
case class InstrLUSHR() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(LUSHR)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "lushr",
        s"($vclasstype$vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else
      mv.visitInsn(LUSHR)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

/** Divide long
  *
  * ..., value1(long), value2(long) -> ..., result(long)
  */
case class InstrLDIV() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(LDIV)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "ldiv",
        MethodDesc(s"($vclasstype$vclasstype$fexprclasstype)$vclasstype"),
        false
      )
    }
    else {
      mv.visitInsn(LDIV)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}


/** Add long
  *
  * ..., value1(long), value2(long) -> ..., result(long)
  * todo: should extend BinOps
  */
case class InstrLADD() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitInsn(LADD)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "ladd",
        s"($vclasstype$vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else
      mv.visitInsn(LADD)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}


case class InstrDMUL() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DMUL)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getVOps,
        "dmul",
        s"($vclasstype$vclasstype$fexprclasstype)$vclasstype",
        false
      )
    }
    else
      mv.visitInsn(DMUL)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, DOUBLE_TYPE())
}

/**
  * Compare double
  *
  * ..., value1(double), value2(double) -> ..., result(int)
  *
  * If value1 is greater than value2, 1(int) is pushed onto the operand stack.
  * If value1 is equal to value2, 0 is pushed.
  * If value1 is less than value2, -1 is pushed.
  * If at least one of value1 and value2 is NaN, -1 is pushed.
  */
case class InstrDCMPL() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DCMPL)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "dcmpl", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    }
    else
      mv.visitInsn(DCMPL)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    if (s.stack.take(2).exists(_._1 == V_TYPE(true)))
      env.setLift(this)
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame2.push(V_TYPE(false), Set(this))
      else {
        frame2.push(INT_TYPE(), Set(this))
      }
    val backtrack: Set[Instruction] =
      if (env.shouldLiftInstr(this)) {
        if (v1 != V_TYPE(true)) prev1
        else if (v2 != V_TYPE(true)) prev2
        else Set()
      }
      else
        Set()
    (newFrame, backtrack)
  }
}


case class InstrLOR() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LOR)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "lor", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    }
    else
      mv.visitInsn(LOR)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

/**
  * ..., value1(long), value2(int) -> ..., result(long)
  */
case class InstrLSHL() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LSHL)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "lshl", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    }
    else
      mv.visitInsn(LSHL)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

/**
  * value1 (long), value2 (int) -> result (long)
  */
case class InstrLSHR() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(LSHR)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "lshr", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    }
    else
      mv.visitInsn(LSHR)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (value2Type, value2Backtrack, frame2) = s.pop()
    val (value1Type, value1Backtrack, frame1) = frame2.pop()
    ((value1Type, value2Type): @unchecked) match {
      case (LONG_TYPE(), INT_TYPE()) => (frame1.push(LONG_TYPE(), Set(this)), Set())
      case (LONG_TYPE(), V_TYPE(false)) => (frame1, value1Backtrack)
      case (V_TYPE(true), INT_TYPE()) => (frame1, value2Backtrack)
      case (V_TYPE(true), V_TYPE(false)) =>
        env.setLift(this)
        (frame1.push(V_TYPE(true), Set(this)), Set())
    }
  }
}

case class InstrLXOR() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LXOR)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "lxor", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    }
    else
      mv.visitInsn(LXOR)

  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

case class InstrLMUL() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LMUL)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "lmul", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    }
    else
      mv.visitInsn(LMUL)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

/**
  * ..., value1 (double), value2 (double) -> ..., result (int)
  */
case class InstrDCMPG() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DCMPG)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "dcmpg", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(DCMPG)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    if (s.stack.take(2).exists(_._1 == V_TYPE(true)))
      env.setLift(this)
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame2.push(V_TYPE(false), Set(this))
      else {
        frame2.push(INT_TYPE(), Set(this))
      }
    val backtrack: Set[Instruction] =
      if (env.shouldLiftInstr(this)) {
        if (v1 != V_TYPE(true)) prev1
        else if (v2 != V_TYPE(true)) prev2
        else Set()
      }
      else
        Set()
    (newFrame, backtrack)
  }
}

/**
  * ..., value1 (double), value2 (double) -> ..., result (double)
  */
case class InstrDDIV() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DDIV)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "ddiv", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(DDIV)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, DOUBLE_TYPE())
}

case class InstrDADD() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(DADD)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "dadd", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(DADD)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, DOUBLE_TYPE())
}

case class InstrLREM() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(LREM)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "lrem", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(LREM)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, LONG_TYPE())
}

/**
  * Divide float
  *
  * ..., value1 (float), value2 (float) -> ..., result (float)
  */
case class InstrFDIV() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FDIV)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "fdiv", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(FDIV)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, FLOAT_TYPE())
}

/**
  * Compare float
  *
  * ..., value1 (float), value2 (float) -> ..., result (int)
  */
case class InstrFCMPG() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FCMPG)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "fcmpg", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(FCMPG)
    }
  }
}

case class InstrFCMPL() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FCMPL)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "fcmpl", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(FCMPL)
    }
  }
}

case class InstrFMUL() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FMUL)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "fmul", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(FMUL)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, FLOAT_TYPE())
}

case class InstrFADD() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FADD)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "fadd", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(FADD)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, FLOAT_TYPE())
}

case class InstrDSUB() extends BinOpNonIntInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(DSUB)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "dsub", s"($vclasstype$vclasstype$fexprclasstype)$vclasstype", false)
    } else {
      mv.visitInsn(DSUB)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, DOUBLE_TYPE())
}