package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.size
import org.scalatest.FunSuite

class Benchmark_size extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("size: 1") {
    println("size 1")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("size: 2") {
    println("size 2")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("size: 3") {
    println("size 3")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("size: 4") {
    println("size 4")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("size: 5") {
    println("size 5")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("size: 6") {
    println("size 6")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("size: 7") {
    println("size 7")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("size: 8") {
    println("size 8")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("size: 9") {
    println("size 9")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("size: 10") {
    println("size 10")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("size: 11") {
    println("size 11")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("size: 12") {
    println("size 12")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("size: 13") {
    println("size 13")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("size: 14") {
    println("size 14")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("size: 15") {
    println("size 15")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("size: 20") {
    println("size 20")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("size: 30") {
    println("size 30")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("size: 40") {
    println("size 40")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("size: 50") {
    println("size 50")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("size: 60") {
    println("size 60")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("size: 70") {
    println("size 70")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("size: 80") {
    println("size 80")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("size: 90") {
    println("size 90")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("size: 100") {
    println("size 100")
    testMain(classOf[size], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
