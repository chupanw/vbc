package edu.cmu.cs.vbc.testutils

import java.lang.annotation.Annotation
import java.lang.reflect.{Field, InvocationTargetException, Method, Modifier}

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.varex.{One, V}
import edu.cmu.cs.vbc.{VERuntime, VException}
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.{Parameter, Parameters}

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
      for (x <- getTestCases if !isSkipped(x)) executeOnce(None, x, FeatureExprFactory.True)
    else
      for (
        x <- getParameters;
        y <- getTestCases if !isSkipped(y)
      ) executeOnce(Some(x.asInstanceOf[Array[V[_]]]), y, FeatureExprFactory.True)

  }

  def executeOnce(params: Option[Array[V[_]]], x: Method, context: FeatureExpr): Unit = {
    if (context.isContradiction()) return
    System.out.println(s"[INFO] Executing ${className}.${x.getName} under $context")
    VERuntime.init()
    val testObject = createObject(params)
    before.map(_.invoke(testObject, context))
    try {
      x.invoke(testObject, context)
      if (VERuntime.hasVException) {
        val expCtx = VERuntime.exceptionCtx.clone()
        expCtx.foreach(fe => executeOnce(params, x, context.and(fe.not())))
      }
      VTestStat.succeed(className, x.getName)
    } catch {
      case invokeExp: InvocationTargetException => {
        invokeExp.getCause match {
          case t: VException =>
            if (!verifyException(t.e, x, t.ctx))
              System.out.println(t)
            if (!t.ctx.equivalentTo(context)) {
              val altCtx = context.and(t.ctx.not())
              executeOnce(params, x, altCtx)
            }
            else if (VERuntime.hasVException) {
              val expCtx = VERuntime.exceptionCtx.clone()
              expCtx.foreach(fe => executeOnce(params, x, context.and(fe.not())))
            }
            VTestStat.succeed(className, x.getName)
          case t =>
            if (!verifyException(t, x, FeatureExprFactory.True))
              throw new RuntimeException("Not a VException", t)
        }
      }
      case e =>
        throw new RuntimeException(s"Expecting InvocationTargetException, but found $e")
    }
    after.map(_.invoke(testObject, context))
  }

  def isSkipped(x: Method): Boolean = x.getName.contains("testSerial") || x.getName.toLowerCase().contains("serialization")

  def verifyException(t: Throwable, m: Method, ctx: FeatureExpr): Boolean = {
    if (isJUnit3) {
      VTestStat.fail(className, m.getName, ctx)
      false
    }
    else {
      val annotation = m.getAnnotation(classOf[org.junit.Test])
      assert(annotation != null, "No @Test annotation in method: " + m.getName)
      val expected = annotation.expected()
      if (!expected.isInstance(t)) {
        VTestStat.fail(className, m.getName, ctx)
        false
      } else true
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
        x.getAnnotation(classOf[org.junit.After]) != null ||
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
      case "model.java.util.ArrayList" | "model.java.util.Collection" =>
        val mToArray = ret.getClass.getMethod("toArray____Array_Ljava_lang_Object", classOf[FeatureExpr])
        val VOfArrayOfVOfVArray = mToArray.invoke(ret, FeatureExprFactory.True)
        assert(VOfArrayOfVOfVArray.isInstanceOf[One[_]], "return value of toArray____Array_Ljava_lang_Object is not One")
        val arrayOfVOfVArray = VOfArrayOfVOfVArray.asInstanceOf[One[_]].getOne.asInstanceOf[Array[V[_]]]
        arrayOfVOfVArray.map(x => x.getOne)
      case noSupport =>
        throw new RuntimeException(s"Unsupported return type of @Parameters: $noSupport")
    }
  }
}
