package edu.cmu.cs.vbc.loader

import edu.cmu.cs.vbc.OpcodePrint
import edu.cmu.cs.vbc.vbytecode.{MethodDesc, TypeDesc}
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import org.objectweb.asm.tree.analysis.{Analyzer, SourceInterpreter, SourceValue}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
  * Rewrite ternary operators in constructor calls
  *
  * They are problematic because they force uninitialized objects to be wrapped and stored
  * in local variables.
  *
  * First, we use a DFA to search for init sequences that have jumps in between. Second, we
  * verify that these init sequences do not overlap, otherwise we need more complicated
  * reordering. Third, we replace each INVOKESPECIAL with a series of STORE, followed by a
  * init sequence that we expect. During bytecode lifting, this sequence will be checked and
  * abstract to a helper instruction created by us.
  *
  * @author chupanw
  */
object TernaryOperatorRewriter {

  def extractAllTernaryOperator(ownerString: String, m: MethodNode): MethodNode = {
    val (newMethodNode, changed) = extractOneTernaryOperator(ownerString, m)
    if (changed)
      extractAllTernaryOperator(ownerString, newMethodNode)
    else
      newMethodNode
  }

  def extractOneTernaryOperator(ownerString: String, m: MethodNode): (MethodNode, Boolean) = {
    val ternaryAnalyzer = new TernaryOperatorAnalyzer(m)
    val analyzer = new Analyzer[SourceValue](ternaryAnalyzer)
    analyzer.analyze(ownerString, m)

    if (ternaryAnalyzer.hasInvokeSpecialWithTernary) {
      var instructions = m.instructions.toArray
      val oldMaxLocals: Int = m.maxLocals

      m.instructions.clear()
      val sourceIndex = ternaryAnalyzer.invokeSpecialAndNew.get.sourceIndex
      val invokeSpecialIndex = ternaryAnalyzer.invokeSpecialAndNew.get.invokeSpecialIndex
      val isNEW = ternaryAnalyzer.invokeSpecialAndNew.get.isNew

      val insBeforeSouce: Array[AbstractInsnNode] = instructions.take(sourceIndex)
      val insBetweenSouceAndInvokeSpecial: Array[AbstractInsnNode] =
        if (isNEW)
          instructions.slice(sourceIndex + 2, invokeSpecialIndex)
        else
          instructions.slice(sourceIndex + 1, invokeSpecialIndex)
      val insIncludeAndAfterInvokeSpecial: Array[AbstractInsnNode] = instructions.drop(invokeSpecialIndex)
      val sourceSeq: Array[AbstractInsnNode] =
        if (isNEW)
          instructions.slice(sourceIndex, sourceIndex + 2)
        else
          Array(instructions(sourceIndex))

      val args = MethodDesc(instructions(invokeSpecialIndex).asInstanceOf[MethodInsnNode].desc).getArgs
      val (storeSeq, loadSeq) = (for (a <- args) yield {
        val argIndex = args.indexWhere(_ eq a)
        val n64Bit = args.take(argIndex).count(_.is64Bit)
        createStoreAndLoadInsn(a, oldMaxLocals + argIndex + n64Bit)
      }) unzip
      val newInstructions = insBeforeSouce ++
        insBetweenSouceAndInvokeSpecial ++
        storeSeq.reverse ++
        sourceSeq ++
        loadSeq ++
        insIncludeAndAfterInvokeSpecial
      for (i <- newInstructions) m.instructions.add(i)
      m.maxLocals = oldMaxLocals + args.length + args.count(_.is64Bit)
    }
    (m, ternaryAnalyzer.hasInvokeSpecialWithTernary)
  }

  def createStoreAndLoadInsn(t: TypeDesc, idx: Int): (VarInsnNode, VarInsnNode) = t match {
    case TypeDesc("I") | TypeDesc("S") | TypeDesc("C") | TypeDesc("Z") | TypeDesc("B") =>
      (new VarInsnNode(ISTORE, idx), new VarInsnNode(ILOAD, idx))
    case TypeDesc("J") => (new VarInsnNode(LSTORE, idx), new VarInsnNode(LLOAD, idx))
    case TypeDesc("F") => (new VarInsnNode(FSTORE, idx), new VarInsnNode(FLOAD, idx))
    case TypeDesc("D") => (new VarInsnNode(DSTORE, idx), new VarInsnNode(DLOAD, idx))
    case _ => (new VarInsnNode(ASTORE, idx), new VarInsnNode(ALOAD, idx))
  }
}

/**
  * Find constructor calls that require rewriting of ternary operators
  *
  * When we find a invokespecial init call, we extract the ternary operators and store
  * the results before loading the caller of invokespecial.
  */
class TernaryOperatorAnalyzer(mn: MethodNode) extends SourceInterpreter(Opcodes.ASM5) {
  /**
    * Pairs of INVOKESPECIAL index and indexes of its sources
    */
  var invokeSpecialAndNew: Option[InvokeSpecialWithTernary] = None

  def hasInvokeSpecialWithTernary: Boolean = invokeSpecialAndNew.isDefined

  /**
    * Find constructor calls that have jumps in init sequence
    *
    * We make two assumptions:
    * 1. Caller of INVOKESPECIAL comes from one source, which is DUP
    * 2. The instruction lexically proceeding DUP is NEW
    */
  override def naryOperation(insn: AbstractInsnNode, values: java.util.List[_ <: SourceValue]) = {
    if (insn.getOpcode == Opcodes.INVOKESPECIAL) {
      val methodInsn = insn.asInstanceOf[MethodInsnNode]
      val methodInsnIndex = mn.instructions.indexOf(insn)
      if (methodInsn.name == "<init>") {
        val ref = values.get(0)
        val refSources = ref.insns.asScala
        assume(refSources.size == 1,
          "Caller of INVOKESPECIAL comes from more than one source")
        val fromNEW = !isALOAD0(refSources.head)  // either ALOAD 0 or NEW
        val sourceIndex =
          if (fromNEW && isDUP(refSources.head))
            mn.instructions.indexOf(refSources.head) - 1
          else
            mn.instructions.indexOf(refSources.head)
        if (fromNEW)
          assume(mn.instructions.get(sourceIndex).getOpcode == Opcodes.NEW,
            "Expecting a NEW, but found " + OpcodePrint.print(mn.instructions.get(sourceIndex).getOpcode))
        if (hasJumpBetween(sourceIndex, methodInsnIndex))
          invokeSpecialAndNew = Some(InvokeSpecialWithTernary(sourceIndex, methodInsnIndex, fromNEW))
      }
    }
    super.naryOperation(insn, values)
  }

  private def isALOAD0(i: AbstractInsnNode): Boolean = i.getOpcode == Opcodes.ALOAD && i.asInstanceOf[VarInsnNode].`var` == 0
  private def isDUP(i: AbstractInsnNode): Boolean = i.getOpcode == Opcodes.DUP
  private def hasJumpBetween(start: Int, end: Int): Boolean = {
    for (i <- Range(start + 1, end)) {
      if (mn.instructions.get(i).isInstanceOf[JumpInsnNode]) return true
    }
    false
  }
}

case class InvokeSpecialWithTernary(sourceIndex: Int,
                                    invokeSpecialIndex: Int,
                                    isNew: Boolean // if not NEW, we assume the source to be ALOAD 0
                                   )
