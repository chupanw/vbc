package edu.cmu.cs.vbc.testutils

import java.lang.annotation.Annotation
import java.lang.reflect.{Field, InvocationTargetException, Method, Modifier}

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.varex.{One, V}
import edu.cmu.cs.vbc.VException
import edu.cmu.cs.vbc.config.{Settings, VERuntime}
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.{Parameter, Parameters}
import org.slf4j.LoggerFactory

/**
  * JUnit 4 support:
  *   Test
  *   Ignore
  *   RunWith(Parameterized.class)
  *   Parameters
  *   Before
  *   After
  *
  * JUnit 3 support:
  *   methods starting with test
  *   setUp
  *   tearDown
  */
class TestClass(c: Class[_], failingTests: List[String] = Nil, excludeTests: List[String] = Nil) {

//  require(checkAnnotations, s"Unsupported annotation in $c")

  val isJUnit3: Boolean        = isSubclassOfTestCase(c)
  val isParameterized: Boolean = isParameterizedTest(c)
  val logger = LoggerFactory.getLogger("varexc")

  def isParameterizedTest(c: Class[_]): Boolean =
    c.isAnnotationPresent(classOf[RunWith]) && c
      .getAnnotation(classOf[RunWith])
      .value() == classOf[Parameterized]

  def isSubclassOfTestCase(c: Class[_]): Boolean = {
    if (c.getName == "java.lang.Object") false
    else if (c.getSuperclass.getName == "junit.framework.TestCase") true
    else isSubclassOfTestCase(c.getSuperclass)
  }

  val className: String = c.getName
  val before: Option[Method] =
    if (isJUnit3)
      getMethodWithName(c, "setUp____V")
    else
      getMethodWithAnnotation(c, classOf[org.junit.Before])
  val after: Option[Method] =
    if (isJUnit3)
      getMethodWithName(c, "tearDown____V")
    else
      getMethodWithAnnotation(c, classOf[org.junit.After])

  /**
    * Find method with specific annotation.
    *
    * @note Assuming only one method has that annotation, such as @Before and @After
    * @param clazz Class to search for
    * @param annotation Annotation to search for
    * @return Option[Method]
    */
  def getMethodWithAnnotation(clazz: Class[_],
                              annotation: Class[_ <: Annotation]): Option[Method] = {
    val x = clazz.getMethods.toList.filter(_.isAnnotationPresent(annotation))
    if (x.isEmpty) {
      if (clazz.getSuperclass != null)
        getMethodWithAnnotation(clazz.getSuperclass, annotation)
      else
        None
    } else if (x.length == 1)
      Some(x.head)
    else
      throw new RuntimeException(s"More than one method have the annotation: $annotation")
  }

  def getMethodWithName(clazz: Class[_], name: String): Option[Method] = {
    val x = clazz.getDeclaredMethods.toList.filter(x => x.getName == name)
    if (x.isEmpty) {
      if (clazz.getSuperclass != null)
        getMethodWithName(clazz.getSuperclass, name)
      else
        None
    } else if (x.length == 1) {
      x.head.setAccessible(true)
      Some(x.head)
    } else
      throw new RuntimeException(s"More than one method have the name: $name")
  }

  def getTestCases: List[Method] = {
    val allTests = {
      if (isJUnit3)
        c.getMethods.toList.filter(x => x.getName.startsWith("test") && x.getParameterCount == 1)
      else {
        val allTests = c.getMethods.toList.filter { x => x.isAnnotationPresent(classOf[org.junit.Test]) }
        allTests.filterNot(x => x.isAnnotationPresent(classOf[org.junit.Ignore]))
      }
    }
    allTests.filterNot(x => excludeTests.exists(y => x.getName.startsWith(y + "__")))
  }

  def getOrderedTestCases: List[Method] = {
    val tests       = getTestCases.sortBy(_.getName)
    val prioritized = tests.filter(t => failingTests.exists(f => t.getName.startsWith(f + "__")))
    // (prioritized ::: tests).distinct
    prioritized
  }

