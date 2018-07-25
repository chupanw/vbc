package edu.cmu.cs.vbc.testutils

import java.lang.annotation.Annotation
import java.lang.reflect.{InvocationTargetException, Method, Modifier}

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.{VERuntime, VException}

case class TestClass(c: Class[_]) {

//  require(checkAnnotations, s"Unsupported annotation in $c")

  val className: String = c.getName
  val before: Option[Method] = getMethodWithAnnotation(c, classOf[org.junit.Before])
  val after: Option[Method] = getMethodWithAnnotation(c, classOf[org.junit.After])

  /**
    * Find method with specific annotation.
    *
    * @note Assuming only one method has that annotation, such as @Before and @After
    * @param clazz Class to search for
    * @param annotation Annotation to search for
    * @return Option[Method]
    */
  def getMethodWithAnnotation(clazz: Class[_], annotation: Class[_ <: Annotation]): Option[Method] = {
    val x = clazz.getMethods.toList.filter(_.isAnnotationPresent(annotation))
    if (x.isEmpty) {
      if (clazz.getSuperclass != null)
        getMethodWithAnnotation(clazz.getSuperclass, annotation)
      else
        None
    }
    else if (x.length == 1)
      Some(x.head)
    else
      throw new RuntimeException(s"More than one method have the annotation: $annotation")
  }

  //todo: also search for superclasses
  def getTestCases: List[Method] = c.getMethods.toList.filter {x =>
    x.isAnnotationPresent(classOf[org.junit.Test])
  }

  def getAllMethods: List[Method] = c.getMethods.toList

  def createObject(): Any = {
    try {
      c.getConstructor(classOf[FeatureExpr]).newInstance(FeatureExprFactory.True)
    } catch {
      case t: Throwable =>
        System.err.println(s"Error creating test object for $c")
        t.printStackTrace()
    }
  }

  def isAbstract: Boolean = Modifier.isAbstract(c.getModifiers)

  // todo: rewrite the filtering part
  def runTests(): Unit = {
    if (getTestCases.isEmpty) return
    if (isAbstract) {
      TestStat.skipClass(className)
      return
    }
    if (skipClass(className)) {
      TestStat.skipClass(className)
      return
    }
    require(checkAnnotations, s"Unsupported annotation in $c")
    getTestCases.filter(isSkipped).foreach(_ => TestStat.skip(className))
    for (x <- getTestCases if !isSkipped(x)) {
//      executeOneTest(x, FeatureExprFactory.True)
      executeOnce(x, FeatureExprFactory.True)
    }
  }

  def executeOnce(x: Method, context: FeatureExpr): Unit = {
    System.out.println(s"[INFO] Executing ${x.getName} under $context")
    VERuntime.init()
    val testObject = createObject()
    before.map(_.invoke(testObject, context))
    try {
      x.invoke(testObject, context)
      if (VERuntime.hasVException)
        executeOnce(x, context.and(VERuntime.exceptionCtx.not()))
      TestStat.succeed(className)
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            if (!verifyException(t.e, x))
              System.out.println(t)
            if (!t.ctx.equivalentTo(context)) {
              val altCtx = context.and(t.ctx.not())
              executeOnce(x, altCtx)
            }
          case t =>
            if (!verifyException(t, x))
              throw new RuntimeException("Not a VException", t)
        }
      }
      case e =>
        throw new RuntimeException(s"Expecting InvocationTargetException, but found $e")
    }
    after.map(_.invoke(testObject, context))
  }

  def isSkipped(x: Method): Boolean = x.getName.contains("testSerial") || x.getName.toLowerCase().contains("serialization")

  def verifyException(t: Throwable, m: Method): Boolean = {
    val annotation = m.getAnnotation(classOf[org.junit.Test])
    assert(annotation != null, "No @Test annotation in method: " + m.getName)
    val expected = annotation.expected()
    if (!expected.isInstance(t)) {
      TestStat.fail(className)
      false
    } else {
      TestStat.succeed(className)
      true
    }
  }

  /**
    * Ensure we support all JUnit annotations in this test class
    *
    * We support the following annotations: @Test, @Before, @After
    *
    * @return true if only using supported annotations
    */
  def checkAnnotations: Boolean = {
    getAllMethods.forall {x =>
      x.getAnnotations.length == 0 ||
        x.getAnnotation(classOf[org.junit.Test]) != null ||
        x.getAnnotation(classOf[org.junit.Before]) != null ||
        x.getAnnotation(classOf[org.junit.After]) != null
    }
  }

  def skipClass(c: String): Boolean = c match {
    case "org.apache.commons.math3.analysis.integration.gauss.HermiteParametricTest" => true
    case "org.apache.commons.math3.analysis.integration.gauss.LegendreParametricTest" => true
    case "org.apache.commons.math3.analysis.integration.gauss.LegendreHighPrecisionParametricTest" => true
    case "org.apache.commons.math3.RetryRunnerTest" => true // @Retry
    case "org.apache.commons.math3.fitting.GaussianFitterTest" => true  // (same) vblock problem
    case "org.apache.commons.math3.util.FastMathStrictComparisonTest" => true
    case "org.apache.commons.math3.util.FastMathTestPerformance" => true  //@BeforeClass
    case _ => false
  }
}
