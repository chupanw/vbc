package de.fosd.typechef.featureexpr

import edu.cmu.cs.varex.mtbdd.MTBDD

trait FeatureExpr extends Serializable {
  val bdd: MTBDD[Boolean]
  def isTautology(): Boolean
  def isContradiction(): Boolean
  def equivalentTo(that: FeatureExpr): Boolean
  def and(that: FeatureExpr): FeatureExpr
  def &(that: FeatureExpr): FeatureExpr
  def |(that: FeatureExpr): FeatureExpr
  def unary_!(): FeatureExpr
  def or(that: FeatureExpr): FeatureExpr
  def not(): FeatureExpr
  def implies(that: FeatureExpr): FeatureExpr
  def isSatisfiable(): Boolean
  def getAllSolutions: java.util.List[String]
  def getAllSolutionsSorted: java.util.List[String]
  def getAllSolutionsScala: List[String]
  def getAllSolutionsSortedScala: List[String]
  def getRelevantOptions: List[String]
  def getOneSolution(): String

  def getSatisfiableAssignment(o1: Any, o2: Any, preferDisabledFeatures: Boolean): Option[(List[SingleFeatureExpr], List[SingleFeatureExpr])]
  def collectDistinctFeatureObjects: Set[SingleFeatureExpr]
  def simplify(fe: FeatureExpr): FeatureExpr
  def toTextExpr: String
  def evaluate(enabledOptions: Set[String]): Boolean
}
