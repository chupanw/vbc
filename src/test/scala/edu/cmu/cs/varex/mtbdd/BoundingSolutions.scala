package edu.cmu.cs.varex.mtbdd

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.config.Settings
import org.scalatest.FunSuite

/**
  * Probably require fork mode to avoid being affected by other options in other tests
  */
class BoundingSolutions extends FunSuite {

  val nTest = 10000
  val nClause = 100

  val features: Array[FeatureExpr] = Array(
    FeatureExprFactory.createDefinedExternal("A"),
    FeatureExprFactory.createDefinedExternal("B"),
    FeatureExprFactory.createDefinedExternal("C"),
    FeatureExprFactory.createDefinedExternal("D"),
    FeatureExprFactory.createDefinedExternal("E"),
    FeatureExprFactory.createDefinedExternal("F"),
    FeatureExprFactory.createDefinedExternal("G"),
    FeatureExprFactory.createDefinedExternal("H"),
    FeatureExprFactory.createDefinedExternal("I"),
    FeatureExprFactory.createDefinedExternal("J")
  )

  def changeDegree(d: Int): Unit = {
    Settings.maxInteractionDegree = d
    MTBDDFactory.clearCache()
  }

  def randomFormula(seed: Long): FeatureExpr = {
    val rand = new scala.util.Random(seed)
    def getClause: FeatureExpr = {
      var clause = FeatureExprFactory.True
      for(i <- 0 until 10) if (rand.nextInt() % 2 == 0) clause = clause & features(i) else clause & !features(i)
      clause
    }
    var res = FeatureExprFactory.False
    for (i <- 0 until nClause) res = res | getClause
    res
  }

  test("For the same formula, solutions of a higher degree bounding should subsume solutions of a lower degree bounding") {
    def oneTest(): Unit = {
      val seed: Long = System.currentTimeMillis()

      changeDegree(1)
      val d1 = randomFormula(seed).getAllSolutionsScala

      changeDegree(2)
      val d2 = randomFormula(seed).getAllSolutionsScala

      changeDegree(3)
      val d3 = randomFormula(seed).getAllSolutionsScala

      changeDegree(4)
      val d4 = randomFormula(seed).getAllSolutionsScala

      changeDegree(5)
      val d5 = randomFormula(seed).getAllSolutionsScala

      changeDegree(6)
      val d6 = randomFormula(seed).getAllSolutionsScala

      changeDegree(7)
      val d7 = randomFormula(seed).getAllSolutionsScala

      changeDegree(8)
      val d8 = randomFormula(seed).getAllSolutionsScala

      changeDegree(9)
      val d9 = randomFormula(seed).getAllSolutionsScala

      changeDegree(10)
      val d10 = randomFormula(seed).getAllSolutionsScala

      assert(d1.forall(d2.contains), s"Subsuming failed for seed: $seed")
      assert(d2.forall(d3.contains), s"Subsuming failed for seed: $seed")
      assert(d3.forall(d4.contains), s"Subsuming failed for seed: $seed")
      assert(d4.forall(d5.contains), s"Subsuming failed for seed: $seed")
      assert(d5.forall(d6.contains), s"Subsuming failed for seed: $seed")
      assert(d6.forall(d7.contains), s"Subsuming failed for seed: $seed")
      assert(d7.forall(d8.contains), s"Subsuming failed for seed: $seed")
      assert(d8.forall(d9.contains), s"Subsuming failed for seed: $seed")
      assert(d9.forall(d10.contains), s"Subsuming failed for seed: $seed")
    }

    0 until nTest foreach {i => println(s"Testing subsuming solutions ${i + 1}/$nTest");oneTest()}
  }

  def C(n: Int, N: Int): Int = ((N - n + 1) to N).product / (1 to n).product

  test("For degree n, sat(P) + sat(!P) = C(n, N) + C(n-1, N) + ... + C(1, N)") {
    def oneTest(degree: Int, seed: Long): Unit = {
      assume(degree >= 1 && degree <= 10)

      changeDegree(degree)
      val p = randomFormula(seed)
      val x = p.getAllSolutions.size()
      val y = (!p).getAllSolutions.size()
      val z = (for (i <- 1 to degree) yield C(i, 10)).sum
      assert(x + y == z + 1, s"Failed at seed $seed, degree $degree: $p")
    }

    0 until nTest foreach {i =>
      println(s"Testing sat(P) + sat(!P) ${i + 1}/$nTest")
      val seed: Long = System.nanoTime()
      for (i <- 1 to 10) oneTest(i, seed)
    }
  }
}
