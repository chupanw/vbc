package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class PrevaylerTest extends FunSuite with DiffLaunchTestInfrastructure {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("NumberKeeper") {
    testMain(classOf[prevayler.RunNumberKeeper], configFile = Some("prevayler.conf"), fm = fm, processTrace = ignoreFileOps)
  }

  /**
    * Ignore parts of the trace that are related to time or cleaning up caching files.
    */
  def ignoreFileOps(trace: List[(FeatureExpr, String)]): List[(FeatureExpr, String)] = {
    trace.filterNot(t => t._2.startsWith("INVK_VIRT: java/io/File;") ||
      t._2.startsWith("INVK_VIRT: org/prevayler/implementation/clock/MachineClock;advanceTo;") ||
      t._2.startsWith("INVK_VIRT: java/util/Date;getTime;()J"))
  }

  def fm(config: Map[String, Boolean]): Boolean = {
    for ((n, v) <- config) classOf[prevayler.RunNumberKeeper].getField(n).set(null, v)
    prevayler.RunNumberKeeper.isValid()
  }
}
