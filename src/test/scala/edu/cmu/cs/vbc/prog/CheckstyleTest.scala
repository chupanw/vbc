package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class CheckstyleTest extends FunSuite with DiffLaunchTestInfrastructure {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  ignore("Checkstyle") {
    testMain(classOf[checkstyle.Main], configFile = Some("checkstyle.conf"), compareTraceAgainstBruteForce = false)
  }
}
