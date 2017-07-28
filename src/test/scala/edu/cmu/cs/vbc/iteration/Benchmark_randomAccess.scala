package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.randomAccess
import org.scalatest.FunSuite

class Benchmark_randomAccess extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("randomAccess: 1") {
    println("randomAccess 1")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("randomAccess: 2") {
    println("randomAccess 2")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("randomAccess: 3") {
    println("randomAccess 3")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("randomAccess: 4") {
    println("randomAccess 4")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("randomAccess: 5") {
    println("randomAccess 5")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("randomAccess: 6") {
    println("randomAccess 6")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("randomAccess: 7") {
    println("randomAccess 7")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("randomAccess: 8") {
    println("randomAccess 8")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("randomAccess: 9") {
    println("randomAccess 9")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("randomAccess: 10") {
    println("randomAccess 10")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("randomAccess: 11") {
    println("randomAccess 11")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("randomAccess: 12") {
    println("randomAccess 12")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("randomAccess: 13") {
    println("randomAccess 13")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("randomAccess: 14") {
    println("randomAccess 14")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("randomAccess: 15") {
    println("randomAccess 15")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("randomAccess: 20") {
    println("randomAccess 20")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("randomAccess: 30") {
    println("randomAccess 30")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("randomAccess: 40") {
    println("randomAccess 40")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("randomAccess: 50") {
    println("randomAccess 50")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("randomAccess: 60") {
    println("randomAccess 60")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("randomAccess: 70") {
    println("randomAccess 70")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("randomAccess: 80") {
    println("randomAccess 80")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("randomAccess: 90") {
    println("randomAccess 90")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("randomAccess: 100") {
    println("randomAccess 100")
    testMain(classOf[randomAccess], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
