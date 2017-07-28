package edu.cmu.cs.vbc.iteration

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.iteration.memory
import org.scalatest.FunSuite

class Benchmark_memory extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("memory: 1") {
    println("memory 1")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 1)
  }
  test("memory: 2") {
    println("memory 2")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 2)
  }
  test("memory: 3") {
    println("memory 3")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 3)
  }
  test("memory: 4") {
    println("memory 4")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 4)
  }
  test("memory: 5") {
    println("memory 5")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 5)
  }
  test("memory: 6") {
    println("memory 6")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 6)
  }
  test("memory: 7") {
    println("memory 7")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("memory: 8") {
    println("memory 8")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 8)
  }
  test("memory: 9") {
    println("memory 9")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 9)
  }
  test("memory: 10") {
    println("memory 10")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 10)
  }
  test("memory: 11") {
    println("memory 11")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 11)
  }
  test("memory: 12") {
    println("memory 12")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 12)
  }
  test("memory: 13") {
    println("memory 13")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 13)
  }
  test("memory: 14") {
    println("memory 14")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 14)
  }
  test("memory: 15") {
    println("memory 15")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 7)
  }
  test("memory: 20") {
    println("memory 20")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 20)
  }
  test("memory: 30") {
    println("memory 30")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 30)
  }
  test("memory: 40") {
    println("memory 40")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 40)
  }
  test("memory: 50") {
    println("memory 50")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 50)
  }
  test("memory: 60") {
    println("memory 60")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 60)
  }
  test("memory: 70") {
    println("memory 70")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 70)
  }
  test("memory: 80") {
    println("memory 80")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 80)
  }
  test("memory: 90") {
    println("memory 90")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 90)
  }
  test("memory: 100") {
    println("memory 100")
    testMain(classOf[memory], compareTraceAgainstBruteForce = false, feListFeatures = 100)
  }
}