  // only public
  def getAllMethods: List[Method] = c.getMethods.toList
  // only public
  def getAllFields: List[Field] = c.getFields.toList

  def createObject(params: Option[Array[V[_]]], ctx: FeatureExpr): Any = {
    try {
      if (params.nonEmpty) createParameterizedObject(params.get)
      else if (isJUnit3) createJUnit3Object()
      else createJUnit4Object(ctx)
    } catch {
      case t: Throwable =>
        val msg = s"Error creating test object for ${c}"
        printlnAndLog(msg)
        printlnAndLog(t.toString)
    }
  }

  def printlnAndLog(msg: String, err: Boolean = false): Unit = {
    if (err) System.err.println(msg)
    else println(msg)
    logger.debug(msg + "\n")
  }

  def createJUnit3Object(): Any = {
    try {
      c.getConstructor(classOf[V[_]], classOf[FeatureExpr], classOf[String])
        .newInstance(V.one(FeatureExprFactory.True, "VE"), FeatureExprFactory.True, null)
    } catch {
      case _: NoSuchMethodException =>
        c.getConstructor(classOf[FeatureExpr]).newInstance(FeatureExprFactory.True)
    }
  }

  def createJUnit4Object(ctx: FeatureExpr): Any = {
    c.getConstructor(classOf[FeatureExpr]).newInstance(ctx)
  }

  def createParameterizedObject(parameters: Array[V[_]]): Any = {
    val all         = c.getConstructors
    val nParameters = parameters.length * 2 + 1 // dummy values + FE
    val filtered    = all.filter(x => x.getParameterCount == nParameters)
    assert(filtered.length == 1,
           "Wrong number of constructors matching the same number of parameters")
    val dummies =
      filtered.head.getParameterTypes.drop(parameters.length + 1).map(x => getDummyValue(x.getName))
    val allParams = parameters.toList ::: FeatureExprFactory.True :: dummies.toList
    filtered.head.newInstance(allParams: _*)
  }

  def getDummyValue(t: String): Object = t match {
    case "int"     => Integer.valueOf(0)
    case "short"   => java.lang.Short.valueOf("0")
    case "boolean" => java.lang.Boolean.valueOf(false)
    case "byte"    => java.lang.Byte.valueOf("0")
    case "char"    => java.lang.Character.valueOf(0)
    case "long"    => java.lang.Long.valueOf(0)
    case "double"  => java.lang.Double.valueOf(0.0)
    case "float"   => java.lang.Float.valueOf(0.0F)
    case _         => null
  }

  def isAbstract: Boolean = Modifier.isAbstract(c.getModifiers)

  // todo: rewrite the filtering part
  def runTests(isFastMode: Boolean = false): Boolean = {
    val allTests = getOrderedTestCases
    if (allTests.isEmpty) {
      VTestStat.skipClass(className);
      return true
    }
    if (isAbstract) {
      VTestStat.skipClass(className)
      return true
    }
    require(checkAnnotations, s"Unsupported annotation in $c")
    allTests.filter(isSkipped).foreach(m => VTestStat.skip(className, m.getName))
    if (isFastMode) {
      runTestsWithMode(allTests, isFastMode = true, shouldAbort = !overallSolutionsFound())
    } else {
      runTestsWithMode(allTests, isFastMode = false, shouldAbort = shouldAbortCompleteMode())
    }
  }

  def runTestsWithMode(allTests: List[Method],
                       isFastMode: Boolean,
                       shouldAbort: => Boolean): Boolean = {
    if (!isParameterized) {
      for (x <- allTests if !isSkipped(x)) {
        executeOnce(None,
                    x,
                    FeatureExprFactory.True,
                    FeatureExprFactory.False,
                    isFastMode = isFastMode)
        writeBDD(c.getName, x.getName)
        if (shouldAbort) return false
      }
    } else {
      for (x <- getParameters;
           y <- allTests if !isSkipped(y)) {
        executeOnce(Some(x.asInstanceOf[Array[V[_]]]),
                    y,
                    FeatureExprFactory.True,
                    FeatureExprFactory.False,
                    isFastMode = isFastMode)
        writeBDD(c.getName, y.getName)
        if (shouldAbort) return false
      }
    }
    overallSolutionsFound()
  }

