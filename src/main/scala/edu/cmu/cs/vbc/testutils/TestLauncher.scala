package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.config.{Settings, VERuntime}

import scala.io.Source._

/**
  * Launch JUnit test cases
  *
  * For example, this can be used to launch test cases from Defects4J
  */
abstract class TestLauncher {

  val configFile: String

  def main(args: Array[String]): Unit = {
    Settings.printSettings()
    assume(args.length == 2, s"Wrong number of arguments: ${args.length}")
    assume(args(0).endsWith("/"), s"Not a folder: ${args(0)}")

    val repository = args(0)
    val version    = args(1)
    val (testClasses, failingTests) = parseRelevantTests(
      args(0) + "RelevantTests/" + version + ".txt")

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

    testClasses.foreach { x =>
      val failing   = failingTests.filter(f => f.className == x).map(_.testName)
      val testClass = new TestClass(testLoader.loadClass(x), failing)
      testClass.runTests()
    }

    if (Settings.printTestResults) VTestStat.printToConsole()
    //  VTestStat.toMarkdown(version, "org.apache.commons.math3.")
  }

  /**
    * Parse the text file that specifies relevant test cases.
    *
    * There can be an optional section at the end of the file that specifies which tests were failing. We prioritize
    * these tests to save time.
    *
    * @param file Full path to the RelevantTests file.
    *
    * @return A list of test classes, and optionally a list of previously failing test cases (empty List if not specified)
    */
  def parseRelevantTests(file: String): (List[String], List[TestString]) = {
    val f           = fromFile(file)
    val validLines  = f.getLines().toList.filterNot(_.startsWith("//"))
    val testClasses = validLines.filterNot(_.startsWith("*"))
    val failingTests =
      validLines.filter(_.startsWith("*")).map(x => TestString(x.substring(1).trim))
    // prioritize test classes that have failing tests
    val orderedTestClasses = (failingTests.map(_.className) ::: testClasses).distinct
    (orderedTestClasses, failingTests)
  }

}

case class TestString(s: String) {
  val (className, testName) = {
    require(s.contains("::"),
            s"Malformed test case string: $s, use :: to separate class and method")
    val idx = s.indexOf("::")
    (s.substring(0, idx), s.substring(idx + 2))
  }
}

object IntroClassLauncher extends TestLauncher {
  override val configFile: String = "intro-class.conf"
}

object ApacheMathLauncher extends TestLauncher {
  override val configFile: String = "apache-math.conf"
}
