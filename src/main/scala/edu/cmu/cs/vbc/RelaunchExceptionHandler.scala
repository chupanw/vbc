package edu.cmu.cs.vbc

import java.lang.reflect.{InvocationTargetException, Method}

import de.fosd.typechef.featureexpr.FeatureExpr
import edu.cmu.cs.vbc.config.{Settings, VERuntime}

trait RelaunchExceptionHandler {

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
      if (nextContext.isSatisfiable()) executeOnce(o, x, args, nextContext)
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            println(t)
            val exploredSoFar = t.ctx.or(context.not()).or(VERuntime.skippedExceptionContext)
            val nextContext = exploredSoFar.not()
            if (nextContext.isSatisfiable()) executeOnce(o, x, args, nextContext)
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

case class VException(e: Throwable, ctx: FeatureExpr) extends RuntimeException {
  override def toString: String =
    s"[VException ${if (Settings.printContext) ctx else "hidden context..."}]: " + e.toString + "\n" + getTracesAsString

  def getTracesAsString: String =
    e.getStackTrace.toList.take(20) mkString("[VException]\t", "\n[VException]\t", "\n")
}

class PotentialInfiniteLoopError(errMsg: String) extends Error(errMsg)
class PotentialStackOverflowError(errMsg: String) extends Error(errMsg)
