package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis._
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

abstract class StoreInstruction(val v: Variable) extends Instruction {
  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (value, prev, frame) = s.pop()
    // these are the two cases where we are certain that we need to lift this store instruction
    if (env.isLVStoredAcrossVBlocks(v) || value == V_TYPE(false))
      env.setLift(this)
    if (env.shouldLiftInstr(this)) {
      env.liftLV(v)
      // more specific tags to determine if we need wrapping or not
      if (value != V_TYPE(false))
        env.setTag(this, env.TAG_NEED_V)  // we need to wrap the value on the operand stack into V
      if (frame.localVar.contains(v) && !frame.localVar(v)._1.isInstanceOf[V_TYPE])
        env.setTag(this, env.TAG_NEED_V2)
    }
    val newFrame = frame.setLocal(
      v,
      if (env.shouldLiftInstr(this)) V_TYPE(false) else value,
      Set(this))
    (newFrame, Set())
  }
}

abstract class LoadInstruction(val v: Variable) extends Instruction {
  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    if (s.localVar(v)._1.isInstanceOf[V_TYPE]) {
      env.setLift(this)
    }
    if (env.shouldLiftInstr(this) && !s.localVar(v)._1.isInstanceOf[V_TYPE])
      env.setTag(this, env.TAG_NEED_V)
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(false), Set(this))
      else
        s.push(s.localVar(v)._1, Set(this))
    (newFrame, Set())
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
    if (env.shouldLiftInstr(this)) {
      if (env.getTag(this, env.TAG_NEED_V)) {
        int2Integer(mv)
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      }
      loadCurrentCtx(mv, env, block)
      mv.visitInsn(SWAP)
      if (env.getTag(this, env.TAG_NEED_V2)) {
        mv.visitVarInsn(ILOAD, env.getVarIdx(v))
        int2Integer(mv)
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      }
      else {
        loadV(mv, env, v)
      }
      callVCreateChoice(mv)
      storeV(mv, env, variable)
    }
    else {
      val idx = env.getVarIdx(variable)
      mv.visitVarInsn(ISTORE, idx)
    }
  }

  override def getVariables() = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }
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
    val idx = env.getVarIdx(variable)
    if (env.shouldLiftInstr(this)) {
      if (env.getTag(this, env.TAG_NEED_V)) {
        mv.visitVarInsn(ILOAD, idx)
        int2Integer(mv)
        callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      }
      else
        mv.visitVarInsn(ALOAD, idx)
    }
    else
      mv.visitVarInsn(ILOAD, env.getVarIdx(variable))
  }

  override def getVariables() = {
    variable match {
      case p: Parameter => Set()
      case lv: LocalVar => Set(lv)
    }
  }
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
    /*
     * Behavior of ALOAD is the same no matter V or not V
     */
    val idx = env.getVarIdx(variable)
    mv.visitVarInsn(ALOAD, idx)
    if (env.getTag(this, env.TAG_NEED_V))
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
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
    if (env.shouldLiftInstr(this)) {
      if (env.getTag(this, env.TAG_NEED_V))
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      loadCurrentCtx(mv, env, block)
      mv.visitInsn(SWAP)
      loadV(mv, env, variable)
      if (env.getTag(this, env.TAG_NEED_V2))
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      callVCreateChoice(mv)
      storeV(mv, env, variable)
    }
    else {
      val idx = env.getVarIdx(variable)
      mv.visitVarInsn(ASTORE, idx)
    }
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
    val idx = env.getVarIdx(variable)
    if (env.shouldLiftInstr(this)) {
      if (env.getTag(this, env.TAG_NEED_V)) {
        mv.visitVarInsn(LLOAD, idx)
        long2Long(mv)
        callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      }
      else
        mv.visitVarInsn(ALOAD, idx)
    }
    else
      mv.visitVarInsn(LLOAD, env.getVarIdx(variable))
  }
}

