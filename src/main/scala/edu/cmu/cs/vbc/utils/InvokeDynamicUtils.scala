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

    def descIsInt(d: String): Boolean = (d == "I" || d == vintclasstype)
    val (invokeObjectDesc, argsDesc, retDesc) = decomposeDesc(desc)
    val lambdaRetDesc =
      if (retDesc == "V") "V"
      else if (descIsInt(retDesc) && descIsInt(invokeObjectDesc)) vintclasstype
      else vclasstype

    val argTypes: Array[Type] = Type.getArgumentTypes(s"($argsDesc)")
    val nArg = argTypes.size

    val argTypeStr = ((for (t <- argTypes.take(nExplodeArgs))
                       yield (if (t.getSort == Type.INT) vintclasstype else vclasstype)) ++
                     (for (t <- argTypes.drop(nExplodeArgs))
                       yield t.toString)).mkString("")

    //////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////
    val firstArgType = if (nArg == 0) None else Some(argTypes(0))
    val (invokeDynamicName, vCallDesc, funType, isReturnVoid, convertBefore, convertAfter) =
      (vCall, firstArgType, lambdaRetDesc) match {
        // Vint.smap(int -> int) : No conversion
      case (VCall.smap, _, ret) if descIsInt(ret) && descIsInt(invokeObjectDesc) =>
        ("apply", s"($biFuncType$fexprclasstype)$vintclasstype", biFuncType, false, false, false)
      // Vint.smap(int -> U) : Convert Vint -> V before mapping
      case (VCall.smap, _, ret) if descIsInt(invokeObjectDesc) =>
        ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, true, false)
      // V.smap(U -> int) : Convert V -> Vint after mapping
      case (VCall.smap, _, ret) if descIsInt(ret) =>
        ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, false, true)

      // Vint.smap(int -> int) : No conversion
      case (VCall.sflatMap, _, ret) if descIsInt(ret) && descIsInt(invokeObjectDesc) =>
        ("apply", s"($biFuncType$fexprclasstype)$vintclasstype", biFuncType, false, false, false)
      // Vint.smap(int -> U) : Convert Vint -> V before mapping
      case (VCall.sflatMap, _, ret) if descIsInt(invokeObjectDesc) =>
        ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, true, false)
      // V.smap(U -> int) : Convert V -> Vint after mapping
      case (VCall.sflatMap, _, ret) if descIsInt(ret) =>
        ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, false, true)

        // V.s{map, flatMap}(U -> T) : No conversion
      case (VCall.smap, _, _) | (VCall.sflatMap, _, _) =>
        ("apply", s"($biFuncType$fexprclasstype)$vclasstype", biFuncType, false, false, false)

        // Vint.sforeach : No conversion (?)
      case (VCall.sforeach, Some(t), _) if t.getSort == Type.OBJECT && descIsInt(t.toString) =>
        ("accept", s"($objIntConsumerType$fexprclasstype)V", objIntConsumerType, true, false, false)
        // V.sforeach : No conversion
      case (VCall.sforeach, _, _) =>
        ("accept", s"($biConsumerType$fexprclasstype)V", biConsumerType, true, false, false)

      case _ => throw new RuntimeException("Unsupported dynamic invoke type: " + vCall)
    }

    //////////////////////////////////////////////////
    // Call INVOKEDYNAMIC
    //////////////////////////////////////////////////


    val invokeDynamicType = "(" + argTypeStr + s")$funType"

    val newInvokeObjDesc: String =
      if (expandArgArray && invokeObjectDesc.startsWith("[") && !isArrayExpanded)
        (if (invokeObjectDesc.startsWith("[I")) s"[$vintclasstype" else s"[$vclasstype")
      else if (convertBefore) vclasstype
      else invokeObjectDesc
    val isIntArray = (newInvokeObjDesc == s"[$vintclasstype")
    val lambdaDesc = "(" + argTypeStr + s"$fexprclasstype" + newInvokeObjDesc + s")$lambdaRetDesc"

    val n = env.clazz.lambdaMethods.size
    val lambdaMtdName: String = "lambda$" + lambdaName + "$" + n
    // invoke the lambda for this nesting-level
    mv.visitInvokeDynamicInsn(
      invokeDynamicName,
      invokeDynamicType,
      new Handle(H_INVOKESTATIC, lamdaFactoryOwner, lamdaFactoryMethod, lamdaFactoryDesc),
      // Arguments:
      Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)" + (if (isReturnVoid) "V" else "Ljava/lang/Object;")),
      new Handle(H_INVOKESTATIC, env.clazz.name, lambdaMtdName, lambdaDesc),
      Type.getType(s"($fexprclasstype$newInvokeObjDesc)$lambdaRetDesc")
    )
    if (isExploding) {
      loadFE(mv, loadCtx, Some(lambdaDesc))
    }
    else {
      loadFE(mv, loadCtx, None)
    }

    if (convertBefore) { // Mapping a Vint to a different type, convert to V first
      mv.visitMethodInsn(INVOKEINTERFACE, vintclassname, "toV", s"()$vclasstype", true)
      mv.visitMethodInsn(INVOKEINTERFACE, vclassname, vCall.toString, vCallDesc, true)
    }
    else if (convertAfter) { // Mapping a V to an int, convert to Vint afterwards
      mv.visitMethodInsn(INVOKEINTERFACE, vclassname, vCall.toString, vCallDesc, true)
      mv.visitMethodInsn(INVOKEINTERFACE, vclassname, "toVint", s"()$vintclasstype", true)
    }
    else { // No special conversion necessary, base invocation on the object description
      if (descIsInt(invokeObjectDesc)) {
        mv.visitMethodInsn(INVOKEINTERFACE, vintclassname, vCall.toString, vCallDesc, true)
      } else {
        mv.visitMethodInsn(INVOKEINTERFACE, vclassname, vCall.toString, vCallDesc, true)
      }
    }

    //////////////////////////////////////////////////
    // helper methods for expanding arrays
    //////////////////////////////////////////////////
    val currentInvokeObjType = TypeDesc(invokeObjectDesc)
    def shouldExpandArray: Boolean = expandArgArray && currentInvokeObjType.isArray && !isArrayExpanded
    def expandArray(mv: MethodVisitor) = {
      mv.visitVarInsn(ALOAD, nArg + 1)  // [V
      0 until nArg foreach {i => mv.visitVarInsn(ALOAD, i)}
      mv.visitVarInsn(ALOAD, nArg)
      val baseType = currentInvokeObjType.getArrayBaseType
      val baseTypeStr = if (baseType.isPrimitive) baseType.toString else ""
      val helperName = "helper$expandArray$" + env.clazz.lambdaMethods.size
      val helperDesc = s"([$vclasstype" + vclasstype * nExplodeArgs + argTypes.drop(nExplodeArgs).map(_.toString).mkString("") + fexprclasstype + ")" + lambdaRetDesc
      val helper = (cv: ClassVisitor) => {
        val mv: MethodVisitor = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, helperName, helperDesc, null, Array[String]())
        mv.visitVarInsn(ALOAD, 0)  // [V
        mv.visitVarInsn(ALOAD, nArg + 1)  // ctx
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"expand${baseTypeStr}Array", MethodDesc(s"([${vclasstype}${fexprclasstype})$vclasstype"), false)
        mv.visitVarInsn(ASTORE, nArg + 2) // store the expanded array
        mv.visitVarInsn(ALOAD, nArg + 2)
        (1 to nArg) foreach {i => mv.visitVarInsn(ALOAD, i)}
        invoke(vCall, mv, env, (m) => m.visitVarInsn(ALOAD, nArg + 1), "explodeArg", desc, nExplodeArgs, isExploding = false, expandArgArray = expandArgArray, isArrayExpanded = true)(lambdaOp)
        if (!isReturnVoid && TypeDesc(retDesc).isArray) {
          mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"compress${baseTypeStr}Array", MethodDesc(s"($vclasstype)[$vclasstype"), false)
          callVCreateOne(mv, (mm) => mm.visitVarInsn(ALOAD, nArg + 1))
        }
        mv.visitVarInsn(ALOAD, nArg + 2)
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"compress${baseTypeStr}Array", MethodDesc(s"($vclasstype)[$vclasstype"), false)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, "copyVArray", MethodDesc(s"([$vclasstype[$vclasstype)V"), false)
        if (isReturnVoid) mv.visitInsn(RETURN) else mv.visitInsn(ARETURN)
        mv.visitMaxs(10, 10)
        mv.visitEnd()
      }
      env.clazz.lambdaMethods += (helperName -> helper)
      mv.visitMethodInsn(INVOKESTATIC, Owner(env.clazz.name), MethodName(helperName), MethodDesc(helperDesc), false)
    }
    def expandIntArray(mv: MethodVisitor) = {
      mv.visitVarInsn(ALOAD, nArg + 1)  // [Vint
      0 until nArg foreach {i => mv.visitVarInsn(ALOAD, i)}
      mv.visitVarInsn(ALOAD, nArg)
      val baseType = currentInvokeObjType.getArrayBaseType
      val baseTypeStr = if (baseType.isPrimitive) baseType.toString else ""
      val helperName = "helper$expandArray$" + env.clazz.lambdaMethods.size
      val helperDesc = s"([$vintclasstype" + vintclasstype * nExplodeArgs + argTypes.drop(nExplodeArgs).map(_.toString).mkString("") + fexprclasstype + ")" + lambdaRetDesc
      val helper = (cv: ClassVisitor) => {
        val mv: MethodVisitor = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, helperName, helperDesc, null, Array[String]())
        mv.visitVarInsn(ALOAD, 0)  // [V
        mv.visitVarInsn(ALOAD, nArg + 1)  // ctx
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"expand${baseTypeStr}Array", MethodDesc(s"([${vintclasstype}${fexprclasstype})$vintclasstype"), false)
        mv.visitVarInsn(ASTORE, nArg + 2) // store the expanded array
        mv.visitVarInsn(ALOAD, nArg + 2)
        (1 to nArg) foreach {i => mv.visitVarInsn(ALOAD, i)}
        invoke(vCall, mv, env, (m) => m.visitVarInsn(ALOAD, nArg + 1), "explodeArg", desc, nExplodeArgs, isExploding = false, expandArgArray = expandArgArray, isArrayExpanded = true)(lambdaOp)
        if (!isReturnVoid && TypeDesc(retDesc).isArray) {
          mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"compress${baseTypeStr}Array", MethodDesc(s"($vintclasstype)[$vintclasstype"), false)
          callVCreateOne(mv, (mm) => mm.visitVarInsn(ALOAD, nArg + 1))
        }
        mv.visitVarInsn(ALOAD, nArg + 2)
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, s"compress${baseTypeStr}Array", MethodDesc(s"($vintclasstype)[$vintclasstype"), false)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, "copyVArray", MethodDesc(s"([$vintclasstype[$vintclasstype)V"), false)
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
          if (isIntArray) {
            expandIntArray(mv)
          } else {
            expandArray(mv)
          }
        }
        else {
          mv.visitVarInsn(ALOAD, 0) // this is the next V to be exploded
          /* load arguments */
          for (i <- 1 until nArg) mv.visitVarInsn(ALOAD, i)
          mv.visitVarInsn(ALOAD, nArg + 1) // last argument of lambda method, the V that just got exploded
          invoke(vCall, mv, env, loadCtx, "explodeArg", shiftDesc(desc), nExplodeArgs - 1, isExploding = true, expandArgArray)(lambdaOp)
        }

        // retDesc: what the function being invoked returns
        // lambdaRetDesc: what this lambda needs to return
        if (retDesc != lambdaRetDesc) {
          if (retDesc == vintclasstype) { // => lambdaRetDesc == v
            mv.visitMethodInsn(INVOKEINTERFACE, vintclassname, "toV", s"()$vclasstype", true)
          }
          else { // => lambdaRetDesc == vint
            mv.visitMethodInsn(INVOKEINTERFACE, vclassname, "toVint", s"()$vintclasstype", true)
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
          if (isIntArray){
            expandIntArray(mv)
          } else {
            expandArray(mv)
          }
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

  /**
    * Invoke INVOCATION, which is assumed to call invokeDynamic using OWNER and DESC
    * First check OWNER and DESC to determine if the function being invoked requires V <-> Vint
    * conversion. If so, convert before calling.
    * Then check if conversion is necessary after calling; if so, do the conversion.
    * @param mv
    * @param desc
    * @param invocation
    */
  def callWithVConversion(mv: MethodVisitor, desc: MethodDesc, invocation: () => Unit): Unit = {
    val (_, argTypes, retType) = decomposeDesc(desc)
    invocation()
    (argTypes, retType) match {
      case (t, "I") if t != "I" => {
        // Must convert V to Vint after
        mv.visitMethodInsn(INVOKEINTERFACE, vclassname, "toVint", s"$vclasstype()$vintclasstype", true)
      }
      case _ => {}
    }
  }
}

object VCall extends Enumeration {
  val smap = Value("smap")
  val sforeach = Value("sforeach")
  val sflatMap = Value("sflatMap")
}
