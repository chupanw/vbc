package edu.cmu.cs.vbc.testutils

import java.io.File

import de.fosd.typechef.featureexpr.{FeatureExprFactory, FeatureExprParser}
import edu.cmu.cs.varex.V
import edu.cmu.cs.vbc.{GlobalConfig, VERuntime}

object CLITestLoader {
  val main = "/Users/chupanw/Projects/Data/mutated-cli/bin/"
  val test = "/Users/chupanw/Projects/Data/mutated-cli/bin/"

  //  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/mutated-cli.jar"
  //  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/mutated-cli.jar"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test, config = Some("cli.conf"), useModel = false, reuseLifted = true)
}

object CLITests {
  val allTests = List(
    //    "org.apache.commons.clivbc.GnuParserTest"
    //    "org.apache.commons.clivbc.BugsTest"  // note: 13666 fails individually
    //    "org.apache.commons.clivbc.OptionGroupTest"
    //    "org.apache.commons.clivbc.OptionBuilderTest"
    //    "org.apache.commons.clivbc.ArgumentIsOptionTest"
//        "org.apache.commons.clivbc.PosixParserTest"
        "org.apache.commons.clivbc.ApplicationTest" // fixme: testMan failed because of memory
//        "org.apache.commons.clivbc.OptionTest"
//        "org.apache.commons.clivbc.ParserTestCase"  // abstract class
//        "org.apache.commons.clivbc.UtilTest"
//        "org.apache.commons.clivbc.ValueTest"
//        "org.apache.commons.clivbc.BasicParserTest"
//        "org.apache.commons.clivbc.PatternOptionBuilderTest"  // fixme: method code too large and one originally failing test
//        "org.apache.commons.clivbc.bug.BugCLI13Test"
//        "org.apache.commons.clivbc.bug.BugCLI18Test"
//        "org.apache.commons.clivbc.bug.BugCLI148Test"
//        "org.apache.commons.clivbc.bug.BugCLI133Test"
//        "org.apache.commons.clivbc.bug.BugCLI71Test"
//        "org.apache.commons.clivbc.bug.BugCLI162Test" // todo: two test cases were failing
//        "org.apache.commons.clivbc.OptionsTest" // todo: two test cases were failing
//        "org.apache.commons.clivbc.HelpFormatterTest"
//        "org.apache.commons.clivbc.ParseRequiredTest" // todo: two test cases were failing
//        "org.apache.commons.clivbc.CommandLineTest"
  )
}

object CLITestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  VERuntime.classloader = Some(CLITestLoader.testLoader)

  CLITests.allTests.foreach {x =>
    val testClass = new TestClass(CLITestLoader.testLoader.loadClass(x))
    testClass.runTests()
  }

  if (GlobalConfig.printTestResults) VTestStat.printToConsole()
}

/**
  * Execute a specified test case
  *
  * This will get called from the commandline
  */
object CLIForkTestCaseLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  VERuntime.classloader = Some(CLITestLoader.testLoader)
  val cName = args(0)
  val mName = args(1)
  val t = new ForkTestCase(CLITestLoader.testLoader.loadClass(cName), mName)
  t.run()
}

/**
  * Execute all test cases of CLI
  *
  * Using IntelliJ to build the whole project as one jar, so that we don't need to
  * call sbt.
  */
object CLIForkTestLauncher extends App {
  import scala.sys.process._
  val jarFile = "out/artifacts/all_jar/vbc.jar"

  CLITests.allTests.foreach(c => {
    val clazz = new TestClass(CLITestLoader.testLoader.loadClass(c))
    clazz.getTestCases.foreach(m => {
      Process(Seq("java", "-Xmx16g", "-cp", jarFile, "edu.cmu.cs.vbc.testutils.CLIForkTestCaseLauncher", c, m.getName)).lineStream.foreach(println)
    })
  })

  //  printSolutions()

  /**
    * Too slow
    */
  def printSolutions(): Unit = {
    val dir = new File("passingCond/")
    assert(dir.exists(), "please create the passingCond directory")
    var overallPassing = FeatureExprFactory.True
    val parser = new FeatureExprParser(FeatureExprFactory.bdd)
    for (f <- dir.listFiles()) {
      overallPassing = overallPassing.and(parser.parseFile(f))
    }
    println(V.getAllLowDegreeSolutions(overallPassing))
  }
}