  def shouldAbortCompleteMode(): Boolean = {
    if (Settings.earlyFail) {
      val fe = VTestStat.getOverallPassingCond
      if (!fe.isSatisfiable()) {
        // print the results so far and abort
        printlnAndLog("-------------------- Abort --------------------")
        //        VTestStat.printToConsole()
        //        System.exit(-1)
        return true
      }
    }
    false
  }

  def overallSolutionsFound(): Boolean = {
    VTestStat.getOverallPassingCond.isSatisfiable()
  }

  def countBlockForAllTests(): Unit = {
    val allTests = getOrderedTestCases
    if (!isParameterized) {
      for (t <- allTests if !isSkipped(t)) {
        countBlockForTest(None, t)
      }
    } else {
      for (p <- getParameters;
           t <- allTests if !isSkipped(t)) {
        countBlockForTest(Some(p.asInstanceOf[Array[V[_]]]), t)
      }
    }
  }

  def countBlockForTest(params: Option[Array[V[_]]], x: Method): Unit = {
    val context = FeatureExprFactory.True
    try {
      printlnAndLog(s"[INFO] Counting blocks for $className.${x.getName}")
      val testObject = createObject(params, context)
      before.map(_.invoke(testObject, context))
      x.invoke(testObject, context)
      after.map(_.invoke(testObject, context))
      System.gc()
    } catch {
      case e: InvocationTargetException =>
        e.getCause match {
          case ve: VException => if (ve.e != null && !verifyException(ve.e, x)) printlnAndLog(ve.e.toString, err = true)
          case _              => e.printStackTrace()
        }
      case e: Throwable => e.printStackTrace()
    }
    VERuntime.putMaxBlockForTest(x)
  }

