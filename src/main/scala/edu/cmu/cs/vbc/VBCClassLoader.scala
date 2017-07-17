package edu.cmu.cs.vbc

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.cmu.cs.vbc.loader.{BasicBlock, Loader, Loop, MethodAnalyzer}
import edu.cmu.cs.vbc.utils.{LiftingPolicy, MyClassWriter, VBCModel}
import edu.cmu.cs.vbc.vbytecode.{Owner, VBCClassNode, VBCMethodNode}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import org.objectweb.asm.util.{CheckClassAdapter, TraceClassVisitor}
import org.objectweb.asm._

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
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES) // COMPUTE_FRAMES implies COMPUTE_MAX
    if (isLift) {
      logger.info(s"lifting $name")
      clazz.toVByteCode(cw, rewriter)
    }
    else {
      clazz.toByteCode(cw, rewriter)
    }

    val cr3 = new ClassReader(cw.toByteArray)
    val node = new ClassNode()
    cr3.accept(node, 0)
    postTransformations(node)

    val cw2 = new ClassWriter(0)
    node.accept(cw2)
    val cr2 = new ClassReader(cw2.toByteArray)

//    val cr2 = new ClassReader(cw.toByteArray)
    cr2.accept(getCheckClassAdapter(getTraceClassVisitor(null)), 0)
    // for debugging
    if (toFileDebugging)
      toFile(name, cw2)
    //        debugWriteClass(getResourceAsStream(resource))
    defineClass(name, cw2.toByteArray, 0, cw2.toByteArray.length)
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

  def postTransformations(node: ClassNode) = {
    // 1. identify blocks -> get control flow graph -- use MethodAnalyzer()
    // 2. identify loops (algorithm for doing this from graph - see dragon book)
    //    - Depth First Spanning Tree, loop section
    // 3. do transformation

    import scala.collection.JavaConversions._
    val mas = node.methods.map(mn => {
      val ma  = new MethodAnalyzer(node.name, mn)
      ma.analyze()
      (mn.name, ma)
    })
    val methodLoops = mas.map(ma => (ma._2, ma._2.loops)).filter(_._2.nonEmpty).toSet
    val listIterations = methodLoops.map(ml => (ml._1, ml._2.filter(isListIteration(_, ml._1)))).filter(_._2.nonEmpty)

    val ctxVars = listIterations.map(mloops =>
      (mloops._1, mloops._2.map(l => (l, firstBodyBlock(l).flatMap(findCtxVar)))))
    val iteratorNextInvocations = ctxVars.flatMap(ctxVar => {
      val ma = ctxVar._1
      ctxVar._2.map(l => {
        val loop = l._1
        val varIndex = l._2
        val nextInvocation = findSome(loop.body, findIteratorNextInvocation)
        (varIndex, nextInvocation, ma)
      })
    })
    iteratorNextInvocations.foreach(nextInvocation => {
      val ma = nextInvocation._3
      for { varIndex <- nextInvocation._1
            insn     <- nextInvocation._2 }
        yield unpackFEPair(insn, varIndex, ma)
    })
  }

  def narrow[To](x: Any): Option[To] = {
    x match {
      case xTo: To => Some(xTo)
      case _ => None
    }
  }
  // Find the first element e in CONTAINER such that F(e).nonEmpty
  def findSome[A, B](container: Traversable[A], f: (A => Option[B])): Option[B] = {
    for (el <- container) {
      val res = f(el)
      if (res.nonEmpty) return res
    }
    None
  }

  def firstBodyBlock(loop: Loop): Option[BasicBlock] = {
    // Sort body blocks so that find acts deterministically
    // Given 2 or more entry successors to choose from, select the latest one by sorting in descending order
    loop.body.toList.sortWith(_.startLine > _.startLine).find(block => loop.entry.successors contains block.startLine)
  }

  def findCtxLoadInsn(mInsn: MethodInsnNode): Option[VarInsnNode] = {
    // Get the ctx loading insn that precedes the given method invocation.
    // Where to find that depends on the method being invoked:
    val varInsn = (mInsn.owner, mInsn.name) match {
      case ("edu/cmu/cs/varex/VOps", "IADD") |
           ("edu/cmu/cs/varex/VOps", "IINC") |
           ("edu/cmu/cs/varex/VOps", "ISUB") |
           ("edu/cmu/cs/varex/VOps", "IMUL") |
           ("de/fosd/typechef/featureexpr/FeatureExpr", "isContradiction") => Some(mInsn.getPrevious)
      case ("edu/cmu/cs/varex/V", "one") => Some(mInsn.getPrevious.getPrevious)
      case ("edu/cmu/cs/varex/V", "choice") => Some(mInsn.getPrevious.getPrevious.getPrevious)
      case _ => None
    }
    val varInsnNode = varInsn.flatMap(narrow[VarInsnNode])
    varInsnNode
  }

  def findCtxVar(block: BasicBlock): Option[Int] = {
    import PartialFunction.cond
    val ctxLoadingInsns = Set(Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE)
    findSome(block.instructions, (insn: AbstractInsnNode) =>
      if (ctxLoadingInsns.contains(insn.getOpcode) && cond(insn) {
        case m: MethodInsnNode => m.owner.contains("edu/cmu/cs/varex/V") || m.name.contains("isContradiction")
      }) {
        val mInsn = narrow[MethodInsnNode](insn)
        val ctxLoadInsn = mInsn.flatMap(findCtxLoadInsn)
        val varIndex = ctxLoadInsn.map(_.`var`)
        varIndex
      }
      else
        None)
  }

  def findIteratorNextInvocation(block: BasicBlock): Option[MethodInsnNode] = {
    findSome(block.instructions, (insn: AbstractInsnNode) => {
      insn match {
        case invokeDynamicNode: InvokeDynamicInsnNode =>
          invokeDynamicNode.bsmArgs(1) match {
            case h: Handle if h.getName contains "INVOKEINTERFACE$next" => Some(invokeDynamicNode)
            case _ => None
          }
        case _ => None
      }
    }).flatMap(idInsn =>
      idInsn.getNext.getNext match {
      case ii: MethodInsnNode if ii.name contains "sflatMap" => Some(ii)
      case _ => None
    })
  }

  def unpackFEPair(after: AbstractInsnNode, at: Int, ma: MethodAnalyzer): Unit = {
    import edu.cmu.cs.vbc.utils.LiftUtils.{fexprclasstype, vclassname, vclasstype}
    ma.insertInsns(after, List(
      // stack =
      // ..., One(FEPair)
      new MethodInsnNode(Opcodes.INVOKEINTERFACE, vclassname, "getOne", "()Ljava/lang/Object;", true),
      new TypeInsnNode(Opcodes.CHECKCAST, "model/java/util/FEPair"),
      // ..., FEPair
      new InsnNode(Opcodes.DUP),
      // ..., FEPair, FEPair
      new FieldInsnNode(Opcodes.GETFIELD, "model/java/util/FEPair", "v", "Ljava/lang/Object;"),
      // ..., FEPair, value
      new InsnNode(Opcodes.SWAP),
      // ..., value, FEPair
      new FieldInsnNode(Opcodes.GETFIELD, "model/java/util/FEPair", "ctx", fexprclasstype),
      // ..., value, FE
      new InsnNode(Opcodes.DUP),
      // ..., value, FE, FE
      new VarInsnNode(Opcodes.ASTORE, at),

      // ..., value, FE
      new InsnNode(Opcodes.SWAP),
      // ..., FE, value
      new MethodInsnNode(Opcodes.INVOKESTATIC, vclassname, "one",
        s"(${fexprclasstype}Ljava/lang/Object;)$vclasstype", true)
      // ..., One(value)
    ))
  }

  def isListIteration(loop: Loop, ma: MethodAnalyzer): Boolean = {
    import PartialFunction.cond
    loop.entry.predecessors.map(ma.blockEnds).exists(block =>
      block.instructions.exists(cond(_) {
        case idInsn: InvokeDynamicInsnNode =>
          cond(idInsn.bsmArgs(1)) {
            case h: Handle => h.getName.contains("INVOKEVIRTUAL$iterator") && h.getDesc.contains("CtxList")
          }
      }))
  }
}
