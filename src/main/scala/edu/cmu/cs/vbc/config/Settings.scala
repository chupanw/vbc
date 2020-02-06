package edu.cmu.cs.vbc.config

import java.text.NumberFormat

import com.typesafe.config.ConfigFactory
import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import org.slf4j.LoggerFactory

/**
  * Global configurations for VarexC
  */
object Settings {
  private val config = ConfigFactory.load("reference.conf")
  private val logger = LoggerFactory.getLogger("genprog")

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
  * Store some global runtime values, should avoid if possible
  */
object VERuntime {
  var hasVException: Boolean          = false
  var exceptionCtx: List[FeatureExpr] = Nil
  var curBlockCount: Long             = 0
  var boundaryCtx: FeatureExpr        = FeatureExprFactory.True
  private val numFormatter = NumberFormat.getNumberInstance
  private val formattedMaxBlock = numFormatter.format(Settings.maxBlockCount)
  def incrementBlockCount(): Unit     = {
    curBlockCount += 1
    if (curBlockCount % 10000 == 0) println(s"#Blocks executed: ${numFormatter.format(curBlockCount)} out of $formattedMaxBlock")
  }
  def init(ctx: FeatureExpr): Unit = {
    hasVException = false
    exceptionCtx = Nil
    curBlockCount = 0
    boundaryCtx = ctx
  }
  def logVException(fe: FeatureExpr): Unit = {
    hasVException = true
    if (!fe.isTautology()) {
      val hasDuplicate = exceptionCtx.exists(x => x.equivalentTo(fe))
      if (!hasDuplicate) exceptionCtx = fe :: exceptionCtx
    }
  }

  def getHiddenContextsOtherThan(that: FeatureExpr): List[FeatureExpr] =
    exceptionCtx.filterNot(_ equivalentTo that)

  def getHiddenContexts: List[FeatureExpr] = exceptionCtx

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
