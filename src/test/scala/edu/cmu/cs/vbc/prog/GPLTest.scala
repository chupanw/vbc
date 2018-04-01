package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class GPLTest extends FunSuite with DiffLaunchTestInfrastructure{
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  test("GPL") {
    testMain(classOf[gpl.Main], fm = fm, configFile = Some("gpl.conf"))
  }

  def fm(config: Map[String, Boolean]): Boolean = {
    for ((n, v) <- config) classOf[edu.cmu.cs.vbc.prog.gpl.Main].getField(n).set(null, v)
    edu.cmu.cs.vbc.prog.gpl.Main.valid()
  }
}
