package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.{MethodVisitor, Type}


object LiftUtils {
  //    val liftedPackagePrefixes = Set("edu.cmu.cs.vbc.test", "edu.cmu.cs.vbc.prog")
  val vclassname = "edu/cmu/cs/varex/V"
  val fexprclassname = "de/fosd/typechef/featureexpr/FeatureExpr"
  val fexprfactoryClassName = "de/fosd/typechef/featureexpr/FeatureExprFactory"
  val vopsclassname = "edu/cmu/cs/varex/VOps"
  val vclasstype = "L" + vclassname + ";"
  val fexprclasstype = "L" + fexprclassname + ";"
  val ctxParameterOffset = 1

  val lamdaFactoryOwner = "java/lang/invoke/LambdaMetafactory"
  val lamdaFactoryMethod = "metafactory"
  val lamdaFactoryDesc = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"

  def liftMethodSignature(desc: String, sig: Option[String]): Option[String] = {
    val sigReader = new SignatureReader(sig.getOrElse(MethodDesc(desc).toObjects.toModels))
    val sw = new LiftSignatureWriter()
    sigReader.accept(sw)
    val newSig = sw.getSignature()
    if (sig != None || newSig != MethodDesc(desc).appendFE.toString)
      Some(newSig)
    else None
  }

  def pushConstant(mv: MethodVisitor, value: Int): Unit = {
    value match {
      case 0 => mv.visitInsn(ICONST_0)
      case 1 => mv.visitInsn(ICONST_1)
      case 2 => mv.visitInsn(ICONST_2)
      case 3 => mv.visitInsn(ICONST_3)
      case 4 => mv.visitInsn(ICONST_4)
      case 5 => mv.visitInsn(ICONST_5)
      case v if v <= Byte.MaxValue && v >= Byte.MinValue => mv.visitIntInsn(BIPUSH, v)
      case _ => mv.visitLdcInsn(value)
    }
  }

  def pushLongConstant(mv: MethodVisitor, value: Long): Unit = {
    value match {
      case 0 => mv.visitInsn(LCONST_0)
      case 1 => mv.visitInsn(LCONST_1)
      case v => mv.visitLdcInsn(v)
    }
  }

  def pushDoubleConstant(mv: MethodVisitor, value: Double): Unit = {
    value match {
      case 0 => mv.visitInsn(DCONST_0)
      case 1 => mv.visitInsn(DCONST_1)
      case v => mv.visitLdcInsn(v)
    }
  }

  def pushFloatConstant(mv: MethodVisitor, value: Float): Unit = mv.visitLdcInsn(value)

  def pushConstantFALSE(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, fexprfactoryClassName, "False", "()Lde/fosd/typechef/featureexpr/FeatureExpr;", false)

  def pushConstantTRUE(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, fexprfactoryClassName, "True", "()Lde/fosd/typechef/featureexpr/FeatureExpr;", false)

  //  def callFExprIsSatisfiable(mv: MethodVisitor) =
  //    mv.visitMethodInsn(INVOKEINTERFACE, fexprclassname, "isSatisfiable", "()Z", true)

  /**
    * This version uses caching, via [[edu.cmu.cs.varex.VCache]]
    */
  def callFExprIsSatisfiable(mv: MethodVisitor) = {
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/varex/VCache", "isSatisfiable", s"($fexprclasstype)Z", false)
  }

  //  def callFExprIsContradiction(mv: MethodVisitor) =
  //    mv.visitMethodInsn(INVOKEINTERFACE, fexprclassname, "isContradiction", "()Z", true)

