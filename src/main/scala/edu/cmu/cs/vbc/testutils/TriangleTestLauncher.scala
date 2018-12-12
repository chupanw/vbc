package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.VERuntime

object TriangleTestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  val triangleMain = "/Users/chupanw/Projects/vbc/target/scala-2.11/test-classes/"
  val triangleTest = "/Users/chupanw/Projects/vbc/target/scala-2.11/test-classes/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, triangleMain, triangleTest)
  VERuntime.classloader = Some(testLoader)

  val tests = List("edu.cmu.cs.vbc.prog.triangle.Triangle_ESTest_improved")

  tests.foreach {x =>
    val testClass = TestClass(testLoader.loadClass(x))
    testClass.runTests()
  }
}