/** Load float from local variable
  *
  * ... -> ..., value
  *
  * @param variable
  *                 local variable to be loaded
  */
case class InstrFLOAD(variable: Variable) extends Instruction {

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
    if (env.shouldLiftInstr(this)) {
      loadV(mv, env, variable)
    }
    else
      mv.visitVarInsn(FLOAD, env.getVarIdx(variable))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    env.setLift(this)
    val newFrame = s.push(V_TYPE(false), Set(this))
    val backtrack: Set[Instruction] =
      if (s.localVar(variable)._1 != V_TYPE(false))
        s.localVar(variable)._2
      else
        Set()
    (newFrame, backtrack)

  }
}

/** Load double from local variable
  *
  * ... -> ..., value
  *
  * @param variable
  *                 local variable to be loaded
  */
case class InstrDLOAD(variable: Variable) extends Instruction {

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
    if (env.shouldLiftInstr(this)) {
      loadV(mv, env, variable)
    }
    else
      mv.visitVarInsn(DLOAD, env.getVarIdx(variable))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    env.setLift(this)
    val newFrame = s.push(V_TYPE(true), Set(this))
    val backtrack: Set[Instruction] =
      if (s.localVar(variable)._1 != V_TYPE(true))
        s.localVar(variable)._2
      else
        Set()
    (newFrame, backtrack)
  }
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
    if (env.shouldLiftInstr(this)) {
      if (env.getTag(this, env.TAG_NEED_V)) {
        long2Long(mv)
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      }
      loadCurrentCtx(mv, env, block)
      mv.visitInsn(SWAP)
      if (env.getTag(this, env.TAG_NEED_V2)) {
        mv.visitVarInsn(LLOAD, env.getVarIdx(v))
        long2Long(mv)
        callVCreateOne(mv, loadCurrentCtx(_, env, block))
      } else {
        loadV(mv, env, v)
      }
      callVCreateChoice(mv)
      storeV(mv, env, variable)
    }
    else {
      val idx = env.getVarIdx(variable)
      mv.visitVarInsn(LSTORE, idx)
    }
  }
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
    if (env.shouldLiftInstr(this)) {
      //new value is already on top of stack
      loadCurrentCtx(mv, env, block)
      mv.visitInsn(SWAP)
      loadV(mv, env, variable)
      //now ctx, newvalue, oldvalue on stack
      callVCreateChoice(mv)
      //now new choice value on stack combining old and new value
      storeV(mv, env, variable)
    }
    else {
      ??? // should not happen until we have a better DFA
      mv.visitVarInsn(FSTORE, env.getVarIdx(variable))
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    env.setLift(this)
    val (value, prev, frame) = s.pop()
    // For now, all local variables are V. Later, this could be relaxed with a careful tagV analysis
    val newFrame = frame.setLocal(variable, V_TYPE(false), Set(this))
    val backtrack =
      if (value != V_TYPE(false))
        prev
      else
        Set[Instruction]()
    (newFrame, backtrack)
  }
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
    if (env.shouldLiftInstr(this)) {
      //new value is already on top of stack
      loadCurrentCtx(mv, env, block)
      mv.visitInsn(SWAP)
      loadV(mv, env, variable)
      //now ctx, newvalue, oldvalue on stack
      callVCreateChoice(mv)
      //now new choice value on stack combining old and new value
      storeV(mv, env, variable)
    }
    else {
      ??? // should not happen until we have a better DFA
      mv.visitVarInsn(DSTORE, env.getVarIdx(variable))
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    env.setLift(this)
    val (value, prev, frame) = s.pop()
    // For now, all local variables are V. Later, this could be relaxed with a careful tagV analysis
    val newFrame = frame.setLocal(variable, V_TYPE(true), Set(this))
    val backtrack =
      if (value != V_TYPE(true))
        prev
      else
        Set[Instruction]()
    (newFrame, backtrack)
  }
}