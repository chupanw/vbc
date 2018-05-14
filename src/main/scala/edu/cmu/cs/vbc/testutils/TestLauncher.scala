package edu.cmu.cs.vbc.testutils

import java.lang.reflect.Method

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.vbytecode.{MethodDesc, MethodName}

/**
  * Launch JUnit test cases
  *
  * For example, this can be used to launch test cases from Defects4J
  */
object TestLauncher extends App {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  // turn this two into parameters
  val testClasspath = "/Users/chupanw/Projects/Data/defects4j-math/1f/target/test-classes/"
  val mainClasspath = "/Users/chupanw/Projects/Data/defects4j-math/1f/target/classes/"

  val testloader = new VBCTestClassLoader(this.getClass.getClassLoader, mainClasspath, testClasspath)

//  classloader.findTestClassFiles().map(println)

//  val testClassName = "org.apache.commons.math3.util.ArithmeticUtilsTest"
//  val testClassName = "org.apache.commons.math3.util.BigRealTest"
//  val testClassName = "org.apache.commons.math3.primes.PrimesTest"
val testClassName = "org.apache.commons.math3.analysis.function.LogitTest"

  val testClass = testloader.loadClass(testClassName)
  val testObject = testClass.getConstructor(classOf[FeatureExpr]).newInstance(FeatureExprFactory.True)
  val testMethods = testloader.getTestMethods(testClassName)

  for (x <- testMethods) {
    val mName = MethodName(x.getName).rename(MethodDesc("()V"))

    try {
      val mtd: Method = testClass.getMethod(mName, classOf[FeatureExpr])
      mtd.invoke(testObject, FeatureExprFactory.True)
    } catch {
      case e: NoSuchMethodException => println("method not found, aborting...")
    }
  }
}
