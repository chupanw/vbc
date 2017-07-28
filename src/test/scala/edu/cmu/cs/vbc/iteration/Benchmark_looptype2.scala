package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.looptype2
import org.scalatest.FunSuite

class Benchmark_looptype2 extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("looptype2: 1") {
    println("looptype2 1")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("looptype2: 2") {
    println("looptype2 2")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("looptype2: 3") {
    println("looptype2 3")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("looptype2: 4") {
    println("looptype2 4")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("looptype2: 5") {
    println("looptype2 5")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("looptype2: 6") {
    println("looptype2 6")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("looptype2: 7") {
    println("looptype2 7")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("looptype2: 8") {
    println("looptype2 8")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("looptype2: 9") {
    println("looptype2 9")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("looptype2: 10") {
    println("looptype2 10")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("looptype2: 11") {
    println("looptype2 11")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("looptype2: 12") {
    println("looptype2 12")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("looptype2: 13") {
    println("looptype2 13")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("looptype2: 14") {
    println("looptype2 14")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("looptype2: 15") {
    println("looptype2 15")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("looptype2: 20") {
    println("looptype2 20")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("looptype2: 30") {
    println("looptype2 30")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("looptype2: 40") {
    println("looptype2 40")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("looptype2: 50") {
    println("looptype2 50")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("looptype2: 60") {
    println("looptype2 60")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("looptype2: 70") {
    println("looptype2 70")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("looptype2: 80") {
    println("looptype2 80")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("looptype2: 90") {
    println("looptype2 90")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("looptype2: 100") {
    println("looptype2 100")
    testMain(classOf[looptype2], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
