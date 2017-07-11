package edu.cmu.cs.vbc.vbytecode

import edu.cmu.cs.vbc.loader.MethodAnalyzer
import org.objectweb.asm
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.Opcodes
import org.scalatest.FlatSpec



/**
  * Created by lukas on 7/10/17.
  */
class MethodAnalyzerTest extends FlatSpec {
  val simpleMethod: MethodNode = {
    var mn = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)
    mn.instructions
    val labels = Array(0, 1, 2, 3).map(l => new asm.Label("L" + l))
    mn.visitLabel(labels(0))
    mn.visitLineNumber(31, labels(0))
    mn.visitInsn(Opcodes.ICONST_1)
    mn.visitVarInsn(Opcodes.ISTORE, 0)

    mn.visitLabel(labels(1))
    mn.visitLineNumber(32, labels(1))
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitInsn(Opcodes.ICONST_1)
    mn.visitInsn(Opcodes.IADD)
    mn.visitVarInsn(Opcodes.ISTORE, 1)

    mn.visitLabel(labels(2))
    mn.visitLineNumber(33, labels(2))
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IMUL)
    mn.visitInsn(Opcodes.IRETURN)

    mn.visitLabel(labels(3))
    mn.visitLocalVariable("x", "I", "I", labels(1), labels(3), 0)
    mn.visitLocalVariable("y", "I", "I", labels(2), labels(3), 1)
    mn.visitMaxs(2, 2)

    mn
  }
  val simpleMethodAnalyzer: MethodAnalyzer = {
    val ma = new MethodAnalyzer("test", simpleMethod)
    ma.analyze()
    ma
  }


  "bBlocks" should "contain a BasicBlock for every block" in {
    assert(simpleMethodAnalyzer.bBlocks.size == 1,
      "analysis of simple method has wrong number of basic blocks")
  }

  "retreatingEdges" should "contain all retreating edges" in {
    val re = simpleMethodAnalyzer.retreatingEdges
    assert(simpleMethodAnalyzer.retreatingEdges.isEmpty,
      "analysis of simple method incorrectly finds retreating edges")
  }

  "loops" should "contain all loops" in {
    assert(simpleMethodAnalyzer.loops.isEmpty,
      "analysis of simple method finds loops when there are none")
  }
}
