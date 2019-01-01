package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.VERuntime

object ValidatorTestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/commons-validator.jar"
  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/commons-validator.jar"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test, useModel = true)
  VERuntime.classloader = Some(testLoader)

  val tests = List("org.apache.commons.validator.routines.CurrencyValidatorTest")

  tests.foreach {x =>
    val testClass = new TestClass(testLoader.loadClass(x))
    testClass.runTests()
  }
}
