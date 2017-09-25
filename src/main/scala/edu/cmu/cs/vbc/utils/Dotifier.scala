package edu.cmu.cs.vbc.utils

import org.objectweb.asm._
import org.objectweb.asm.util.Printer
import org.objectweb.asm.util.Printer.{OPCODES, TYPES}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * A printer that prints dot files of classes it visits
  *
  * Note that current version relies on certain assumptions of our lifted bytecode, so
  * it might not work for some generic unlifted bytecode.
  *
  * @author chupanw
  */
class Dotifier() extends Printer(Opcodes.ASM5) {

  val textBuf: ListBuffer[String] = ListBuffer.empty[String]
  val edges: ListBuffer[(String, Label, Boolean)] = ListBuffer.empty
  val tryCatchEdges: ListBuffer[(Label, Label, Label, String)] = ListBuffer.empty
  val label2Node: mutable.Map[Label, String] = mutable.Map.empty[Label, String]
  val tab1 = "    "
  val tab2 = "        "
  val tab3 = "            "
  var currentBlock: Int = 0
  var currentMethodCount: Int = 0

  override def visit(version: Int,
                     access: Int,
                     name: String,
                     signature: String,
                     superName: String,
                     interfaces: Array[String]) = {
    textBuf += s"digraph ${name.replace('/', '_')} {\n"
  }

  override def visitSource(file: String, debug: String) = {}

  override def visitOuterClass(owner: String, name: String, desc: String) = {}

  override def visitClassAnnotation(desc: String, visible: Boolean): Dotifier = this

  override def visitClassAttribute(attr: Attribute) = {}

  override def visitInnerClass(name: String, outerName: String, innerName: String, access: Int) = {}

  override def visitField(access: Int, name: String, desc: String, signature: String, value: scala.Any) = this

