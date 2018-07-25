package edu.cmu.cs.vbc

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}

/**
  * Global configurations for VarexC
  */
object GlobalConfig {
  val logTrace = false
  val detectComplexLoop = false
  val printContext = true
}


/**
  * Store some global runtime values, should avoid if possible
  */
object VERuntime {
  var hasVException: Boolean = false
  var exceptionCtx: FeatureExpr = FeatureExprFactory.True
  def init(): Unit = {
    hasVException = false
    exceptionCtx = FeatureExprFactory.True
  }
  def logVException(fe: FeatureExpr): Unit = {
    hasVException = true
    exceptionCtx = exceptionCtx.and(fe)
  }
}