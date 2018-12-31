package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.{GlobalConfig, VERuntime}

object CLITestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  val main = "/Users/chupanw/Projects/Data/mutated-cli/bin/"
  val test = "/Users/chupanw/Projects/Data/mutated-cli/bin/"

//  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/mutated-cli.jar"
//  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/mutated-cli.jar"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test, config = Some("cli.conf"))
  VERuntime.classloader = Some(testLoader)

  val tests = List(
//    "org.apache.commons.clivbc.GnuParserTest"
//    "org.apache.commons.clivbc.BugsTest"  // todo: 13666 fails individually
//    "org.apache.commons.clivbc.OptionGroupTest"
    "org.apache.commons.clivbc.OptionBuilderTest"
//    "org.apache.commons.clivbc.ArgumentIsOptionTest"
//    "org.apache.commons.clivbc.PosixParserTest"
//    "org.apache.commons.clivbc.ApplicationTest"
//    "org.apache.commons.clivbc.OptionTest"
//    "org.apache.commons.clivbc.ParserTestCase"  // abstract class
//    "org.apache.commons.clivbc.UtilTest"
//    "org.apache.commons.clivbc.ValueTest"
//    "org.apache.commons.clivbc.BasicParserTest"
//    "org.apache.commons.clivbc.PatternOptionBuilderTest"  // fixme: method code too large
//    "org.apache.commons.clivbc.bug.BugCLI13Test"
//    "org.apache.commons.clivbc.bug.BugCLI18Test"
//    "org.apache.commons.clivbc.bug.BugCLI148Test"
//    "org.apache.commons.clivbc.bug.BugCLI133Test"
//    "org.apache.commons.clivbc.bug.BugCLI71Test"
//    "org.apache.commons.clivbc.bug.BugCLI162Test" // todo: two test cases were failing
//    "org.apache.commons.clivbc.OptionsTest" // todo: two test cases were failing
//    "org.apache.commons.clivbc.HelpFormatterTest"
//    "org.apache.commons.clivbc.ParseRequiredTest" // todo: two test cases were failing
//    "org.apache.commons.clivbc.CommandLineTest"
  )

  tests.foreach {x =>
    val testClass = TestClass(testLoader.loadClass(x))
    testClass.runTests()
  }

  if (GlobalConfig.printTestResults) VTestStat.printToConsole()
}