  /**
    * This version uses caching, via [[edu.cmu.cs.varex.VCache]]
    *
    * @param mv
    */
  def callFExprIsContradiction(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/varex/VCache", "isContradiction", s"($fexprclasstype)Z", false)

  def callFExprOr(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKEINTERFACE, fexprclassname, "or", "(Lde/fosd/typechef/featureexpr/FeatureExpr;)Lde/fosd/typechef/featureexpr/FeatureExpr;", true)

  //  def callFExprAnd(mv: MethodVisitor) =
  //    mv.visitMethodInsn(INVOKEINTERFACE, fexprclassname, "and", "(Lde/fosd/typechef/featureexpr/FeatureExpr;)Lde/fosd/typechef/featureexpr/FeatureExpr;", true)

  /**
    * This version uses caching, via [[edu.cmu.cs.varex.VCache]]
    *
    * @param mv
    */
  def callFExprAnd(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/varex/VCache", "and", s"($fexprclasstype$fexprclasstype)$fexprclasstype", false)

  //  def callFExprNot(mv: MethodVisitor) =
  //    mv.visitMethodInsn(INVOKEINTERFACE, fexprclassname, "not", "()Lde/fosd/typechef/featureexpr/FeatureExpr;", true)

  /**
    * This version uses caching, via [[edu.cmu.cs.varex.VCache]]
    *
    * @param mv
    */
  def callFExprNot(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/varex/VCache", "not", s"($fexprclasstype)$fexprclasstype", false)

  def storeFExpr(mv: MethodVisitor, env: MethodEnv, v: Variable) =
    mv.visitVarInsn(ASTORE, env.getVarIdx(v))

  def loadFExpr(mv: MethodVisitor, env: MethodEnv, v: Variable) =
    mv.visitVarInsn(ALOAD, env.getVarIdx(v))

  def loadCurrentCtx(mv: MethodVisitor, env: VMethodEnv, block: Block) =
  /*if (env.isMain) pushConstantTRUE(mv) else */ loadFExpr(mv, env, env.getVBlockVar(block))

  def loadMethodCtx(mv: MethodVisitor, env: VMethodEnv): Unit = mv.visitVarInsn(ALOAD, env.getVarIdx(env.ctxParameter))

  def storeV(mv: MethodVisitor, env: MethodEnv, v: Variable) =
    mv.visitVarInsn(ASTORE, env.getVarIdx(v))

  def loadV(mv: MethodVisitor, env: MethodEnv, v: Variable) =
    mv.visitVarInsn(ALOAD, env.getVarIdx(v))

  def updateBlockCtxIfNotThrowingException(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    loadCurrentCtx(mv, env, block)
    mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, MethodName("updateCurrentContextFromGlobal"), MethodDesc(s"($fexprclasstype)$fexprclasstype"), false) // insert this call into INVOKEDYNAMIC
    mv.visitVarInsn(ASTORE, env.getBlockVarVIdx(block))
  }

  /**
    * precondition: plain reference on top of stack
    * postcondition: V reference on top of stack
    */
  def callVCreateOne(mv: MethodVisitor, loadCtx: (MethodVisitor) => Unit) = {
    loadCtx(mv)
    mv.visitInsn(SWAP)
    mv.visitMethodInsn(INVOKESTATIC, vclassname, "one", s"(${fexprclasstype}Ljava/lang/Object;)$vclasstype", true)
  }

  /**
    * precondition: feature expression and two V references on top of stack
    * postcondition: V reference on top of stack
    */
  def callVCreateChoice(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, vclassname, "choice", "(Lde/fosd/typechef/featureexpr/FeatureExpr;Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;)Ledu/cmu/cs/varex/V;", true)

  /**
    * Helper function to get the method descriptor
    *
    * @param strs strs.last is the return type, the rest is parameter list
    * @return method descriptor according to ASM library standard
    */
  def genSign(strs: String*): String =
    strs.dropRight(1).mkString("(", "", ")") + strs.last

  def replaceArgsWithObject(desc: String): String =
    "(" + "Ljava/lang/Object;" * Type.getArgumentTypes(desc).length + ")" + Type.getReturnType(desc)

  //////////////////////////////////////////////////
  // Model class related utils
  //////////////////////////////////////////////////

  def vCls(cls: String) = s"edu/cmu/cs/vbc/model/$cls"

  def vClsType(cls: String) = s"Ledu/cmu/cs/vbc/model/$cls;"

  val IntClass = "java/lang/Integer"
  val IntType = "Ljava/lang/Integer;"
  val BooleanClass = "java/lang/Boolean"
  val BooleanType = "Ljava/lang/Boolean;"
  val StringClass = "java/lang/String"
  val StringType = "Ljava/lang/String;"
  val ObjectClass = "java/lang/Object"
  val ObjectType = "Ljava/lang/Object;"

  def int2Integer(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, Owner.getInt, "valueOf", MethodDesc(s"(I)${TypeDesc.getInt}"), false)

  def Integer2int(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKEVIRTUAL, Owner.getInt, "intValue", MethodDesc("()I"), false)

  def long2Long(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, Owner.getLong, "valueOf", MethodDesc(s"(J)${TypeDesc.getLong}"), false)

  def Long2long(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKEVIRTUAL, Owner.getLong, "longValue", MethodDesc("()J"), false)

  def Character2char(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKEVIRTUAL, Owner.getChar, "charValue", MethodDesc("()C"), false)

  def char2Character(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, Owner.getChar, "valueOf", MethodDesc(s"(C)${TypeDesc.getChar}"), false)

  def double2Double(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, Owner.getDouble, "valueOf", MethodDesc(s"(D)${TypeDesc.getDouble}"), false)

  def Double2double(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKEVIRTUAL, Owner.getDouble, "doubleValue", MethodDesc("()D"), false)

  def float2Float(mv: MethodVisitor) =
    mv.visitMethodInsn(INVOKESTATIC, Owner.getFloat, "valueOf", MethodDesc(s"(F)${TypeDesc.getFloat}"), false)

  def boxReturnValue(typeSort: Int, mv: MethodVisitor): Unit = {
    typeSort match {
      case Type.INT | Type.BOOLEAN | Type.BYTE | Type.CHAR | Type.SHORT =>
        mv.visitMethodInsn(INVOKESTATIC,
          Owner.getInt,
          MethodName("valueOf"),
          MethodDesc(s"(I)${TypeDesc.getInt}"),
          false)
      case Type.LONG =>
        mv.visitMethodInsn(INVOKESTATIC,
          Owner.getLong,
          MethodName("valueOf"),
          MethodDesc(s"(J)${TypeDesc.getLong}"),
          false)
      case Type.DOUBLE =>
        mv.visitMethodInsn(INVOKESTATIC,
          Owner.getDouble,
          MethodName("valueOf"),
          MethodDesc(s"(D)${TypeDesc.getDouble}"),
          false)
      case Type.FLOAT =>
        mv.visitMethodInsn(INVOKESTATIC,
          Owner.getFloat,
          MethodName("valueOf"),
          MethodDesc(s"(F)${TypeDesc.getFloat}"),
          false)
      case Type.OBJECT => // do nothing
      case Type.VOID => // do nothing
      case Type.ARRAY => // do nothing
      case _ => ???
    }
  }
}