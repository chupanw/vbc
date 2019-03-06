package de.fosd.typechef.featureexpr

import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr
import edu.cmu.cs.varex.mtbdd.V

class SingleFeatureExpr(override val bdd: V[Boolean]) extends BDDFeatureExpr(bdd) {
  def feature: String = ???
}