  override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]) = {
    val clusterName = "cluster_" + name.replace('<', '_').replace('>', '_')
    textBuf += tab1 +
      "subgraph " + clusterName + " {\n" +
      tab2 + "label=\"" + name + "\"\n" +
      tab2 + s"b$currentMethodCount$currentBlock [fontname=monaco, shape=box, label=" +
      '\"'
    this
  }

  override def visitClassEnd() = textBuf += "}"

  override def visit(name: String, value: scala.Any) = {}

  override def visitEnum(name: String, desc: String, value: String) = {}

  override def visitAnnotation(name: String, desc: String) = this

  override def visitArray(name: String) = this

  override def visitAnnotationEnd() = {}

  override def visitFieldAnnotation(desc: String, visible: Boolean) = this

  override def visitFieldAttribute(attr: Attribute) = {}

  override def visitFieldEnd() = {}

  override def visitAnnotationDefault() = this

  override def visitMethodAnnotation(desc: String, visible: Boolean) = this

  override def visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean) = this

  override def visitMethodAttribute(attr: Attribute) = {}

  override def visitCode() = {}

  override def visitFrame(`type`: Int, nLocal: Int, local: Array[AnyRef], nStack: Int, stack: Array[AnyRef]) = {}

  override def visitInsn(opcode: Int) = {
    if (opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
      val buf = new StringBuilder
      // finish the current node
      buf ++= OPCODES(opcode) + "\\l"
      buf ++= "\"]\n"
      // start the next node
      buf ++= tab2 + s"b$currentMethodCount${currentBlock + 1} [fontname=monaco, shape=none, label=" + '\"'
      currentBlock += 1
      textBuf += buf.toString()
    }
    else
      textBuf += OPCODES(opcode) + "\\l"
  }

  override def visitIntInsn(opcode: Int, operand: Int) = {
    val buf = new StringBuilder
    buf.append(OPCODES(opcode))
      .append(' ')
      .append(if (opcode == Opcodes.NEWARRAY) TYPES(operand) else operand)
      .append("\\l")
    textBuf += buf.toString()
  }

  override def visitVarInsn(opcode: Int, `var`: Int) = {
    textBuf += OPCODES(opcode) + " " + `var` + "\\l"
  }

  override def visitTypeInsn(opcode: Int, `type`: String) = {
    textBuf += OPCODES(opcode) + " " + `type` + "\\l"
  }

  override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) = {
    textBuf += OPCODES(opcode) + " " + s"$owner.$name : $desc\\l"
  }

  override def visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, bsmArgs: AnyRef*): Unit = {
    val buf = new StringBuilder
    buf.append("INVOKEDYNAMIC " + bsmArgs(1) + "\\l")
    textBuf += buf.toString()
  }

  override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) = {
    textBuf += OPCODES(opcode) + " " + s"$owner.$name $desc\\l"
  }

  override def visitJumpInsn(opcode: Int, label: Label) = {
    val buf = new StringBuilder
    // finish the current node
    buf ++= OPCODES(opcode) + "\\l"
    buf ++= "\"]\n"
    // start the next node
    buf ++= tab2 + s"b$currentMethodCount${currentBlock + 1} [fontname=monaco, shape=box, label=" + '\"'
    edges.append((s"b$currentMethodCount$currentBlock", label, opcode == Opcodes.GOTO))
    currentBlock += 1
    textBuf += buf.toString()
  }

  override def visitLabel(label: Label) = {
    // Due to redundant labels, there might be multiple labels inside one block, but
    // one of them must be the start of the block
    if (edges.exists(e => e._2 == label)) {
      currentBlock += 1
      textBuf += "\"]\n" + tab2 + s"b$currentMethodCount$currentBlock [fontname=monaco, shape=box, label=" + '\"'
      edges.append((s"b$currentMethodCount${currentBlock - 1}", label, true))
    }
    label2Node += (label -> s"b$currentMethodCount$currentBlock")
  }

  override def visitLdcInsn(cst: scala.Any) = {
    val cstString = cst match {
      case s: String => s
      case t: Type => t.getDescriptor + ".class"
      case _ => cst.toString
    }
    textBuf += "LDC " + cstString + "\\l"
  }

  override def visitIincInsn(`var`: Int, increment: Int) = {
    textBuf += "IINC " + `var` + " " + increment + "\\l"
  }

  override def visitTableSwitchInsn(min: Int, max: Int, dflt: Label, labels: Label*) = ???  // should not happen

  override def visitLookupSwitchInsn(dflt: Label, keys: Array[Int], labels: Array[Label]) = ??? // should not happen

  override def visitMultiANewArrayInsn(desc: String, dims: Int) = {
    textBuf += "MULTIANEWARRAY " + desc + " " + dims + "\\l"
  }

  override def visitTryCatchBlock(start: Label, end: Label, handler: Label, `type`: String) = {
    tryCatchEdges.append((start, end, handler, `type`))
  }

  override def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int) = {}

  override def visitLineNumber(line: Int, start: Label) = {
    textBuf += "LINENUMBER " + line + " " + start + "\\l"
  }

  override def visitMaxs(maxStack: Int, maxLocals: Int) = {
    val buf = new mutable.StringBuilder()
    buf ++= "\"]\n"
    for ((start: String, end: Label, isGoto: Boolean) <- edges) {
      buf.append(tab2).append(start).append(" -> ").append(label2Node(end)).append("\n")
      if (!isGoto)
        buf.append(tab2).append(start).append(" -> ").append(s"${start.take(2)}${start.drop(2).toInt + 1}").append("\n")
    }
    for ((start: Label, end: Label, handler: Label, exp: String) <- tryCatchEdges) {
      val startID: Int = label2Node(start).drop(2).toInt
      val endID: Int = label2Node(end).drop(2).toInt
      val handlerBlock: String = label2Node(handler)
      val bPrefix = label2Node(start).take(2)
      for (i <- Range.inclusive(startID, endID)) {
        buf.append(tab2).append(s"$bPrefix$i -> $handlerBlock [style=dashed, headlabel=" + "\"" + s"$exp" + "\"]\n")
      }
    }
    buf ++= tab1 + "}\n"
    currentMethodCount += 1
    textBuf += buf.toString()
    edges.clear()
    tryCatchEdges.clear()
  }

  override def visitMethodEnd() = {
  }
}
