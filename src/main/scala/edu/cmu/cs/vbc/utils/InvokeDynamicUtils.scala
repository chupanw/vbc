package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.{ClassVisitor, Handle, MethodVisitor, Type}

/**
  * Utilities for doing invokedynamic on V object. For example, GETFIELD, ANEWARRAY, AASTORE, etc.
  *
  * Support nested invokedynamic. For example, AASTORE is usually invoked on a V array ref, with V index and
  * V value on stack.
  *
  * Stack before execution: ... array(V), index(V), value(V)
  * Stack after execution: ...
  *
  * To do invokedynamic on V array reference, the desc parameter for [[InvokeDynamicUtils.invoke()]] method should be:
  *
  * [Ledu/cmu/cs/varex/V;(Ljava/lang/Integer;Ledu/cmu/cs/varex/V;)V
  *
  * The type before parenthesis is the type of object that invokedynamic is performed on, the types inside parenthesis are
  * types of the arguments, and finally the type after parenthesis is the return type.
  *
  * Since index is a V, we need a nested invokedynamic. The desc for this nested invokedynamic should be:
  *
  * [Ljava/lang/Integer;(Ledu/cmu/cs/varex/V;[edu/cmu/cs/varex/V;)V
  *
  * Note that this desc is different from the previous one because arguments need to be shifted to meet different call sites.
  * Shifting rule is documented in method [[InvokeDynamicUtils.shiftDesc()]].
  *
  * While calling invoke, lambda methods will be generated to explode arguments if necessary, but users need to provide a final
  * lambda method body. For example, for GETFIELD, the method body includes loading object reference and invoking GETFIELD.
  *
  * @author chupanw
  */
object InvokeDynamicUtils {

  val biFuncType = "Ljava/util/function/BiFunction;"
  val biConsumerType = "Ljava/util/function/BiConsumer;"
  val toIntBiFuncType = "Ljava/util/function/ToIntBiFunction;"
  val objIntConsumerType = "Ljava/util/function/ObjIntConsumer;"

