package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.VBCClassLoader
import edu.cmu.cs.vbc.config.{Settings, VERuntime}
import edu.cmu.cs.vbc.vbytecode.instructions.{InstrINIT_CONDITIONAL_FIELDS, InstrINIT_FIELD_TO_ONE}
import edu.cmu.cs.vbc.vbytecode.{Block, CFG, VBCClassNode, VBCMethodNode}

import scala.io.Source._
import scala.util.control.Breaks._

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
    val version = args(1)
    val (testClasses, failingTests) = parseRelevantTests(
      args(0) + "RelevantTests/" + version + ".txt")

    FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

    // turn this two into parameters
    val testClasspath = s"$repository$version/target/test-classes/"
    val mainClasspath = s"$repository$version/target/classes/"

    if (Settings.enablePerTestBlockCount) {
      val blockCountTestLoader = new VBCTestClassLoader(this.getClass.getClassLoader,
        mainClasspath,
        testClasspath,
        rewriter = replaceInitConditional,
        useModel = false,
        config = Some(configFile),
        reuseLifted = false)
      setClassLoader(blockCountTestLoader)
      testClasses.foreach { x =>
        new TestClass(blockCountTestLoader.loadClass(x)).countBlockForAllTests()
      }
      VTestStat.clear()
    }

    val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader,
      mainClasspath,
      testClasspath,
      useModel = false,
      config = Some(configFile),
      reuseLifted = false)
    setClassLoader(testLoader)
    if (Settings.fastMode) {
      breakable {
        for (x <- testClasses) {
          val failing = failingTests.filter(f => f.className == x).map(_.testName)
          val testClass = new TestClass(testLoader.loadClass(x), failing)
          val hasSolutionSoFar = testClass.runTests(isFastMode = true)
          if (!hasSolutionSoFar) break
        }
      }
      println("-------------------- Fast Mode Results --------------------")
      VTestStat.printToConsole()
      if (VTestStat.getOverallPassingCond.isSatisfiable()) return
      else {
        println(
          "-------------------- fast mode failed, going back to complete mode --------------------")
        VTestStat.clear()
      }
    }

    testClasses.foreach { x =>
      val failing = failingTests.filter(f => f.className == x).map(_.testName)
      val testClass = new TestClass(testLoader.loadClass(x), failing)
      testClass.runTests(isFastMode = false)
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
    val f = fromFile(file)
    val validLines = f.getLines().toList.filterNot(_.startsWith("//"))
    val testClasses = validLines.filterNot(_.startsWith("*"))
    val failingTests =
      validLines.filter(_.startsWith("*")).map(x => TestString(x.substring(1).trim))
    // prioritize test classes that have failing tests
    val orderedTestClasses = (failingTests.map(_.className) ::: testClasses).distinct
    (orderedTestClasses, failingTests)
  }

  def replaceInitConditional(m: VBCMethodNode, c: VBCClassNode): VBCMethodNode = {
    def processBlock(b: Block): Block = {
      Block(
        b.instr flatMap {
          case _: InstrINIT_CONDITIONAL_FIELDS => List(InstrINIT_FIELD_TO_ONE())
          case i => List(i)
        },
        b.exceptionHandlers,
        b.exceptions,
        shouldJumpBack = b.shouldJumpBack
      )
    }

    m.copy(body = CFG(m.body.blocks.map(processBlock)))
  }

  def setClassLoader(loader: ClassLoader): Unit = {
    VBCClassLoader.clearCache()
    Thread.currentThread().setContextClassLoader(loader)
    VERuntime.classloader = Some(loader)
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
