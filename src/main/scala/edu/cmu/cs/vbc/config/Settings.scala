package edu.cmu.cs.vbc.config

import java.text.NumberFormat

import com.typesafe.config.ConfigFactory
import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * Global configurations for VarexC
  */
object Settings {
  private val config = ConfigFactory.load("reference.conf")
  private val logger = LoggerFactory.getLogger("genprog")

  val fastMode: Boolean = config.getBoolean("fastMode")

  val logTrace: Boolean                 = config.getBoolean("logging.logTrace")
  val detectComplexLoop: Boolean        = config.getBoolean("misc.detectComplexLoop")
  val printContext: Boolean             = config.getBoolean("printing.printContext")
  val printExpandArrayWarnings: Boolean = config.getBoolean("printing.printExpandArrayWarnings")
  val printTestResults: Boolean         = config.getBoolean("printing.printTestResults")
  val writeBDDs: Boolean                = config.getBoolean("logging.writeBDDs")
  val blockCounting: Boolean            = config.getBoolean("misc.blockCounting")
  val earlyFail: Boolean                = config.getBoolean("earlyFail")

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
  private[cs] var maxInteractionDegree: Int = config.getInt("maxInteractionDegree")

  /**
    * Maximum number of VBlocks we can execute before throwing an exception
    *
    * This number is not used if [[blockCounting]] is false
    *
    * This limit is intentionally large because we hope identify infinite loops so that we can remove them.
    */
  val maxBlockCount: Long = config.getLong("maxBlockCount")

  def printSettings(): Unit = {
    val message = s"""**********************************************************************
                     |*                              Settings                              *
                     |**********************************************************************
                     |fastMode: $fastMode
                     |maxInteractionDegree: $maxInteractionDegree
                     |maxBlockCount: $maxBlockCount
                     |earlyFail: $earlyFail
                     |blockCounting: $blockCounting
                     |detectComplexLoop: $detectComplexLoop
                     |printContext: $printContext
                     |printExpandArrayWarnings: $printExpandArrayWarnings
                     |printTestResults: $printTestResults
                     |logTrace: $logTrace
                     |writeBDDs: $writeBDDs
                     |""".stripMargin
    println(message)
    logger.info(message)
  }
}

/**
  * Store some global runtime, should avoid if possible
  *
  * This object is a centralized place for all kinds of runtime states that can affect variational execution,
  * mostly for exception handling, but also sometimes for performance or logging.
  *
  * If weird bugs occur, check this object first...
  */
object VERuntime {

  /**
    * A set of contexts that show under what configurations the current variational execution is still valid.
    * Parts of an execution can become invalid if, for example, some exceptions occur and we decide to ignore them.
    * To explore the contexts that have exceptions, we restart variational execution.
    */
  var postponedExceptionContext: FeatureExpr = FeatureExprFactory.False
  var thrownExceptionContext: FeatureExpr    = FeatureExprFactory.False
  var globalContext: FeatureExpr             = FeatureExprFactory.False
  var curBlockCount: Long                    = 0

  /**
    * Used when execution crosses the boundary of VE environment and the Digester library. See [[edu.cmu.cs.varex.VOps.populate()]]
    */
  var boundaryCtx: FeatureExpr = FeatureExprFactory.True

  private val numFormatter      = NumberFormat.getNumberInstance
  private val formattedMaxBlock = numFormatter.format(Settings.maxBlockCount)
  def incrementBlockCount(): Unit = {
    curBlockCount += 1
    if (curBlockCount % (Settings.maxBlockCount / 10) == 0)
      println(
        s"#Blocks executed: ${numFormatter.format(curBlockCount)} out of max $formattedMaxBlock")
  }
  def resetBlockCount(): Unit = {
    curBlockCount = 0
  }

  /**
    * Initialize all mutable states.
    *
    * All vars or mutable objects within this [[VERuntime]] object should get initialized here.
    *
    * MUST BE CALLED BEFORE EACH RESTART OF VARIATIONAL EXECUTION.
    */
  def init(initContext: FeatureExpr, boundaryCtx: FeatureExpr): Unit = {

    /** Exception handling  */
    this.globalContext = initContext
    this.postponedExceptionContext = FeatureExprFactory.False
    this.thrownExceptionContext = FeatureExprFactory.False

    /** Logging */
    this.curBlockCount = 0

    /** Model class issues */
    this.boundaryCtx = boundaryCtx
  }

  def postponeExceptionCtx(ctx: FeatureExpr): Unit = {
    postponedExceptionContext = postponedExceptionContext or ctx
  }

  def throwExceptionCtx(ctx: FeatureExpr): Unit = {
    thrownExceptionContext = ctx
  }

  def shouldPostpone(currentCtx: FeatureExpr): Boolean = {
    (globalContext and (postponedExceptionContext or currentCtx).not()).isSatisfiable()
  }

  def getExploredContext(startingCtx: FeatureExpr): FeatureExpr = {
    val hasPostponedExp = postponedExceptionContext.isSatisfiable()
    val hasThrownExp    = thrownExceptionContext.isSatisfiable()
    (hasPostponedExp, hasThrownExp) match {
      case (false, false) => startingCtx
      case (true, false)  => startingCtx.and(postponedExceptionContext.not())
      case (_, true)      => startingCtx.and(thrownExceptionContext)
    }
  }

  var classloader: Option[ClassLoader] = None

  def loadFeatures(featureFile: String): Unit = {
    val resource = getClass.getResourceAsStream("/" + featureFile)
    for (line <- io.Source.fromInputStream(resource).getLines()) {
      val features = line.split(' ')
      features.foreach(x => FeatureExprFactory.createDefinedExternal(x.trim))
    }
    println("features loaded")
  }
}
