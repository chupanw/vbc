package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.binarySearch
import org.scalatest.FunSuite

class Benchmark_binarySearch extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("binarySearch: 1") {
    println("binarySearch 1")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("binarySearch: 2") {
    println("binarySearch 2")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("binarySearch: 3") {
    println("binarySearch 3")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("binarySearch: 4") {
    println("binarySearch 4")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("binarySearch: 5") {
    println("binarySearch 5")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("binarySearch: 6") {
    println("binarySearch 6")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("binarySearch: 7") {
    println("binarySearch 7")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("binarySearch: 8") {
    println("binarySearch 8")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("binarySearch: 9") {
    println("binarySearch 9")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("binarySearch: 10") {
    println("binarySearch 10")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("binarySearch: 11") {
    println("binarySearch 11")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("binarySearch: 12") {
    println("binarySearch 12")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("binarySearch: 13") {
    println("binarySearch 13")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("binarySearch: 14") {
    println("binarySearch 14")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("binarySearch: 15") {
    println("binarySearch 15")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("binarySearch: 20") {
    println("binarySearch 20")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("binarySearch: 30") {
    println("binarySearch 30")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("binarySearch: 40") {
    println("binarySearch 40")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("binarySearch: 50") {
    println("binarySearch 50")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("binarySearch: 60") {
    println("binarySearch 60")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("binarySearch: 70") {
    println("binarySearch 70")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("binarySearch: 80") {
    println("binarySearch 80")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("binarySearch: 90") {
    println("binarySearch 90")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("binarySearch: 100") {
    println("binarySearch 100")
    testMain(classOf[binarySearch], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
