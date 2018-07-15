package edu.cmu.cs.vbc.loader

import org.objectweb.asm.{Label, Opcodes}
import org.objectweb.asm.tree.{InsnNode, LabelNode, MethodNode, TryCatchBlockNode}

object TryCatchBlock {

  def wrapMethodBody(m: MethodNode): MethodNode = {
    if (m.name == "<init>") return m
    val startL: LabelNode = m.instructions.getFirst match {
      case n: LabelNode => n
      case _ =>
        val s = new LabelNode(new Label("methodStart"))
        m.instructions.insert(s)
        s
    }
    val endL: LabelNode = m.instructions.getLast match {
      case n: LabelNode => n
      case _ =>
        val e = new LabelNode(new Label("methodEnd"))
        m.instructions.add(e)
        e
    }

    m.instructions.add(new InsnNode(Opcodes.ATHROW))

    val tryCatchBlock: TryCatchBlockNode = new TryCatchBlockNode(startL, endL, endL, "java/lang/Throwable")
    m.tryCatchBlocks.add(tryCatchBlock)
    m
  }
}
