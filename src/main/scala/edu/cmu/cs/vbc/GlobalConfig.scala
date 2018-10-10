package edu.cmu.cs.vbc

import de.fosd.typechef.featureexpr.FeatureExpr

import scala.collection.mutable

/**
  * Global configurations for VarexC
  */
object GlobalConfig {
  val logTrace = false
  val detectComplexLoop = false
  val printContext = false
  val printExpandArrayWarnings = false
}


/**
  * Store some global runtime values, should avoid if possible
  */
object VERuntime {
  var hasVException: Boolean = false
  var exceptionCtx: mutable.Set[FeatureExpr] = mutable.Set()
  def init(): Unit = {
    hasVException = false
    exceptionCtx.clear
  }
  def logVException(fe: FeatureExpr): Unit = {
    hasVException = true
    if (!fe.isTautology())
      exceptionCtx.add(fe)
  }

  var classloader: Option[ClassLoader] = None
}