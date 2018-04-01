package edu.cmu.cs.vbc.utils

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.cmu.cs.vbc.vbytecode.{MethodDesc, MethodName, Owner, TypeDesc}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.{ClassReader, ClassWriter, Handle, Type}

import scala.collection.JavaConversions._

/** Create model class for JDK classes
  *
  * Copy existing code from JDK, and then replace all JDK classes with model classes
  *
  * @param fqName
  * fully qualified name of this class
  * @author chupanw
  */
class VBCModel(fqName: String) extends LazyLogging {

  require(fqName.startsWith(VBCModel.prefix), "Cannot create model class for: " + fqName)

  val modelClsName: String = fqName.replace('.', '/')
  val originalClsName: String = modelClsName.substring(VBCModel.prefix.length + 1)

  val alwaysUseModelClass: List[String] = List(
    "model/java/lang/Integer",  // stringSize()
    "model/java/lang/Long",  // stringSize()
    "model/java/lang/System", // arrayCopy()
    "model/java/util/Arrays",  // native sorting methods
    "model/java/util/List"  // Some classes of Jetty require not lifting List, but apparently the Arrays
                            //  class is using the List model class, so we have to add it here as well.
  )

  def getModelClassBytes(isLift: Boolean) : Array[Byte] = {
    val eis = this.getClass.getClassLoader.getResourceAsStream(modelClsName + ".class")
    if (eis != null && (isLift || alwaysUseModelClass.contains(modelClsName))) {
      logger.info(s"Using existing model class: " + fqName)
      val cr = new ClassReader(eis)
      val cn = new ClassNode(ASM5)
      cr.accept(cn, 0)
      val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
      cn.accept(cw)
      cw.toByteArray
    }
    else {
      logger.info(s"Creating model class: " + fqName)
      generateModelClass()
    }
  }

