package edu.cmu.cs.vbc.config

import java.lang.reflect.Method
import java.text.NumberFormat

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.{VBCClassLoader, VException}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * Global configurations for VarexC
  */
object Settings {
  private val config = ConfigFactory.load("reference.conf")
  private val logger = LoggerFactory.getLogger("genprog")

  val fastMode: Boolean              = config.getBoolean("varexc.fastMode")
  val enableStackTraceCheck: Boolean = config.getBoolean("varexc.enableStackTraceCheck")

  val logTrace: Boolean          = config.getBoolean("varexc.logging.logTrace")
  val detectComplexLoop: Boolean = config.getBoolean("varexc.misc.detectComplexLoop")
  val printContext: Boolean      = config.getBoolean("varexc.printing.printContext")
  val printExpandArrayWarnings: Boolean =
    config.getBoolean("varexc.printing.printExpandArrayWarnings")
  val printTestResults: Boolean = config.getBoolean("varexc.printing.printTestResults")
  val writeBDDs: Boolean        = config.getBoolean("varexc.logging.writeBDDs")
  val earlyFail: Boolean        = config.getBoolean("varexc.earlyFail")

  // execute `java -XX:+PrintFlagsFinal -version` and look for MaxJavaStackTraceDepth
  val maxStackDepth: Int = config.getInt("varexc.maxStackDepth")

  /**
    * Interaction degree defined as minimum number of individual options that must be enable to satisfy a feature expression
    *
    * For example,
    * degree(A & B) = 2
    * degree(A & !B) = 1,
    * degree (A | B)  = 1,
    * degree((A & B) | (C & D)) = 2
    * degree((A & B) | C) = 1
    */
  private[cs] var maxInteractionDegree: Int = config.getInt("varexc.maxInteractionDegree")

  /**
    * Maximum number of VBlocks we can execute before throwing an exception
    *
    * This number is not used if [[enableBlockCounting]] is false
    *
    * This limit is intentionally large because we hope identify infinite loops so that we can remove them.
    */
  val maxBlockCount: Long          = config.getLong("varexc.blockCount.maxBlockCount")
  val enableBlockCounting: Boolean = config.getBoolean("varexc.blockCount.enableBlockCounting")
  val enablePerTestBlockCount: Boolean =
    config.getBoolean("varexc.blockCount.enablePerTestBlockCount")
  val maxBlockCountFactor: Int = config.getInt("varexc.blockCount.maxBlockCountFactor")

  def validate(): Unit = {
    if (!enableBlockCounting)
      assert(maxBlockCount == -1 && !enablePerTestBlockCount,
             "Should not set maxBlockCount and perTestCount when blockCounting is false")
    else if (enablePerTestBlockCount) {
      assert(maxBlockCount == -1, "Should not set maxBlockCount when perTestCount is true")
      assert(maxBlockCountFactor >= 10, "maxBlockCountFactor should be at least 10")
    } else
      assert(maxBlockCount > 0, "Invalid max block count")
  }

