package edu.cmu.cs.vbc

import java.io.{File, FileWriter}
import java.lang.annotation.Annotation
import java.lang.reflect.{Field, InvocationTargetException, Method, Modifier}
import java.net.URLClassLoader
import java.nio.file.FileSystems

import edu.cmu.cs.vbc.testutils.{Project, TestString}
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.slf4j.LoggerFactory

import scala.io.Source.fromFile

/**
  * TODO:
  *   update canFix (coordinate with VarexC)
  *   update MongoDB solutions
  *   set up proper logging
  */
abstract class BFVerifier {

  def getSolutions(): List[List[String]] = {
    val tmpDirPath = FileSystems.getDefault.getPath(System.getProperty("java.io.tmpdir"), "solutions.txt")
    val line = fromFile(tmpDirPath.toFile).getLines().toList.head
    val split = line.split(',').toList
    split.map(e => e.dropWhile(_ != '{').takeWhile(_ != '}').tail.split('&').toList)
  }

  def genProject(args: Array[String]): Project

  def run(args: Array[String], profiler: Boolean = false): Unit = {
    run(minimize(getSolutions()), args, profiler)
  }

  def run(solutions: List[List[String]], args: Array[String], profiler: Boolean): Unit = {
    val p: Project = genProject(args)
    val testLoader = new BFTestLoader(p.mainClassPath, p.testClassPath, p.libJars)
    var remainSolutions = solutions
    val globalOptionsClass = testLoader.loadClass("varexc.GlobalOptions")
    p.testClasses.foreach(x => {
      val testClass = new BFTestClass(testLoader.loadClass(x), globalOptionsClass, remainSolutions, profiler)
      remainSolutions = testClass.runTests()
      if (profiler) {
        println(testClass.timer.toList.sortBy(_._2).reverse)
      }
    })
    println(remainSolutions)
    val tmpDirPath = FileSystems.getDefault.getPath(System.getProperty("java.io.tmpdir"), "solutions-bf.txt")
    val solutionsWriter = new FileWriter(tmpDirPath.toFile)
    solutionsWriter.write(remainSolutions.toString())
    solutionsWriter.close()
  }

  def minimize(solutions: List[List[String]]): List[List[String]] = {
    def go(l: List[List[String]], min: collection.mutable.ListBuffer[List[String]]): List[List[String]] = {
      if (l.isEmpty) min.toList
      else {
        val head = l.head
        min.append(head)
        val filtered = l.filterNot(x => head.diff(x).isEmpty)
        go(filtered, min)
      }
    }
    go(solutions.sortBy(_.size), collection.mutable.ListBuffer[List[String]]())
  }

  def profile(args: Array[String], degree: Int): Unit = {
    val p: Project = genProject(args)
    val testLoader = new BFTestLoader(p.mainClassPath, p.testClassPath, p.libJars)
    val globalOptionsClass = testLoader.loadClass("varexc.GlobalOptions")
    val options = globalOptionsClass.getFields.toList.filter(f => f.getAnnotations.nonEmpty).map(_.getName)
    val solutions = genSolutions(options, degree)
    run(solutions, args, true)
  }

  def genSolutions(options: List[String], degree: Int): List[List[String]] = {
    if (options.size == degree)
      List(options)
    else {
      degree match {
        case 1 => options.map(List(_))
        case n =>
          val head = options.head
          val withHead = genSolutions(options.tail, n - 1).map(head :: _)
          withHead ::: genSolutions(options.tail, degree)
      }
    }
  }
}










class BFTestLoader(mainClasspath: String,
                   testClasspath: String,
                   libJars: Array[String] = Array()) {
  require(mainClasspath.endsWith(".jar") || new File(mainClasspath).isDirectory, "URLClassLoader expects a jar or directory")
  require(testClasspath.endsWith(".jar") || new File(testClasspath).isDirectory, "URLClassLoader expects a jar or directory")
  require(libJars.forall(_.endsWith(".jar")), "libJars should only contain jars")

  val allClasspaths = Array(mainClasspath, testClasspath) ++ libJars
  private val urlClassLoader = new URLClassLoader(allClasspaths.map(new File(_).toURI.toURL), this.getClass.getClassLoader)

  def loadClass(name: String): Class[_] = {
    urlClassLoader.loadClass(name)
  }
}













class BFTestClass(c: Class[_], globalOptionsClass: Class[_], pendingSolutions: List[List[String]], profiler: Boolean) {
  val isJUnit3: Boolean = isSubclassOfTestCase(c)
  val isParameterized: Boolean = isParameterizedTest(c)
  val logger = LoggerFactory.getLogger("varexc")
  val timer: collection.mutable.HashMap[List[String], Long] = collection.mutable.HashMap[List[String], Long]()

  def isSubclassOfTestCase(c: Class[_]): Boolean = {
    if (c.getName == "java.lang.Object") false
    else if (c.getSuperclass.getName == "junit.framework.TestCase") true
    else isSubclassOfTestCase(c.getSuperclass)
  }

  def isParameterizedTest(c: Class[_]): Boolean =
    c.isAnnotationPresent(classOf[RunWith]) && c.getAnnotation(classOf[RunWith]).value() == classOf[Parameterized]

  val className: String = c.getName
  val before: Option[Method] =
    if (isJUnit3)
      getMethodWithName(c, "setUp")
    else
      getMethodWithAnnotation(c, classOf[org.junit.Before])
  val after: Option[Method] =
    if (isJUnit3)
      getMethodWithName(c, "tearDown")
    else
      getMethodWithAnnotation(c, classOf[org.junit.After])


