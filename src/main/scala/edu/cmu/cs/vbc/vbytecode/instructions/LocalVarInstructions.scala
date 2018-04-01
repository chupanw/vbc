package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis._
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

abstract class StoreInstruction(val v: Variable) extends Instruction {
  def updateStack(s: VBCFrame, env: VMethodEnv, is64Bit: Boolean): UpdatedFrame = {
    val (value, prev, frame) = s.pop()
    // these are the two cases where we are certain that we need to lift this store instruction
    if (env.isLVStoredAcrossVBlocks(v) || value == V_TYPE(is64Bit))
      env.setLift(this)
    if (env.shouldLiftInstr(this)) {
      env.liftLV(v)
      // more specific tags to determine if we need wrapping or not
      if (value != V_TYPE(is64Bit))
        env.setTag(this, env.TAG_NEED_V)  // we need to wrap the value on the operand stack into V
      if (frame.localVar.contains(v) && !frame.localVar(v)._1.isInstanceOf[V_TYPE])
        env.setTag(this, env.TAG_NEED_V2)
    }
    val newFrame = frame.setLocal(
      v,
      if (env.shouldLiftInstr(this)) V_TYPE(is64Bit) else value,
      Set(this))
    (newFrame, Set())
  }

  def store(mv: MethodVisitor, env: VMethodEnv, block: Block, loadOp: Int, storeOp: Int, convert: (MethodVisitor => Unit)): Unit = {
    if (env.shouldLiftInstr(this)) {
      if (env.getTag(this, env.TAG_NEED_V)) {
        convert(mv)
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      }
      loadCurrentCtx(mv, env, block)
      mv.visitInsn(SWAP)
      if (env.getTag(this, env.TAG_NEED_V2)) {
        mv.visitVarInsn(loadOp, env.getVarIdx(v))
        convert(mv)
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      } else {
        loadV(mv, env, v)
      }
      callVCreateChoice(mv)
      storeV(mv, env, v)
    }
    else {
      val idx = env.getVarIdx(v)
      mv.visitVarInsn(storeOp, idx)
    }
  }
}

abstract class LoadInstruction(val v: Variable) extends Instruction {
  def updateStack(s: VBCFrame, env: VMethodEnv, is64Bit: Boolean): UpdatedFrame = {
    if (s.localVar(v)._1.isInstanceOf[V_TYPE]) {
      env.setLift(this)
    }
    if (env.shouldLiftInstr(this) && !s.localVar(v)._1.isInstanceOf[V_TYPE])
      env.setTag(this, env.TAG_NEED_V)
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(is64Bit), Set(this))
      else
        s.push(s.localVar(v)._1, Set(this))
    (newFrame, Set())
  }

  def load(mv: MethodVisitor, env: VMethodEnv, block: Block, loadOp: Int, convert: (MethodVisitor => Unit)): Unit = {
    val idx = env.getVarIdx(v)
    if (env.shouldLiftInstr(this)) {
      if (env.getTag(this, env.TAG_NEED_V)) {
        mv.visitVarInsn(loadOp, idx)
        convert(mv)
        callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      }
      else
        mv.visitVarInsn(ALOAD, idx)
    }
    else
      mv.visitVarInsn(loadOp, env.getVarIdx(v))
  }
}

/**
  * ISTORE instruction
  *
  * @param variable
  */
case class InstrISTORE(variable: Variable) extends StoreInstruction(v = variable) {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitVarInsn(ISTORE, env.getVarIdx(variable))

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    store(mv, env, block, ILOAD, ISTORE, int2Integer)
  }

  override def getVariables() = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = false)
}


/**
  * ILOAD instruction
  *
  * @param variable
  */
case class InstrILOAD(variable: Variable) extends LoadInstruction(v = variable) {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitVarInsn(ILOAD, env.getVarIdx(variable))

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    load(mv, env, block, ILOAD, int2Integer)
  }

  override def getVariables() = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = false)
}


/**
  * IINC instruction
  *
  * @param variable
  * @param increment
  */
