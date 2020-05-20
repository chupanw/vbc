package edu.cmu.cs.vbc

import java.lang.reflect.{InvocationTargetException, Method, Modifier}

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.varex.V
import edu.cmu.cs.vbc.VBCLauncher.processArgs
import edu.cmu.cs.vbc.config.VERuntime

trait RelaunchExceptionHandler4Test {

  def executeOnce(o: Option[Object],
                  x: Method,
                  args: Array[Object],
                  context: FeatureExpr): Unit = {
    if (context.isContradiction()) return
    System.out.println(s"[INFO] Executing ${x.getName} under $context")
    VERuntime.init(x, context, context, false, None)
    try {
      if (o.isDefined)
        x.invoke(o.get, context)
      else
        x.invoke(null, args :+ context: _*)
      System.gc()
      val succeedingContext = VERuntime.getExploredContext(context)
      val exploredSoFar = succeedingContext.or(context.not()).or(VERuntime.skippedExceptionContext)
      val nextContext = exploredSoFar.not()
      if (nextContext.isSatisfiable()) {
        TestTraceOutput.putRestartMark(nextContext)
        executeOnce(o, x, args, nextContext)
      }
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            TestTraceOutput.trace ::= (t.ctx, t.e.toString)
            println(t)
            val exploredSoFar = t.ctx.or(context.not()).or(VERuntime.skippedExceptionContext)
            val nextContext = exploredSoFar.not()
            if (nextContext.isSatisfiable()) {
              TestTraceOutput.putRestartMark(nextContext)
              executeOnce(o, x, args, nextContext)
            }
          case t =>
            throw new RuntimeException("Something is wrong, not a VException", t)
        }
      }
      case e =>
        throw new RuntimeException(
          s"Expecting InvocationTargetException, but found ${e.printStackTrace()}")
    }
  }
}

object VBCLauncher4Test extends RelaunchExceptionHandler4Test {

  def invokeLiftedMain(cls: Class[_], args: Array[String]): Unit = {
    try {
      val mtd: Method =
        cls.getMethod("main__Array_Ljava_lang_String__V", classOf[V[_]], classOf[FeatureExpr])
      val modifiers = mtd.getModifiers
      if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
        executeOnce(None, mtd, Array(processArgs(args)), FeatureExprFactory.True)
    } catch {
      case _: NoSuchMethodException =>
        println(s"No lifted main method found in ${cls.getName}, aborting...")
      case e => e.printStackTrace()
    }
  }

  def invokeUnliftedMain(cls: Class[_], args: Array[String]): Unit = {
    try {
      val m         = cls.getMethod("main", classOf[Array[String]])
      val modifiers = m.getModifiers
      if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && m.getReturnType.getName == "void")
        m.invoke(null, args)
    } catch {
      case _: NoSuchMethodException =>
        println(s"No unlifted main method found in ${cls.getName}, aborting")
      case invoke: InvocationTargetException =>
        TestTraceOutput.trace ::= (TestTraceOutput.t, invoke.getCause.toString)
      case e => e.printStackTrace()
    }
  }
}
