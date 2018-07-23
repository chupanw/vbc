package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.OpcodePrint
import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis.{VBCFrame, V_TYPE}
import edu.cmu.cs.vbc.utils.LiftUtils
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.{Label, MethodVisitor, Type}

trait Instruction {

  def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block)

  def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block)

  /**
    * Update the stack symbolically after executing this instruction
    *
    * @return UpdatedFrame is a tuple consisting of new VBCFrame and a backtrack instructions.
    *         If backtrack instruction set is not empty, we need to backtrack because we finally realise we need to lift
    *         that instruction. By default every backtracked instruction should be lifted, except for GETFIELD,
    *         PUTFIELD, INVOKEVIRTUAL, and INVOKESPECIAL, because lifting them or not depends on the type of object
    *         currently on stack. If the object is a V, we need to lift these instructions with INVOKEDYNAMIC.
    *
    *         If backtrack instruction set is not empty, the returned VBCFrame is useless, current frame will be pushed
    *         to queue again and reanalyze later. (see [[edu.cmu.cs.vbc.analysis.VBCAnalyzer.computeBeforeFrames]]
    */
  def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame

  def doBacktrack(env: VMethodEnv) = env.setLift(this)

  def getVariables: Set[LocalVar] = Set()

  final def isJumpInstr: Boolean = getJumpInstr.isDefined

  def getJumpInstr: Option[JumpInstruction] = None

  def isReturnInstr: Boolean = false
  def isATHROW: Boolean = false
  def isRETURN: Boolean = false


  /**
    * Used to identify the start of init method
    *
    * @see [[Rewrite.rewrite()]]
    */
  def isALOAD0: Boolean = false

  /**
    * Used to identify the start of init method
    *
    * @see [[Rewrite.rewrite()]]
    */
  def isINVOKESPECIAL_OBJECT_INIT: Boolean = false

  /**
    * instructions should not be compared for structural equality but for object identity.
    * overwriting case class defaults to original Java defaults
    */
  override def equals(that: Any) = that match {
    case t: AnyRef => t eq this
    case _ => false
  }
}


case class UNKNOWN(opCode: Int = -1) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    throw new RuntimeException("Unknown Instruction in " + s"${env.clazz.name}#${env.method.name}" + ": " + OpcodePrint.print(opCode))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
  }


  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame =
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode) + s" in ${env.method.name} of ${env.clazz.name}")
}

trait EmptyInstruction extends Instruction

case class InstrNOP() extends EmptyInstruction {
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = toByteCode(mv, env, block)

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(NOP)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set.empty[Instruction])
}

case class InstrLINENUMBER(line: Int) extends EmptyInstruction {
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = toByteCode(mv, env, block)

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    val l = new Label()
    mv.visitLabel(l)
    mv.visitLineNumber(line, l)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set())
}


/**
  * Helper instruction for initializing conditional fields
  */
