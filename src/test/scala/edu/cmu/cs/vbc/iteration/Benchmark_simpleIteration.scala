package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.simpleIteration
import org.scalatest.FunSuite

class Benchmark_simpleIteration extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("simpleIteration: 1") {
    println("simpleIteration 1")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("simpleIteration: 2") {
    println("simpleIteration 2")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("simpleIteration: 3") {
    println("simpleIteration 3")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("simpleIteration: 4") {
    println("simpleIteration 4")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("simpleIteration: 5") {
    println("simpleIteration 5")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("simpleIteration: 6") {
    println("simpleIteration 6")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("simpleIteration: 7") {
    println("simpleIteration 7")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("simpleIteration: 8") {
    println("simpleIteration 8")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("simpleIteration: 9") {
    println("simpleIteration 9")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("simpleIteration: 10") {
    println("simpleIteration 10")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("simpleIteration: 11") {
    println("simpleIteration 11")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("simpleIteration: 12") {
    println("simpleIteration 12")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("simpleIteration: 13") {
    println("simpleIteration 13")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("simpleIteration: 14") {
    println("simpleIteration 14")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("simpleIteration: 15") {
    println("simpleIteration 15")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("simpleIteration: 20") {
    println("simpleIteration 20")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("simpleIteration: 30") {
    println("simpleIteration 30")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("simpleIteration: 40") {
    println("simpleIteration 40")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("simpleIteration: 50") {
    println("simpleIteration 50")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("simpleIteration: 60") {
    println("simpleIteration 60")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("simpleIteration: 70") {
    println("simpleIteration 70")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("simpleIteration: 80") {
    println("simpleIteration 80")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("simpleIteration: 90") {
    println("simpleIteration 90")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("simpleIteration: 100") {
    println("simpleIteration 100")
    testMain(classOf[simpleIteration], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
