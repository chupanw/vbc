package edu.cmu.cs.vbc.utils

import edu.cmu.cs.varex.VOps

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

  var nLifting = 0
  var nTotal = 0
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
    this.nLifting += nLifting
    this.nTotal += nTotal
  }

  var nComplex: Int = 0
  var nSimple: Int = 0

  def printStatistics(): Unit = {
    println("Complex Loop: " + nComplex)
    println("Simple Loop: " + nSimple)

    println("\n\n")
    println(header("Lifting Ratio"))
    for (p <- m)
      println(p._1 + "\n" + p._2.toString())

    println(nLifting.toDouble / nTotal)
    println(VOps.nSimpleInvocations)
  }
}
