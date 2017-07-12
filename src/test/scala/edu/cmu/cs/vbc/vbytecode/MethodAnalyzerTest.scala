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
  val lessSimpleMethod = {
    var mn = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)
    val l0 = new asm.Label()

    mn.visitLabel(l0) // 0
    mn.visitLineNumber(37, l0) // 1
    mn.visitInsn(Opcodes.ICONST_1) // 2
    mn.visitVarInsn(Opcodes.ISTORE, 0) // 3
    val l1 = new asm.Label()
    mn.visitLabel(l1) // 4
    mn.visitLineNumber(38, l1) // 5
    mn.visitVarInsn(Opcodes.ILOAD, 0) // 6
    mn.visitInsn(Opcodes.ICONST_2) // 7
    mn.visitInsn(Opcodes.IREM) // 8
    val l2 = new asm.Label()
    mn.visitJumpInsn(Opcodes.IFNE, l2) // 9
    val l3 = new asm.Label()

    mn.visitLabel(l3) // 10
    mn.visitLineNumber(39, l3) // 11
    mn.visitVarInsn(Opcodes.ILOAD, 0) // 12
    mn.visitInsn(Opcodes.ICONST_2) // 13
    mn.visitInsn(Opcodes.IDIV) // 14
    mn.visitVarInsn(Opcodes.ISTORE, 0) // 15
    val l4 = new asm.Label()
    mn.visitJumpInsn(Opcodes.GOTO, l4) // 16

    mn.visitLabel(l2)  // 17
    mn.visitLineNumber(42, l2) // 18
    mn.visitFrame(Opcodes.F_APPEND, 1, Array[AnyRef](Opcodes.INTEGER), 0, null) // 19
    mn.visitVarInsn(Opcodes.ILOAD, 0) // 20
    mn.visitInsn(Opcodes.ICONST_3) // 21
    mn.visitInsn(Opcodes.IMUL) // 22
    mn.visitVarInsn(Opcodes.ISTORE, 0) // 23

    mn.visitLabel(l4)  // 24
    mn.visitLineNumber(44, l4)  // 25
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null) // 26
    mn.visitVarInsn(Opcodes.ILOAD, 0) // 27
    mn.visitInsn(Opcodes.IRETURN) // 28
    val l5 = new asm.Label()
    mn.visitLabel(l5) // 29
    mn.visitLocalVariable("x", "I", null, l1, l5, 0) // 30
    mn.visitMaxs(2, 1)

    mn
  }
  val lessSimpleMethodAnalyzer: MethodAnalyzer = {
    val ma = new MethodAnalyzer("test", lessSimpleMethod)
    ma.analyze()
    ma
  }
  val evenLessSimpleMethod = {
    var mn = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)
    val l0 = new asm.Label()
    mn.visitLabel(l0)
    mn.visitLineNumber(48, l0)
    mn.visitInsn(Opcodes.ICONST_1)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    val l1 = new asm.Label()
    mn.visitLabel(l1)
    mn.visitLineNumber(49, l1)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IREM)
    val l2 = new asm.Label()
    mn.visitJumpInsn(Opcodes.IFNE, l2)
    val l3 = new asm.Label()
    mn.visitLabel(l3)
    mn.visitLineNumber(50, l3)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IDIV)
    mn.visitInsn(Opcodes.IRETURN)
    mn.visitLabel(l2)
    mn.visitLineNumber(53, l2)
    mn.visitFrame(Opcodes.F_APPEND, 1, Array[AnyRef](Opcodes.INTEGER), 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitInsn(Opcodes.ICONST_3)
    mn.visitInsn(Opcodes.IMUL)
    mn.visitInsn(Opcodes.IRETURN)
    val l4 = new asm.Label()
    mn.visitLabel(l4)
    mn.visitLocalVariable("x", "I", null, l1, l4, 0)
    mn.visitMaxs(2, 1)

    mn
  }
  val evenLessSimpleMethodAnalyzer: MethodAnalyzer = {
    val ma = new MethodAnalyzer("test", evenLessSimpleMethod)
    ma.analyze()
    ma
  }

  val simpleLoop = {
    var mn = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)
    val l0 = new asm.Label()
    mn.visitLabel(l0)
    mn.visitLineNumber(58, l0)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    val l1 = new asm.Label()
    mn.visitLabel(l1)
    mn.visitLineNumber(59, l1)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    val l2 = new asm.Label()
    mn.visitLabel(l2)
    mn.visitFrame(Opcodes.F_APPEND, 2, Array[AnyRef](Opcodes.INTEGER, Opcodes.INTEGER), 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitInsn(Opcodes.ICONST_5)
    val l3 = new asm.Label()
    mn.visitJumpInsn(Opcodes.IF_ICMPGE, l3)
    val l4 = new asm.Label()
    mn.visitLabel(l4)
    mn.visitLineNumber(60, l4)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitInsn(Opcodes.IADD)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    val l5 = new asm.Label()
    mn.visitLabel(l5)
    mn.visitLineNumber(59, l5)
    mn.visitIincInsn(1, 1)
    mn.visitJumpInsn(Opcodes.GOTO, l2)
    mn.visitLabel(l3)
    mn.visitLineNumber(62, l3)
    mn.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitInsn(Opcodes.IRETURN)
    val l6 = new asm.Label()
    mn.visitLabel(l6)
    mn.visitLocalVariable("i", "I", null, l2, l3, 1)
    mn.visitLocalVariable("x", "I", null, l1, l6, 0)
    mn.visitMaxs(2, 2)

    mn
  }
  val simpleLoopAnalyzer: MethodAnalyzer = {
    val ma = new MethodAnalyzer("test", simpleLoop)
    ma.analyze()
    ma
  }
  
  val lessSimpleLoop = {
    var mn = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)

    val l0 = new asm.Label()
    mn.visitLabel(l0)
    mn.visitLineNumber(66, l0)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    val l1 = new asm.Label()
    mn.visitLabel(l1)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    val l2 = new asm.Label()
    mn.visitLabel(l2)
    mn.visitLineNumber(67, l2)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IREM)
    val l3 = new asm.Label()
    mn.visitJumpInsn(Opcodes.IFNE, l3)
    val l4 = new asm.Label()
    mn.visitLabel(l4)
    mn.visitLineNumber(68, l4)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    val l5 = new asm.Label()
    mn.visitJumpInsn(Opcodes.GOTO, l5)
    mn.visitLabel(l3)
    mn.visitLineNumber(71, l3)
    mn.visitFrame(Opcodes.F_APPEND, 2, Array[AnyRef](Opcodes.INTEGER, Opcodes.INTEGER), 0, null)
    mn.visitInsn(Opcodes.ICONST_1)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    mn.visitLabel(l5)
    mn.visitLineNumber(73, l5)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 2)
    val l6 = new asm.Label()
    mn.visitLabel(l6)
    mn.visitFrame(Opcodes.F_APPEND, 1, Array[AnyRef](Opcodes.INTEGER), 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    val l7 = new asm.Label()
    mn.visitJumpInsn(Opcodes.IF_ICMPGE, l7)
    val l8 = new asm.Label()
    mn.visitLabel(l8)
    mn.visitLineNumber(74, l8)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.ICONST_1)
    val l9 = new asm.Label()
    mn.visitJumpInsn(Opcodes.IF_ICMPNE, l9)
    val l10 = new asm.Label()
    mn.visitLabel(l10)
    mn.visitLineNumber(75, l10)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.IADD)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    val l11 = new asm.Label()
    mn.visitJumpInsn(Opcodes.GOTO, l11)
    mn.visitLabel(l9)
    mn.visitLineNumber(78, l9)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IMUL)
    mn.visitInsn(Opcodes.IADD)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    mn.visitLabel(l11)
    mn.visitLineNumber(73, l11)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitIincInsn(2, 1)
    mn.visitJumpInsn(Opcodes.GOTO, l6)
    mn.visitLabel(l7)
    mn.visitLineNumber(81, l7)
    mn.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitInsn(Opcodes.IRETURN)
    val l12 = new asm.Label()
    mn.visitLabel(l12)
    mn.visitLocalVariable("i", "I", null, l6, l7, 2)
    mn.visitLocalVariable("x", "I", null, l1, l12, 0)
    mn.visitLocalVariable("result", "I", null, l2, l12, 1)
    mn.visitMaxs(3, 3)

    mn
  }
  val lessSimpleLoopAnalyzer: MethodAnalyzer = {
    val ma = new MethodAnalyzer("test", lessSimpleLoop)
    ma.analyze()
    ma
  }

  val multiLoop = {
    var mn = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)

    val l0 = new asm.Label()
    mn.visitLabel(l0)
    mn.visitLineNumber(85, l0)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    val l1= new asm.Label()
    mn.visitLabel(l1)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    val l2= new asm.Label()
    mn.visitLabel(l2)
    mn.visitLineNumber(86, l2)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IREM)
    val l3= new asm.Label()
    mn.visitJumpInsn(Opcodes.IFNE, l3)
    val l4= new asm.Label()
    mn.visitLabel(l4)
    mn.visitLineNumber(87, l4)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    val l5= new asm.Label()
    mn.visitJumpInsn(Opcodes.GOTO, l5)
    mn.visitLabel(l3)
    mn.visitLineNumber(90, l3)
    mn.visitFrame(Opcodes.F_APPEND, 2, Array[AnyRef](Opcodes.INTEGER, Opcodes.INTEGER), 0, null)
    mn.visitInsn(Opcodes.ICONST_1)
    mn.visitVarInsn(Opcodes.ISTORE, 0)
    mn.visitLabel(l5)
    mn.visitLineNumber(92, l5)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitInsn(Opcodes.ICONST_0)
    mn.visitVarInsn(Opcodes.ISTORE, 2)
    val l6= new asm.Label()
    mn.visitLabel(l6)
    mn.visitFrame(Opcodes.F_APPEND, 1, Array[AnyRef](Opcodes.INTEGER), 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    val l7= new asm.Label()
    mn.visitJumpInsn(Opcodes.IF_ICMPGE, l7)
    val l8= new asm.Label()
    mn.visitLabel(l8)
    mn.visitLineNumber(93, l8)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.ICONST_1)
    val l9= new asm.Label()
    mn.visitJumpInsn(Opcodes.IF_ICMPNE, l9)
    val l10= new asm.Label()
    mn.visitLabel(l10)
    mn.visitLineNumber(94, l10)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.IADD)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    val l11= new asm.Label()
    mn.visitJumpInsn(Opcodes.GOTO, l11)
    mn.visitLabel(l9)
    mn.visitLineNumber(97, l9)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IMUL)
    mn.visitInsn(Opcodes.IADD)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    mn.visitLabel(l11)
    mn.visitLineNumber(92, l11)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitIincInsn(2, 1)
    mn.visitJumpInsn(Opcodes.GOTO, l6)
    mn.visitLabel(l7)
    mn.visitLineNumber(100, l7)
    mn.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mn.visitIntInsn(Opcodes.BIPUSH, 50)
    mn.visitVarInsn(Opcodes.ISTORE, 2)
    val l12= new asm.Label()
    mn.visitLabel(l12)
    mn.visitFrame(Opcodes.F_APPEND, 1, Array[AnyRef](Opcodes.INTEGER), 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitVarInsn(Opcodes.ILOAD, 0)
    val l13= new asm.Label()
    mn.visitJumpInsn(Opcodes.IF_ICMPLE, l13)
    val l14= new asm.Label()
    mn.visitLabel(l14)
    mn.visitLineNumber(101, l14)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IREM)
    val l15= new asm.Label()
    mn.visitJumpInsn(Opcodes.IFNE, l15)
    val l16= new asm.Label()
    mn.visitLabel(l16)
    mn.visitLineNumber(102, l16)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.ISUB)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    val l17= new asm.Label()
    mn.visitJumpInsn(Opcodes.GOTO, l17)
    mn.visitLabel(l15)
    mn.visitLineNumber(105, l15)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitVarInsn(Opcodes.ILOAD, 2)
    mn.visitInsn(Opcodes.ICONST_2)
    mn.visitInsn(Opcodes.IMUL)
    mn.visitInsn(Opcodes.ISUB)
    mn.visitVarInsn(Opcodes.ISTORE, 1)
    mn.visitLabel(l17)
    mn.visitLineNumber(100, l17)
    mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    mn.visitIincInsn(2, -(1))
    mn.visitJumpInsn(Opcodes.GOTO, l12)
    mn.visitLabel(l13)
    mn.visitLineNumber(108, l13)
    mn.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mn.visitVarInsn(Opcodes.ILOAD, 1)
    mn.visitInsn(Opcodes.IRETURN)
    val l18= new asm.Label()
    mn.visitLabel(l18)
    mn.visitLocalVariable("i", "I", null, l6, l7, 2)
    mn.visitLocalVariable("i", "I", null, l12, l13, 2)
    mn.visitLocalVariable("x", "I", null, l1, l18, 0)
    mn.visitLocalVariable("result", "I", null, l2, l18, 1)
    mn.visitMaxs(3, 3)

    mn
  }
  val multiLoopAnalyzer: MethodAnalyzer = {
    val ma = new MethodAnalyzer("test", multiLoop)
    ma.analyze()
    ma
  }

  "bBlocks" should "contain a BasicBlock for every block" in {
    val msg = "analysis of simple method has wrong number of basic blocks"
    assert(simpleMethodAnalyzer.bBlocks.size == 1, msg)
    assert(lessSimpleMethodAnalyzer.bBlocks.size == 4, msg)
    assert(evenLessSimpleMethodAnalyzer.bBlocks.size == 3, msg)

    assert(simpleLoopAnalyzer.bBlocks.size == 4, msg)
    assert(lessSimpleLoopAnalyzer.bBlocks.size == 10, msg)
    assert(multiLoopAnalyzer.bBlocks.size == 16, msg)
  }

  "retreatingEdges" should "contain all retreating edges" in {
    var msg = "analysis of simple method incorrectly finds retreating edges"
    assert(simpleMethodAnalyzer.retreatingEdges.isEmpty, msg)
    assert(lessSimpleMethodAnalyzer.retreatingEdges.isEmpty, msg)
    assert(evenLessSimpleMethodAnalyzer.retreatingEdges.isEmpty, msg)

    msg = "incorrect retreating edges"
    var re = simpleLoopAnalyzer.retreatingEdges
    assert(re.size == 1, msg)
    assert(re.get(simpleLoopAnalyzer.blockStarts(13)) contains Set(simpleLoopAnalyzer.blockStarts(8)), msg)

    re = lessSimpleLoopAnalyzer.retreatingEdges
    assert(re.size == 1, msg)
    assert(re.get(lessSimpleLoopAnalyzer.blockStarts(54)) contains Set(lessSimpleLoopAnalyzer.blockStarts(28)), msg)

    re = multiLoopAnalyzer.retreatingEdges
    assert(re.size == 2, msg)
    assert(re.get(multiLoopAnalyzer.blockStarts(54)) contains Set(multiLoopAnalyzer.blockStarts(28)), msg)
    assert(re.get(multiLoopAnalyzer.blockStarts(91)) contains Set(multiLoopAnalyzer.blockStarts(64)), msg)
  }

  "loops.size" should "be the correct number of loops" in {
    var msg = "analysis of simple method finds loops when there are none"
    assert(simpleMethodAnalyzer.loops.isEmpty, msg)
    assert(lessSimpleMethodAnalyzer.loops.isEmpty, msg)
    assert(evenLessSimpleMethodAnalyzer.loops.isEmpty, msg)

    msg = "incorrect number of loops"
    assert(simpleLoopAnalyzer.loops.size == 1, msg)
    assert(lessSimpleLoopAnalyzer.loops.size == 1, msg)
    assert(multiLoopAnalyzer.loops.size == 2, msg)
  }

  "loops" should "contain the relevant nodes in their bodies" in {
    val msg = "loops does not contain expected BasicBlocks"
    var loop = simpleLoopAnalyzer.loops.find(_ => true).get
    var body = loop.body.toList.sortWith(_.startLine < _.startLine)
    assert(body.size == 1, msg)
    assert(loop.entry.startLine == 8, msg)
    assert(body.head.startLine == 13, msg)

    loop = lessSimpleLoopAnalyzer.loops.find(_ => true).get
    body = loop.body.toList.sortWith(_.startLine < _.startLine)
    assert(body.size == 4, msg)
    assert(loop.entry.startLine == 28, msg)
    assert(body.head.startLine == 33, msg)
    assert(body.last.startLine == 54, msg)

    val loops = multiLoopAnalyzer.loops.toList.sortWith(_.entry.startLine < _.entry.startLine)
    loop = loops.head
    body = loop.body.toList.sortWith(_.startLine < _.startLine)
    assert(body.size == 4, msg)
    assert(loop.entry.startLine == 28, msg)
    assert(body.head.startLine == 33, msg)
    assert(body.last.startLine == 54, msg)

    loop = loops.last
    body = loop.body.toList.sortWith(_.startLine < _.startLine)
    assert(body.size == 4, msg)
    assert(loop.entry.startLine == 64, msg)
    assert(body.head.startLine == 69, msg)
    assert(body.last.startLine == 91, msg)
  }
}
