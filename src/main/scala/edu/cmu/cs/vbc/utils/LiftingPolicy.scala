package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode._

/**
  * Define lifting policy for methods and fields.
  *
  * In general, all classes in JDK should be lifted automatically. However, there are some
  * cases where default lifting is inefficient and we might need model classes. This object
  * defines which classes/methods/fields in JDK should be substituted, and which substitution
  * should we use.
  *
  * @todo Specify policies in configuration files or use drop-in.
  */
object LiftingPolicy {

  def shouldLiftClass(owner: Owner): Boolean = owner.name match {
    case x if x.startsWith("edu/cmu/cs/vbc/prog/") => true
    case x if x.endsWith("java/lang/StringBuilder") => true
    case x if x.endsWith("java/lang/AbstractStringBuilder") => true
    case x if x.contains("java/util/ArrayList") => true // so that inner classes could be included.
    case x if x.endsWith("java/util/AbstractList") => true
    case x if x.endsWith("java/util/AbstractCollection") => true
    case x if x.endsWith("java/lang/Enum") => true
    case x if x.contentEquals("model/java/util/Collection") => true
    case x if x.endsWith("java/util/Iterator") => true // false
    case x if x.endsWith("model/java/util/Iterator") => true
//    case x if x.endsWith("java/util/List") => true
    case x if x.endsWith("java/util/Collections") => true
    case x if x.endsWith("java/util/Collections$EmptySet") => true
    case x if x.endsWith("java/util/Collections$EmptyList") => true
    case x if x.endsWith("java/util/Collections$EmptyMap") => true
    case x if x.endsWith("java/util/AbstractSet") => true
    case x if x.endsWith("java/util/AbstractMap") => true
//    case x if x.endsWith("java/util/LinkedList") => true
//    case x if x.endsWith("java/util/LinkedList$Node") => true
//    case x if x.endsWith("java/util/LinkedList$ListItr") => true
    case x if x.endsWith("java/util/ListIterator") => true
    case x if x.endsWith("java/util/AbstractSequentialList") => true
    case _ => false
  }

  /**
    * Return true if we want to life this method call
    *
    * @todo return the actual model class name if we decided to lift this method call.
    */
  def shouldLiftMethodCall(owner: Owner, name: MethodName, desc: MethodDesc): Boolean = {
    if (shouldLiftClass(owner)) true
    else {
      owner match {
        case Owner("java/lang/Integer") => false
        case Owner("java/lang/Character") => false
        case Owner("java/lang/Short") => false
        case Owner("java/lang/Byte") => false
        case Owner("java/lang/String") => false
        case Owner("java/lang/Object") => false
        case Owner("java/io/PrintStream") => false
        case Owner("java/lang/Math") => false
        case Owner("java/lang/System") => false
        case Owner("java/lang/Class") => false
        case Owner("java/net/URL") => false
        case Owner("java/io/File") => false
        case Owner("java/io/FileReader") => false
        case Owner("java/io/Reader") => false
//        case Owner("model/java/util/LinkedList") => false
        case o if o.name.endsWith("Exception") => false
        case _ => true
      }
    }
  }

  /**
    * Return true if we want to lift this field
    *
    * @todo return the actual model class name if we decided to lift this method call.
    */
  def shouldLiftField(owner: Owner, name: FieldName, desc: TypeDesc): Boolean = {
    (owner, name, desc) match {
      case (Owner("java/lang/System"), FieldName("out"), _) => false
      case _ => true
    }
  }

  case class LiftedCall(owner: Owner, name: MethodName, desc: MethodDesc, isLifting: Boolean)

  def liftCall(owner: Owner, name: MethodName, desc: MethodDesc): LiftedCall = {
    val shouldLiftMethod = LiftingPolicy.shouldLiftMethodCall(owner, name, desc)
    if (shouldLiftMethod) {
      if (name.contentEquals("<init>")) {
        // cpwtodo: handle exception in <init>
        LiftedCall(owner.toModel, name, desc.toVs_AppendFE_AppendArgs, isLifting = true)
      } else {
        LiftedCall(owner.toModel, name.rename(desc.toModels), desc.toVs.appendFE.toVReturnType, isLifting = true)
      }
    }
    else {
      replaceCall(owner, name, desc, isVE = true)
    }
  }

  def replaceCall(owner: Owner, name: MethodName, desc: MethodDesc, isVE: Boolean): LiftedCall = {
    // Although we are not lifting this method call, we might replace this call with our specialized
    // call because:
    //  (1) avoid native
    //  (2) activate our array expanding by changing the signature of System.arraycopy.
    //  (3) package access method
    (owner.name, name.name, desc.descString) match {
      case ("java/lang/System", "arraycopy", _) if isVE =>
        // We need array argument type in order to trigger array expansions and compressions.
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/System"), name, MethodDesc(s"([${TypeDesc.getObject}I[${TypeDesc.getObject}II)V"), isLifting = false)
      case ("java/lang/Integer", "stringSize", _) =>
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/Integer"), name, desc, isLifting = false)
      case ("java/lang/Integer", "getChars", _) =>
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/Integer"), name, desc, isLifting = false)
      case _ => LiftedCall(owner.toModel, name, desc.toModels, isLifting = false)
    }
  }
}