  def generateModelClass(): Array[Byte] = {
    val resource: String = originalClsName + ".class"
    val is: InputStream = ClassLoader.getSystemResourceAsStream(resource)
    assert(is != null, s"class $originalClsName not found")
    val cr = new ClassReader(is)
    val cn = new ClassNode(ASM5)
    cr.accept(cn, 0)
    transformClass(cn)
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES)
    cn.accept(cw)
    // debugging
    VBCModel.toFile(fqName, cw)
    cw.toByteArray
  }

  def rename(n: String): String = Owner(n).toModel

  def rewriteTypeDesc(t: String): String = TypeDesc(t).toModel

  def rewriteMethodDesc(m: String): String = MethodDesc(m).toModels

  def transformClass(cn: ClassNode): Unit = {
    cn.interfaces = cn.interfaces.map(rename)
    cn.name = rename(cn.name)
    cn.sourceFile = null
    cn.superName = rename(cn.superName)
    // outer class, outer method
    if (cn.outerClass != null) cn.outerClass = rename(cn.outerClass)
    if (cn.outerMethodDesc != null) cn.outerMethodDesc = MethodDesc(cn.outerMethodDesc).toModels
    // fields
    cn.fields.foreach(transformField)
    // methods
    cn.methods.foreach(transformMethod)
    // inner classes
    cn.innerClasses.foreach(transformInnerClass)

//    // add additional method to String class
//    if (cn.name == Owner("java/lang/String").toModel.toString) {
//      cn.methods.add(createValueOf())
//    }
  }

  def transformInnerClass(in: InnerClassNode): Unit = {
    in.name = rename(in.name)
    if (in.outerName != null) in.outerName = rename(in.outerName)
  }

  def transformField(f: FieldNode): Unit = {
    f.desc = rewriteTypeDesc(f.desc)
  }

  def transformMethod(m: MethodNode): Unit = {
    m.desc = rewriteMethodDesc(m.desc)
    m.exceptions = m.exceptions.map(rename)
    if (m.localVariables != null) m.localVariables.foreach(lv => lv.desc = rewriteTypeDesc(lv.desc))
    m.instructions.toArray.foreach(transformInsn)
//    wrapString(m.instructions)
  }

  def transformInsn(i: AbstractInsnNode): Unit = {
    def rewriteHandle(h: Handle): Handle = {
      new Handle(h.getTag, rename(h.getOwner), h.getName, rewriteMethodDesc(h.getDesc))
    }

    def rewriteType(t: Type): Type = {
      Type.getType(MethodDesc(t.getDescriptor).toModels)
    }

    i match {
      case fi: FieldInsnNode =>
        fi.owner = rename(fi.owner)
        fi.desc = rewriteTypeDesc(fi.desc)
      case d: InvokeDynamicInsnNode =>
        d.desc = rewriteMethodDesc(d.desc)
        d.bsm = rewriteHandle(d.bsm)
        d.bsmArgs.indices foreach { i =>
          d.bsmArgs(i) match {
            case t: Type if t.getSort == Type.METHOD => d.bsmArgs(i) = rewriteType(t)
            case h: Handle => d.bsmArgs(i) = rewriteHandle(h)
            case i: Integer =>  // nothing
          }
        }
      case m: MethodInsnNode =>
        m.owner = rename(m.owner)
        m.desc = rewriteMethodDesc(m.desc)
      case ma: MultiANewArrayInsnNode =>
        ma.desc = rename(ma.desc)
      case t: TypeInsnNode =>
        try {
          t.desc = rename(t.desc)
        } catch {
          case e: IllegalArgumentException => t.desc = rewriteTypeDesc(t.desc)
        }
      case ldc: LdcInsnNode if ldc.cst.isInstanceOf[Type] =>
        val t = ldc.cst.asInstanceOf[Type]
        if (t.getSort == Type.OBJECT) {
          ldc.cst = Type.getType(TypeDesc(t.getDescriptor).toModel.desc)
        }
      case _ => // keep it as it is
    }
  }

  def wrapString(instructions: InsnList): Unit = {
    def createWrapperCall(): MethodInsnNode = new MethodInsnNode(
      INVOKESTATIC,
      Owner("java/lang/String").toModel,
      MethodName("valueOf"),
      MethodDesc(s"(Ljava/lang/String;)${Owner("java/lang/String").toModel.getTypeDesc}"),
      false
    )
    val LDCs = instructions.toArray.reverse.filter {
      case ldc: LdcInsnNode if ldc.cst.isInstanceOf[String] => true
      case _ => false
    }
    LDCs.foreach(ldc => instructions.insert(ldc, createWrapperCall()))
  }

  def createValueOf(): MethodNode = {

    val mn = new MethodNode(ASM5)
    val newOwner = Owner("java/lang/String").toModel
    mn.access = ACC_PUBLIC + ACC_STATIC
    mn.name = "valueOf"
    mn.desc = s"(Ljava/lang/String;)${Owner("java/lang/String").toModel.getTypeDesc}"
    mn.exceptions = List()
    val instructions: InsnList = new InsnList()
    instructions.add(new TypeInsnNode(NEW, newOwner))
    instructions.add(new InsnNode(DUP))
    instructions.add(new VarInsnNode(ALOAD, 0))
    instructions.add(new MethodInsnNode(
      INVOKEVIRTUAL,
      Owner("java/lang/String"),
      MethodName("getBytes"),
      MethodDesc("()[B"),
      false
    ))
    instructions.add(new MethodInsnNode(
      INVOKESPECIAL,
      newOwner,
      MethodName("<init>"),
      MethodDesc(s"([B)V"),
      false
    ))
    instructions.add(new InsnNode(ARETURN))
    mn.instructions = instructions
    mn.maxLocals = 2
    mn.maxStack = 3
    return mn
  }
}

object VBCModel {

  val prefix = "model"

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
}

class MyClassWriter(flag: Int) extends ClassWriter(flag) {
  override protected def getCommonSuperClass(type1: String, type2: String): String = {
    val t1 = if (type1.startsWith(VBCModel.prefix)) type1.substring(VBCModel.prefix.length + 1) else type1
    val t2 = if (type2.startsWith(VBCModel.prefix)) type2.substring(VBCModel.prefix.length + 1) else type2
    Owner(super.getCommonSuperClass(t1, t2)).toModel
  }
}
