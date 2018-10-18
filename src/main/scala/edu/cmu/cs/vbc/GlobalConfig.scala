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
  /**
    * Interaction degree defined as minimum number of individual options that must be enable to satisfy a feature expression
    *
    * For example,
    *   degree(A & B) = 2
    *   degree(A & !B) = 1,
    *   degree (A | B)  = 1,
    *   degree((A & B) | (C & D)) = 2
    *   degree((A & B) | C) = 1
    */
  val maxInteractionDegree = 5
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