case class InstrINIT_CONDITIONAL_FIELDS() extends Instruction {
  import InstrINIT_CONDITIONAL_FIELDS._

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    // do nothing
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {

    if (env.method.name == "___clinit___") {
      env.clazz.fields.filter(f => f.isStatic && f.hasConditionalAnnotation()).foreach(f => {
        createChoice(f.name, mv, env, block)
        mv.visitFieldInsn(PUTSTATIC, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
      env.clazz.fields.filter(f => f.isStatic && !f.hasConditionalAnnotation()).foreach(f => {
        createOne(f, mv, env, block)
        mv.visitFieldInsn(PUTSTATIC, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
    }
    else {
      env.clazz.fields.filter(f => !f.isStatic && f.hasConditionalAnnotation()).foreach(f => {
        mv.visitVarInsn(ALOAD, 0)
        createChoice(f.name, mv, env, block)
        mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
      env.clazz.fields.filter(f => !f.isStatic && !f.hasConditionalAnnotation()).foreach(f => {
        mv.visitVarInsn(ALOAD, 0)
        createOne(f, mv, env, block)
        mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
    }
  }


  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set())
}

case object InstrINIT_CONDITIONAL_FIELDS {
  import LiftUtils._

  def createChoice(fName: String, mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitLdcInsn(fName)
    mv.visitMethodInsn(INVOKESTATIC, fexprfactoryClassName, "createDefinedExternal", "(Ljava/lang/String;)Lde/fosd/typechef/featureexpr/SingleFeatureExpr;", false)
    mv.visitInsn(ICONST_1)
    mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
    callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    mv.visitInsn(ICONST_0)
    mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
    callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    callVCreateChoice(mv)
  }

  def createOne(f: VBCFieldNode, mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    Type.getType(f.desc).getSort match {
      case Type.INT =>
        if (f.value == null) mv.visitInsn(ICONST_0) else pushConstant(mv, f.value.asInstanceOf[Int])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
      case Type.OBJECT => mv.visitInsn(ACONST_NULL)
      case Type.SHORT =>
        if (f.value == null) mv.visitInsn(ICONST_0) else pushConstant(mv, f.value.asInstanceOf[Int])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
      case Type.BYTE =>
        if (f.value == null) mv.visitInsn(ICONST_0) else pushConstant(mv, f.value.asInstanceOf[Int])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
      case Type.CHAR =>
        if (f.value == null) mv.visitInsn(ICONST_0) else pushConstant(mv, f.value.asInstanceOf[Int])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
      case Type.BOOLEAN =>
        if (f.value == null) mv.visitInsn(ICONST_0) else pushConstant(mv, f.value.asInstanceOf[Int])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
      case Type.LONG =>
        if (f.value == null) mv.visitInsn(LCONST_0) else pushLongConstant(mv, f.value.asInstanceOf[Long])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getLong, "valueOf", s"(J)${Owner.getLong.getTypeDesc}", false)
      case Type.DOUBLE =>
        if (f.value == null) mv.visitInsn(DCONST_0) else pushDoubleConstant(mv, f.value.asInstanceOf[Double])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getDouble, "valueOf", s"(D)${Owner.getDouble.getTypeDesc}", false)
      case Type.FLOAT =>
        if (f.value == null) mv.visitInsn(FCONST_0) else pushFloatConstant(mv, f.value.asInstanceOf[Float])
        float2Float(mv)
      case Type.ARRAY => mv.visitInsn(ACONST_NULL)
      case _ =>
        ???
    }
    callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
  }
}

case class InstrStartTimer(id: String) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {}

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitLdcInsn(id)
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/utils/Profiler"), MethodName("startTimer"), MethodDesc("(Ljava/lang/String;)V"), false)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = (s, Set())
}

case class InstrStopTimer(id: String) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {}

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitLdcInsn(id)
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/utils/Profiler"), MethodName("stopTimer"), MethodDesc("(Ljava/lang/String;)V"), false)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = (s, Set())
}

/**
  * Our own instruction for wrapping the value on stack into V.One
  *
  * This is used, for example, in our fake TryCatchBlocks to wrap the exceptions.
  */
case class InstrWrapOne() extends Instruction {
  import LiftUtils._
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {} // do nothing
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit =
    callVCreateOne(mv, loadCurrentCtx(_, env, block))
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (_, _, frame) = s.pop()
    (frame.push(V_TYPE(false), Set(this)), Set())
  }
}

case class InstrUpdateCtxFromVException() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {}

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    import LiftUtils._
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, MethodName("extractCtxFromVException"), MethodDesc(s"(Ljava/lang/Throwable;)$fexprclasstype"), false)
    loadCurrentCtx(mv, env, block)
    mv.visitMethodInsn(INVOKEINTERFACE, Owner(s"$fexprclassname"), MethodName("and"), MethodDesc(s"($fexprclasstype)$fexprclasstype"), true)
    mv.visitVarInsn(ASTORE, env.getBlockVarVIdx(block))
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = (s, Set())
}


case class InstrCheckThrow() extends Instruction {
  import edu.cmu.cs.vbc.utils.LiftUtils._
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {}
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
    loadCurrentCtx(mv, env, block)
    loadMethodCtx(mv, env)
    mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, MethodName("checkAndThrow"), MethodDesc(s"($vclasstype$fexprclasstype$fexprclasstype)V"), false)
  }
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, newFrame) = s.pop()
    val backtrack =
      if (!v.isInstanceOf[V_TYPE]) prev
      else Set[Instruction]()
    (s, backtrack)
  }
}
