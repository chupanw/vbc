package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.{FrameEntry, UpdatedFrame}
import edu.cmu.cs.vbc.analysis.{VBCFrame, VBCType, V_REF_TYPE, V_TYPE}
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

/**
  * IF* and GOTO instructions
  */
trait JumpInstruction extends Instruction {
  /**
    * gets the successor of a jump. the first value is the
    * target of an unconditional jump, but it can be None
    * when it just falls through to the next block
    * the second value is the target of a conditional jump,
    * if any
    */
  def getSuccessor(): (Option[Int], Option[Int])

  override def getJumpInstr: Option[JumpInstruction] = Some(this)

  def updateStack1(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v1, prev1, newFrame) = s.pop()
    env.setLift(this)
    if (v1 != V_TYPE(false)) return (newFrame, prev1)
    val backtrack = backtrackNonVStackElements(s)
    (newFrame, backtrack)
  }

  def updateStack2(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, newFrame) = frame1.pop()
    env.setLift(this)
    if (v1 != V_TYPE(false)) return (newFrame, prev1)
    if (v1 != V_TYPE(false)) return (newFrame, prev2)
    val backtrack = backtrackNonVStackElements(s)
    (newFrame, backtrack)
  }

  def backtrackNonVStackElements(f: VBCFrame): Set[Instruction] = {
    (Tuple2[VBCType, Set[Instruction]](V_TYPE(false), Set()) /: f.stack) (  // initial V_TYPE(false) is useless
      (a: FrameEntry, b: FrameEntry) => {
        if (b._1.isInstanceOf[V_REF_TYPE])
          throw new RuntimeException("Could not store uninitialized objects to unbalanced stack variables")
        if (!b._1.isInstanceOf[V_TYPE]) (a._1, a._2 ++ b._2)
        else a
      })._2
  }

  def copy(target: Int): JumpInstruction = this match {
    case _: InstrIFEQ => InstrIFEQ(target)
    case _: InstrIFNE => InstrIFNE(target)
    case _: InstrGOTO => InstrGOTO(target)
    case _: InstrIF_ICMPEQ => InstrIF_ICMPEQ(target)
    case _: InstrIF_ICMPGE => InstrIF_ICMPGE(target)
    case _: InstrIFGE => InstrIFGE(target)
    case _: InstrIF_ICMPLT => InstrIF_ICMPLT(target)
    case _: InstrIF_ICMPNE => InstrIF_ICMPNE(target)
    case _: InstrIFGT => InstrIFGT(target)
    case _: InstrIFNONNULL => InstrIFNONNULL(target)
    case _: InstrIFLT => InstrIFLT(target)
    case _: InstrIF_ICMPLE => InstrIF_ICMPLE(target)
    case _: InstrIFNULL => InstrIFNULL(target)
    case _: InstrIFLE => InstrIFLE(target)
    case _: InstrIF_ICMPGT => InstrIF_ICMPGT(target)
    case _: InstrIF_ACMPNE => InstrIF_ACMPNE(target)
    case _: InstrIF_ACMPEQ => InstrIF_ACMPEQ(target)
  }
}

/**
  * assumptions (for now)
  *
  * the if statement is the last statement in a block, making a decision
  * between the next block or the referenced block
  *
  * for now, jumps can only be made forward, not backward (loops not yet
  * supported)
  *
  * for now, blocks need to be balanced wrt to the stack (not enforced yet)
  */
case class InstrIFEQ(targetBlockIdx: Int) extends JumpInstruction {


  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    val targetBlock = env.getBlock(targetBlockIdx)
    mv.visitJumpInsn(IFEQ, env.getBlockLabel(targetBlock))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {

    /**
      * creating a variable for the decision
      *
      * on top of the stack is the condition, which should be V[Int];
      * that is, we want to know when that value is different from 0
      *
      * the condition is then stored as feature expression in a new
      * variable. this variable is used at the beginning of the relevant
      * blocks to modify the ctx
      *
      * the actual modification of ctx happens Block.toVByteCode
      */

    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenEQ", "(Ledu/cmu/cs/varex/V;)Lde/fosd/typechef/featureexpr/FeatureExpr;", false)
    //only evaluate condition, jump in block implementation
    else
      mv.visitJumpInsn(IFEQ, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def getSuccessor() = (None, Some(targetBlockIdx))

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack1(s, env)
}


/**
  * InstrIFNE: jump if the value on top of stack is not 0
  *
  * @param targetBlockIdx
  */
case class InstrIFNE(targetBlockIdx: Int) extends JumpInstruction {

  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IFNE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenNE", "(Ledu/cmu/cs/varex/V;)Lde/fosd/typechef/featureexpr/FeatureExpr;", false)
    else
      mv.visitJumpInsn(IFNE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack1(s, env)
}


case class InstrGOTO(targetBlockIdx: Int) extends JumpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    val targetBlock = env.getBlock(targetBlockIdx)
    mv.visitJumpInsn(GOTO, env.getBlockLabel(targetBlock))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (!env.shouldLiftInstr(this)) {
      mv.visitJumpInsn(GOTO, env.getBlockLabel(env.getBlock(targetBlockIdx)))
    }
    // if we should lift GOTO, it will be handled in block implementation
  }


  override def getSuccessor() = (Some(targetBlockIdx), None)

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    // Lift only if it is jump across VBlock boundary
    // OR
    // if it is jump to a VBlock head because this block might leave some value on the operand stack
    if (env.getVBlock(env.getBlockForInstruction(this)) != env.getVBlock(env.getBlock(targetBlockIdx)) ||
      env.isVBlockHead(env.getBlock(targetBlockIdx)))
      env.setLift(this)
    val backtrack = backtrackNonVStackElements(s)
    (s, backtrack)
  }
}


