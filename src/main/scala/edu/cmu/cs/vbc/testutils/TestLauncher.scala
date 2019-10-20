package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.{GlobalConfig, VERuntime}

import scala.io.Source._

/**
  * Launch JUnit test cases
  *
  * For example, this can be used to launch test cases from Defects4J
  */
trait TestLauncher {

  val configFile = "intro-class.conf"

  def main(args: Array[String]): Unit = {
    assume(args.length == 2, s"Wrong number of arguments: ${args.length}")
    assume(args(0).endsWith("/"), s"Not a folder: ${args(0)}")

    val repository = args(0)
    val version = args(1)
    val relevantTests = fromFile(args(0) + "RelevantTests/" + version + ".txt")

    FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

    // turn this two into parameters
    val testClasspath = s"$repository$version/target/test-classes/"
    val mainClasspath = s"$repository$version/target/classes/"

    val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader,
                                            mainClasspath,
                                            testClasspath,
                                            useModel = false,
                                            config = Some(configFile))
    VERuntime.classloader = Some(testLoader)

    val tests = relevantTests.getLines().toList.filterNot(_.startsWith("//"))
    tests.foreach { x =>
      val testClass = new TestClass(testLoader.loadClass(x))
      testClass.runTests()
    }

    if (GlobalConfig.printTestResults) VTestStat.printToConsole()
    //  VTestStat.toMarkdown(version, "org.apache.commons.math3.")
  }
}

object IntroClassLauncher extends TestLauncher

object ApacheMathLauncher extends TestLauncher {
  override val configFile: String = "apache-math.conf"
}
