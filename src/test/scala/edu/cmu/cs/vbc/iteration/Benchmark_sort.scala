package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.sort
import org.scalatest.FunSuite

class Benchmark_sort extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("sort: 1") {
    println("sort 1")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("sort: 2") {
    println("sort 2")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("sort: 3") {
    println("sort 3")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("sort: 4") {
    println("sort 4")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("sort: 5") {
    println("sort 5")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("sort: 6") {
    println("sort 6")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("sort: 7") {
    println("sort 7")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("sort: 8") {
    println("sort 8")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("sort: 9") {
    println("sort 9")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("sort: 10") {
    println("sort 10")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("sort: 11") {
    println("sort 11")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("sort: 12") {
    println("sort 12")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("sort: 13") {
    println("sort 13")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("sort: 14") {
    println("sort 14")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("sort: 15") {
    println("sort 15")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("sort: 20") {
    println("sort 20")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("sort: 30") {
    println("sort 30")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("sort: 40") {
    println("sort 40")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("sort: 50") {
    println("sort 50")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("sort: 60") {
    println("sort 60")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("sort: 70") {
    println("sort 70")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("sort: 80") {
    println("sort 80")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("sort: 90") {
    println("sort 90")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("sort: 100") {
    println("sort 100")
    testMain(classOf[sort], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
