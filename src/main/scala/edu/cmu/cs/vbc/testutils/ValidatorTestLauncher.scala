package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.{GlobalConfig, VERuntime}

object ValidatorTestLoader {
//  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/commons-validator.jar"
//  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/commons-validator.jar"

  val main = "/Users/chupanw/Projects/Data/mutated-validator/target/classes/"
  val test = "/Users/chupanw/Projects/Data/mutated-validator/target/test-classes/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test, useModel = false, config = Some("validator.conf"), reuseLifted = false)
}

object ValidatorTests {
  val allTests = List(
//    "org.apache.commons.validator.routines.TimeValidatorTest"
//    "org.apache.commons.validator.routines.CurrencyValidatorTest"
//    "org.apache.commons.validator.routines.BigDecimalValidatorTest"
//    "org.apache.commons.validator.routines.AbstractNumberValidatorTest" // abstract
//    "org.apache.commons.validator.routines.BigIntegerValidatorTest"
//    "org.apache.commons.validator.routines.IBANValidatorTest"
//    "org.apache.commons.validator.routines.PercentValidatorTest"
//    "org.apache.commons.validator.routines.ISSNValidatorTest"
//    "org.apache.commons.validator.routines.LongValidatorTest"
//    "org.apache.commons.validator.routines.RegexValidatorTest"
//    "org.apache.commons.validator.routines.IntegerValidatorTest"
//    "org.apache.commons.validator.routines.UrlValidatorTest"
//    "org.apache.commons.validator.routines.CodeValidatorTest"
//    "org.apache.commons.validator.routines.CreditCardValidatorTest"
//    "org.apache.commons.validator.routines.ISBNValidatorTest"
//    "org.apache.commons.validator.routines.DomainValidatorTest"
//    "org.apache.commons.validator.routines.EmailValidatorTest"
//    "org.apache.commons.validator.routines.CalendarValidatorTest"
//    "org.apache.commons.validator.routines.DoubleValidatorTest"
//    "org.apache.commons.validator.routines.FloatValidatorTest"
//    "org.apache.commons.validator.routines.AbstractCalendarValidatorTest" // abstract
//    "org.apache.commons.validator.routines.checkdigit.ISBNCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.IBANCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.SedolCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.ISINCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.ModulusTenABACheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.ModulusTenCUSIPCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.AbstractCheckDigitTest" // abstract
//    "org.apache.commons.validator.routines.checkdigit.ISSNCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.VerhoeffCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.ModulusTenEAN13CheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.ModulusTenLuhnCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.CUSIPCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.ModulusTenSedolCheckDigitTest"
//    "org.apache.commons.validator.routines.checkdigit.LuhnCheckDigitTest",
//    "org.apache.commons.validator.routines.checkdigit.ISBN10CheckDigitTest",
//    "org.apache.commons.validator.routines.checkdigit.EAN13CheckDigitTest",
//    "org.apache.commons.validator.routines.checkdigit.ABANumberCheckDigitTest"
//    "org.apache.commons.validator.routines.DateValidatorTest",
//    "org.apache.commons.validator.routines.InetAddressValidatorTest"
//    "org.apache.commons.validator.routines.ShortValidatorTest",
//    "org.apache.commons.validator.routines.ByteValidatorTest"
    "org.apache.commons.validator.ParameterTest"
//    "org.apache.commons.validator.MultipleTest"
//    "org.apache.commons.validator.EmailTest"
//    "org.apache.commons.validator.ExceptionTest"
//    "org.apache.commons.validator.ShortTest"
//    "org.apache.commons.validator.custom.CustomValidatorResources"
//    "org.apache.commons.validator.RetrieveFormTest"
//    "org.apache.commons.validator.VarTest"
//    "org.apache.commons.validator.MultipleConfigFilesTest"
//    "org.apache.commons.validator.ParameterValidatorImpl"
//    "org.apache.commons.validator.TypeBean"
//    "org.apache.commons.validator.ValueBean"
//    "org.apache.commons.validator.RequiredIfTest"
//    "org.apache.commons.validator.util.FlagsTest"
//    "org.apache.commons.validator.AbstractCommonTest"
//    "org.apache.commons.validator.LongTest"
//    "org.apache.commons.validator.ExtensionTest"
//    "org.apache.commons.validator.ValidatorTest"
//    "org.apache.commons.validator.GenericTypeValidatorTest"
//    "org.apache.commons.validator.DoubleTest"
//    "org.apache.commons.validator.CreditCardValidatorTest"
//    "org.apache.commons.validator.UrlTest"
//    "org.apache.commons.validator.RequiredNameTest"
//    "org.apache.commons.validator.ISBNValidatorTest"
//    "org.apache.commons.validator.CustomValidatorResourcesTest"
//    "org.apache.commons.validator.LocaleTest"
//    "org.apache.commons.validator.GenericTypeValidatorImpl"
//    "org.apache.commons.validator.ValidatorResourcesTest"
//    "org.apache.commons.validator.GenericValidatorImpl"
//    "org.apache.commons.validator.ResultPair"
//    "org.apache.commons.validator.DateTest"
//    "org.apache.commons.validator.EntityImportTest"
//    "org.apache.commons.validator.AbstractNumberTest"
//    "org.apache.commons.validator.IntegerTest"
//    "org.apache.commons.validator.ByteTest"
//    "org.apache.commons.validator.FieldTest"
//    "org.apache.commons.validator.FloatTest"
//    "org.apache.commons.validator.ValidatorResultsTest"
//    "org.apache.commons.validator.NameBean"
//    "org.apache.commons.validator.GenericValidatorTest"
  )
}

object ValidatorTestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  VERuntime.classloader = Some(ValidatorTestLoader.testLoader)
  Thread.currentThread().setContextClassLoader(ValidatorTestLoader.testLoader)

  ValidatorTests.allTests.foreach {x =>
    val testClass = new TestClass(ValidatorTestLoader.testLoader.loadClass(x))
    testClass.runTests()
  }

  if (GlobalConfig.printTestResults) VTestStat.printToConsole()
}
