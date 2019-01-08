package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode.{MethodDesc, MethodName, Owner}
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes._
import LiftUtils._

/**
  * A collection of methods for validator in order to make beanutils work
  */
object ValidatorBeanBridge {

  def bridge(cName: String, cv: ClassVisitor): Unit = {
    if (cName == "org/apache/commons/validator/ValidatorAction") {
      unliftedEmptyInit(cName, cv)
    }
    if (cName == "org/apache/commons/validator/ValidatorResources")
      unliftedAddValidatorAction(cName, cv)
    if (cName == "org/apache/commons/validator/FormSetFactory") {
      unliftedEmptyInit(cName, cv)
      unliftedCreateObject(cName, cv)
    }
    if (cName == "org/apache/commons/validator/FormSet") {
      unliftedAddForm(cName, cv)
      unliftedAddConstant(cName, cv)
    }
    if (cName == "org/apache/commons/validator/Form") {
      unliftedEmptyInit(cName, cv)
      unliftedAddField(cName, cv)
    }
    if (cName == "org/apache/commons/validator/Field") {
      unliftedEmptyInit(cName, cv)
      unliftedAddArg(cName, cv)
      unliftedAddVar(cName, cv)
    }
    if (cName == "org/apache/commons/validator/Arg") {
      unliftedEmptyInit(cName, cv)
    }
    if (cName == "org/apache/commons/validator/Var") {
      unliftedEmptyInit(cName, cv)
      unliftedSetters("Name", cName, cv)
      unliftedSetters("Value", cName, cv)
      unliftedSetters("JsType", cName, cv)
    }
  }

  /////////////////////////////
  ////////// Generic //////////
  /////////////////////////////

  /**
    * called in newInstance() in digester library
    */
  def unliftedEmptyInit(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", "()V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKESPECIAL, cName, MethodName("<init>"), MethodDesc("()V").appendFE, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  /////////////////////////////////////
  ////////// ValidatorAction //////////
  /////////////////////////////////////

  def unliftedSetters(property: String, cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, s"set$property", "(Ljava/lang/String;)V", "()V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName(s"set$property").rename(MethodDesc("(Ljava/lang/String;)V")), MethodDesc(s"($vclasstype)V").toVReturnType.appendFE, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  ////////////////////////////////////////
  ////////// ValidatorResources //////////
  ////////////////////////////////////////
  /**
    * Unknown call location
    */
  def unliftedAddValidatorAction(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "addValidatorAction", "(Lorg/apache/commons/validator/ValidatorAction;)V", "(Lorg/apache/commons/validator/ValidatorAction;)V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName("addValidatorAction").rename(MethodDesc("(Lorg/apache/commons/validator/ValidatorAction;)V")), MethodDesc(s"($vclasstype)V").appendFE.toVReturnType, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  /////////////////////////////////////////
  //////////// FormSetFactory /////////////
  /////////////////////////////////////////

  def unliftedCreateObject(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "createObject", "(Lorg/xml/sax/Attributes;)Ljava/lang/Object;", "(Lorg/xml/sax/Attributes;)Ljava/lang/Object;", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName("createObject").rename(MethodDesc("(Lorg/xml/sax/Attributes;)Ljava/lang/Object;")), MethodDesc(s"($vclasstype)Ljava/lang/Object;").appendFE.toVReturnType, false)
    mv.visitMethodInsn(INVOKEINTERFACE, s"$vclassname", "getOne", "()Ljava/lang/Object;", true)
    mv.visitInsn(ARETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  ///////////////////////////////
  //////////// Form /////////////
  ///////////////////////////////

  def unliftedAddField(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "addField", "(Lorg/apache/commons/validator/Field;)V", "(Lorg/apache/commons/validator/Field;)V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName("addField").rename(MethodDesc("(Lorg/apache/commons/validator/Field;)V")), MethodDesc(s"($vclasstype)V").appendFE.toVReturnType, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  //////////////////////////////////
  //////////// FormSet /////////////
  //////////////////////////////////

  def unliftedAddForm(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "addForm", "(Lorg/apache/commons/validator/Form;)V", "(Lorg/apache/commons/validator/Form;)V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName("addForm").rename(MethodDesc("(Lorg/apache/commons/validator/Form;)V")), MethodDesc(s"($vclasstype)V").appendFE.toVReturnType, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  def unliftedAddConstant(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "addConstant", "(Ljava/lang/String;Ljava/lang/String;)V", "(Ljava/lang/String;Ljava/lang/String;)V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitVarInsn(ALOAD, 2)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName("addConstant").rename(MethodDesc("(Ljava/lang/String;Ljava/lang/String;)V")), MethodDesc(s"($vclasstype$vclasstype)V").appendFE.toVReturnType, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  ////////////////////////////////
  //////////// Field /////////////
  ////////////////////////////////

  def unliftedAddArg(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "addArg", "(Lorg/apache/commons/validator/Arg;)V", "(Lorg/apache/commons/validator/Arg;)V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName("addArg").rename(MethodDesc("(Lorg/apache/commons/validator/Arg;)V")), MethodDesc(s"($vclasstype)V").appendFE.toVReturnType, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  def unliftedAddVar(cName: String, cv: ClassVisitor): Unit = {
    val mv = cv.visitMethod(ACC_PUBLIC, "addVar", "(Lorg/apache/commons/validator/Var;)V", "(Lorg/apache/commons/validator/Var;)V", Array.empty)
    mv.visitCode()
    mv.visitVarInsn(ALOAD, 0)
    mv.visitVarInsn(ALOAD, 1)
    callVCreateOne(mv, m => m.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false))
    mv.visitMethodInsn(INVOKESTATIC, Owner("edu/cmu/cs/vbc/VERuntime"), MethodName("boundaryCtx"), MethodDesc(s"()$fexprclasstype"), false)
    mv.visitMethodInsn(INVOKEVIRTUAL, cName, MethodName("addVar").rename(MethodDesc("(Lorg/apache/commons/validator/Var;)V")), MethodDesc(s"($vclasstype)V").appendFE.toVReturnType, false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }
}