  def printSettings(): Unit = {
    validate()
    val message =
      s"""**********************************************************************
         |*                              Settings                              *
         |**********************************************************************
         |${config.getValue("varexc").render(ConfigRenderOptions.concise().setFormatted(true))}
         |""".stripMargin
    print(message)
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
  var skippedExceptionContext: FeatureExpr   = FeatureExprFactory.False
  var globalContext: FeatureExpr             = FeatureExprFactory.False
  private var curBlockCount: Long            = 0
  private val maxBlockPerTest                = mutable.Map[String, Long]()
  var entryMethod: Method                    = _
  var isFastMode: Boolean                    = false
  var curStackDepth: Int                     = 0 // excluding internal method calls

  var expectedException: Option[Class[_]] = None

  var classloader: Option[ClassLoader] = None

  /**
    * Used when execution crosses the boundary of VE environment and the Digester library. See [[edu.cmu.cs.varex.VOps.populate()]]
    */
  var boundaryCtx: FeatureExpr = FeatureExprFactory.True

  private val numFormatter = NumberFormat.getNumberInstance

  def incrementBlockCount(): Unit = {
    curBlockCount += 1
  }

  def increaseStackDepth(): Unit = {
    curStackDepth += 1
  }

  def decreaseStackDepth(): Unit = {
    curStackDepth -= 1
  }

  def getBlockCount: Long = curBlockCount

  def resetBlockCount(): Unit = {
    curBlockCount = 0
    println("Resetting block count")
  }

  def genMethodKey(m: Method): String =
    m.getDeclaringClass.getCanonicalName + "#" + m.getName + "(" + m.getParameters
      .map(x => x.getName + ":" + x.getType.getCanonicalName)
      .mkString("_") + ")"

  def getMaxBlockCount: Long =
    if (Settings.enablePerTestBlockCount) maxBlockPerTest(genMethodKey(entryMethod))
    else Settings.maxBlockCount

  def isBlockCountReached: Boolean =
    if (entryMethod == null) false else curBlockCount > getMaxBlockCount

  def putMaxBlockForTest(m: Method): Unit = {
    val cnt   = curBlockCount * Settings.maxBlockCountFactor
    val key   = genMethodKey(m)
    val value = Math.max(maxBlockPerTest.getOrElse(key, 0L), cnt)
    maxBlockPerTest.put(key, value)
    println("Setting max block to " + numFormatter.format(value))
    resetBlockCount()
  }

  /**
    * Initialize all mutable states.
    *
    * All vars or mutable objects within this [[VERuntime]] object should get initialized here.
    *
    * MUST BE CALLED BEFORE EACH RESTART OF VARIATIONAL EXECUTION.
    */
  def init(entryMethod: Method,
           initContext: FeatureExpr,
           boundaryCtx: FeatureExpr,
           isFastMode: Boolean,
           expectedException: Option[Class[_]]): Unit = {

    /** Exception handling  */
    this.globalContext = initContext
    this.postponedExceptionContext = FeatureExprFactory.False
    this.thrownExceptionContext = FeatureExprFactory.False
    this.skippedExceptionContext = FeatureExprFactory.False
    this.isFastMode = isFastMode
    this.savedRestart = 0
    this.curStackDepth = 0
    this.expectedException = expectedException

    /** Block counting */
    this.entryMethod = entryMethod

    /** Logging */
    this.curBlockCount = 0

    /** Model class issues */
    this.boundaryCtx = boundaryCtx
  }

  var savedRestart: Int = 0 // debug
  def postponeException(t: Throwable, ctx: FeatureExpr): Unit = {
    val e = t match {
      case ve: VException => ve.e
      case _ => t
    }
    postponedExceptionContext = postponedExceptionContext or ctx
    if (!isFastMode && Settings.enableStackTraceCheck) {
      val handler = VBCClassLoader.potentialHandler(e, classloader.get)
      if (handler.nonEmpty) {
        println(
          s"Restart needed for <${e.getClass.getName}> as it can be handled: ${handler.get.toString} with ${VBCClassLoader
            .getMightHandle(handler.get.getClassName, handler.get.getMethodName)}")
      } else if (expectedException.exists(_.isInstance(e))) {
        println(s"Restart needed for <${e.getClass.getName}> as it is expected by the test case")
      } else {
        savedRestart += 1
        skippedExceptionContext = skippedExceptionContext or ctx
        println(s"Skipping restart of <${e.getClass.getName}>, total save so far: $savedRestart")
      }
    }
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

  def loadFeatures(featureFile: String): Unit = {
    val resource = getClass.getResourceAsStream("/" + featureFile)
    for (line <- io.Source.fromInputStream(resource).getLines()) {
      val features = line.split(' ')
      features.foreach(x => FeatureExprFactory.createDefinedExternal(x.trim))
    }
    println("features loaded")
  }
}
