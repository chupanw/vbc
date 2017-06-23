package edu.cmu.cs.vbc.vbytecode

import javax.lang.model.SourceVersion

import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.utils.VBCModel
import org.objectweb.asm.Type

/**
  * Wrapper for method and field owner.
  *
  * In ASM library, owner is a class name (or array type) represented by a String. By declaring it as a class,
  * we can avoid messing up with the order of owner, name and desc in cases like:
  *
  * {{{mv.visitMethodInsn(INVOKESPECIAL, owner, name, desc, itf)}}}
  *
  * We might also add some checking to ensure this is actually a valid class name or valid array type.
  *
  * @author chupanw
  */
case class Owner(name: String) extends TypeVerifier {
  require(isValidInternalName(name), s"Invalid Owner name: $name")

  override def equals(obj: scala.Any): Boolean = obj match {
    case o: Owner => name == o.name
    case s: String => name == s
    case _ => false
  }

  def isValidInternalName(s: String): Boolean = {
    if (s.startsWith("[")) {
      // array type
      isValidType(s.tail)
    }
    else {
      // Not array type, then it should be class
      // Should be a valid Java class name
      // Fully qualified name should be separated by "/" instead of "."
      !name.contains('.') && SourceVersion.isName(s.replace('/', '.'))
    }
  }

  def getTypeDesc: TypeDesc = TypeDesc(Type.getObjectType(name).getDescriptor)

  val modelExceptionList = List(
    // inheritance hierarchy
    "java/lang/Object",
    // some static field initial values are String constants
    "java/lang/String",
    // ATHROW can only throw java/lang/Throwable
    "java/lang/Throwable",
    // UnsatisfiedLinkError because of natives
    "java/lang/System",
    // because we are not lifting System
    "java/io/PrintStream",
    // reflection and exception handling
    "java/lang/Class",
    // Integer call a package access method from java/lang/Class
    "java/lang/Integer", "java/lang/Short", "java/lang/Byte", "java/lang/Float", "java/lang/Double", "java/lang/Character",
    // Native
    "java/lang/Math",
    // I/O natives
    "java/io/UnixFileSystem", "java/io/FileSystem", "java/io/DefaultFileSystem", "java/io/File", "java/net/URL",
    "java/io/FileInputStream", "java/io/InputStream"
  )

  /** Get the corresponding model class
    *
    * @return
    * Object -> prepend "model/"
    * Array -> [baseType.toModel
    */
  def toModel: Owner = name match {
    case s: String if s.startsWith("[") => Owner("[" + TypeDesc(s.tail).toModel)
    case s: String if s.startsWith("java") =>
      if (modelExceptionList.contains(name))
        this
      else if (name.endsWith("Exception") || name.endsWith("Error"))
        this
      else
        Owner(VBCModel.prefix + "/" + name)
    case _ => this
  }

  override def toString: String = name
}

/** Store implicit conversion to String, avoid changing too much existing code. */
object Owner {
  implicit def ownerToString(owner: Owner): String = owner.name

  def getInt = Owner("java/lang/Integer")
  def getShort = Owner("java/lang/Short")
  def getByte = Owner("java/lang/Byte")
  def getBoolean = Owner("java/lang/Boolean")
  def getString = Owner("java/lang/String")
  def getLong = Owner("java/lang/Long")
  def getException = Owner("java/lang/Exception")
  def getFloat = Owner("java/lang/Float")
  def getDouble = Owner("java/lang/Double")
  def getArrayOps = Owner("edu/cmu/cs/varex/ArrayOps")
  def getVOps = Owner("edu/cmu/cs/varex/VOps")
  def getChar = Owner("java/lang/Character")
  def getSystem = Owner("java/lang/System")
}


/**
  * Wrapper for method name
  */
case class MethodName(name: String) {
  require(name == "<init>" || name == "<clinit>" || SourceVersion.isIdentifier(name), s"Invalid method name: $name")

  override def equals(obj: scala.Any): Boolean = obj match {
    case MethodName(s) => s == name
    case s: String => s == name
    case _ => false
  }

  override def toString: String = name

  def rename(desc: MethodDesc): MethodName = {
    def replace(s: String): String = s.replace(";", "").replace("/", "_").replace("[", "Array_")
    name match {
      case "<init>" | "<clinit>" | "___clinit___" | "______clinit______" => this
      case _ =>
        val args = desc.getArgs.map(_.toModel)
        val argsString = replace(args.mkString("__", "_", "__"))
        val retString = replace(desc.getReturnType.map(_.toModel).map(_.toString).getOrElse("V"))
        MethodName(name + argsString + retString)
    }
  }

  def liftCLINIT: MethodName = name match {
    case "<clinit>" => MethodName("______clinit______")
    case _ => this
  }
}

object MethodName {
  implicit def methodNameToString(m: MethodName): String = m.name
}


/**
  * Wrapper for field name
  */
