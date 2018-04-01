package edu.cmu.cs.vbc

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory, SingleFeatureExpr}
import edu.cmu.cs.varex.{V, VHelper}
import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis.{VBCFrame, V_TYPE}
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions.{InstrINIT_CONDITIONAL_FIELDS, Instruction}
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

import scala.collection.JavaConversions._

/**
  * Package of specific byte code instructions that trace the
  * execution both in the variational and nonvariational way
  * in order to subsequently compare both executions
  *
  * uses lots of global state that should probably be extended
  * at some point to support multithreading. should be okay for
  * now as simple testing infrastructure
  */
object TestTraceOutput {

  trait TraceElement


  var trace: List[(FeatureExpr, String)] = Nil

  val t = FeatureExprFactory.True

  def trace_s(s: String): Unit = {
    trace ::=(t, s)
  }

  def vtrace_s(ctx: FeatureExpr, s: String): Unit = {
    assert(ctx.isInstanceOf[FeatureExpr], s"ctx ($ctx) not FeatureExpr but " + ctx.getClass)
    trace ::=(ctx, s)
  }

  def trace_int(v: Int, s: String): Unit = {
    trace ::=(t, s + ";" + v)
  }

  def trace_printlnI(v: Int): Unit = {
    trace ::=(t, v.toString)
  }

  def vtrace_int(v: V[Any], ctx: FeatureExpr, s: String): Unit = {
    assert(ctx.isInstanceOf[FeatureExpr], "ctx not FeatureExpr but " + ctx.getClass)
    assert(v.isInstanceOf[V[_]], "v not V[Int] but " + v.getClass)
    for ((ctx, i) <- VHelper.explode(ctx, v)) {
      if (i != null)
        trace ::=(ctx, s + ";" + i.toString)
    }
  }

  def trace_string(s: java.lang.Object): Unit = {
    trace ::=(t, if (s == null) "null" else s.toString)
  }

  def vtrace_string(v: V[java.lang.Object], ctx: FeatureExpr): Unit = {
    assert(ctx.isInstanceOf[FeatureExpr], "ctx not FeatureExpr but " + ctx.getClass)
    //v might be of type String for now, due to incomplete lifting
    //        if (v.isInstanceOf[String])
    //            trace ::=(ctx, v.asInstanceOf[String])
    //        else
    for ((ictx, s) <- VHelper.explode(ctx, v))
      trace ::=(ctx.and(ictx), if (s == null) "null" else s.toString)
  }

}

object TraceConfig {

  var options: Set[SingleFeatureExpr] = Set()
  var config: Map[String, Boolean] = Map()

  def getOption(n: String) = {
    val o = FeatureExprFactory.createDefinedExternal(n)
    options += o
    o
  }


  def getVConfig(n: String) = V.choice(getOption(n), new Integer(1), new Integer(0))

  def getConfig(n: String) = {
    var c = config.get(n)
    assert(c.isDefined, s"option $n not configured")
    c.get
  }
}


case class TraceInstr_ConfigInit() extends Instruction {
  import InstrINIT_CONDITIONAL_FIELDS._

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    for (conditionalField <- env.clazz.fields
         if conditionalField.hasConditionalAnnotation) {
      mv.visitVarInsn(ALOAD, 0)
      mv.visitLdcInsn(conditionalField.name)
      mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TraceConfig", "getConfig", "(Ljava/lang/String;)Z", false)
      if (conditionalField.isStatic)
        mv.visitFieldInsn(PUTSTATIC, env.clazz.name, conditionalField.name, "Z")
      else
        mv.visitFieldInsn(PUTFIELD, env.clazz.name, conditionalField.name, "Z")
    }
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {

    if (env.method.name == "___clinit___") {
      env.clazz.fields.filter(f => f.isStatic && f.hasConditionalAnnotation()).foreach(f => {
        mv.visitLdcInsn(f.name)
        mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TraceConfig", "getVConfig", "(Ljava/lang/String;)Ledu/cmu/cs/varex/V;", false)
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
        mv.visitLdcInsn(f.name)
        mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TraceConfig", "getVConfig", "(Ljava/lang/String;)Ledu/cmu/cs/varex/V;", false)
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


/**
  * trace a single string output
  */
case class TraceInstr_S(s: String) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitLdcInsn(s)
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "trace_s", "(Ljava/lang/String;)V", false)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    loadFExpr(mv, env, env.getVBlockVar(block))
    mv.visitLdcInsn(s)
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "vtrace_s", "(Lde/fosd/typechef/featureexpr/FeatureExpr;Ljava/lang/String;)V", false)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set())
}

/**
  * trace a print instruction, getting the string value on top of the stack
  */
case class TraceInstr_Print() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "trace_string", "(Ljava/lang/Object;)V", false)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
    loadFExpr(mv, env, env.getVBlockVar(block))
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "vtrace_string", "(Ledu/cmu/cs/varex/V;Lde/fosd/typechef/featureexpr/FeatureExpr;)V", false)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val backtrack =
      if (s.stack.head._1 != V_TYPE(false)) s.stack.head._2
      else Set[Instruction]()
    (s, backtrack)
  }
}

/**
  * trace a print instruction, getting the string value on top of the stack
  */
case class TraceInstr_PrintI() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "trace_printlnI", "(I)V", false)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
    loadFExpr(mv, env, env.getVBlockVar(block))
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "vtrace_string", "(Ledu/cmu/cs/varex/V;Lde/fosd/typechef/featureexpr/FeatureExpr;)V", false)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val backtrack =
      if (s.stack.head._1 != V_TYPE(false)) s.stack.head._2
      else Set[Instruction]()
    (s, backtrack)
  }
}

/**
  * trace a getfield instruction (after the instruction) with the value of the field
  * that is now on top of the stack
  */
case class TraceInstr_GetField(s: String, desc: String) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    assert(desc == "I" || desc == "Z", "only integer fields supported for now")
    mv.visitInsn(DUP)
    mv.visitLdcInsn(s)
    mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "trace_int", "(ILjava/lang/String;)V", false)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
    if (env.shouldLiftInstr(this)) {
      loadFExpr(mv, env, env.getVBlockVar(block))
      mv.visitLdcInsn(s)
      mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "vtrace_int", "(Ledu/cmu/cs/varex/V;Lde/fosd/typechef/featureexpr/FeatureExpr;Ljava/lang/String;)V", false)
    }
    else {
      assert(desc == "I" || desc == "Z", "only integer fields supported for now")
      mv.visitLdcInsn(s)
      mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/TestTraceOutput", "trace_int", "(ILjava/lang/String;)V", false)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    if (s.stack.head._1 == V_TYPE(false)) env.setLift(this)
    (s, Set())
  }
}
