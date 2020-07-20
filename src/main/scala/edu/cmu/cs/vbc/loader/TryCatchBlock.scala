package edu.cmu.cs.vbc.loader

import org.objectweb.asm.tree.{InsnNode, LabelNode, MethodNode, TryCatchBlockNode}
import org.objectweb.asm.{Label, Opcodes}

object TryCatchBlock {

  def wrapMethodBody(m: MethodNode): MethodNode = {
    if (m.instructions.size() == 0) return m
    val startL: LabelNode =
      if (m.name == "<init>") {
        val instructions = m.instructions.toArray
        val initLabel: LabelNode =
          instructions.find(
            x => x.isInstanceOf[LabelNode] && x.asInstanceOf[LabelNode].getLabel.info != null && x.asInstanceOf[LabelNode].getLabel.info.equals("EndOfInitSeq")
          ).get.asInstanceOf[LabelNode]
        initLabel
      }
      else {
        m.instructions.getFirst match {
          case n: LabelNode => n
          case _ =>
            val methodStartLabel = new Label()
            methodStartLabel.info = "methodStart"
            val s = new LabelNode(methodStartLabel)
            m.instructions.insert(s)
            s
        }
      }
    val endL: LabelNode = m.instructions.getLast match {
      case n: LabelNode => n
      case _ =>
        val methodEndLabel = new Label()
        methodEndLabel.info = "methodEnd"
        val e = new LabelNode(methodEndLabel)
        m.instructions.add(e)
        e
    }

    m.instructions.add(new InsnNode(Opcodes.ATHROW))

    val tryCatchBlock: TryCatchBlockNode = new TryCatchBlockNode(startL, endL, endL, "java/lang/Throwable")
    m.tryCatchBlocks.add(tryCatchBlock)
    m
  }
}
