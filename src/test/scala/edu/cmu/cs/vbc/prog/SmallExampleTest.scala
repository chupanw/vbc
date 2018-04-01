package edu.cmu.cs.vbc.prog

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.FunSuite


/**
  * simple starter, checks for successful execution without crashes, no assertion checking
  */
class SmallExampleTest extends FunSuite with DiffLaunchTestInfrastructure {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  test("ifelse") {
    testMain(classOf[edu.cmu.cs.vbc.prog.IfElseExample])
  }

  test("test1") {
    testMain(classOf[edu.cmu.cs.vbc.prog.Test1])
  }


  test("invoke example") {
    testMain(classOf[edu.cmu.cs.vbc.prog.InvokeExample])
  }

  test("unbalanced stack example") {
    testMain(classOf[edu.cmu.cs.vbc.prog.UnbalancedStackExample])
  }


  test("static fields") {
    testMain(classOf[edu.cmu.cs.vbc.prog.StaticFieldsExample])
  }

  test("static fields with clinit") {
    testMain(classOf[edu.cmu.cs.vbc.prog.StaticFieldsWithClinit])
  }
test("conditional field assignment") {
    testMain(classOf[edu.cmu.cs.vbc.prog.FieldTest])
  }

  test("conditional method invocations") {
    testMain(classOf[edu.cmu.cs.vbc.prog.MethodTest])
  }

  test("reference array") {
    testMain(classOf[edu.cmu.cs.vbc.prog.RefArrayExample])
  }

  test("int array") {
    testMain(classOf[edu.cmu.cs.vbc.prog.IntArrayExample])
  }

  test("char array") {
    testMain(classOf[edu.cmu.cs.vbc.prog.CharArrayExample])
  }

  test("LinkedList") {
    testMain(classOf[LinkedListExample], configFile = Some("util.conf"))
  }

  test("Different ways of superclass initialization") {
    testMain(classOf[InitExample])
  }

  test("Exceptions") {
    testMain(classOf[ExceptionExample], compareTraceAgainstBruteForce = false)
  }

  test("StringBuilderExample") {
    testMain(classOf[StringBuilderExample])
  }

  test("ArrayListExample") {
    testMain(classOf[ArrayListExample], configFile = Some("util.conf"))
  }

  ignore("LongExample") {
    testMain(classOf[LongExample])
  }

  test("SwitchExample") {
    testMain(classOf[SwitchExample])
  }

  test("ExpandObjectArrayExample") {
    testMain(classOf[ExpandObjectArrayExample], configFile = Some("checkstyle.conf"))
  }

  test("TryCatchExample") {
    testMain(classOf[TryCatchExample], configFile = Some("default.conf"))
  }

  test("VBlockAnalysisTest") {
    testMain(classOf[VBlockAnalysisTest])
  }

  test("HashMapExample") {
    testMain(classOf[HashMapExample], configFile = Some("util.conf"))
  }

  test("HashSetExample") {
    testMain(classOf[HashSetExample], configFile = Some("util.conf"))
  }

  test("TernaryOperatorExample") {
    testMain(classOf[TernaryOperatorExample], configFile = Some("default.conf"))
  }

  test("MultiArrayExample") {
    testMain(classOf[MultiArrayExample], configFile = Some("default.conf"))
  }
}
