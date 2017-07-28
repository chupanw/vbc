package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.findElement
import org.scalatest.FunSuite

class Benchmark_findElement extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("findElement: 1") {
    println("findElement 1")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("findElement: 2") {
    println("findElement 2")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("findElement: 3") {
    println("findElement 3")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("findElement: 4") {
    println("findElement 4")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("findElement: 5") {
    println("findElement 5")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("findElement: 6") {
    println("findElement 6")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("findElement: 7") {
    println("findElement 7")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("findElement: 8") {
    println("findElement 8")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("findElement: 9") {
    println("findElement 9")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("findElement: 10") {
    println("findElement 10")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("findElement: 11") {
    println("findElement 11")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("findElement: 12") {
    println("findElement 12")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("findElement: 13") {
    println("findElement 13")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("findElement: 14") {
    println("findElement 14")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("findElement: 15") {
    println("findElement 15")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("findElement: 20") {
    println("findElement 20")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("findElement: 30") {
    println("findElement 30")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("findElement: 40") {
    println("findElement 40")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("findElement: 50") {
    println("findElement 50")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("findElement: 60") {
    println("findElement 60")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("findElement: 70") {
    println("findElement 70")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("findElement: 80") {
    println("findElement 80")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("findElement: 90") {
    println("findElement 90")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("findElement: 100") {
    println("findElement 100")
    testMain(classOf[findElement], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
