package de.fosd.typechef.featureexpr

import edu.cmu.cs.varex.mtbdd.V

trait FeatureExpr {
  val bdd: V[Boolean]
  def isTautology(): Boolean
  def isContradiction(): Boolean
  def equivalentTo(that: FeatureExpr): Boolean
  def and(that: FeatureExpr): FeatureExpr
  def or(that: FeatureExpr): FeatureExpr
  def not(): FeatureExpr
  def implies(that: FeatureExpr): FeatureExpr
  def isSatisfiable(): Boolean

  def getSatisfiableAssignment(o1: Any, o2: Any, preferDisabledFeatures: Boolean): Option[(List[SingleFeatureExpr], List[SingleFeatureExpr])]
  def collectDistinctFeatureObjects: Set[SingleFeatureExpr]
  def simplify(fe: FeatureExpr): FeatureExpr
  def toTextExpr: String
  def evaluate(o: Set[String]): Boolean
}
