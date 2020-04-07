package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class QuEvalTest extends FunSuite with DiffLaunchTestInfrastructure {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  test("QuEval") {
    testMain(classOf[queval.queval.MainClass], fm = fm, configFile = Some("queval.conf"))
  }

  def fm(config: Map[String, Boolean]): Boolean = {
    for ((n, v) <- config) classOf[queval.queval.Configuration].getField(n).set(null, v)
    queval.queval.Configuration.validWithoutExceptions()
  }
}
