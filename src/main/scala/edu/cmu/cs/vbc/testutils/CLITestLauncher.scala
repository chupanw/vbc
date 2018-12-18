package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.VERuntime

object CLITestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/mutated-cli.jar"
  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/mutated-cli.jar"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test)
  VERuntime.classloader = Some(testLoader)

  val tests = List("org.apache.commons.cli.ValuesTest")

  tests.foreach {x =>
    val testClass = TestClass(testLoader.loadClass(x))
    testClass.runTests()
  }
}