case class InstrIINC(variable: Variable, increment: Int) extends Instruction {
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadV(mv, env, variable)
      pushConstant(mv, increment)
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "IINC", s"(Ledu/cmu/cs/varex/V;I$fexprclasstype)Ledu/cmu/cs/varex/V;", false)

      //create a choice with the original value
      loadFExpr(mv, env, env.getVBlockVar(block))
      mv.visitInsn(SWAP)
      loadV(mv, env, variable)
      callVCreateChoice(mv)

      storeV(mv, env, variable)
    }
    else
      toByteCode(mv, env, block)
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitIincInsn(env.getVarIdx(variable), increment)

  override def getVariables() = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    if (s.localVar(variable)._1 == V_TYPE(false)) {
      env.setLift(this)
      (s.setLocal(variable, V_TYPE(false), Set(this)), Set())
    }
    else
      (s, Set())
  }

  override def doBacktrack(env: VMethodEnv): Unit = {
    throw new RuntimeException("Unexpected backtracking to IINC")
  }
}


/**
  * ALOAD instruction
  */
case class InstrALOAD(variable: Variable) extends LoadInstruction(v = variable) {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    val idx = env.getVarIdx(variable)
    mv.visitVarInsn(ALOAD, idx)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    load(mv, env, block, ALOAD, (_: MethodVisitor) => {})
  }

  override def getVariables() = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  /**
    * Used to identify the start of init method
    *
    * @see [[Rewrite.rewrite()]]
    */
  override def isALOAD0: Boolean = variable.getIdx().contains(0)

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = false)
}


/**
  * ASTORE: store reference into local variable
  *
  * @param variable
  */
case class InstrASTORE(variable: Variable) extends StoreInstruction(v = variable) {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    val idx = env.getVarIdx(variable)
    mv.visitVarInsn(ASTORE, idx)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    store(mv, env, block, ALOAD, ASTORE, (_: MethodVisitor) => {})
  }

  override def getVariables = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def doBacktrack(env: VMethodEnv): Unit = {
    // This should not happen. Backtracking can only go to ALOAD
    throw new RuntimeException("No expecting backtracking to ASTORE")
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = false)
}

/** Load long from local variable
  *
  * ... -> ..., value
  *
  * @param variable
  *                 local variable to be loaded
  */
case class InstrLLOAD(variable: Variable) extends LoadInstruction(v = variable) {

  /** Help env collect all local variables */
  override def getVariables: Set[LocalVar] = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitVarInsn(LLOAD, env.getVarIdx(variable))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    load(mv, env, block, LLOAD, long2Long)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = true)
}

/** Load float from local variable
  *
  * ... -> ..., value
  *
  * @param variable
  *                 local variable to be loaded
  */
case class InstrFLOAD(variable: Variable) extends LoadInstruction(v = variable) {

  /** Help env collect all local variables */
  override def getVariables: Set[LocalVar] = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitVarInsn(FLOAD, env.getVarIdx(variable))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    load(mv, env, block, FLOAD, float2Float)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = false)
}

/** Load double from local variable
  *
  * ... -> ..., value
  *
  * @param variable
  *                 local variable to be loaded
  */
case class InstrDLOAD(variable: Variable) extends LoadInstruction(v = variable) {

  /** Help env collect all local variables */
  override def getVariables: Set[LocalVar] = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitVarInsn(DLOAD, env.getVarIdx(variable))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    load(mv, env, block, DLOAD, double2Double)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = true)
}


/** Store long into local variable
  *
  * ..., value -> ...
  */
case class InstrLSTORE(variable: Variable) extends StoreInstruction(v = variable) {

  /** Help env collect all local variables */
  override def getVariables: Set[LocalVar] = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitVarInsn(LSTORE, env.getVarIdx(variable))

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    store(mv, env, block, LLOAD, LSTORE, long2Long)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = true)
}

case class InstrFSTORE(variable: Variable) extends StoreInstruction(v = variable) {

  override def getVariables: Set[LocalVar] = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitVarInsn(FSTORE, env.getVarIdx(variable))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    store(mv, env, block, FLOAD, FSTORE, float2Float)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = false)
}

case class InstrDSTORE(variable: Variable) extends StoreInstruction(v = variable) {

  /** Help env collect all local variables */
  override def getVariables: Set[LocalVar] = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitVarInsn(DSTORE, env.getVarIdx(variable))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    store(mv, env, block, DLOAD, DSTORE, double2Double)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack(s, env, is64Bit = true)
}