case class FieldName(name: String) {
  require(SourceVersion.isIdentifier(name), s"Invalid field name: $name")

  override def equals(obj: scala.Any): Boolean = obj match {
    case FieldName(f) => f == name
    case s: String => name == s
    case _ => false
  }

  override def toString: String = name
}

object FieldName {
  implicit def fieldNameToString(f: FieldName): String = f.name
}


/**
  * Wrapper for method descriptor
  */
case class MethodDesc(descString: String) extends TypeVerifier {
  require(isValidMethod(descString), s"Invalid method descriptor: $descString")

  val mt = Type.getMethodType(descString)

  override def equals(obj: scala.Any): Boolean = obj match {
    case MethodDesc(md) => md == descString
    case s: String => descString == s
    case _ => false
  }

  def getArgCount: Int = mt.getArgumentTypes.size

  /** Get the return type if not void
    *
    * @return
    * None if void
    */
  def getReturnType: Option[TypeDesc] = if (isReturnVoid) None else Some(TypeDesc(Type.getReturnType(descString).getDescriptor))

  // Get the return type as a V or Vint, as appropriate
  def getReturnTypeAsV: String = if (isReturnIntOrBool) vintclasstype else vclasstype

  def getReturnTypeSort: Int = mt.getReturnType.getSort

  def isReturnVoid: Boolean = Type.getReturnType(descString).getDescriptor == "V"

  def isReturnIntOrBool: Boolean = getReturnType.getOrElse("") == "I" || getReturnType.getOrElse("") == "Z"

  def getArgs: Array[TypeDesc] = Type.getMethodType(descString).getArgumentTypes.map(t => TypeDesc(t.getDescriptor))

  override def toString: String = descString

  /** Append FeatureExpr parameter to the parameter list
    *
    * @return
    * transformed MethodDesc
    */
  def appendFE: MethodDesc = {
    val args = Type.getArgumentTypes(descString) :+ Type.getType(fexprclasstype)
    val argsString = args.map(_.getDescriptor).mkString("(", "", ")")
    val retString = Type.getReturnType(descString).getDescriptor
    MethodDesc(argsString + retString)
  }

  /** Turn all arguments into Vs, append FeatureExpr and finally append original argument types
    *
    * Only <init> method requires this transformation
    *
    * @return
    *         transformed method descriptor
    */
  def toVs_AppendFE_AppendArgs: MethodDesc = {
    val args = getArgs.map(_.toModel)
    assert(!args.exists(_.contentEquals(vclasstype)), "could not append V argument types")
    val argsString = (toVs.appendFE.getArgs ++ args).mkString("(", "", ")")
    val retString = "V" // because this is <init>
    MethodDesc(argsString + retString)
  }

  /** Transform all primitive types to corresponding Object types
    *
    * @return
    * transformed MethodDesc
    */
  def toObjects: MethodDesc = {
    val args = Type.getArgumentTypes(descString)
    val argsString: String = args.map(t => TypeDesc(t.getDescriptor).toObject).mkString("(", "", ")")
    val retString: String = if (isReturnVoid) "V" else getReturnType.get.toObject
    MethodDesc(argsString + retString)
  }

  /** Change all arguments and return type (if not void) to V.
    *
    * @return
    * transformed MethodDesc
    */
  def toVs: MethodDesc = {
    val args = Type.getArgumentTypes(descString)
    val vArgs = args.map(t => if (t.getSort == Type.INT || t.getSort == Type.BOOLEAN) vintclasstype else vclasstype)
    val argsString = vArgs.mkString("(", "", ")")
    val retString: String = if (isReturnVoid) "V" else getReturnTypeAsV
    MethodDesc(argsString + retString)
  }

  /** Change arguments and return type (if not void) to corresponding model class
    *
    * @return
    */
  def toModels: MethodDesc = {
    val args = Type.getArgumentTypes(descString)
    assert(!args.exists(_.getDescriptor == vclasstype), "Arguments are already V types")
    val argsString: String = args.map(t => TypeDesc(t.getDescriptor).toModel).mkString("(", "", ")")
    val retString: String = if (isReturnVoid) "V" else getReturnType.get.toModel
    MethodDesc(argsString + retString)
  }

  /** Change method return type to V
    *
    * @return
    *         transformed MethodDesc
    */
  def toVReturnType: MethodDesc = {
    if (isReturnVoid) {
      val args = Type.getArgumentTypes(descString)
      val argsString: String = args.map(_.toString).mkString("(", "", ")")
      val retString: String = getReturnTypeAsV
      MethodDesc(argsString + retString)
    }
    else
      this
  }
}

object MethodDesc {
  implicit def methodDescToString(md: MethodDesc): String = md.descString
}


/**
  * Wrapper for field descriptor
  */
