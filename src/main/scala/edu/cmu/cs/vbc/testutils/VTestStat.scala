package edu.cmu.cs.vbc.testutils

import java.io.{File, FileWriter}

import de.fosd.typechef.featureexpr.bdd.{BDDFeatureExpr, BDDFeatureModel}
import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory, SingleFeatureExpr}
import edu.cmu.cs.varex.V
import edu.cmu.cs.vbc.GlobalConfig
import org.slf4j.LoggerFactory

import scala.collection.mutable

object VTestStat {
  private val skippedClasses = mutable.ListBuffer[String]()
  private val classes = mutable.HashMap[String, VTestStatClass]()
  private val genprogLogger = LoggerFactory.getLogger("genprog")
  var hasOverallSolution: Boolean = false

  def skipClass(c: String): Unit = skippedClasses += c
  def skip(c: String, m: String): Unit = classes.getOrElseUpdate(c, VTestStatClass(c)).skipMethod(m)
  def succeed(c: String, m: String): Unit = classes.getOrElseUpdate(c, VTestStatClass(c)).succeedMethod(m)
  def fail(c: String, m: String, ctx: FeatureExpr): Unit = classes.getOrElseUpdate(c, VTestStatClass(c)).fail(m, ctx)

  def clear(): Unit = {
    skippedClasses.clear()
    classes.clear()
    hasOverallSolution = false
  }

  def printlnAndLog(s: String): Unit = {
    println(s)
    genprogLogger.info(s)
  }

  def printToConsole(): Unit = {
    printlnAndLog("")
    printlnAndLog("*********************************")
    printlnAndLog("*          Test Report          *")
    printlnAndLog("*********************************")
    classes.toList.sortWith((x, y) => x._1.compare(y._1) < 0).unzip._2.foreach(_.print2Console())
    skippedClasses foreach printlnAndLog
    val overallPassingCond = classes.values.map(_.getOverallPassingCondition).foldLeft(FeatureExprFactory.True)(_.and(_))
//    printOneSolution(overallPassingCond)
    printAllSolutions(overallPassingCond)
  }

  def toMarkdown(version: String, removePrefix: String): Unit = {
    val header = "| ID | Test | Passing Condition |\n| -- | ---- | ----------------- |\n"
    val rows = classes.unzip._2.flatMap(x => x.toMarkdown(removePrefix))
    val rowsWithIndex = rows.zipWithIndex.map(p => s"| ${p._2 + 1} | ${p._1}")
    val writer = new FileWriter(new File(version + ".md"))
    writer.write(header)
    rowsWithIndex foreach {x => writer.write(x + "\n")}
    writer.close()

    println(s"Table generated: $version.md")
  }

  /**
    * Print one solution that has a degree lower than [[GlobalConfig.maxInteractionDegree]]
    *
    * We assume that [[de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr.getSatisfiableAssignment()]] can give us the
    * smallest solution.
    */
  def printOneSolution(fe: FeatureExpr): Unit = {
    val hasLDSolution = fe.isSatisfiable() && !V.isDegreeTooHigh(fe)
    if (hasLDSolution)
      printlnAndLog(s"All test cases can pass if, for example, : ${V.getOneLowDegreeSolution(fe)}")
    else
      printlnAndLog(s"To pass all test cases, no solution within ${GlobalConfig.maxInteractionDegree} mutations")
  }

  def printAllSolutions(fe: FeatureExpr): Unit = {
    val hasLDSolution = fe.isSatisfiable() && !V.isDegreeTooHigh(fe)
    hasOverallSolution = hasLDSolution
    if (hasLDSolution)
      printlnAndLog(s"All test cases can pass if: ${V.getAllLowDegreeSolutions(fe)}, todo: also print options that do not matter")  //todo
    else
      printlnAndLog(s"To pass all test cases, no solution within ${GlobalConfig.maxInteractionDegree} mutations")
  }
}

