package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.looptype4
import org.scalatest.FunSuite

class Benchmark_looptype4 extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("looptype4: 1") {
    println("looptype4 1")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("looptype4: 2") {
    println("looptype4 2")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("looptype4: 3") {
    println("looptype4 3")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("looptype4: 4") {
    println("looptype4 4")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("looptype4: 5") {
    println("looptype4 5")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("looptype4: 6") {
    println("looptype4 6")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("looptype4: 7") {
    println("looptype4 7")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("looptype4: 8") {
    println("looptype4 8")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("looptype4: 9") {
    println("looptype4 9")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("looptype4: 10") {
    println("looptype4 10")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("looptype4: 11") {
    println("looptype4 11")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("looptype4: 12") {
    println("looptype4 12")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("looptype4: 13") {
    println("looptype4 13")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("looptype4: 14") {
    println("looptype4 14")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("looptype4: 15") {
    println("looptype4 15")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("looptype4: 20") {
    println("looptype4 20")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("looptype4: 30") {
    println("looptype4 30")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("looptype4: 40") {
    println("looptype4 40")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("looptype4: 50") {
    println("looptype4 50")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("looptype4: 60") {
    println("looptype4 60")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("looptype4: 70") {
    println("looptype4 70")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("looptype4: 80") {
    println("looptype4 80")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("looptype4: 90") {
    println("looptype4 90")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("looptype4: 100") {
    println("looptype4 100")
    testMain(classOf[looptype4], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
