package edu.cmu.cs.vbc.vbytecode

import edu.cmu.cs.vbc.loader.Loader
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm
import org.objectweb.asm.tree.{ClassNode, MethodNode}
import org.objectweb.asm.Opcodes
import org.scalatest.FlatSpec

import PartialFunction._


/**
  * Created by lukas on 7/10/17.
  */
class CFGLoopAnalysisTest extends FlatSpec {

  /*  Method code:

    private static int simple() {
        int x = 1;
        int y = x + 1;
        return y*2;
    }

    private static int lessSimple() {
        int x  = 1;
        if (x%2 == 0) {
            x /= 2;
        }
        else {
            x *= 3;
        }
        return x;
    }

    private static int evenLessSimple() {
        int x = 1;
        if (x%2 == 0) {
            return x/2;
        }
        else {
            return x*3;
        }
    }

    private static int simpleLoop() {
        int x = 0;
        for (int i = 0; i < 5; i++) {
            x += i;
        }
        return x;
    }

    private static int lessSimpleLoop() {
        int x = 0, result = 0;
        if (x%2 == 0) {
            x = 2;
        }
        else {
            x = 1;
        }
        for (int i = 0; i < x; i++) {
            if (i == 1) {
                result += i;
            }
            else {
                result += i*2;
            }
        }
        return result;
    }

    private static int multiLoop() {
        int x = 0, result = 0;
        if (x%2 == 0) {
            x = 2;
        }
        else {
            x = 1;
        }
        for (int i = 0; i < x; i++) {
            if (i == 1) {
                result += i;
            }
            else {
                result += i*2;
            }
        }
        for (int i = 50; i > x; i--) {
            if (i%2 == 0) {
                result -= i;
            }
            else {
                result -= i*2;
            }
        }
        return result;
    }

    private static void iteratorLoop() {
        LinkedList<Integer> l = new LinkedList<>();
        for (Integer el : l) {
            System.out.println(el);
        }
     }

    private void iteratorMultiLoop() {
        List<Integer> l1 = new LinkedList<>();
        List<Integer> l2 = new LinkedList<>();
        for (Integer el : l1) {
            el += 1;
        }
        for (Integer el : l2) {
            el += 1;
        }
    }
  */
  val loader = new Loader()
  val clazz = {
    val cn = new ClassNode()
    cn.name = "test"
    loader.adaptClass(cn)
  }
  def envFor(what: MethodNode) = new VMethodEnv(clazz, loader.adaptMethod(clazz.name, what))
  
  
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
  val simpleMethodEnv: VMethodEnv = envFor(simpleMethod)
  
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
  val lessSimpleMethodEnv: VMethodEnv = envFor(lessSimpleMethod)
  
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
  val evenLessSimpleMethodEnv: VMethodEnv = envFor(evenLessSimpleMethod)

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
  val simpleLoopEnv: VMethodEnv = envFor(simpleLoop)
  
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
  val lessSimpleLoopEnv: VMethodEnv = envFor(lessSimpleLoop)

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
  val multiLoopEnv: VMethodEnv = envFor(multiLoop)

  val iteratorLoop = {
    var mn = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)

