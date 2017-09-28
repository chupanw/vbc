package edu.cmu.cs.vbc

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.cmu.cs.vbc.loader.Loader
import edu.cmu.cs.vbc.utils._
import edu.cmu.cs.vbc.vbytecode.{Owner, VBCClassNode, VBCMethodNode}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import org.objectweb.asm.util.{CheckClassAdapter, Textifier, TraceClassVisitor}
import org.objectweb.asm._

import scala.sys.process.Process

/**
  * Custom class loader to modify bytecode before loading the class.
  *
  * the @param rewriter parameter allows the user of the classloader to
  * perform additional instrumentation as a last step before calling
  * toByteCode/toVByteCode when the class is loaded;
  * by default no rewriting is performed
  */
class VBCClassLoader(parentClassLoader: ClassLoader,
                     isLift: Boolean = true,
                     rewriter: VBCMethodNode => VBCMethodNode = a => a,
                     toFileDebugging: Boolean = true) extends ClassLoader(parentClassLoader) with LazyLogging {

  val loader = new Loader()

  override def loadClass(name: String): Class[_] = {
    if (name.startsWith(VBCModel.prefix)) {
      val model = new VBCModel(name)
      val bytes = model.getModelClassBytes
      if (shouldLift(name)) {
        val clazz = loader.loadClass(bytes)
        liftClass(name, clazz)
      }
      else {
        defineClass(name, bytes, 0, bytes.length)
      }
    }
    else if (shouldLift(name))
      findClass(name)
    else
      super.loadClass(name)
  }

  override def findClass(name: String): Class[_] = {
    val resource: String = name.replace('.', '/') + ".class"
    val is: InputStream = getResourceAsStream(resource)
    assert(is != null, s"Class file not found: $name")
    val clazz: VBCClassNode = loader.loadClass(is)
    liftClass(name, clazz)
  }

  def liftClass(name: String, clazz: VBCClassNode): Class[_] = {
    import scala.collection.JavaConversions._
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES) // COMPUTE_FRAMES implies COMPUTE_MAX
    val dotifier = new Dotifier()
    val textifier = new Textifier()
    val cv = new TraceClassVisitor(new TraceClassVisitor(cw, textifier, null), dotifier, null)
    try {
      if (isLift) {
        logger.info(s"lifting $name")
        clazz.toVByteCode(cv, rewriter)
      }
      else {
        logger.info(s"lifting $name")
        clazz.toByteCode(cv, rewriter)
      }
    } catch {
      case e: Throwable =>
        println("Exception thrown in ASM: ")
        println(e.getClass + ": " + e.getMessage)
        println(e.getStackTrace.toList mkString("\t", "\n\t", "\n"))
        println("Please check bug.gv and bug.txt")
        val writer = new PrintWriter(new File("bug.gv"))
        writer.write(dotifier.textBuf.mkString(""))
        writer.write("}")
        writer.close()
        val writer2 = new PrintWriter(new File("bug.txt"))
        writer2.write(textifier.text.mkString(""))
        writer2.close()
        val output = Process("dot -Tpdf -O bug.gv").lineStream
        println("Output from dot: " + output)
        System.exit(1)
    }

    val cr2 = new ClassReader(cw.toByteArray)
    cr2.accept(getCheckClassAdapter(getTraceClassVisitor(null)), 0)
    // for debugging
    if (toFileDebugging)
      toFile(name, cw)
    //        debugWriteClass(getResourceAsStream(resource))
    defineClass(name, cw.toByteArray, 0, cw.toByteArray.length)
  }

  /**
    * Get the default TraceClassVisitor chain, which simply prints the bytecode
    *
    * @param next next ClassVisitor in the chain, usually a ClassWriter in this case
    * @return a ClassVisitor that should be accepted by ClassReader
    */
  def getTraceClassVisitor(next: ClassVisitor): ClassVisitor = new TraceClassVisitor(next, null)

  def getCheckClassAdapter(next: ClassVisitor): ClassVisitor = new CheckClassAdapter(next)

  /** Filter classes to lift
    *
    * @param name (partial) name of the class
    * @return true if the class needs to be lifted
    */
  private def shouldLift(name: String): Boolean = LiftingPolicy.shouldLiftClass(Owner(name.replace('.', '/')))


  def toFile(name: String, cw: ClassWriter) = {
    val replaced = name.replace(".", "/")
    val file = new File("lifted/" + replaced)
    file.getParentFile.mkdirs()
    val outFile = new FileOutputStream("lifted/" + replaced + ".class")
    outFile.write(cw.toByteArray)

    val sourceOutFile = new FileWriter("lifted/" + replaced + ".txt")
    val printer = new TraceClassVisitor(new PrintWriter(sourceOutFile))
    new ClassReader(cw.toByteArray).accept(printer, 0)
  }

  def debugWriteClass(is: InputStream) = {
    val cr = new ClassReader(is)
    val classNode = new ClassNode(ASM5)
    cr.accept(classNode, 0)
    val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cr.accept(cw, 0)

    toFile(classNode.name + "_", cw)
  }
}

