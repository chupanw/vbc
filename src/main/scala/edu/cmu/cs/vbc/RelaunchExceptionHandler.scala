package edu.cmu.cs.vbc

import java.lang.reflect.{InvocationTargetException, Method}

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}

trait RelaunchExceptionHandler {

  /**
    * OUTDATED, USE WITH CAUTION
    */
  def executeOnce(o: Option[Object], x: Method, args: Array[Object], context: FeatureExpr): Unit = {
    System.out.println(s"[INFO] Executing ${x.getName} under $context")
    VERuntime.init(context)
    try {
      if (o.isDefined)
        x.invoke(o.get, context)
      else
        x.invoke(null, args :+ context:_*)
      if (VERuntime.hasVException) {
        val expCtx = VERuntime.getHiddenContextsOtherThan(context)
        expCtx.foreach(fe => executeOnce(o, x, args, context.and(fe.not())))
      }
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            System.out.println(t)
            if (!t.ctx.equivalentTo(context)) {
              val altCtx = context.and(t.ctx.not())
              executeOnce(o, x, args, altCtx)
            }
          case t =>
            throw new RuntimeException("Not a VException", t)
        }
      }
      case e =>
        throw new RuntimeException(s"Expecting InvocationTargetException, but found ${e.printStackTrace()}")
    }
  }
}

case class VException(e: Throwable, ctx: FeatureExpr) extends RuntimeException {
  VERuntime.logVException(ctx)

  override def toString: String = s"[VException ${if (GlobalConfig.printContext) ctx else "hidden context..."}]: " + e.toString + "\n" + getTracesAsString
  def getTracesAsString: String = e.getStackTrace.toList mkString("[VException]\t", "\n[VException]\t", "\n")
}