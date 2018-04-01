package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class ElevatorTest extends FunSuite with DiffLaunchTestInfrastructure {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  test("Elevator") {
    testMain(classOf[elevator.PL_Interface_impl], fm = fm, configFile = Some("elevator.conf"))
  }

  def fm(config: Map[String, Boolean]): Boolean = {
    for ((n, v) <- config) classOf[edu.cmu.cs.vbc.prog.elevator.FeatureSwitches].getField(n).set(null, v)
    edu.cmu.cs.vbc.prog.elevator.FeatureSwitches.valid_product()
  }
}