case class TypeDesc(desc: String) extends TypeVerifier {
  require(isValidType(desc), s"Invalid field descriptor: $desc")

  def this(t: Type) = this(t.getDescriptor)

  override def equals(obj: scala.Any): Boolean = obj match {
    case t: TypeDesc => desc == t.desc
    case s: String => desc == s
    case _ => false
  }

  def isPrimitive: Boolean = desc == "Z" || desc == "C" || desc == "B" || desc == "S" || desc == "I" || desc == "F" ||
    desc == "J" || desc == "D"

  def hasVPrim: Boolean = isPrimitive && (desc match {
    case "I" | "Z" | "C" | "B" => true
    case _ => false
  })

  def isPrimitiveWithV: Boolean = isPrimitive && hasVPrim

  def VPrimTypes: Set[String] = Set(vintclasstype)

  def isVPrim: Boolean = VPrimTypes.contains(desc)

  def toVPrimType: String = desc match {
    case "I" | "Z" | "C" | "B" => vintclasstype
    case d => d
  }

  def toVPrimName: String = toVPrimType match {
    case `vintclasstype` => vintclassname
    case d => d
  }

  def toObject: TypeDesc = desc match {
    case "Z" => TypeDesc("Ljava/lang/Boolean;")
    case "C" => TypeDesc("Ljava/lang/Character;")
    case "B" => TypeDesc("Ljava/lang/Byte;")
    case "S" => TypeDesc("Ljava/lang/Short;")
    case "I" => TypeDesc("Ljava/lang/Integer;")
    case "F" => TypeDesc("Ljava/lang/Float;")
    case "J" => TypeDesc("Ljava/lang/Long;")
    case "D" => TypeDesc("Ljava/lang/Double;")
    case _ => this
  }

  def castInt: TypeDesc = this match {
    case TypeDesc("Z") => TypeDesc("I")
    case TypeDesc("C") => TypeDesc("I")
    case TypeDesc("S") => TypeDesc("I")
    case TypeDesc("B") => TypeDesc("I")
    case _ => this
  }

  def toVType: TypeDesc = TypeDesc(if (isPrimitiveWithV) toVPrimType else vclasstype)

  def toVName: String = if (isPrimitiveWithV) toVPrimName else vclassname

  def toVPrimFunction: String = desc match {
    case "I" | "Z" | "C" | "B" | `vintclasstype` => "toVint"
    case _ => ???
  }

  def isArray: Boolean = desc(0) == '['

  def getArrayBaseType: TypeDesc = {
    assert(isArray, "Can't get base type from non-array type")
    TypeDesc(desc.tail)
  }

  /** Get the owner (if exists) of this type.
    *
    * @return
    * none if current type is array or primitive
    */
  def getOwner: Option[Owner] =
    if (isArray || isPrimitive)
      None
    else
      Some(Owner(desc.tail.init))

  override def toString: String = desc

  /** Transform to corresponding model class
    *
    * @return
    * Object -> model class descriptor
    * Array -> [baseType.toModel
    * Primitive -> unchanged
    */
  def toModel: TypeDesc = {
    if (isArray)
      TypeDesc("[" + getArrayBaseType.toModel)
    else if (isPrimitive)
      this
    else
      getOwner.get.toModel.getTypeDesc
  }

  def toVArray: TypeDesc = if (isArray) TypeDesc("[" + vclasstype) else this
}

object TypeDesc {
  implicit def typeDescToString(td: TypeDesc): String = td.desc

  def getInt: TypeDesc = TypeDesc("Ljava/lang/Integer;")
  def getString: TypeDesc = TypeDesc("Ljava/lang/String;")
  def getLong: TypeDesc = TypeDesc("Ljava/lang/Long;")
  def getException: TypeDesc = TypeDesc("Ljava/lang/Exception;")
  def getObject: TypeDesc = TypeDesc("Ljava/lang/Object;")
  def getChar: TypeDesc = TypeDesc("Ljava/lang/Character;")
  def getBoolean: TypeDesc = TypeDesc("Ljava/lang/Boolean;")
}

trait TypeVerifier {

  def isValidType(s: String): Boolean = s.size match {
    case 1 => s == "Z" || s == "C" || s == "B" || s == "S" || s == "I" || s == "F" || s == "J" || s == "D"
    case o if s.startsWith("L") && s.endsWith(";") => SourceVersion.isName(s.init.tail.replace('/', '.'))
    case array if s.startsWith("[") => isValidType(s.tail)
    case _ => false
  }

  def isValidParameterList(prefix: String, s: String): Boolean = {
    if (s.isEmpty) {
      prefix.isEmpty
    }
    else {
      if (isValidType(prefix + s.head))
        isValidParameterList("", s.tail)
      else
        isValidParameterList(prefix + s.head, s.tail)
    }
  }

  def isValidMethod(s: String): Boolean =
    if (s.startsWith("(") && s.contains(")")) {
      val split = s.tail.split(')')
      split.size == 2 && isValidParameterList("", split(0)) && (isValidType(split(1)) || split(1) == "V")
    }
    else
      false
}
