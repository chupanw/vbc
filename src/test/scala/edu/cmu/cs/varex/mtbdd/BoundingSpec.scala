package edu.cmu.cs.varex.mtbdd

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.config.Settings
import org.scalatest.{FlatSpec, FunSuite}

class BoundingSpec extends FlatSpec {

  val A: FeatureExpr = FeatureExprFactory.createDefinedExternal("A")
  val B: FeatureExpr = FeatureExprFactory.createDefinedExternal("B")
  val C: FeatureExpr = FeatureExprFactory.createDefinedExternal("C")
  val D: FeatureExpr = FeatureExprFactory.createDefinedExternal("D")

  def changeDegree(d: Int): Unit = {
    Settings.maxInteractionDegree = d
    MTBDDFactory.clearCache()
  }

  "Bounding with degree 1" should "work for some trivial cases" in {
    val False = FeatureExprFactory.False
    changeDegree(1)
    assert((A & B).bdd eq False.bdd)
    assert((A | B).bdd eq ((A & !B) | (!A & B)).bdd)

    val x1 = (A & B) | (!A & B)
    assert(x1.bdd eq B.bdd)

    val x2 = A | B | C
    val y2 = (A & !B & !C) | (!A & B & !C) | (!A & !B & C)
    assert(x2.bdd eq y2.bdd)
  }

  "Bounding with degree 2" should "turn (A & C & !D) & (A & B) into FALSE" in {
    changeDegree(2)
    val left = A.and(C).and(D.not()).and(A.and(B))
    assert(left.bdd eq FeatureExprFactory.False.bdd)
  }

  it should "turn (A & B) | (A & C) | (B & C) into (A & B & !C) | (A & C & !B) | (B & C & !A)" in {
    changeDegree(2)
    val left = A.and(B).or(A.and(C)).or(B.and(C))
    val right =  A.and(B).and(C.not()).or(A.and(C).and(B.not())).or(B.and(C).and(A.not()))
    assert(left.bdd eq right.bdd)
  }

  it should "turn !(!A|!B|!C) into FALSE" in {
    changeDegree(2)
    val left = A.not().or(B.not()).or(C.not()).not()
    assert(left.bdd eq MTBDDFactory.FALSE)
  }

  it should "yield 2 solutions for (A&C)|(A&!C&D)" in {
    changeDegree(2)
    val f = (A & C) | (A & !C & D)
    assert(f.getAllSolutions.size() == 2) // {A, C}, {A, D}
  }

  "Bounding with degree 3" should "yield 5 solutions for (A&C)|(A&!C&D)" in {
    changeDegree(3)
    val f = (A & C) | (A & !C & D)
    assert(f.getAllSolutions.size() == 5) // {A, B, C}, {A, C, D}, {A, C}, {A, D}, {A, B, D}
  }

  "Bounding with degree 4" should "yield 5 solutions for (A&C)|(A&!C&D)" in {
    changeDegree(4)
    val f = (A & C) | (A & !C & D)
    assert(f.getAllSolutions.size() == 6) // {A, B, C}, {A, C, D}, {A, C}, {A, D}, {A, B, D}, {A, B, C, D}
  }

  "Bounding" should "yield canonical BDDs" in {
    changeDegree(1)
    val x = !B
    val y = A | (!A & !B)
    assert(x.bdd eq y.bdd)
  }
}

