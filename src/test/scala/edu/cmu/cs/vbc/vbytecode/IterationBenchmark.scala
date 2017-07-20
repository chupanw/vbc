package edu.cmu.cs.vbc.vbytecode

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite

class IterationBenchmark extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("Iteration Benchmark") {
    testMain(classOf[edu.cmu.cs.vbc.prog.IterationBenchmark])
  }
}
