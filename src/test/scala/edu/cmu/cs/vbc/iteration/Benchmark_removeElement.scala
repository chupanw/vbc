package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.removeElement
import org.scalatest.FunSuite

class Benchmark_removeElement extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("removeElement: 1") {
    println("removeElement 1")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("removeElement: 2") {
    println("removeElement 2")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("removeElement: 3") {
    println("removeElement 3")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("removeElement: 4") {
    println("removeElement 4")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("removeElement: 5") {
    println("removeElement 5")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("removeElement: 6") {
    println("removeElement 6")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("removeElement: 7") {
    println("removeElement 7")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("removeElement: 8") {
    println("removeElement 8")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("removeElement: 9") {
    println("removeElement 9")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("removeElement: 10") {
    println("removeElement 10")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("removeElement: 11") {
    println("removeElement 11")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("removeElement: 12") {
    println("removeElement 12")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("removeElement: 13") {
    println("removeElement 13")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("removeElement: 14") {
    println("removeElement 14")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("removeElement: 15") {
    println("removeElement 15")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("removeElement: 20") {
    println("removeElement 20")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("removeElement: 30") {
    println("removeElement 30")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("removeElement: 40") {
    println("removeElement 40")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("removeElement: 50") {
    println("removeElement 50")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("removeElement: 60") {
    println("removeElement 60")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("removeElement: 70") {
    println("removeElement 70")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("removeElement: 80") {
    println("removeElement 80")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("removeElement: 90") {
    println("removeElement 90")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("removeElement: 100") {
    println("removeElement 100")
    testMain(classOf[removeElement], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
