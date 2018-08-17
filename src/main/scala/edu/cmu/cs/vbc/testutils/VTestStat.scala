package edu.cmu.cs.vbc.testutils

import java.io.{File, FileWriter}

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
    skippedClasses foreach println
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
       |${if (succeededMethods.nonEmpty) "[Succeeded]" else ""}
       |${succeededMethods.filter(x => !failedMethods.contains(x)).mkString("\t", "\n\t", "")}
       |${if (skippedMethods.nonEmpty) "[Skipped]" else ""}
       |${skippedMethods.mkString("\t", "\n\t", "")}
     """.stripMargin

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

  override def toString: String = m + "  pass if  " + failingCtx.not()

  def toMarkdown(clz: String): String = s"$clz.$m | `${failingCtx.not()}` |"
}