  def runTests(): List[List[String]] = {
    val allTests = getTestCases
    if (allTests.isEmpty || isAbstract) {
      return pendingSolutions
    }
    require(checkAnnotations, s"Unsupported annotation in $c")
    var remainSolutions = pendingSolutions
    if (!isParameterized) {
      for (x <- allTests) {
        remainSolutions = execute(None, x, remainSolutions)
      }
    } else {
      for (x <- getParameters;
           y <- allTests) {
        execute(Some(x.asInstanceOf[Array[_]]), y, remainSolutions)
      }
    }
    remainSolutions
  }

  def execute(params: Option[Array[_]], x: Method, pendingSolutions: List[List[String]]): List[List[String]] = {
    printlnAndLog(s"[INFO] Verifying $className.${x.getName}\t remaining solutions: ${pendingSolutions.size}")
    val testObject = createObject(params)
    pendingSolutions.flatMap { s =>
      var start = 0L
      for (v <- s) {
        val f = globalOptionsClass.getDeclaredField(v)
        f.setAccessible(true)
        f.setBoolean(null, true)
      }
      if (profiler) {
        println("profiling " + s)
        start = System.currentTimeMillis()
      }
      try {
        before.map(_.invoke(testObject))
        x.invoke(testObject)
        after.map(_.invoke(testObject))
        if (getExpectedException(x).nonEmpty && getExpectedException(x).get != classOf[Test.None])
          Nil
        else
          List(s)
      } catch {
        case t: InvocationTargetException =>
          if (verifyException(t.getCause, x))
            List(s)
          else {
            printlnAndLog(s"Wrong patch: $s")
            printlnAndLog(t.getCause.toString + "\n" + t.getCause.getStackTrace.take(3).mkString("\t", "\n\t", "\n"))
            Nil
          }
        case e =>
          throw new RuntimeException(
            s"Expecting InvocationTargetException, but found ${e.printStackTrace()}")
      } finally {
        for (v <- s) {
          val f = globalOptionsClass.getDeclaredField(v)
          f.setAccessible(true)
          f.setBoolean(null, false)
        }
        if (profiler) {
          val duration = System.currentTimeMillis() - start
          timer.put(s, timer.getOrElse(s, 0L) + duration)
        }
      }
    }
  }

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
  // only public
  def getAllMethods: List[Method] = c.getMethods.toList
  // only public
  def getAllFields: List[Field] = c.getFields.toList
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
  def getParameters: Array[_] = {
    val m = getMethodWithAnnotation(c, classOf[Parameters])
    assert(m.nonEmpty, "No method with @Parameters annotation")
    val ret = m.get.invoke(null)
    ret.getClass.getName match {
      case "java.util.ArrayList" | "java.util.Collection" |
           "java.util.Arrays$ArrayList" =>
        val mToArray =
          ret.getClass.getMethod("toArray")
        mToArray.setAccessible(true)
        val array = mToArray.invoke(ret)
        array.asInstanceOf[Array[_]]
      case noSupport =>
        throw new RuntimeException(s"Unsupported return type of @Parameters: $noSupport")
    }
  }
  def printlnAndLog(msg: String): Unit = {
    println(msg)
    logger.debug(msg + "\n")
  }
  def createObject(params: Option[Array[_]]): Any = {
    try {
      if (params.nonEmpty) createParameterizedObject(params.get)
      else if (isJUnit3) createJUnit3Object()
      else createJUnit4Object()
    } catch {
      case t: Throwable =>
        val msg = s"Error creating test object for ${c}"
        printlnAndLog(msg)
        printlnAndLog(t.toString)
    }
  }
  def createParameterizedObject(parameters: Array[_]): Any = {
    val all         = c.getConstructors
    val filtered    = all.filter(x => x.getParameterCount == parameters.length)
    assert(filtered.length == 1,
      "Wrong number of constructors matching the same number of parameters")
    val allParams = parameters.toList
    filtered.head.newInstance(allParams: _*)
  }
  def createJUnit3Object(): Any = {
    try {
      c.getConstructor(classOf[String]).newInstance("BF")
    } catch {
      case _: NoSuchMethodException =>
        c.getConstructor().newInstance()
    }
  }
  def createJUnit4Object(): Any = {
    c.getConstructor().newInstance()
  }
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
  def getTestCases: List[Method] =
    if (isJUnit3)
      c.getMethods.toList.filter(x => x.getName.startsWith("test") && x.getParameterCount == 0)
    else {
      val allTests = c.getMethods.toList.filter { x =>
        x.isAnnotationPresent(classOf[org.junit.Test])
      }
      allTests.filterNot(x => x.isAnnotationPresent(classOf[org.junit.Ignore]))
    }
  def isAbstract: Boolean = Modifier.isAbstract(c.getModifiers)
}












class BFApacheMathProject(args: Array[String]) extends Project(args) {
  override def getRelevantTestFilePath: String = mkPath(project, "RelevantTests", version + ".txt").toFile.getAbsolutePath

  override val libJars: Array[String] = getLibJars

  def getLibJars: Array[String] = {
    val libPath = mkPath(project, version, "lib")
    libPath.toFile.listFiles().filter(_.getName.endsWith(".jar")).filterNot(_.getName.contains("varexc")).map(_.getAbsolutePath)
  }

  /**
    * Execute test classes that have no marks, excluding failing test classes
    */
  override def parseRelevantTests(file: String): (List[String], List[TestString], List[TestString]) = {
    val f = fromFile(file)
    val validLines = f.getLines().toList.filterNot(_.startsWith("//"))
    val testClasses = validLines.filterNot(l => l.startsWith("*") || l.startsWith("-"))
    (testClasses, Nil, Nil)
  }
}









object BFApacheMathVefier extends BFVerifier with App {
  override def genProject(args: Array[String]) = new BFApacheMathProject(args)

//  run(args)
  profile(args, 2)
}