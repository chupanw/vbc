package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.removeIndex
import org.scalatest.FunSuite

class Benchmark_removeIndex extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("removeIndex: 1") {
    println("removeIndex 1")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("removeIndex: 2") {
    println("removeIndex 2")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("removeIndex: 3") {
    println("removeIndex 3")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("removeIndex: 4") {
    println("removeIndex 4")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("removeIndex: 5") {
    println("removeIndex 5")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("removeIndex: 6") {
    println("removeIndex 6")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("removeIndex: 7") {
    println("removeIndex 7")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("removeIndex: 8") {
    println("removeIndex 8")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("removeIndex: 9") {
    println("removeIndex 9")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("removeIndex: 10") {
    println("removeIndex 10")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("removeIndex: 11") {
    println("removeIndex 11")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("removeIndex: 12") {
    println("removeIndex 12")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("removeIndex: 13") {
    println("removeIndex 13")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("removeIndex: 14") {
    println("removeIndex 14")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("removeIndex: 15") {
    println("removeIndex 15")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("removeIndex: 20") {
    println("removeIndex 20")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("removeIndex: 30") {
    println("removeIndex 30")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("removeIndex: 40") {
    println("removeIndex 40")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("removeIndex: 50") {
    println("removeIndex 50")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("removeIndex: 60") {
    println("removeIndex 60")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("removeIndex: 70") {
    println("removeIndex 70")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("removeIndex: 80") {
    println("removeIndex 80")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("removeIndex: 90") {
    println("removeIndex 90")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("removeIndex: 100") {
    println("removeIndex 100")
    testMain(classOf[removeIndex], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