  def invokeWithCacheClear(
                            vCall: VCall.Value,
                            mv: MethodVisitor,
                            env: VMethodEnv,
                            loadCtx: MethodVisitor => Unit,
                            lambdaName: String,
                            desc: String,
                            nExplodeArgs: Int = 0,
                            expandArgArray: Boolean
                          )
                          (lambdaOp: MethodVisitor => Unit): Unit = {
    // clear the cache of ArrayOps
    if (expandArgArray)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, MethodName("clearCache"), MethodDesc("()V"), false)
    invoke(vCall, mv, env, loadCtx, lambdaName, desc, nExplodeArgs, expandArgArray = expandArgArray)(lambdaOp)
  }

  /**
    * Perform the sMap operation
    *
    * @param vCall
    *              method of V interface to be called
    * @param mv
    *           current method visitor
    * @param env
    *            current method environment
    * @param loadCtx
    *                       default way of loading context
    * @param lambdaName
    *                   key word for the purpose of this lambda method, mostly for debugging purpose
    * @param desc
    *             Format: typeA(typeB*)typeC
    *                     typeA describe the V object on which operations are performed
    *                     typeB* describe the arguments passed to lambda function
    *                     typeC is the return type of lambda function
    * @param nExplodeArgs
    *                     number of arguments to explode, arguments to be exploded should be the
    *                       first $nExplodeArgs values in the argument list
    * @param isExploding
    *                    whether we are generating lambda methods to explode arguments
    * @param lambdaOp
    *                 lambda method body
    */
  def invoke(
              vCall: VCall.Value,
              mv: MethodVisitor,
              env: VMethodEnv,
              loadCtx: MethodVisitor => Unit,
              lambdaName: String,
              desc: String,
              nExplodeArgs: Int = 0,
              isExploding: Boolean = false,
              expandArgArray: Boolean = false,
              isArrayExpanded: Boolean = false  //todo: avoid this parameter
            )
            (lambdaOp: MethodVisitor => Unit): Unit = {

    def areSamePrimitive(t1: TypeDesc, t2: TypeDesc): Boolean =
      t1.isPrimitiveWithV && t2.isPrimitiveWithV && t1.toVType == t2.toVType

    val (invokeObjectDescStr, argsDesc, retDescStr) = decomposeDesc(desc)
    val invokeObjectDesc = TypeDesc(invokeObjectDescStr)
    val retDescIsVoid = retDescStr == "V"
    val retDesc = if (!retDescIsVoid) Some(TypeDesc(retDescStr)) else None
    val lambdaRetDescStr =
      if (retDescIsVoid) "V"
      else if (areSamePrimitive(invokeObjectDesc, retDesc.get)) retDesc.get.toVPrimType
      else vclasstype
    val lambdaRetDesc = if (!retDescIsVoid) Some(TypeDesc(lambdaRetDescStr)) else None

    val argTypes: Array[Type] = Type.getArgumentTypes(s"($argsDesc)")
    val nArg = argTypes.size

    val argTypeStr = ((for (t <- argTypes.take(nExplodeArgs))
                       yield new TypeDesc(t).toVType.desc) ++
                     (for (t <- argTypes.drop(nExplodeArgs))
                       yield t.toString)).mkString("")

    //////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////
    val firstArgType = if (nArg == 0) None else Some(argTypes(0))
    val (invokeDynamicName, vCallDesc, funType, isReturnVoid, convertBefore, convertAfter, lambda2ndArg) =
      (vCall, invokeObjectDesc, lambdaRetDesc) match {
          // VPrim.smap(P -> P) where (P in Prim) : No conversion
        case (VCall.smap, _, Some(ret)) if ret.isPrimitiveWithV && ret.toVPrimType == invokeObjectDescStr =>
          ("apply", s"($biFuncType$fexprclasstype)${ret.toVPrimType}", biFuncType, false, false, false, "Ljava/lang/Object;")
        // VPrim.smap(P -> U) where (P in Prim) (U not in Prim) : Convert VPrim -> V before mapping
        // note that these case orderings are significant (e.g: this case being met implies !descIsPrim(ret.desc))
        case (VCall.smap, _, Some(ret)) if ret.toVPrimType == invokeObjectDescStr =>
          ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, true, false, "Ljava/lang/Object;")
        // V.smap(U -> P) where (P in Prim) (U not in Prim) : Convert V -> VPrim after mapping
        case (VCall.smap, _, Some(ret)) if ret.isPrimitiveWithV =>
          ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, false, true, "Ljava/lang/Object;")

          // VPrim.sflatMap(P -> P) where (P in Prim) : No conversion
        case (VCall.sflatMap, _, Some(ret)) if ret.isPrimitiveWithV && ret.toVPrimType == invokeObjectDescStr =>
          ("apply", s"($biFuncType$fexprclasstype)${ret.toVPrimType}", biFuncType, false, false, false, "Ljava/lang/Object;")
          // VPrim.sflatMap(P -> U) where (P in Prim) (U not in Prim) : Convert VPrim -> V before mapping
        case (VCall.sflatMap, _, Some(ret)) if ret.toVPrimType == invokeObjectDescStr =>
          ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, true, false, "Ljava/lang/Object;")
        // V.sflatMap(U -> P) where (P in Prim) (U not in Prim) : Convert V -> VPrim after mapping
        case (VCall.sflatMap, _, Some(ret)) if ret.isPrimitiveWithV =>
          ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, false, true, "Ljava/lang/Object;")

        // V.s{map, flatMap}(U -> T) : No conversion
      case (VCall.smap, _, _) | (VCall.sflatMap, _, _) =>
        ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, false, false, "Ljava/lang/Object;")

      case (VCall.sforeach, t, _) =>
        ("accept", s"(${t.getConsumerType}$fexprclasstype)V", t.getConsumerType, true, false, false,
          if (t.isPrimitiveWithV) t.desc else "Ljava/lang/Object;")

      case _ => throw new RuntimeException("Unsupported dynamic invoke type: " + vCall)
    }

    //////////////////////////////////////////////////
    // Call INVOKEDYNAMIC
    //////////////////////////////////////////////////


    val invokeDynamicType = "(" + argTypeStr + s")$funType"

    val invokeObjectArrayType = (if (invokeObjectDescStr.startsWith("[")) Some(TypeDesc(invokeObjectDescStr.drop(1))) else None)
    val newInvokeObjDesc: String =
      if (expandArgArray && invokeObjectDescStr.startsWith("[") && !isArrayExpanded)
        ("[" + (if (invokeObjectArrayType.get.isPrimitiveWithV) invokeObjectArrayType.get.toVPrimType else vclasstype))
      else if (convertBefore) vclasstype
      else invokeObjectDescStr
    val lambdaDesc = s"($argTypeStr$fexprclasstype$newInvokeObjDesc)$lambdaRetDescStr"

    val n = env.clazz.lambdaMethods.size
    val lambdaMtdName: String = "lambda$" + lambdaName + "$" + n
    // invoke the lambda for this nesting-level
    mv.visitInvokeDynamicInsn(
      invokeDynamicName,
      invokeDynamicType,
      new Handle(H_INVOKESTATIC, lamdaFactoryOwner, lamdaFactoryMethod, lamdaFactoryDesc),
      // Arguments:
      Type.getType(s"(Ljava/lang/Object;$lambda2ndArg)" + (if (isReturnVoid) "V" else "Ljava/lang/Object;")),
      new Handle(H_INVOKESTATIC, env.clazz.name, lambdaMtdName, lambdaDesc),
      Type.getType(s"($fexprclasstype$newInvokeObjDesc)$lambdaRetDescStr")
    )
    if (isExploding) {
      loadFE(mv, loadCtx, Some(lambdaDesc))
    }
    else {
      loadFE(mv, loadCtx, None)
    }

    val invokeObj = TypeDesc(invokeObjectDescStr)
    if (convertBefore) {
      // mapping VPrim to something else, convert to V first
      mv.visitMethodInsn(INVOKEINTERFACE, invokeObj.toVName, "toV", s"()$vclasstype", true)
    }
    mv.visitMethodInsn(INVOKEINTERFACE, invokeObj.toVName, vCall.toString, vCallDesc, true)
    if (convertAfter && !retDescIsVoid) {
      // mapping V to Prim, convert to VPrim afterwards
      mv.visitMethodInsn(INVOKEINTERFACE, invokeObj.toVName, lambdaRetDesc.get.toVPrimFunction,
        s"()${lambdaRetDesc.get.toVPrimType}", true)
    }


    //////////////////////////////////////////////////
    // helper methods for expanding arrays
    //////////////////////////////////////////////////
    val currentInvokeObjType = TypeDesc(invokeObjectDescStr)
    def shouldExpandArray: Boolean = expandArgArray && currentInvokeObjType.isArray && !isArrayExpanded
    def expandArray(mv: MethodVisitor) = {
      mv.visitVarInsn(ALOAD, nArg + 1)  // [V
      0 until nArg foreach {i => mv.visitVarInsn(ALOAD, i)}
      mv.visitVarInsn(ALOAD, nArg)
      val baseType = currentInvokeObjType.getArrayBaseType
      val baseTypeStr = if (baseType.isPrimitive) baseType.toString else ""
      val baseTypeVType = baseType.toVType.desc
      val helperName = "helper$expandArray$" + env.clazz.lambdaMethods.size
      val helperDesc = s"([$baseTypeVType" + baseTypeVType * nExplodeArgs +
        argTypes.drop(nExplodeArgs).map(_.toString).mkString("") + fexprclasstype + ")" + lambdaRetDescStr
      val helper = (cv: ClassVisitor) => {
        val mv: MethodVisitor = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, helperName, helperDesc, null, Array[String]())
        mv.visitVarInsn(ALOAD, 0)  // [V
        mv.visitVarInsn(ALOAD, nArg + 1)  // ctx
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"expand${baseTypeStr}Array", MethodDesc(s"([${baseTypeVType}${fexprclasstype})$baseTypeVType"), false)
        mv.visitVarInsn(ASTORE, nArg + 2) // store the expanded array
        mv.visitVarInsn(ALOAD, nArg + 2)
        (1 to nArg) foreach {i => mv.visitVarInsn(ALOAD, i)}
        invoke(vCall, mv, env, (m) => m.visitVarInsn(ALOAD, nArg + 1), "explodeArg", desc, nExplodeArgs, isExploding = false, expandArgArray = expandArgArray, isArrayExpanded = true)(lambdaOp)
        if (!isReturnVoid && retDesc.get.isArray) {
          mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"compress${baseTypeStr}Array", MethodDesc(s"($baseTypeVType)[$baseTypeVType"), false)
          callVCreateOne(mv, (mm) => mm.visitVarInsn(ALOAD, nArg + 1))
        }
        mv.visitVarInsn(ALOAD, nArg + 2)
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"compress${baseTypeStr}Array", MethodDesc(s"($baseTypeVType)[$baseTypeVType"), false)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, "copyVArray", MethodDesc(s"([$baseTypeVType[$baseTypeVType)V"), false)
        if (isReturnVoid) mv.visitInsn(RETURN) else mv.visitInsn(ARETURN)
        mv.visitMaxs(10, 10)
        mv.visitEnd()
      }
      env.clazz.lambdaMethods += (helperName -> helper)
      mv.visitMethodInsn(INVOKESTATIC, Owner(env.clazz.name), MethodName(helperName), MethodDesc(helperDesc), false)
    }

    //////////////////////////////////////////////////
    // Generate lambda method body
    //////////////////////////////////////////////////
    val lambda =
      if (nExplodeArgs != 0) (cv: ClassVisitor) => {
        val mv: MethodVisitor = cv.visitMethod(
          ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC,
          lambdaMtdName,
          lambdaDesc,
          lambdaDesc,
          Array[String]() // Empty exception list
        )
        mv.visitCode()

        if (shouldExpandArray) {
          expandArray(mv)
        }
        else {

          mv.visitVarInsn(ALOAD, 0) // this is the next V to be exploded
          /* load arguments */
          MethodDesc(lambdaDesc).getArgs.tail.zipWithIndex.foreach(el => mv.visitVarInsn(el._1.getLoadInsn, el._2 + 1))
          //          for (i <- 1 until nArg) mv.visitVarInsn(ALOAD, i)
          //          mv.visitVarInsn(ALOAD, nArg + 1) // last argument of lambda method, the V that just got exploded
          invoke(vCall, mv, env, loadCtx, "explodeArg", shiftDesc(desc), nExplodeArgs - 1, isExploding = true, expandArgArray)(lambdaOp)
        }

        // retDesc: what the function being invoked returns
        // lambdaRetDescStr: what this lambda needs to return
        if (retDesc != lambdaRetDescStr) {
          if (lambdaRetDescStr == vclasstype) { // => retDesc == VPrim, so need to convert VPrim to V before returning
            mv.visitMethodInsn(INVOKEINTERFACE, retDesc.get.toVName, "toV", s"()$vclasstype", true)
          }
          else if (!retDescIsVoid) { // => retDesc == V, so need to convert V to VPrim before returning
            mv.visitMethodInsn(INVOKEINTERFACE, vclassname, lambdaRetDesc.get.toVPrimFunction, s"()$lambdaRetDescStr", true)
          }
        }

        if (isReturnVoid) mv.visitInsn(RETURN) else mv.visitInsn(ARETURN)
        mv.visitMaxs(10, 10)
        mv.visitEnd()
      }
      else (cv: ClassVisitor) => {
        val mv: MethodVisitor = cv.visitMethod(
          ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC,
          lambdaMtdName,
          lambdaDesc,
          lambdaDesc,
          Array[String]() // Empty exception list
        )
        mv.visitCode()
        if (shouldExpandArray) {
          expandArray(mv)
          if (isReturnVoid) mv.visitInsn(RETURN) else mv.visitInsn(ARETURN)
        }
        else
          lambdaOp(mv)
        mv.visitMaxs(10, 10)
        mv.visitEnd()
      }
    env.clazz.lambdaMethods += (lambdaMtdName -> lambda)

  }

  /**
    * Extract three different parts from desc
    * @param desc descriptor for invokedynamic
    * @return (invoke object descriptor, argument list descriptor, return type descriptor)
    */
  private def decomposeDesc(desc: String): (String, String, String) = {
    val split: Array[String] = desc.split('(')
    assert(split.size == 2, "Description format is wrong")
    val split2 = split(1).split(')')
    assert(split2.size == 2, "Description format is wrong")
    (split(0), split2(0), split2(1))
  }

  /**
    * Shift types in descriptor for generating nested invokedynamic
    *
    * Shifting rules:
    * Ref(Arg1, Arg2, Arg3)Ret ->
    * Arg1(Arg2, Arg3, Ref)Ret ->
    * Arg2(Arg3, Ref, Arg1)Ret ->
    * Arg3(Ref, Arg1, Arg2)Ret
    */
  private def shiftDesc(desc: String): String = {
    val (invokeObjDesc, argsDesc, returnDesc) = decomposeDesc(desc)
    val argsType: Array[Type] = Type.getArgumentTypes(s"($argsDesc)")

    val newInvokeObjDesc = argsType(0)
    val newArgsDesc: String = (for (i <- argsType.tail) yield i.toString).mkString("") + invokeObjDesc
    newInvokeObjDesc + "(" + newArgsDesc + ")" + returnDesc
  }

  /**
    * Load current ctx.
    *
    * The way of loading current ctx depends on whether we are inside lambda methods
    */
  private def loadFE(mv: MethodVisitor, defaultLoadFE: MethodVisitor => Unit, argsDesc: Option[String]): Unit = {
    if (argsDesc.isDefined) {
      val argTypes: Array[Type] = Type.getArgumentTypes(argsDesc.get)
      val feIdx = argTypes.indexOf(Type.getType(fexprclasstype))
      assert(feIdx >= 0)
      mv.visitVarInsn(ALOAD, feIdx)
    }
    else {
      defaultLoadFE(mv)
    }
  }
}

object VCall extends Enumeration {
  val smap = Value("smap")
  val sforeach = Value("sforeach")
  val sflatMap = Value("sflatMap")
}
