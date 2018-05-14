package edu.cmu.cs.vbc.testutils

import java.io.{File, InputStream}
import java.lang.reflect.Method
import java.net.URLClassLoader

import edu.cmu.cs.vbc.VBCClassLoader
import edu.cmu.cs.vbc.utils.MyClassWriter
import edu.cmu.cs.vbc.vbytecode.VBCClassNode
import org.objectweb.asm.ClassWriter

class VBCTestClassLoader(parent: ClassLoader,
                         mainClasspath: String,
                         testClasspath: String) extends VBCClassLoader(parentClassLoader = parent, configFile = Some("test-default.conf")) {

  require(mainClasspath.endsWith("/"), "URLClassLoader expects a directory path to end with /")
  require(testClasspath.endsWith("/"), "URLClassLoader expects a directory path to end with /")
  private val urlClassLoader = new URLClassLoader(Array(mainClasspath, testClasspath).map(new File(_).toURI.toURL))

  override def findClass(name: String): Class[_] = {
    val resource: String = name.replace('.', '/') + ".class"
    val is: InputStream = urlClassLoader.getResourceAsStream(resource)
    if (is == null) throw new ClassNotFoundException(name)
    val clazz: VBCClassNode = loader.loadClass(is)
    liftClass(name, clazz)
  }


  override def loadClassAndUseModelClasses(name: String): Class[_] = {
    val resource: String = name.replace('.', '/') + ".class"
    val is: InputStream = urlClassLoader.getResourceAsStream(resource)
    val clazz: VBCClassNode = loader.loadClass(is)
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES) // COMPUTE_FRAMES implies COMPUTE_MAX
    clazz.toByteCode(cw)
    defineClass(name, cw.toByteArray, 0, cw.toByteArray.length)
  }

  /**
    * Find a list of class names under testClasspath
    */
  def findTestClassFiles(): List[String] = {

    def getFullClassname(path: String): String = path.substring(testClasspath.length).
      replace(File.pathSeparator, ".").
      reverse.drop(6).reverse // drop the ".class" file extension

    def go(dir: File): List[String] = {
      val allFiles = dir.listFiles()
      val thisLevel: List[String] = allFiles.filter(x => x.isFile && x.getName.endsWith(".class")).map(x => getFullClassname(x.getAbsolutePath)).toList
      val nested: List[String] = allFiles.filter(_.isDirectory).flatMap(go).toList
      thisLevel ::: nested
    }

    go(new File(testClasspath))
  }

  def getTestMethods(cName: String): List[Method] = {
    val c = urlClassLoader.loadClass(cName)
    c.getMethods.toList.filter {x => x.isAnnotationPresent(classOf[org.junit.Test])}
  }

}
