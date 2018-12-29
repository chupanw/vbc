package edu.cmu.cs.vbc.testutils

import java.io.{BufferedWriter, FileWriter}
import java.lang.annotation.Annotation
import java.lang.reflect.{Field, InvocationTargetException, Method, Modifier}

import de.fosd.typechef.featureexpr.bdd.{BDDFeatureExpr, FExprBuilder}
import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.varex.{One, V}
import edu.cmu.cs.vbc.{GlobalConfig, VERuntime, VException}
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.{Parameter, Parameters}

import scala.collection.mutable

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
case class TestClass(c: Class[_]) {

//  require(checkAnnotations, s"Unsupported annotation in $c")

  val isJUnit3: Boolean = isSubclassOfTestCase(c)
  val isParameterized: Boolean = isParameterizedTest(c)

  def isParameterizedTest(c: Class[_]): Boolean =
    c.isAnnotationPresent(classOf[RunWith]) && c.getAnnotation(classOf[RunWith]).value() == classOf[Parameterized]

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

  def getMethodWithName(clazz: Class[_], name: String): Option[Method] = {
    val x = clazz.getDeclaredMethods.toList.filter(x => x.getName == name)
    if (x.isEmpty) {
      if (clazz.getSuperclass != null)
        getMethodWithName(clazz.getSuperclass, name)
      else
        None
    }
    else if (x.length == 1) {
      x.head.setAccessible(true)
      Some(x.head)
    }
    else
      throw new RuntimeException(s"More than one method have the name: $name")
  }

  //todo: also search for superclasses
  def getTestCases: List[Method] =
    if (isJUnit3)
      c.getMethods.toList.filter(x => x.getName.startsWith("test"))
    else {
      val allTests = c.getMethods.toList.filter{x => x.isAnnotationPresent(classOf[org.junit.Test])}
      allTests.filterNot(x => x.isAnnotationPresent(classOf[org.junit.Ignore]))
    }

  // only public
  def getAllMethods: List[Method] = c.getMethods.toList
  // only public
  def getAllFields: List[Field] = c.getFields.toList

  def createObject(params: Option[Array[V[_]]]): Any = {
    try {
      if (params.nonEmpty) createParameterizedObject(params.get)
      else if (isJUnit3) createJUnit3Object()
      else createJUnit4Object()
    } catch {
      case t: Throwable =>
        System.err.println(s"Error creating test object for $c")
        t.printStackTrace()
    }
  }

  def createJUnit3Object(): Any = {
    try {
      c.getConstructor(classOf[V[_]], classOf[FeatureExpr], classOf[String]).newInstance(V.one(FeatureExprFactory.True, "VE"), FeatureExprFactory.True, null)
    } catch {
      case _: NoSuchMethodException =>
        c.getConstructor(classOf[FeatureExpr]).newInstance(FeatureExprFactory.True)
    }
  }

  def createJUnit4Object(): Any = {
    c.getConstructor(classOf[FeatureExpr]).newInstance(FeatureExprFactory.True)
  }

  def createParameterizedObject(parameters: Array[V[_]]): Any = {
    val all = c.getConstructors
    val nParameters = parameters.length * 2 + 1 // dummy values + FE
    val filtered = all.filter(x => x.getParameterCount == nParameters)
    assert(filtered.length == 1, "Wrong number of constructors matching the same number of parameters")
    val dummies = filtered.head.getParameterTypes.drop(parameters.length + 1).map(x => getDummyValue(x.getName))
    val allParams = parameters.toList ::: FeatureExprFactory.True :: dummies.toList
    filtered.head.newInstance(allParams:_*)
  }

  def getDummyValue(t: String): Object = t match {
    case "int" => Integer.valueOf(0)
    case "short" => java.lang.Short.valueOf("0")
    case "boolean" => java.lang.Boolean.valueOf(false)
    case "byte" => java.lang.Byte.valueOf("0")
    case "char" => java.lang.Character.valueOf(0)
    case "long" => java.lang.Long.valueOf(0)
    case "double" => java.lang.Double.valueOf(0.0)
    case "float" => java.lang.Float.valueOf(0.0F)
    case _ => null
  }

  def isAbstract: Boolean = Modifier.isAbstract(c.getModifiers)

  // todo: rewrite the filtering part
  def runTests(): Unit = {
    if (getTestCases.isEmpty) {VTestStat.skipClass(className); return}
    if (isAbstract) {
      VTestStat.skipClass(className)
      return
    }
    require(checkAnnotations, s"Unsupported annotation in $c")
    getTestCases.filter(isSkipped).foreach(m => VTestStat.skip(className, m.getName))
    if (!isParameterized)
      for (x <- getTestCases if !isSkipped(x)) {
        val failingConditions = mutable.ArrayBuffer[FeatureExpr]()
        executeOnce(None, x, FeatureExprFactory.True, failingConditions, mutable.ArrayBuffer[FeatureExpr]())
        writeBDD(failingConditions.foldLeft(FeatureExprFactory.False)(_ or _), c.getName, x.getName)
      }
    else
      for (
        x <- getParameters;
        y <- getTestCases if !isSkipped(y)
      ) {
        val failingConditions = mutable.ArrayBuffer[FeatureExpr]()
        executeOnce(Some(x.asInstanceOf[Array[V[_]]]), y, FeatureExprFactory.True, failingConditions, mutable.ArrayBuffer[FeatureExpr]())
        writeBDD(failingConditions.foldLeft(FeatureExprFactory.False)(_ or _), c.getName, y.getName)
      }

  }

  def executeOnce(params: Option[Array[V[_]]],  // test case parameters, in case of parameterized test
                  x: Method,  // test case to be executed
                  context: FeatureExpr, // current context
                  accFailingCtx: mutable.ArrayBuffer[FeatureExpr],  // do we really need this?
                  accCtx: mutable.ArrayBuffer[FeatureExpr]  // used to filter examined contexts
                 ): Unit = {
    /**
      * Helper function to analyze contexts that could cause VExceptions but were caught internally
      */
    def analyzeHiddenContexts(except: FeatureExpr): Unit = {
      /*
          If a hidden exception A is thrown before B, which is later caught, A might invalidate B, so we
          need to analyze A.
      */
      val hidden = VERuntime.getHiddenContextsOtherThan(except).filter(x => !accCtx.exists(_ equivalentTo x.and(context)))
      hidden.foreach(fe => {
        accFailingCtx += fe
        executeOnce(params, x, context.and(fe), accFailingCtx, accCtx)
        executeOnce(params, x, context.and(fe.not()), accFailingCtx, accCtx)
      })
    }
    if (context.isContradiction()) return
    if (accCtx.exists(_.equivalentTo(context))) return
    accCtx += context
    System.out.println(s"[INFO] Executing ${className}.${x.getName} under ${if (GlobalConfig.printContext) context else "[hidden context]"}")
    VERuntime.init()
    val testObject = createObject(params)
    try {
      before.map(_.invoke(testObject, context))
      x.invoke(testObject, context)
      analyzeHiddenContexts(context)
      VTestStat.succeed(className, x.getName)
      after.map(_.invoke(testObject, context))
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            if (!verifyException(t.e, x, t.ctx, context, accCtx))
              System.out.println(t)
            if (!t.ctx.equivalentTo(context)) {
              analyzeHiddenContexts(t.ctx)
              /* This is being conservative, so that we don't miss any VExceptions */
              accFailingCtx += t.ctx
              executeOnce(params, x, context.and(t.ctx), accFailingCtx, accCtx)
              executeOnce(params, x, context.and(t.ctx.not()), accFailingCtx, accCtx)
            }
            else
              analyzeHiddenContexts(context)
            VTestStat.succeed(className, x.getName) // this is conservative, will be filtered by failing test cases
          case t =>
            if (!verifyException(t, x, FeatureExprFactory.True, context, accCtx))
              throw new RuntimeException("Not a VException", t)
        }
      }
      case e =>
        throw new RuntimeException(s"Expecting InvocationTargetException, but found $e")
    }
  }

  def isSkipped(x: Method): Boolean = x.getName.contains("testSerial") || x.getName.toLowerCase().contains("serialization")

  def verifyException(t: Throwable, m: Method, expCtx: FeatureExpr, context: FeatureExpr, accCtxs: mutable.ArrayBuffer[FeatureExpr]): Boolean = {
    def checkAndLog(e: FeatureExpr, c: FeatureExpr): Unit =
      if (e.equivalentTo(c) && VERuntime.getHiddenContextsOtherThan(e).isEmpty)
        VTestStat.fail(className, m.getName, e)

    if (isJUnit3) {
      checkAndLog(expCtx, context)
      false
    }
    else {
      val annotation = m.getAnnotation(classOf[org.junit.Test])
      assert(annotation != null, "No @Test annotation in method: " + m.getName)
      val expected = annotation.expected()
      if (!expected.isInstance(t)) {
        checkAndLog(expCtx, context)
        false
      } else true
    }
  }

  /**
    * Ensure we support all JUnit annotations in this test class
    *
    * We support the following annotations: @Test, @Before, @After, @Ignore,
    *
    * @return true if only using supported annotations
    */
  def checkAnnotations: Boolean = {
    getAllMethods.forall {x =>
      x.getAnnotations.length == 0 ||
        x.getAnnotation(classOf[org.junit.Test]) != null ||
        x.getAnnotation(classOf[org.junit.Before]) != null ||
        x.getAnnotation(classOf[org.junit.After]) != null ||
        x.getAnnotation(classOf[org.junit.Ignore]) != null ||
        x.getAnnotation(classOf[Parameters]) != null
    }
  }

  def existParameterField: Boolean = getAllFields.exists(f => f.isAnnotationPresent(classOf[Parameter]))
  def existParametersMethod: Boolean = getAllMethods.exists(m => m.isAnnotationPresent(classOf[Parameters]))
  def getParameters: Array[_] = {
    val m = getMethodWithAnnotation(c, classOf[Parameters])
    assert(m.nonEmpty, "No method with @Parameters annotation")
    val ps = m.get.invoke(null, FeatureExprFactory.True)
    assert(ps.isInstanceOf[One[_]], s"return value of @Parameters method is not One: $ps")
    val ret = ps.asInstanceOf[One[_]].getOne
    ret.getClass.getName match {
      case "model.java.util.ArrayList" | "model.java.util.Collection" | "model.java.util.Arrays$ArrayList" =>
        val mToArray = ret.getClass.getMethod("toArray____Array_Ljava_lang_Object", classOf[FeatureExpr])
        mToArray.setAccessible(true)
        val VOfArrayOfVOfVArray = mToArray.invoke(ret, FeatureExprFactory.True)
        assert(VOfArrayOfVOfVArray.isInstanceOf[One[_]], "return value of toArray____Array_Ljava_lang_Object is not One")
        val arrayOfVOfVArray = VOfArrayOfVOfVArray.asInstanceOf[One[_]].getOne.asInstanceOf[Array[V[_]]]
        arrayOfVOfVArray.map(x => x.getOne)
      case noSupport =>
        throw new RuntimeException(s"Unsupported return type of @Parameters: $noSupport")
    }
  }

  def writeBDD(failingCond: FeatureExpr, cName: String, mName: String): Unit = {
    if (GlobalConfig.writeBDDs) {
      val originMethodName = mName.substring(0, mName.indexOf('_'))
      val fileName = "test." + cName + "." + originMethodName + ".txt"

      val bddFactory = FExprBuilder.bddFactory
      val writer = new FileWriter(fileName)
      bddFactory.save(new BufferedWriter(writer), failingCond.asInstanceOf[BDDFeatureExpr].bdd);
    }
  }
}
