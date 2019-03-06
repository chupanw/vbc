package de.fosd.typechef.featureexpr

import java.io.Writer

import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr
import edu.cmu.cs.varex.mtbdd.{MTBDDFactory, V}

object FeatureExprFactory {
  val True: FeatureExpr = new BDDFeatureExpr(MTBDDFactory.TRUE)
  val False: FeatureExpr = new BDDFeatureExpr(MTBDDFactory.FALSE)
  def createDefinedExternal(name: String): SingleFeatureExpr = new SingleFeatureExpr(MTBDDFactory.feature(name))
  def setDefault(o: Any): Unit = {}
  val bdd: Any = new AnyRef()
}

class BDDFeatureExprFactory() {
  def save(writer: Writer, o: Any): Unit = ???
}