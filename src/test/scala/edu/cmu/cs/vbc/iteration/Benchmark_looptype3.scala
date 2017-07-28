package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.looptype3
import org.scalatest.FunSuite

class Benchmark_looptype3 extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("looptype3: 1") {
    println("looptype3 1")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("looptype3: 2") {
    println("looptype3 2")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("looptype3: 3") {
    println("looptype3 3")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("looptype3: 4") {
    println("looptype3 4")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("looptype3: 5") {
    println("looptype3 5")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("looptype3: 6") {
    println("looptype3 6")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("looptype3: 7") {
    println("looptype3 7")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("looptype3: 8") {
    println("looptype3 8")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("looptype3: 9") {
    println("looptype3 9")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("looptype3: 10") {
    println("looptype3 10")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("looptype3: 11") {
    println("looptype3 11")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("looptype3: 12") {
    println("looptype3 12")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("looptype3: 13") {
    println("looptype3 13")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("looptype3: 14") {
    println("looptype3 14")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("looptype3: 15") {
    println("looptype3 15")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("looptype3: 20") {
    println("looptype3 20")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("looptype3: 30") {
    println("looptype3 30")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("looptype3: 40") {
    println("looptype3 40")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("looptype3: 50") {
    println("looptype3 50")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("looptype3: 60") {
    println("looptype3 60")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("looptype3: 70") {
    println("looptype3 70")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("looptype3: 80") {
    println("looptype3 80")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("looptype3: 90") {
    println("looptype3 90")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("looptype3: 100") {
    println("looptype3 100")
    testMain(classOf[looptype3], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
