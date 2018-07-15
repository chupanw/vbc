package edu.cmu.cs.vbc

import java.lang.reflect.{InvocationTargetException, Method}

import de.fosd.typechef.featureexpr.FeatureExpr

trait RelaunchExceptionHandler {

  def executeOnce(o: Option[Object], x: Method, args: Array[Object], context: FeatureExpr): Unit = {
    try {
      if (o.isDefined)
        x.invoke(o.get, context)
      else
        x.invoke(null, args :+ context:_*)
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            if (!t.ctx.equivalentTo(context)) {
              System.err.println(t)
              val altCtx = context.and(t.ctx.not())
              System.err.println(s"[INFO] Re-executing under $altCtx")
              executeOnce(o, x, args, altCtx)
            }
          case t =>
            throw new RuntimeException("Not a VException", t)
        }
      }
      case e =>
        throw new RuntimeException(s"Expecting InvocationTargetException, but found $e")
    }
  }
}

case class VException(e: Throwable, ctx: FeatureExpr) extends Throwable {
  override def toString: String = s"[VException $ctx]: " + e.toString + "\n" + getTracesAsString
  def getTracesAsString: String = e.getStackTrace.toList mkString("[VException]\t", "\n[VException]\t", "\n")
}