case class InstrIF_ICMPEQ(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * (Unconditional target, Conditional target)
    * None if next block for unconditional target
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ICMPEQ, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenIEQ", "(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;)Lde/fosd/typechef/featureexpr/FeatureExpr;", false)
    else
      mv.visitJumpInsn(IF_ICMPEQ, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack2(s, env)
}


case class InstrIF_ICMPGE(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * (Unconditional target, Conditional target)
    * None if next block for unconditional target
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ICMPGE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenIGE", s"($vclasstype$vclasstype$fexprclasstype)$fexprclasstype", false)
    }
    else
      mv.visitJumpInsn(IF_ICMPGE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack2(s, env)
}


case class InstrIFGE(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * (Unconditional target, Conditional target)
    * None if next block for unconditional target
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IFGE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenGE", genSign(vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IFGE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack1(s, env)
}


case class InstrIF_ICMPLT(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * (Unconditional target, Conditional target)
    * None if next block for unconditional target
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ICMPLT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenILT", s"($vclasstype$vclasstype$fexprclasstype)$fexprclasstype", false)
    }
    else
      mv.visitJumpInsn(IF_ICMPLT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack2(s, env)
}


case class InstrIF_ICMPNE(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * (Unconditional target, Conditional target)
    * None if next block for unconditional target
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ICMPNE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenINE", genSign(vclasstype, vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IF_ICMPNE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack2(s, env)
}


case class InstrIFGT(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * (Unconditional target, Conditional target)
    * None if next block for unconditional target
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IFGT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenGT", genSign(vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IFGT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = updateStack1(s, env)
}

case class InstrIFNONNULL(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * gets the successor of a jump. the first value is the
    * target of an unconditional jump, but it can be None
    * when it just falls through to the next block
    * the second value is the target of a conditional jump,
    * if any
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IFNONNULL, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack1(s, env)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenNONNULL", genSign(vclasstype, fexprclasstype), false)
    }
    else {
      mv.visitJumpInsn(IFNONNULL, env.getBlockLabel(env.getBlock(targetBlockIdx)))
    }
  }
}


case class InstrIFLT(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * gets the successor of a jump. the first value is the
    * target of an unconditional jump, but it can be None
    * when it just falls through to the next block
    * the second value is the target of a conditional jump,
    * if any
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IFLT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack1(s, env)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenLT", genSign(vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IFLT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }
}

case class InstrIF_ICMPLE(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * gets the successor of a jump. the first value is the
    * target of an unconditional jump, but it can be None
    * when it just falls through to the next block
    * the second value is the target of a conditional jump,
    * if any
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ICMPLE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack2(s, env)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenILE", genSign(vclasstype, vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IF_ICMPLE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }
}

case class InstrIFNULL(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * gets the successor of a jump. the first value is the
    * target of an unconditional jump, but it can be None
    * when it just falls through to the next block
    * the second value is the target of a conditional jump,
    * if any
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit =
    mv.visitJumpInsn(IFNULL, env.getBlockLabel(env.getBlock(targetBlockIdx)))

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack1(s, env)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenNULL", genSign(vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IFNULL, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }
}

case class InstrIFLE(targetBlockIdx: Int) extends JumpInstruction {
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IFLE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenLE", genSign(vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IFLE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack1(s, env)
}

case class InstrIF_ICMPGT(targetBlockIdx: Int) extends JumpInstruction {
  /**
    * gets the successor of a jump. the first value is the
    * target of an unconditional jump, but it can be None
    * when it just falls through to the next block
    * the second value is the target of a conditional jump,
    * if any
    */
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ICMPGT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenIGT", s"($vclasstype$vclasstype$fexprclasstype)$fexprclasstype", false)
    }
    else
      mv.visitJumpInsn(IF_ICMPGT, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack2(s, env)
}

/** Branch if reference comparison succeeds
  *
  * ..., value1(reference), value2(reference) => ...
  */
case class InstrIF_ACMPNE(targetBlockIdx: Int) extends JumpInstruction {
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ACMPNE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenANE", genSign(vclasstype, vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IF_ACMPNE, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack2(s, env)
}

/** Branch if reference comparison succeeds.
  *
  * ..., value1(reference), value2(reference) => ...
  */
case class InstrIF_ACMPEQ(targetBlockIdx: Int) extends JumpInstruction {
  override def getSuccessor(): (Option[Int], Option[Int]) = (None, Some(targetBlockIdx))

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitJumpInsn(IF_ACMPEQ, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenAEQ", genSign(vclasstype, vclasstype, fexprclasstype), false)
    else
      mv.visitJumpInsn(IF_ACMPEQ, env.getBlockLabel(env.getBlock(targetBlockIdx)))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStack2(s, env)
}