case class VTestStatClass(c: String) {
  import VTestStat.printlnAndLog
  private var failedMethods: mutable.HashMap[String, VTestStatMethod] = mutable.HashMap.empty
  private var skippedMethods: mutable.Set[String] = mutable.Set()
  private var succeededMethods: mutable.Set[String] = mutable.Set()

  /* logging */
  def skipMethod(m: String): Unit = skippedMethods += m
  def succeedMethod(m: String): Unit = succeededMethods += m
  def fail(m: String, ctx: FeatureExpr): Unit = failedMethods.getOrElseUpdate(m, VTestStatMethod(m)) logFailingContext ctx

  /* report */
  def getOverallPassingCondition(): FeatureExpr = failedMethods.values.map(_.getPassingContext).foldLeft(FeatureExprFactory.True)(_.and(_))

  /**
    * Use with caution, might be too slow.
    */
  def getOverallPassingConditionExcludingHigherD(): FeatureExpr = failedMethods.values.map(_.getPassingContextExcludingHighD).foldLeft(FeatureExprFactory.True)(_.and(_))

  /**
    * Print one solution that has a degree lower than [[GlobalConfig.maxInteractionDegree]]
    *
    * We assume that [[de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr.getSatisfiableAssignment()]] can give us the
    * smallest solution.
    */
  def printOneSolution(fe: FeatureExpr): Unit = {
    val hasLDSolution = fe.isSatisfiable() && !V.isDegreeTooHigh(fe)
    if (hasLDSolution)
      printlnAndLog(s"$c can be fixed if, for example : ${V.getOneLowDegreeSolution(fe)}\n")
    else
      printlnAndLog(s"$c cannot be fixed with ${GlobalConfig.maxInteractionDegree} mutations\n")
  }

  /**
    * Print solutions on the fly, as getting all solutions might take too long
    *
    * Excluding higher order interactions.
    *   For individual test cases and test classes, output if satisfiable or not with regard to low interaction degree
    *   For all test cases, get one lower-degree solution
    */
  def print2Console (): Unit = {
    printlnAndLog(c)
    printlnAndLog("[Failed]")
    failedMethods.toList.sortWith((x, y) => x._1.compareTo(y._1) < 0).unzip._2.foreach(_.print2Console)
    if (succeededMethods.exists(x => !failedMethods.contains(x))) {
      printlnAndLog("[Succeeded]")
      printlnAndLog(succeededMethods.filter(x => !failedMethods.contains(x)).mkString("\t", "\n\t", ""))
    }
    if (skippedMethods.nonEmpty) {
      printlnAndLog("[Skipped]")
      printlnAndLog(skippedMethods.mkString("\t", "\n\t", ""))
    }
    printOneSolution(getOverallPassingCondition())
  }

  /**
    * Only used when we are not limiting the degree of interactions
    */
  override def toString: String =
    s"""
       |$c pass if $getOverallPassingCondition
       |[Failed]
       |${failedMethods.toList.sortWith((x, y) => x._1.compareTo(y._1) < 0).unzip._2.mkString("\t", "\n\t", "")}
       |${if (succeededMethods.nonEmpty) "[Succeeded]" else ""}
       |${succeededMethods.filter(x => !failedMethods.contains(x)).mkString("\t", "\n\t", "")}
       |${if (skippedMethods.nonEmpty) "[Skipped]" else ""}
       |${skippedMethods.mkString("\t", "\n\t", "")}
     """.stripMargin

  /**
    * Same limitations as toString
    */
  def toMarkdown(removePrefix: String): List[String] = {
    val shortenClazzName = c.substring(removePrefix.length)
    val skipped = for (m <- skippedMethods.toList) yield shortenClazzName + s".$m" + " | " + "skipped" + "|"
    val succeeded = for (m <- succeededMethods.filter(x => !failedMethods.contains(x)).toList) yield s"$shortenClazzName.$m | ✔ |"
    val failed = failedMethods.toList.unzip._2.map(x => x.toMarkdown(shortenClazzName))
    skipped ::: succeeded ::: failed
  }
}

case class VTestStatMethod(m: String) {
  private var failingCtx: FeatureExpr = FeatureExprFactory.False

  def logFailingContext(fe: FeatureExpr): Unit = failingCtx = failingCtx or fe

  /* reporting */
  def getPassingContextExcludingHighD: FeatureExpr = restrictStartingContext(failingCtx.not())
  def getPassingContext: FeatureExpr = failingCtx.not()

  def oneSolution(fe: FeatureExpr): String = {
    val hasLDSolution = fe.isSatisfiable() && !V.isDegreeTooHigh(fe)
    if (hasLDSolution)
      s" pass if, for example : ${V.getOneLowDegreeSolution(fe)}\n"
    else
      s" cannot pass with ${GlobalConfig.maxInteractionDegree} mutations\n"
  }

  /**
    * In case there are too many mutations, we only output if there exists solutions that are
    * low-degree interactions
    */
  def print2Console(): Unit= {
    import VTestStat.printlnAndLog
    val x = failingCtx.not()
//    val msg = (if (x.isContradiction() || V.isDegreeTooHigh(x)) s" has no " else " has ") + s"solutions with degree lower than ${GlobalConfig.maxInteractionDegree}"
//    val msg = " pass if " + failingCtx.not()
    val msg = oneSolution(failingCtx.not())
    printlnAndLog("\t" + m + msg)
  }

  /**
    * Used only if we are not limiting the degree of interactions
    */
  override def toString: String = m + "  pass if  " + getPassingContext
  def toMarkdown(clz: String): String = s"$clz.$m | `${getPassingContext}` |"

  type Solution = (List[SingleFeatureExpr], List[SingleFeatureExpr])

  /**
    * Rule out all solutions that have high-degree interactions.
    *
    * Use with caution, might be very slow.
    * Might be faster to just append a feature model that excludes all higher order interactions
    *
    * @param fe FeatureExpr to be restricted
    * @return Restricted FeatureExpr
    */
  def restrictStartingContext(fe: FeatureExpr): FeatureExpr = {
    def getAllSats(x: FeatureExpr, options: Set[SingleFeatureExpr], allSat: Set[Solution]): Set[Solution] = {
      val sat = x.getSatisfiableAssignment(BDDFeatureModel.empty, options, preferDisabledFeatures = true)
      if (sat.isDefined) {
        val satFE = construct(sat.get._1, sat.get._2)
        getAllSats(x.and(satFE.not()), options, allSat + sat.get)
      } else
        allSat
    }
    def construct(enable: List[SingleFeatureExpr], disable: List[SingleFeatureExpr]): FeatureExpr = {
      enable.foldLeft(FeatureExprFactory.True)(_ and _) and disable.map(_.not()).foldLeft(FeatureExprFactory.True)(_ and _)
    }
    if (FeatureExprFactory.True.equivalentTo(fe) || FeatureExprFactory.False.equivalentTo(fe)) fe
    else {
      val allSat: Set[Solution] = getAllSats(fe, fe.collectDistinctFeatureObjects, Set())
      val invalidSat = allSat.filter(x => x._1.size > GlobalConfig.maxInteractionDegree)
      invalidSat.foldLeft(fe)((soFar, next) => soFar.and(construct(next._1, next._2).not()))
    }
  }
}