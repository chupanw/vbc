package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.{GlobalConfig, VERuntime}

object ValidatorTestLoader {
//  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/commons-validator.jar"
//  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/commons-validator.jar"

  val main = "/Users/chupanw/Projects/Data/mutated-validator/target/classes/"
  val test = "/Users/chupanw/Projects/Data/mutated-validator/target/test-classes/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test, useModel = false, config = Some("validator.conf"), reuseLifted = true)
}

object ValidatorTests {
  val allTests = List(
    "org.apache.commons.validator.routines.TimeValidatorTest",
    "org.apache.commons.validator.routines.CurrencyValidatorTest",
    "org.apache.commons.validator.routines.BigDecimalValidatorTest",
    "org.apache.commons.validator.routines.BigIntegerValidatorTest",
    "org.apache.commons.validator.routines.PercentValidatorTest",
    "org.apache.commons.validator.routines.LongValidatorTest",
    "org.apache.commons.validator.routines.RegexValidatorTest",
    "org.apache.commons.validator.routines.IntegerValidatorTest",
    "org.apache.commons.validator.routines.CodeValidatorTest",
    "org.apache.commons.validator.routines.DomainValidatorTest",
    "org.apache.commons.validator.routines.EmailValidatorTest",
    "org.apache.commons.validator.routines.DoubleValidatorTest",
    "org.apache.commons.validator.routines.FloatValidatorTest",
    "org.apache.commons.validator.routines.InetAddressValidatorTest",
    "org.apache.commons.validator.routines.ShortValidatorTest",
    "org.apache.commons.validator.routines.ByteValidatorTest",
    "org.apache.commons.validator.ParameterTest",
    "org.apache.commons.validator.MultipleTest",
    "org.apache.commons.validator.EmailTest",
    "org.apache.commons.validator.ExceptionTest",
    "org.apache.commons.validator.ShortTest",
    "org.apache.commons.validator.VarTest",
    "org.apache.commons.validator.ParameterValidatorImpl",
    "org.apache.commons.validator.TypeBean",
    "org.apache.commons.validator.ValueBean",
    "org.apache.commons.validator.RequiredIfTest",
    "org.apache.commons.validator.util.FlagsTest",
    "org.apache.commons.validator.LongTest",
    "org.apache.commons.validator.ExtensionTest",
    "org.apache.commons.validator.ValidatorTest",
    "org.apache.commons.validator.GenericTypeValidatorTest",
    "org.apache.commons.validator.DoubleTest",
    "org.apache.commons.validator.CreditCardValidatorTest",
    "org.apache.commons.validator.RequiredNameTest",
    "org.apache.commons.validator.CustomValidatorResourcesTest",
    "org.apache.commons.validator.GenericTypeValidatorImpl",
    "org.apache.commons.validator.ValidatorResourcesTest",
    "org.apache.commons.validator.GenericValidatorImpl",
    "org.apache.commons.validator.ResultPair",
    "org.apache.commons.validator.DateTest",
    "org.apache.commons.validator.EntityImportTest",
    "org.apache.commons.validator.IntegerTest",
    "org.apache.commons.validator.ByteTest",
    "org.apache.commons.validator.FieldTest",
    "org.apache.commons.validator.FloatTest",
    "org.apache.commons.validator.ValidatorResultsTest",
    "org.apache.commons.validator.NameBean",
    "org.apache.commons.validator.GenericValidatorTest",
        "org.apache.commons.validator.RetrieveFormTest", // fixme: concurrent modifications
        "org.apache.commons.validator.routines.IBANValidatorTest", // fixme: out of memory
        "org.apache.commons.validator.routines.ISSNValidatorTest", // fixme: out of memory
        "org.apache.commons.validator.routines.UrlValidatorTest",  // fixme: potential infinite loop
        "org.apache.commons.validator.routines.CreditCardValidatorTest", // fixme: out of memory
        "org.apache.commons.validator.routines.ISBNValidatorTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.CalendarValidatorTest", // fixme: memory
        "org.apache.commons.validator.routines.checkdigit.ISBNCheckDigitTest", // fixme: slow, memory
        "org.apache.commons.validator.routines.checkdigit.IBANCheckDigitTest", // fixed by disabling a few mutants, fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.SedolCheckDigitTest",  // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ISINCheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ModulusTenABACheckDigitTest",  // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ModulusTenCUSIPCheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ISSNCheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.VerhoeffCheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ModulusTenEAN13CheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ModulusTenLuhnCheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.CUSIPCheckDigitTest",  // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ModulusTenSedolCheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.LuhnCheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ISBN10CheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.EAN13CheckDigitTest", // fixme: might be expensive
        "org.apache.commons.validator.routines.checkdigit.ABANumberCheckDigitTest",  // fixme: might be expensive
        "org.apache.commons.validator.routines.DateValidatorTest", // fixme: out of memory
        "org.apache.commons.validator.MultipleConfigFilesTest",  // fixme: stackoverflow
        "org.apache.commons.validator.UrlTest",  // fixme: infinite loop
        "org.apache.commons.validator.ISBNValidatorTest",  // fixme:
        "org.apache.commons.validator.LocaleTest", // fixme: one test failed
        "org.apache.commons.validator.routines.AbstractNumberValidatorTest", // abstract
        "org.apache.commons.validator.routines.AbstractCalendarValidatorTest", // abstract
        "org.apache.commons.validator.routines.checkdigit.AbstractCheckDigitTest", // abstract
        "org.apache.commons.validator.AbstractNumberTest",
        "org.apache.commons.validator.custom.CustomValidatorResources",
  "org.apache.commons.validator.AbstractCommonTest"
  )
}

object ValidatorTestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  VERuntime.classloader = Some(ValidatorTestLoader.testLoader)
  VERuntime.loadFeatures("validator.txt")
  Thread.currentThread().setContextClassLoader(ValidatorTestLoader.testLoader)

  ValidatorTests.allTests.foreach {x =>
    val testClass = new TestClass(ValidatorTestLoader.testLoader.loadClass(x))
    testClass.runTests()
  }

  if (GlobalConfig.printTestResults) VTestStat.printToConsole()
}
