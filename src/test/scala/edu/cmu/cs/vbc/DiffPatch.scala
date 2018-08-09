package edu.cmu.cs.vbc

import java.io.File
import java.lang.annotation.Annotation
import java.lang.reflect.{Field, InvocationTargetException, Method, Modifier}
import java.net.{URL, URLClassLoader}

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory, FeatureExprParser}
import edu.cmu.cs.varex.annotation.VConditional

import scala.collection.mutable
import scala.io.Source


object DiffPatch extends App {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  testVersion(
    repository = "/Users/chupanw/Projects/Data/PatchStudy/Math-git/",
    version = "Math-14f",
    vResultsPath = "Math-14f.md"
  )

  /**
    * Test if normal execution differs from variational execution:
    *   - Missing test class or test case
    *   - Missing exceptions
    *   - Wrong exception conditions
    */
  def testVersion(repository: String,
                  version: String,
                  vResultsPath: String): Unit = {
    val relevantTests = Source.fromFile(repository + "RelevantTests/" + version + ".txt")
    val testClasspath = s"$repository$version/target/test-classes/"
    val mainClasspath = s"$repository$version/target/classes/"

    val testClasses = relevantTests.getLines().toList.filterNot(_.startsWith("//"))
    val loader = new TestClassLoader(Array(mainClasspath, testClasspath).map(new File(_).toURI.toURL))
    val vr = new VResult(vResultsPath)
    testClasses.foreach {x =>
      val testClass = new NormalTestClass(loader.loadClass(x), loader, vr)
      testClass.runTests()
    }
  }
}

/**
  * Parse markdown files to extract passing conditions for test cases
  */
class VResult(vResultPath: String) {
  val lines: List[String] = Source.fromFile(vResultPath).getLines().toList.drop(2)
  val m: mutable.HashMap[String, FeatureExpr] = mutable.HashMap[String, FeatureExpr]()
  val feParser = new FeatureExprParser(featureFactory = FeatureExprFactory.bdd)
  for (l <- lines if !l.contains("skipped")) {
    val entries = l.split("\\| ")
    val test = entries(2).split("_")(0)
    val fe = if (l.contains("✔")) FeatureExprFactory.True else feParser.parse(entries(3).init.stripMargin.init.init.tail)
    m.put(test, fe)
  }
  def get(test: String): FeatureExpr = m(test)
}


/**
  * Use reflection to tweak fields with VConditional annotation
  */
class NormalTestClass(c: Class[_], loader: TestClassLoader, vr: VResult) {
  val className: String = c.getName
  val before: Option[Method] = getMethodWithAnnotation(c, classOf[org.junit.Before])
  val after: Option[Method] = getMethodWithAnnotation(c, classOf[org.junit.After])

  /**
    * Find method with specific annotation.
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

  def getTestCases: List[Method] = c.getMethods.toList.filter {x =>
    x.isAnnotationPresent(classOf[org.junit.Test])
  }

  def getAllMethods: List[Method] = c.getMethods.toList

  def createObject(): Any = {
    try {
      c.getConstructor().newInstance()
    } catch {
      case t: Throwable =>
        System.err.println(s"Error creating test object for $c")
        t.printStackTrace()
    }
  }

  def isAbstract: Boolean = Modifier.isAbstract(c.getModifiers)

  def isSkipped(x: Method): Boolean = x.getName.contains("testSerial") || x.getName.toLowerCase().contains("serialization")

  def runTests(): Unit = {
    if (getTestCases.isEmpty | isAbstract) return
    for (x <- getTestCases if !isSkipped(x)) {
      executeOneTest(x)
    }
  }

  def executeOneTest(x: Method): Unit = {
    var failingCond: FeatureExpr = FeatureExprFactory.False
    val testObject = createObject()
    // collect all patches
    loader.setAllTrue()
//    System.out.println(s"[INFO] Executing ${x.getName} under True")
    before.map(_.invoke(testObject))
    try {
      x.invoke(testObject)
    } catch {
      case e: InvocationTargetException =>
        if (!verifyException(e.getCause, x))
          failingCond = failingCond.or(FeatureExprFactory.True)
    }
    after.map(_.invoke(testObject))
    // explore all combinations
    val combos: List[(List[Field], List[Field])] = loader.explode(loader.patches.toList)
    for (p <- combos) {
//      System.out.println(s"[INFO] Executing ${x.getName} under ${p._1.map(_.getName)}")
      p._1.foreach(x => x.setBoolean(null, true))
      p._2.foreach(x => x.setBoolean(null, false))
      val testObject = createObject()
      before.map(_.invoke(testObject))
      try {
        x.invoke(testObject)
      } catch {
        case e: InvocationTargetException =>
          if (!verifyException(e.getCause, x))
            failingCond = failingCond.or(getFEFromFields(p._1, p._2))
      }
      after.map(_.invoke(testObject))
    }
    // check results
    val bf = failingCond.not()
    val ve = vr.get(c.getName.substring(25) + "." + x.getName)
    if (!ve.equivalentTo(bf)) {
      System.err.println(s"MISMATCH in ${c.getName}.${x.getName}")
      System.err.println(s"\tBF: ${bf}")
      System.err.println(s"\tVE: ${ve}")
    }
  }

  def getFEFromFields(selected: List[Field], deselected: List[Field]): FeatureExpr = {
    val selectedFE = selected.map(x => FeatureExprFactory.createDefinedExternal(x.getName))
    val deselectedFE = deselected.map(x => FeatureExprFactory.createDefinedExternal(x.getName).not())
    val a = selectedFE.foldLeft(FeatureExprFactory.True)(_ and _)
    val b = deselectedFE.foldLeft(FeatureExprFactory.True)(_ and _)
    a.and(b)
  }

  def verifyException(t: Throwable, m: Method): Boolean = {
    val annotation = m.getAnnotation(classOf[org.junit.Test])
    assert(annotation != null, "No @Test annotation in method: " + m.getName)
    val expected = annotation.expected()
    expected.isInstance(t)
  }
}

/**
  * Load classes used for testing and collect a list of fields with VConditional
  */
class TestClassLoader(urls: Array[URL]) extends URLClassLoader(urls) {
  val patches = new mutable.HashSet[Field]()
  override def findClass(name: String): Class[_] = {
    val clazz = super.findClass(name)
    val condFields = clazz.getFields.filter(f => f.isAnnotationPresent(classOf[VConditional]))
    condFields.foreach(x => patches.add(x))
    clazz
  }

  override def loadClass(name: String): Class[_] =
    if (name.startsWith("org.apache.commons.math"))
      findClass(name)
    else
      super.loadClass(name)

  def setAllTrue(): Unit = patches.foreach(x => x.setBoolean(null, true))

  def explode(fs: List[Field]): List[(List[Field], List[Field])] = {
    if (fs.isEmpty) List((Nil, Nil))
    else if (fs.size == 1) List((List(fs.head), Nil), (Nil, List(fs.head)))
    else {
      val r = explode(fs.tail)
      r.map(x => (fs.head :: x._1, x._2)) ++ r.map(x => (x._1, fs.head :: x._2))
    }
  }
}

