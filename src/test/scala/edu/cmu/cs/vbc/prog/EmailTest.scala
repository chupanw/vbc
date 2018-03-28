package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import edu.cmu.cs.vbc.prog.email.FeatureSwitches
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class EmailTest extends FunSuite with DiffLaunchTestInfrastructure {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  test("Email") {
    testMain(classOf[email.PL_Interface_impl], fm = featureModel, configFile = Some("email.conf"))
  }

  test("Email-Model") {
    testMain(classOf[email.PL_Interface_impl], fm = featureModel, configFile = Some("email-model.conf"), useModel = true)
  }

  def featureModel(config: Map[String, Boolean]): Boolean = {
    for ((n, v) <- config) classOf[FeatureSwitches].getField(n).set(null, v)
    FeatureSwitches.valid_product()
  }
}
