package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}

import scala.collection.mutable

object VTestStat {
  private val skippedClasses = mutable.ListBuffer[String]()
  private val classes = mutable.HashMap[String, VTestStatClass]()

  def skipClass(c: String): Unit = skippedClasses += c
  def skip(c: String, m: String): Unit = classes.getOrElseUpdate(c, VTestStatClass(c)).skipMethod(m)
  def succeed(c: String, m: String): Unit = classes.getOrElseUpdate(c, VTestStatClass(c)).succeedMethod(m)
  def fail(c: String, m: String, ctx: FeatureExpr): Unit = classes.getOrElseUpdate(c, VTestStatClass(c)).fail(m, ctx)

  def printToConsole(): Unit = {
    println()
    println("*********************************")
    println("*          Test Report          *")
    println("*********************************")
    classes.toList.sortWith((x, y) => x._1.compare(y._1) < 0).unzip._2.foreach(println)
  }
}

case class VTestStatClass(c: String) {
  private var failedMethods: mutable.HashMap[String, VTestStatMethod] = mutable.HashMap.empty
  private var skippedMethods: mutable.ListBuffer[String] = mutable.ListBuffer.empty
  private var succeededMethods: mutable.ListBuffer[String] = mutable.ListBuffer.empty

  def skipMethod(m: String): Unit = skippedMethods += m
  def succeedMethod(m: String): Unit = succeededMethods += m
  def fail(m: String, ctx: FeatureExpr): Unit = failedMethods.getOrElseUpdate(m, VTestStatMethod(m)) logFailingContext ctx

  override def toString: String =
    s"""
       |$c
       |[Failed]
       |${failedMethods.toList.sortWith((x, y) => x._1.compareTo(y._1) < 0).unzip._2.mkString("\t", "\n\t", "")}
       |${if (succeededMethods.nonEmpty) "[Succeed]" else ""}
       |${succeededMethods.mkString("\t", "\n\t", "")}
       |${if (skippedMethods.nonEmpty) "[Skipped]" else ""}
       |${skippedMethods.mkString("\t", "\n\t", "")}
     """.stripMargin
}

case class VTestStatMethod(m: String) {
  private var failingCtx: FeatureExpr = FeatureExprFactory.False

  def logFailingContext(fe: FeatureExpr): Unit = failingCtx = failingCtx or fe

  override def toString: String = m + "  <-  " + (if (failingCtx.isSatisfiable()) failingCtx else "")
}