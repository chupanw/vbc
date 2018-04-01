package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class PrevaylerTest extends FunSuite with DiffLaunchTestInfrastructure {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("NumberKeeper") {
    testMain(classOf[prevayler.RunNumberKeeper], configFile = Some("prevayler.conf"), compareTraceAgainstBruteForce = false)
  }

}
