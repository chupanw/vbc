package de.fosd.typechef.featureexpr.bdd

import de.fosd.typechef.featureexpr.{FeatureExpr, SingleFeatureExpr}
import edu.cmu.cs.varex.mtbdd.{MTBDDFactory, MTBDD}
import edu.cmu.cs.varex.mtbdd.MTBDDFactory.boolOps

class BDDFeatureExpr(val bdd: MTBDD[Boolean]) extends FeatureExpr {
  def isTautology(): Boolean = bdd == MTBDDFactory.TRUE
  def isContradiction(): Boolean = bdd == MTBDDFactory.FALSE
  def equivalentTo(that: FeatureExpr): Boolean = this.bdd == that.bdd
  def and(that: FeatureExpr): FeatureExpr = new BDDFeatureExpr(this.bdd.and(that.bdd))
  def or(that: FeatureExpr): FeatureExpr = new BDDFeatureExpr(this.bdd.or(that.bdd))
  def not(): FeatureExpr = new BDDFeatureExpr(this.bdd.not)
  def implies(that: FeatureExpr): FeatureExpr = new BDDFeatureExpr(this.bdd.not.or(that.bdd))
  def isSatisfiable(): Boolean = !(this.bdd == MTBDDFactory.FALSE)

  /*
   * Unimplemented
   */
  def getSatisfiableAssignment(o1: Any, o2: Any, preferDisabledFeatures: Boolean): Option[(List[SingleFeatureExpr], List[SingleFeatureExpr])] = ???
  def collectDistinctFeatureObjects: Set[SingleFeatureExpr] = ???
  def simplify(fe: FeatureExpr): FeatureExpr = ???
  def toTextExpr: String = "<NOT IMPLEMENTED>"
  def evaluate(o: Set[String]): Boolean = ???

  override def toString: String = "Unknown context"
}
