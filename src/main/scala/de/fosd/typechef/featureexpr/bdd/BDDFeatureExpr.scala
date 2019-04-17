package de.fosd.typechef.featureexpr.bdd

import java.util

import de.fosd.typechef.featureexpr.{FeatureExpr, SingleFeatureExpr}
import edu.cmu.cs.varex.mtbdd.{MTBDD, MTBDDFactory}
import edu.cmu.cs.varex.mtbdd.MTBDDFactory.boolOps

import scala.collection.JavaConversions._

class BDDFeatureExpr(val bdd: MTBDD[Boolean]) extends FeatureExpr {
  def isTautology(): Boolean = bdd == MTBDDFactory.TRUE
  def isContradiction(): Boolean = bdd == MTBDDFactory.FALSE
  def equivalentTo(that: FeatureExpr): Boolean = this.bdd == that.bdd
  def and(that: FeatureExpr): FeatureExpr = new BDDFeatureExpr(this.bdd.and(that.bdd))
  def &(that: FeatureExpr): FeatureExpr = and(that)
  def or(that: FeatureExpr): FeatureExpr = new BDDFeatureExpr(this.bdd.or(that.bdd))
  def |(that: FeatureExpr): FeatureExpr = or(that)
  def not(): FeatureExpr = new BDDFeatureExpr(this.bdd.not)
  def unary_!(): FeatureExpr = not()
  def implies(that: FeatureExpr): FeatureExpr = new BDDFeatureExpr(this.bdd.not.or(that.bdd))
  def isSatisfiable(): Boolean = !(this.bdd == MTBDDFactory.FALSE)

  override def getAllSolutions: java.util.List[String] = bdd.allSat
  override def getAllSolutionsScala: List[String] = bdd.allSat
  override def getOneSolution(): String = bdd.oneSat
  override def getAllSolutionsSorted: util.List[String] = bdd.allSatSorted
  override def getAllSolutionsSortedScala: List[String] = bdd.allSatSorted

  /* Unimplemented */
  def getSatisfiableAssignment(o1: Any, o2: Any, preferDisabledFeatures: Boolean): Option[(List[SingleFeatureExpr], List[SingleFeatureExpr])] = ???
  def collectDistinctFeatureObjects: Set[SingleFeatureExpr] = ???
  def simplify(fe: FeatureExpr): FeatureExpr = ???
  def toTextExpr: String = toString
  def evaluate(enabledOptions: Set[String]): Boolean = bdd.evaluate(enabledOptions)

  /* Debugging */
  override def toString: String = boolOps(bdd).toString
  override def equals(obj: Any): Boolean = obj match {
    case that: BDDFeatureExpr => this.bdd eq that.bdd
    case _ => false
  }
}
