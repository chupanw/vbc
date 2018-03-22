package edu.cmu.cs.vbc.utils

/**
  * Collect and print statistics
  *
  * @author chupanw
  */
object Statistics {

  /**
    * Collect statistics output using a StringBuilder
    */
  val m: collection.mutable.Map[String, StringBuilder] = collection.mutable.Map()

  def header(name: String) = s"********** $name **********"

  /**
    * Collect
    *
    * @param methodName
    * @param nLifting
    * @param nTotal
    */
  def collectLiftingRatio(className: String, methodName: String, nLifting: Int, nTotal: Int) = {
    val printer = m.getOrElseUpdate(className, new StringBuilder)
    printer.append(String.format("%-50s %10s", "\t" + methodName + ":", nLifting + "/" + nTotal + "\n"))
  }

  def printStatistics(): Unit = {
    println("\n\n")
    println(header("Lifting Ratio"))
    for (p <- m)
      println(p._1 + "\n" + p._2.toString())
  }
}
