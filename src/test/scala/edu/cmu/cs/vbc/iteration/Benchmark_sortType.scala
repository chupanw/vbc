package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.sortType
import org.scalatest.FunSuite

class Benchmark_sortType extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("sortType: 1") {
    println("sortType 1")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("sortType: 2") {
    println("sortType 2")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("sortType: 3") {
    println("sortType 3")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("sortType: 4") {
    println("sortType 4")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("sortType: 5") {
    println("sortType 5")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("sortType: 6") {
    println("sortType 6")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("sortType: 7") {
    println("sortType 7")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("sortType: 8") {
    println("sortType 8")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("sortType: 9") {
    println("sortType 9")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("sortType: 10") {
    println("sortType 10")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("sortType: 11") {
    println("sortType 11")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("sortType: 12") {
    println("sortType 12")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("sortType: 13") {
    println("sortType 13")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("sortType: 14") {
    println("sortType 14")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("sortType: 15") {
    println("sortType 15")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("sortType: 20") {
    println("sortType 20")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("sortType: 30") {
    println("sortType 30")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("sortType: 40") {
    println("sortType 40")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("sortType: 50") {
    println("sortType 50")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("sortType: 60") {
    println("sortType 60")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("sortType: 70") {
    println("sortType 70")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("sortType: 80") {
    println("sortType 80")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("sortType: 90") {
    println("sortType 90")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("sortType: 100") {
    println("sortType 100")
    testMain(classOf[sortType], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
