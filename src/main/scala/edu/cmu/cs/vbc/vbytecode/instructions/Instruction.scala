package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.OpcodePrint
import edu.cmu.cs.vbc.analysis.VBCFrame
import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
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

  def getJumpInstr: Option[JumpInstruction] = None

  final def isJumpInstr: Boolean = getJumpInstr.isDefined

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
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
  }


  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame =
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode) + s" in ${env.method.name} of ${env.clazz.name}")
}

trait EmptyInstruction extends Instruction

case class InstrNOP() extends EmptyInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(NOP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = toByteCode(mv, env, block)

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set.empty[Instruction])
}

case class InstrLINENUMBER(line: Int) extends EmptyInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    val l = new Label()
    mv.visitLabel(l)
    mv.visitLineNumber(line, l)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = toByteCode(mv, env, block)

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
      import edu.cmu.cs.vbc.utils.LiftUtils._
      env.clazz.fields.filter(f => !f.isStatic && f.hasConditionalAnnotation()).foreach(f => {
        mv.visitVarInsn(ALOAD, 0)
        TypeDesc(f.desc) match {
          case t if t.isPrimitive =>
            createPrimChoice(f, mv, env, block)
            mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, t.toVPrimType)
          case _ =>
            createChoice(f.name, mv, env, block)
            mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, vclasstype)
        }
      })
      env.clazz.fields.filter(f => !f.isStatic && !f.hasConditionalAnnotation()).foreach(f => {
        mv.visitVarInsn(ALOAD, 0)
        TypeDesc(f.desc) match {
          case t if t.isPrimitive =>
            createPrimOne(f, mv, env, block)
            mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, t.toVPrimType)
          case _ =>
            createOne(f, mv, env, block)
            mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, vclasstype)
        }
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
      case Type.BOOLEAN =>
        if (f.value == null) mv.visitInsn(ICONST_0) else pushConstant(mv, f.value.asInstanceOf[Int])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", s"(I)${Owner.getInt.getTypeDesc}", false)
      case Type.LONG =>
        if (f.value == null) mv.visitInsn(LCONST_0) else pushLongConstant(mv, f.value.asInstanceOf[Long])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getLong, "valueOf", s"(J)${Owner.getLong.getTypeDesc}", false)
      case Type.ARRAY => mv.visitInsn(ACONST_NULL)
      case _ =>
        ???
    }
    callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
  }

  def createPrimChoice(f: VBCFieldNode, mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    val fieldDesc = TypeDesc(f.desc)
    mv.visitLdcInsn(f.name)
    mv.visitMethodInsn(INVOKESTATIC, fexprfactoryClassName, "createDefinedExternal", "(Ljava/lang/String;)Lde/fosd/typechef/featureexpr/SingleFeatureExpr;", false)
    mv.visitInsn(ICONST_1)
    callVPrimCreateOne(mv, (m) => loadCurrentCtx(m, env, block), fieldDesc)
    mv.visitInsn(ICONST_0)
    callVPrimCreateOne(mv, (m) => loadCurrentCtx(m, env, block), fieldDesc)
    callVPrimCreateChoice(mv, fieldDesc)
  }

  def createPrimOne(f: VBCFieldNode, mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    Type.getType(f.desc).getSort match {
      case Type.INT | Type.BOOLEAN | Type.CHAR | Type.BYTE =>
        if (f.value == null) mv.visitInsn(ICONST_0) else pushConstant(mv, f.value.asInstanceOf[Int])
      case Type.LONG =>
        if (f.value == null) mv.visitInsn(LCONST_0) else pushLongConstant(mv, f.value.asInstanceOf[Long])
        mv.visitMethodInsn(INVOKESTATIC, Owner.getLong, "valueOf", s"(J)${Owner.getLong.getTypeDesc}", false)
      case _ =>
        ???
    }
    callVPrimCreateOne(mv, (m) => loadCurrentCtx(m, env, block), TypeDesc(f.desc))
  }
}