  def executeOnce(
      params: Option[Array[V[_]]], // test case parameters, in case of parameterized test
      x: Method, // test case to be executed
      context: FeatureExpr, // current context
      exploredContext: FeatureExpr, // used to filter examined contexts
      isFastMode: Boolean
  ): Unit = {
    if (context.isContradiction()) return
    printlnAndLog(s"[INFO] Executing ${className}.${x.getName} under ${if (Settings.printContext) context
    else "[hidden context]"}")
    VERuntime.init(x, context, context, isFastMode, getExpectedException(x))
    val testObject = createObject(params, context)
    try {
      before.map(_.invoke(testObject, context))
      x.invoke(testObject, context)
      after.map(_.invoke(testObject, context))
      System.gc()
      val succeedingContext = VERuntime.getExploredContext(context)
      if (getExpectedException(x).nonEmpty && getExpectedException(x).get != classOf[Test.None]) {
        VTestStat.fail(className, x.getName)
        printlnAndLog("Expecting exception, but didn't catch anything", err = true)
      } else {
        VTestStat.succeed(className, x.getName, succeedingContext)
      }
      if (VERuntime.skippedExceptionContext.isSatisfiable()) VTestStat.fail(className, x.getName)
      val exploredSoFar =
        succeedingContext.or(exploredContext).or(VERuntime.skippedExceptionContext)
      val nextContext = exploredSoFar.not()
      if (!isFastMode && nextContext.isSatisfiable())
        executeOnce(params, x, nextContext, exploredSoFar, isFastMode)
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            if (!verifyException(t.e, x)) {
              // unexpected exceptions occurred
              VTestStat.fail(className, x.getName)
              printlnAndLog(t.toString)
            } else {
              VTestStat.succeed(className, x.getName, t.ctx)
              if (VERuntime.skippedExceptionContext.isSatisfiable())
                VTestStat.fail(className, x.getName)
            }
            val exploredSoFar = t.ctx.or(exploredContext).or(VERuntime.skippedExceptionContext)
            val nextContext   = exploredSoFar.not()
            if (!isFastMode && nextContext.isSatisfiable())
              executeOnce(params, x, nextContext, exploredSoFar, isFastMode)
          case t =>
            if (!verifyException(t, x))
              throw new RuntimeException("Something wrong, not a VException", t)
        }
      }
      case e =>
        throw new RuntimeException(
          s"Expecting InvocationTargetException, but found ${e.printStackTrace()}")
    }
  }

  def isSkipped(x: Method): Boolean =
    x.getName.contains("testSerial") || x.getName.toLowerCase().contains("serialization")

  /**
    * Verify if the caught exception is expected by the test case
    *
    * @param t the Throwable
    * @param m test case method
    * @return true if it is expected
    */
  def verifyException(t: Throwable, m: Method): Boolean = {
    val expected = getExpectedException(m)
    expected.exists(x => x.isInstance(t))
  }

  def getExpectedException(m: Method): Option[Class[_]] = {
    if (isJUnit3) None
    else {
      val annotation = m.getAnnotation(classOf[org.junit.Test])
      assert(annotation != null, "No @Test annotation in method: " + m.getName)
      Some(annotation.expected())
    }
  }

  /**
    * Ensure we support all JUnit annotations in this test class
    *
    * We support the following annotations: @Test, @Before, @After, @Ignore, @Deprecated (java.lang)
    *
    * @return true if only using supported annotations
    */
  def checkAnnotations: Boolean = {
    getAllMethods.forall { x =>
      x.getAnnotations.length == 0 ||
      x.getAnnotation(classOf[org.junit.Test]) != null ||
      x.getAnnotation(classOf[org.junit.Before]) != null ||
      x.getAnnotation(classOf[org.junit.After]) != null ||
      x.getAnnotation(classOf[org.junit.Ignore]) != null ||
      x.getAnnotation(classOf[Parameters]) != null ||
      x.getAnnotation(classOf[Deprecated]) != null
    }
  }

  def existParameterField: Boolean =
    getAllFields.exists(f => f.isAnnotationPresent(classOf[Parameter]))
  def existParametersMethod: Boolean =
    getAllMethods.exists(m => m.isAnnotationPresent(classOf[Parameters]))
  def getParameters: Array[_] = {
    val m = getMethodWithAnnotation(c, classOf[Parameters])
    assert(m.nonEmpty, "No method with @Parameters annotation")
    val ps = m.get.invoke(null, FeatureExprFactory.True)
    assert(ps.isInstanceOf[One[_]], s"return value of @Parameters method is not One: $ps")
    val ret = ps.asInstanceOf[One[_]].getOne
    ret.getClass.getName match {
      case "model.java.util.ArrayList" | "model.java.util.Collection" |
          "model.java.util.Arrays$ArrayList" =>
        val mToArray =
          ret.getClass.getMethod("toArray____Array_Ljava_lang_Object", classOf[FeatureExpr])
        mToArray.setAccessible(true)
        val VOfArrayOfVOfVArray = mToArray.invoke(ret, FeatureExprFactory.True)
        assert(VOfArrayOfVOfVArray.isInstanceOf[One[_]],
               "return value of toArray____Array_Ljava_lang_Object is not One")
        val arrayOfVOfVArray =
          VOfArrayOfVOfVArray.asInstanceOf[One[_]].getOne.asInstanceOf[Array[V[_]]]
        arrayOfVOfVArray.map(x => x.getOne)
      case noSupport =>
        throw new RuntimeException(s"Unsupported return type of @Parameters: $noSupport")
    }
  }

  def writeBDD(cName: String, mName: String): Unit = {
    if (Settings.writeBDDs) {
      ???
//      if (VTestStat.classes(cName).failedMethods.contains(mName)) {
//        val failingCond      = VTestStat.classes(cName).failedMethods(mName).failingCtx
//        val originMethodName = mName.substring(0, mName.indexOf("__"))
//        val fileName         = "test." + cName + "." + originMethodName + ".txt"
//
//        val bddFactory = FExprBuilder.bddFactory
//        val writer     = new FileWriter(fileName)
//        bddFactory.save(new BufferedWriter(writer), failingCond.asInstanceOf[BDDFeatureExpr].bdd)
//      }
    }
  }
}