    val l0 = new asm.Label()
    mn.visitLabel(l0)
    mn.visitLineNumber(44, l0)
    mn.visitTypeInsn(Opcodes.NEW, "java/util/LinkedList")
    mn.visitInsn(Opcodes.DUP)
    mn.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/LinkedList", "<init>", "()V", false)
    mn.visitVarInsn(Opcodes.ASTORE, 1)
    val l1 = new asm.Label()
    mn.visitLabel(l1)
    mn.visitLineNumber(62, l1)
    mn.visitVarInsn(Opcodes.ALOAD, 1)
    mn.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/LinkedList", "iterator", "()Ljava/util/Iterator;", false)
    mn.visitVarInsn(Opcodes.ASTORE, 2)
    val l2 = new asm.Label()
    mn.visitLabel(l2)
    mn.visitFrame(Opcodes.F_APPEND, 2, Array[AnyRef]("java/util/LinkedList", "java/util/Iterator"), 0, null)
    mn.visitVarInsn(Opcodes.ALOAD, 2)
    mn.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true)
    val l3 = new asm.Label()
    mn.visitJumpInsn(Opcodes.IFEQ, l3)
    mn.visitVarInsn(Opcodes.ALOAD, 2)
    mn.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true)
    mn.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer")
    mn.visitVarInsn(Opcodes.ASTORE, 3)
    val l4 = new asm.Label()
    mn.visitLabel(l4)
    mn.visitLineNumber(63, l4)
    mn.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    mn.visitVarInsn(Opcodes.ALOAD, 3)
    mn.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false)
    val l5 = new asm.Label()
    mn.visitLabel(l5)
    mn.visitLineNumber(65, l5)
    mn.visitJumpInsn(Opcodes.GOTO, l2)
    mn.visitLabel(l3)
    mn.visitLineNumber(68, l3)
    mn.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mn.visitInsn(Opcodes.RETURN)
    val l6 = new asm.Label()
    mn.visitLabel(l6)
    mn.visitLocalVariable("el", "Ljava/lang/Integer;", null, l4, l5, 3)
    mn.visitLocalVariable("this", "Ledu/cmu/cs/vbc/prog/IterationExample;", null, l0, l6, 0)
    mn.visitLocalVariable("l", "Ljava/util/LinkedList;", "Ljava/util/LinkedList<Ljava/lang/Integer;>;", l1, l6, 1)
    mn.visitMaxs(2, 4)

    mn
  }
  val iteratorLoopEnv: VMethodEnv = envFor(iteratorLoop)


  val iteratorMultiLoop = {
    var mv = new MethodNode(Opcodes.ACC_PUBLIC, "test", "()V", null, null)
    mv.visitCode
    val labels = List.range(0, 12).map(l => new asm.Label("L" + l))
    mv.visitLabel(labels(0))
    mv.visitLineNumber(73, labels(0))
    mv.visitTypeInsn(Opcodes.NEW, "java/util/LinkedList")
    mv.visitInsn(Opcodes.DUP)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/LinkedList", "<init>", "()V", false)
    mv.visitVarInsn(Opcodes.ASTORE, 1)
    mv.visitLabel(labels(1))
    mv.visitLineNumber(74, labels(1))
    mv.visitTypeInsn(Opcodes.NEW, "java/util/LinkedList")
    mv.visitInsn(Opcodes.DUP)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/LinkedList", "<init>", "()V", false)
    mv.visitVarInsn(Opcodes.ASTORE, 2)
    mv.visitLabel(labels(2))
    mv.visitLineNumber(75, labels(2))
    mv.visitVarInsn(Opcodes.ALOAD, 1)
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true)
    mv.visitVarInsn(Opcodes.ASTORE, 3)
    mv.visitLabel(labels(3))
    mv.visitFrame(Opcodes.F_APPEND, 3, Array[AnyRef]("java/util/List", "java/util/List", "java/util/Iterator"), 0, null)
    mv.visitVarInsn(Opcodes.ALOAD, 3)
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true)
    mv.visitJumpInsn(Opcodes.IFEQ, labels(4))
    mv.visitVarInsn(Opcodes.ALOAD, 3)
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true)
    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer")
    mv.visitVarInsn(Opcodes.ASTORE, 4)
    mv.visitLabel(labels(5))
    mv.visitLineNumber(76, labels(5))
    mv.visitVarInsn(Opcodes.ALOAD, 4)
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitInsn(Opcodes.IADD)
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
    mv.visitVarInsn(Opcodes.ASTORE, 4)
    mv.visitLabel(labels(6))
    mv.visitLineNumber(77, labels(6))
    mv.visitJumpInsn(Opcodes.GOTO, labels(3))
    mv.visitLabel(labels(4))
    mv.visitLineNumber(78, labels(4))
    mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mv.visitVarInsn(Opcodes.ALOAD, 2)
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true)
    mv.visitVarInsn(Opcodes.ASTORE, 3)
    mv.visitLabel(labels(7))
    mv.visitFrame(Opcodes.F_APPEND, 1, Array[AnyRef]("java/util/Iterator"), 0, null)
    mv.visitVarInsn(Opcodes.ALOAD, 3)
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true)
    mv.visitJumpInsn(Opcodes.IFEQ, labels(8))
    mv.visitVarInsn(Opcodes.ALOAD, 3)
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true)
    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer")
    mv.visitVarInsn(Opcodes.ASTORE, 4)
    mv.visitLabel(labels(9))
    mv.visitLineNumber(79, labels(9))
    mv.visitVarInsn(Opcodes.ALOAD, 4)
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitInsn(Opcodes.IADD)
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
    mv.visitVarInsn(Opcodes.ASTORE, 4)
    mv.visitLabel(labels(10))
    mv.visitLineNumber(80, labels(10))
    mv.visitJumpInsn(Opcodes.GOTO, labels(7))
    mv.visitLabel(labels(8))
    mv.visitLineNumber(81, labels(8))
    mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mv.visitInsn(Opcodes.RETURN)
    mv.visitLabel(labels(11))
    mv.visitLocalVariable("el", "Ljava/lang/Integer;", null, labels(5), labels(6), 4)
    mv.visitLocalVariable("el", "Ljava/lang/Integer;", null, labels(9), labels(10), 4)
    mv.visitLocalVariable("this", "Ledu/cmu/cs/vbc/prog/IterationExample;", null, labels(0), labels(11), 0)
    mv.visitLocalVariable("l1", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Integer;>;", labels(1), labels(11), 1)
    mv.visitLocalVariable("l2", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Integer;>;", labels(2), labels(11), 2)
    mv.visitMaxs(2, 5)
    mv.visitEnd()

    mv
  }
  val iteratorMultiLoopEnv: VMethodEnv = envFor(iteratorMultiLoop)








  "retreatingEdges" should "contain all retreating edges" in {
    var msg = "analysis incorrectly finds retreating edges"
    assert(simpleMethodEnv.loopAnalysis.retreatingEdges.isEmpty, msg)
    assert(lessSimpleMethodEnv.loopAnalysis.retreatingEdges.isEmpty, msg)
    assert(evenLessSimpleMethodEnv.loopAnalysis.retreatingEdges.isEmpty, msg)

    msg = "incorrect number of retreating edges"
    var re = simpleLoopEnv.loopAnalysis.retreatingEdges
    assert(re.size == 1, msg)

    re = lessSimpleLoopEnv.loopAnalysis.retreatingEdges
    assert(re.size == 1, msg)

    re = multiLoopEnv.loopAnalysis.retreatingEdges
    assert(re.size == 2, msg)

    re = iteratorLoopEnv.loopAnalysis.retreatingEdges
    assert(re.size == 1, msg)

    re = iteratorMultiLoopEnv.loopAnalysis.retreatingEdges
    assert(re.size == 2, msg)
  }

  "loops.size" should "be the correct number of loops" in {
    var msg = "analysis finds loops when there are none"
    assert(simpleMethodEnv.loopAnalysis.loops.isEmpty, msg)
    assert(lessSimpleMethodEnv.loopAnalysis.loops.isEmpty, msg)
    assert(evenLessSimpleMethodEnv.loopAnalysis.loops.isEmpty, msg)

    msg = "incorrect number of loops"
    assert(simpleLoopEnv.loopAnalysis.loops.size == 1, msg)
    assert(lessSimpleLoopEnv.loopAnalysis.loops.size == 1, msg)
    assert(multiLoopEnv.loopAnalysis.loops.size == 2, msg)
    assert(iteratorLoopEnv.loopAnalysis.loops.size == 1, msg)
    assert(iteratorMultiLoopEnv.loopAnalysis.loops.size == 2, msg)
  }

  "loops" should "contain the relevant nodes in their bodies" in {
    val msg = "loops does not contain expected Blocks"
    var loop = simpleLoopEnv.loopAnalysis.loops.find(_ => true).get
    var body = loop.body
    assert(body.size == 1, msg)
    assert(loop.entry.instr.exists(cond(_) { case v: InstrICONST => v.v == 5 }), msg)
    assert(loop.entry.instr.exists(cond(_) { case c: InstrIF_ICMPGE => true }), msg)
    assert(body.find(_ => true).get.instr.exists(cond(_) { case a: InstrIADD => true }), msg)


    loop = lessSimpleLoopEnv.loopAnalysis.loops.find(_ => true).get
    body = loop.body
    assert(body.size == 4, msg)
    assert(loop.entry.instr.exists(cond(_) { case l: InstrILOAD => l.getVariables().exists(_.name == "i")}))
    assert(loop.entry.instr.exists(cond(_) { case c: InstrIF_ICMPGE => true }), msg)
    var bodyList = body.toList.sortBy(_.instr.head match {
      case l: InstrLINENUMBER => l.line
      case _ => Int.MaxValue
    })
    assert(bodyList.head.instr.exists(cond(_) { case i: InstrIINC => i.getVariables().exists(_.name == "i") }))
    assert(bodyList(1).instr.exists(cond(_) { case c: InstrIF_ICMPNE => true }))
    assert(bodyList(2).instr.exists(cond(_) { case a: InstrIADD => true }))
    assert(!bodyList(2).instr.exists(cond(_) { case m: InstrIMUL => true }))
    assert(bodyList(3).instr.exists(cond(_) { case a: InstrIADD => true }))
    assert(bodyList(3).instr.exists(cond(_) { case m: InstrIMUL => true }))

    var loops = multiLoopEnv.loopAnalysis.loops.toList.sortBy(l => l.body.toList.head.instr.head match {
      case l: InstrLINENUMBER => l.line
      case _ => Int.MaxValue
    })
    loop = loops.head
    body = loop.body
    assert(body.size == 4, msg)
    assert(loop.entry.instr.exists(cond(_) { case l: InstrILOAD => l.getVariables().exists(_.name == "i")}))
    assert(loop.entry.instr.exists(cond(_) { case c: InstrIF_ICMPGE => true }), msg)
    bodyList = body.toList.sortBy(_.instr.head match {
      case l: InstrLINENUMBER => l.line
      case _ => Int.MaxValue
    })
    assert(bodyList.head.instr.exists(cond(_) { case i: InstrIINC => i.getVariables().exists(_.name == "i") && i.increment == 1 }))
    assert(bodyList(1).instr.exists(cond(_) { case c: InstrIF_ICMPNE => true }))
    assert(bodyList(2).instr.exists(cond(_) { case a: InstrIADD => true }))
    assert(!bodyList(2).instr.exists(cond(_){ case m: InstrIMUL => true }))
    assert(bodyList(3).instr.exists(cond(_) { case a: InstrIADD => true }))
    assert(bodyList(3).instr.exists(cond(_) { case m: InstrIMUL => true }))

    loop = loops.last
    body = loop.body
    assert(body.size == 4, msg)
    assert(loop.entry.instr.exists(cond(_) { case l: InstrILOAD => l.getVariables().exists(_.name == "i")}))
    assert(loop.entry.instr.exists(cond(_) { case c: InstrIF_ICMPLE => true }), msg)
    bodyList = body.toList.sortBy(_.instr.head match {
      case l: InstrLINENUMBER => l.line
      case _ => Int.MaxValue
    })
    assert(bodyList.head.instr.exists(cond(_) { case i: InstrIINC => i.getVariables().exists(_.name == "i") && i.increment == -1}))
    assert(bodyList(1).instr.exists(cond(_) { case c: InstrIREM => true }))
    assert(bodyList(1).instr.exists(cond(_) { case c: InstrIFNE => true }))
    assert(bodyList(2).instr.exists(cond(_) { case a: InstrISUB => true }))
    assert(!bodyList(2).instr.exists(cond(_){ case m: InstrIMUL => true }))
    assert(bodyList(3).instr.exists(cond(_) { case a: InstrISUB => true }))
    assert(bodyList(3).instr.exists(cond(_) { case m: InstrIMUL => true }))


    loop = iteratorLoopEnv.loopAnalysis.loops.find(_ => true).get
    body = loop.body
    assert(body.size == 1, msg)
    assert(loop.entry.instr.exists(cond(_) { case ii: InstrINVOKEINTERFACE => ii.name == "hasNext" }), msg)
    assert(loop.entry.instr.exists(cond(_) { case c: InstrIFEQ => true }), msg)

    var blocksBefore = iteratorLoopEnv.getPredecessors(loop.entry)
    assert(blocksBefore.exists(_.instr.exists(cond(_) { case ii: InstrINVOKEVIRTUAL => ii.name == "iterator"})), msg)

    var bodyBlock = body.find(_ => true).get
    assert(bodyBlock.instr.exists(cond(_) { case ii: InstrINVOKEINTERFACE => ii.name == "next" }), msg)
    assert(bodyBlock.instr.exists(cond(_) { case s: InstrASTORE => s.getVariables.exists(_.name == "el") }), msg)
    assert(bodyBlock.instr.exists(cond(_) { case s: InstrALOAD => s.getVariables.exists(_.name == "el") }), msg)
    assert(bodyBlock.instr.exists(cond(_) { case g: InstrGETSTATIC => g.name == "out" }), msg)
    assert(bodyBlock.instr.exists(cond(_) { case iv: InstrINVOKEVIRTUAL => iv.name == "println" }), msg)



    loops = iteratorMultiLoopEnv.loopAnalysis.loops.toList.sortBy(l => l.body.toList.head.instr.head match {
      case l: InstrLINENUMBER => l.line
      case _ => Int.MaxValue
    })
    loop = loops.head
    body = loop.body
    assert(body.size == 1, msg)
    assert(loop.entry.instr.exists(cond(_) { case ii: InstrINVOKEINTERFACE => ii.name == "hasNext" }), msg)
    assert(loop.entry.instr.exists(cond(_) { case c: InstrIFEQ => true }), msg)

    blocksBefore = iteratorMultiLoopEnv.getPredecessors(loop.entry)
    assert(blocksBefore.exists(_.instr.exists(cond(_) { case ii: InstrINVOKEINTERFACE => ii.name == "iterator"})), msg)

    val bodyBlockInstr = body.flatMap(_.instr)
    assert(bodyBlockInstr.exists(cond(_) { case ii: InstrINVOKEINTERFACE => ii.name == "next" }), msg)
    assert(bodyBlockInstr.exists(cond(_) { case s: InstrASTORE => s.getVariables.exists(_.name == "el") }), msg)
    assert(bodyBlockInstr.exists(cond(_) { case s: InstrALOAD => s.getVariables.exists(_.name == "el") }), msg)
    assert(bodyBlockInstr.exists(cond(_) { case s: InstrIADD => true }), msg)
  }
}
