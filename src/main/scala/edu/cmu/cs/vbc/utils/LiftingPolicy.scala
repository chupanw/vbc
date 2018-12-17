package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.ModelConfig
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

  private var currentConfig: ModelConfig = ModelConfig.defaultConfig
  private[vbc] def setConfig(_config: ModelConfig) = currentConfig = _config
  def getConfig: ModelConfig = currentConfig

  /**
    * Return true if we lift this class
    *
    * By default, all JDK classes are copied as model classes, except for a few that we do not
    * want to change their names. For modeled JDK classes, we need to decided which of them
    * should be lifted. Reasons for lifting JDK classes include, but not limited to:
    *
    * - program classes extend JDK classes
    *
    * @param owner  The actual class name that will appear in the lifted bytecode.
    *               If this is a JDK class, it will be prefixed with "model".
    */
  def shouldLiftClass(owner: Owner): Boolean = {
    if (currentConfig.jdkLiftingClasses.exists(n => owner.name.matches("model.*" + n))) return true
    if (currentConfig.libraryLiftingClasses.exists(n => owner.name.matches(".*" + n))) true
    else if ((owner.name.startsWith("edu/uclm/esi/iso5/juegos/monopoly") || owner.name.startsWith("org/apache/commons/math") || owner.name.startsWith("edu/cmu/cs/vbc/prog/")) && !currentConfig.programNotLiftingClasses.exists(n => owner.name.matches(".*" + n)))
      true
    else false
  }

  /**
    * Return true if we want to life this method call
    *
    * The parameter ``owner`` might be renamed to model classes later.
    */
  def shouldLiftMethodCall(owner: Owner, name: MethodName, desc: MethodDesc): Boolean = {
    if (currentConfig.notLiftingClasses.exists(n => owner.name.matches(n))) false
    else if (currentConfig.notLiftingClasses.exists(n => (VBCModel.prefix + "/" + owner.name).matches(n))){
      // Sometimes we create model classes, but use them like other JDK classes that we don't lift
      // e.g., model.java.util.URLClassLoader, we need it because we want to lift classes loaded from urls.
      false
    }
    else if (owner == Owner.getVOps) false
    else true
  }

  /**
    * Return true if we want to lift this field
    *
    * @todo return the actual model class name if we decided to lift this method call.
    */
  def shouldLiftField(owner: Owner, name: FieldName, desc: TypeDesc): Boolean = {
    (owner, name, desc) match {
      case (Owner("java/lang/System"), FieldName("out"), _) => false
      case (Owner("java/lang/System"), FieldName("err"), _) => false
      case (Owner("java/util/Locale"), _, _) => false
      case (Owner("java/lang/Boolean"), _, _) => false
      case (Owner("java/lang/Byte"), FieldName("TYPE"), _) => false
      case (Owner("java/lang/Integer"), FieldName("TYPE"), _) => false
      case (Owner("java/lang/Character"), FieldName("TYPE"), _) => false
      case (Owner("java/lang/Float"), FieldName("TYPE"), _) => false
      case (Owner("java/lang/Double"), FieldName("TYPE"), _) => false
      case (Owner("java/lang/Long"), FieldName("TYPE"), _) => false
      case (Owner("java/lang/Short"), FieldName("TYPE"), _) => false
      case (Owner("java/lang/Void"), FieldName("TYPE"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/api/SeverityLevel"), FieldName("ERROR"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/api/SeverityLevel"), FieldName("INFO"), _) => false
      case (Owner("java/nio/charset/CodingErrorAction"), FieldName("REPLACE"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/LineSeparatorOption"), FieldName("SYSTEM"), _) => false
      case (Owner("java/math/BigInteger"), FieldName("ONE"), _) => false
      case (Owner("java/math/BigInteger"), FieldName("ZERO"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/whitespace/PadOption"), _, _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/blocks/BlockOption"), FieldName("STMT"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/blocks/LeftCurlyOption"), _, _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/blocks/RightCurlyOption"), _, _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/api/Scope"), _, _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/whitespace/WrapOption"), _, _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/annotation/AnnotationUseStyleCheck$ElementStyle"), FieldName("COMPACT_NO_ARRAY"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/annotation/AnnotationUseStyleCheck$TrailingArrayComma"), FieldName("NEVER"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/annotation/AnnotationUseStyleCheck$ClosingParens"), FieldName("NEVER"), _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/checks/imports/ImportOrderOption"), _, _) => false
      case (Owner("antlr/TokenStreamRecognitionException"), _, _) => false
      case (Owner("edu/cmu/cs/vbc/prog/checkstyle/TreeWalker$AstState"), _, _) => false
      case (Owner("java/io/File"), _, _) => false
      case (Owner("org/eclipse/jetty/io/Buffers$Type"), _, _) => false
      case (Owner("java/util/concurrent/TimeUnit"), _, _) => false
      case (Owner("org/eclipse/jetty/server/DispatcherType"), _, _) => false
      case (Owner("org/eclipse/jetty/webapp/MetaDataComplete"), _, _) => false
      case (Owner("org/eclipse/jetty/webapp/Origin"), _, _) => false
      case (Owner("org/eclipse/jetty/util/URIUtil"), _, _) => false
      case (Owner("org/apache/commons/math3/exception/util/LocalizedFormats"), _, _) => false
      case (Owner("java/math/BigDecimal"), _, _) => false
      case (Owner("java/math/RoundingMode"), _, _) => false
      case (Owner("java/math/MathContext"), _, _) => false
      case (Owner("java/lang/String"), _, _) => false
      case (Owner("org/apache/commons/math3/util/ResizableDoubleArray$ExpansionMode"), _, _) => false
      case (Owner("org/apache/commons/math3/dfp/DfpField$RoundingMode"), _, _) => false
      case (Owner("org/apache/commons/math3/util/MathArrays$OrderDirection"), _, _) => false
      case (Owner("org/apache/commons/math3/analysis/solvers/AllowedSolution"), _, _) => false
      case (Owner("org/apache/commons/math3/analysis/solvers/BaseSecantSolver$Method"), _, _) => false
      case (Owner("org/apache/commons/math3/stat/ranking/NaNStrategy"), _, _) => false
      case (Owner("org/apache/commons/math3/stat/ranking/TiesStrategy"), _, _) => false
      case (Owner("org/apache/commons/math3/stat/clustering/KMeansPlusPlusClusterer$EmptyClusterStrategy"), _, _) => false
      case (Owner("org/apache/commons/math3/stat/clustering/DBSCANClusterer$PointStatus"), _, _) => false
      case (Owner("org/apache/commons/math3/util/MathArrays$Position"), _, _) => false
      case _ =>
        if (currentConfig.notLiftingClasses.exists(n => owner.name.matches(n)))
          false
        else
          true

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
        LiftedCall(owner.toModel, name.rename(desc.toModels), desc.toVs.appendFE.toVReturnTypeIfReturningVoid, isLifting = true)
      }
    }
    else {
      replaceCall(owner, name, desc, isVE = true)
    }
  }

  def replaceCall(owner: Owner, name: MethodName, desc: MethodDesc, isVE: Boolean): LiftedCall = {
    val v = "Ledu/cmu/cs/varex/V;"
    val fe = "Lde/fosd/typechef/featureexpr/FeatureExpr;"
    // Although we are not lifting this method call, we might replace this call with our specialized
    // call because:
    //  (1) avoid native
    //  (2) activate our array expanding by changing the signature of System.arraycopy.
    //  (3) package access method
    (owner.name, name.name, desc.descString) match {
//      case ("java/lang/System", "arraycopy", _) if isVE =>
//        // We need array argument type in order to trigger array expansions and compressions.
//        LiftedCall(Owner(VBCModel.prefix + "/java/lang/System"), name, MethodDesc(s"([${TypeDesc.getObject}I[${TypeDesc.getObject}II)V"), isLifting = false)
      case ("java/lang/System", "arraycopy", _) if isVE =>
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/System"), MethodName("vArrayCopy"), MethodDesc(s"($v$v$v$v$v$fe)$v"), isLifting = true)
      case ("java/lang/Integer", "stringSize", _) =>
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/Integer"), name, desc, isLifting = false)
      case ("java/lang/Long", "stringSize", _) =>
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/Long"), name, desc, isLifting = false)
      case ("java/lang/Integer", "getChars", _) =>
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/Integer"), name, desc, isLifting = false)
      case ("java/lang/Long", "getChars", _) =>
        LiftedCall(Owner(VBCModel.prefix + "/java/lang/Long"), name, desc, isLifting = false)
      case (o, _, _) if o.startsWith("[") && isVE =>
        LiftedCall(Owner(s"[Ledu/cmu/cs/varex/V;"), name, desc, isLifting = false)
      case ("java/lang/Object", "hashCode", "()I") =>
        LiftedCall(owner, name, desc, isLifting = true)
      case ("java/lang/Object", "equals", "(Ljava/lang/Object;)Z") =>
        LiftedCall(owner, name, desc, isLifting = true)
      case ("java/lang/Object", "toString", "()Ljava/lang/String;") =>
        LiftedCall(owner, name, desc, isLifting = true)
      case ("edu/cmu/cs/varex/VOps", _, _) =>
        LiftedCall(owner, name, desc, isLifting = false)
      case _ => LiftedCall(owner.toModel, name, desc.toModels, isLifting = false)
    }
  }
}
