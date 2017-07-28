package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.expensiveIteration
import org.scalatest.FunSuite

class Benchmark_expensiveIteration extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("expensiveIteration: 1") {
    println("expensiveIteration 1")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("expensiveIteration: 2") {
    println("expensiveIteration 2")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("expensiveIteration: 3") {
    println("expensiveIteration 3")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("expensiveIteration: 4") {
    println("expensiveIteration 4")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("expensiveIteration: 5") {
    println("expensiveIteration 5")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("expensiveIteration: 6") {
    println("expensiveIteration 6")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("expensiveIteration: 7") {
    println("expensiveIteration 7")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("expensiveIteration: 8") {
    println("expensiveIteration 8")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("expensiveIteration: 9") {
    println("expensiveIteration 9")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("expensiveIteration: 10") {
    println("expensiveIteration 10")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("expensiveIteration: 11") {
    println("expensiveIteration 11")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("expensiveIteration: 12") {
    println("expensiveIteration 12")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("expensiveIteration: 13") {
    println("expensiveIteration 13")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("expensiveIteration: 14") {
    println("expensiveIteration 14")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("expensiveIteration: 15") {
    println("expensiveIteration 15")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("expensiveIteration: 20") {
    println("expensiveIteration 20")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("expensiveIteration: 30") {
    println("expensiveIteration 30")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("expensiveIteration: 40") {
    println("expensiveIteration 40")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("expensiveIteration: 50") {
    println("expensiveIteration 50")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("expensiveIteration: 60") {
    println("expensiveIteration 60")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("expensiveIteration: 70") {
    println("expensiveIteration 70")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("expensiveIteration: 80") {
    println("expensiveIteration 80")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("expensiveIteration: 90") {
    println("expensiveIteration 90")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("expensiveIteration: 100") {
    println("expensiveIteration 100")
    testMain(classOf[expensiveIteration], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
