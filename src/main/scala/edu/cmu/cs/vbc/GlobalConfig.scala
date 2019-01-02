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
  val printTestResults = true
  val writeBDDs = false
  val blockCounting = true
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
  val maxInteractionDegree = 1000
  /**
    * Maximum number of VBlocks we can execute before throwing an exception
    *
    * This number is not used if [[blockCounting]] is false
    *
    * This limit is intentionally large because we hope identify infinite loops so that we can remove them.
    */
  val maxBlockCount = 1000000 // 1 million
}


/**
  * Store some global runtime values, should avoid if possible
  */
object VERuntime {
  var hasVException: Boolean = false
  var exceptionCtx: List[FeatureExpr] = Nil
  var curBlockCount: Int = 0
  def incrementBlockCount(): Unit = curBlockCount += 1
  def init(): Unit = {
    hasVException = false
    exceptionCtx = Nil
    curBlockCount = 0
  }
  def logVException(fe: FeatureExpr): Unit = {
    hasVException = true
    if (!fe.isTautology()) {
      val hasDuplicate = exceptionCtx.exists(x => x.equivalentTo(fe))
      if (!hasDuplicate) exceptionCtx = fe :: exceptionCtx
    }
  }

  def getHiddenContextsOtherThan(that: FeatureExpr): List[FeatureExpr] = exceptionCtx.filterNot(_ equivalentTo that)

  def getHiddenContexts: List[FeatureExpr] = exceptionCtx

  var classloader: Option[ClassLoader] = None
}