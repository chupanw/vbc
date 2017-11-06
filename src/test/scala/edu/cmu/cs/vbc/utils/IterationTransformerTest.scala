package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode.instructions._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.util.{Textifier, TraceClassVisitor}
import org.objectweb.asm.{ClassReader, ClassVisitor, ClassWriter}
import org.scalatest.{FunSuite, Matchers}


class IterationTransformerTest extends FunSuite with Matchers {

  def equal(a: CFG, b: CFG): Boolean = {
    val sizeEqual = a.blocks.size == b.blocks.size
    // No value equality for instrs by design, so this is an ugly workaround
    val blockStrs = a.blocks.zip(b.blocks) map (el => (el._1.instr.toList.toString + "\n", el._2.instr.toList.toString + "\n"))
    val blocksEqual = blockStrs.foldLeft(true)((collected, el) => (el._1 == el._2) && collected)
    if (sizeEqual && blocksEqual)
      true
    else {
      print(blockStrs)
      false
    }
  }

  val cfg_1loop = CFG(List(
    Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
    Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
    Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrDUP(), InstrGOTO(3)),
    Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
    Block(InstrLDC("orig 5"), // 4
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(5)),
    Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 5: loop ^
    Block(InstrLDC("orig 7"), InstrDUP(), InstrPOP(), InstrGOTO(7)), // 6
    Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(7)) // 7
  ))
  val cfg_2loop = CFG(List(
    Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
    Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
    Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrDUP(), InstrGOTO(3)),
    Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
    Block(InstrLDC("orig 5"), // 4
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(5)),
    Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 5: loop ^
    Block(InstrLDC("orig 7"), InstrDUP(), // 6
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(), InstrGOTO(7)),
    Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(8)), // 7: loop entry
    Block(InstrLDC("orig 9"), // 8
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(9)),
    Block(InstrLDC("orig 10"), InstrDUP(), InstrPOP(), InstrIFEQ(10)), // 9
    Block(InstrLDC("orig 11"), InstrDUP(), InstrPOP(), InstrGOTO(7)), // 10: loop ^
    Block(InstrLDC("orig 12"), InstrDUP(), InstrPOP(), InstrGOTO(11)) // 11
    ))


  // ===== insertCleanupBlocks =====
  test("insertCleanupBlocks works for one loop") {
    val itt = new IterationTransformer()
    val loopEntry = cfg_1loop.blocks(3)
    val loopBody = Set(cfg_1loop.blocks(4), cfg_1loop.blocks(5))
    val (newCFG, cleanupBlocks, blockUpdates) = itt.insertCleanupBlocks(cfg_1loop, List(Loop(loopEntry, loopBody)))
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrDUP(), InstrGOTO(3)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
      Block(InstrLDC("orig 5"), // 4
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFNE(6)),
      Block(List(InstrPOP(), InstrPOP(), InstrGOTO(3)), Nil), // 5: cleanup block
      Block(InstrDUP(), InstrPOP(), InstrGOTO(7)), // 6: second split-half
      Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 7: loop ^
      Block(InstrLDC("orig 7"), InstrDUP(), InstrPOP(), InstrGOTO(9)), // 8
      Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(9)) // 9
    ))), "Block splitting doesn't work as expected")
  }

  test("insertCleanupBlocks works for multiple loops") {
    val itt = new IterationTransformer()
    val loop1Entry = cfg_2loop.blocks(3)
    val loop1Body = Set(cfg_2loop.blocks(4), cfg_2loop.blocks(5))
    val loop2Entry = cfg_2loop.blocks(7)
    val loop2Body = Set(cfg_2loop.blocks(8), cfg_2loop.blocks(9), cfg_2loop.blocks(10))
    val (newCFG, cleanupBlocks, blockUpdates) = itt.insertCleanupBlocks(cfg_2loop,
      List(Loop(loop1Entry, loop1Body), Loop(loop2Entry, loop2Body)))
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrDUP(), InstrGOTO(3)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
      Block(InstrLDC("orig 5"), // 4
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFNE(6)),
      Block(List(InstrPOP(), InstrPOP(), InstrGOTO(3)), Nil), // 5: cleanup block
      Block(InstrDUP(), InstrPOP(), InstrGOTO(7)), // 6: second split-half
      Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 7: loop ^
      Block(InstrLDC("orig 7"), InstrDUP(), // 8
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrPOP(), InstrGOTO(9)),
      Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(10)), // 9: loop entry
      Block(InstrLDC("orig 9"), // 10
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFNE(12)),
      Block(List(InstrPOP(), InstrPOP(), InstrGOTO(9)), Nil), // 11: cleanup block
      Block(List(InstrDUP(), InstrPOP(), InstrGOTO(13)), Nil), // 12: second split-half
      Block(InstrLDC("orig 10"), InstrDUP(), InstrPOP(), InstrIFEQ(14)), // 13
      Block(InstrLDC("orig 11"), InstrDUP(), InstrPOP(), InstrGOTO(9)), // 14: loop ^
      Block(InstrLDC("orig 12"), InstrDUP(), InstrPOP(), InstrGOTO(15)) // 15
    ))), "Block splitting doesn't work as expected")
  }

  test("insertCleanupBlocks returns valid map from loop to cleanup blocks") {
    val itt = new IterationTransformer()
    val loop1Entry = cfg_2loop.blocks(3)
    val loop1Body = Set(cfg_2loop.blocks(4), cfg_2loop.blocks(5))
    val loop1 = Loop(loop1Entry, loop1Body)
    val loop2Entry = cfg_2loop.blocks(7)
    val loop2Body = Set(cfg_2loop.blocks(8), cfg_2loop.blocks(9), cfg_2loop.blocks(10))
    val loop2 = Loop(loop2Entry, loop2Body)
    val (newCFG, cleanupBlocks, blockUpdates) = itt.insertCleanupBlocks(cfg_2loop,
      List(loop1, loop2))
    assert(cleanupBlocks(loop1) == 5, "return of insertCleanupBlocks does not map final cleanup block index properly")
    assert(cleanupBlocks(loop2) == 11, "return of insertCleanupBlocks does not map final cleanup block index properly")
  }

  test("insertCleanupBlocks returns valid map to updated block indices") {
    val itt = new IterationTransformer()
    val loop1Entry = cfg_2loop.blocks(3)
    val loop1Body = Set(cfg_2loop.blocks(4), cfg_2loop.blocks(5))
    val loop1 = Loop(loop1Entry, loop1Body)
    val loop2Entry = cfg_2loop.blocks(7)
    val loop2Body = Set(cfg_2loop.blocks(8), cfg_2loop.blocks(9), cfg_2loop.blocks(10))
    val loop2 = Loop(loop2Entry, loop2Body)
    val (newCFG, cleanupBlocks, blockUpdates) = itt.insertCleanupBlocks(cfg_2loop,
      List(loop1, loop2))
    assert(blockUpdates(0) == 0)
    assert(blockUpdates(1) == 1)
    assert(blockUpdates(2) == 2)
    assert(blockUpdates(3) == 3)
    assert(blockUpdates(4) == 4)
    assert(blockUpdates(5) == 7)
    assert(blockUpdates(6) == 8)
    assert(blockUpdates(7) == 9)
    assert(blockUpdates(8) == 10)
    assert(blockUpdates(9) == 13)
    assert(blockUpdates(10) == 14)
    assert(blockUpdates(11) == 15)
  }


  // ===== transformLoopPredecessor =====
  test("transformLoopPredecessor works") {
    val loopPredecessor = cfg_2loop.blocks(2)
    val env = ???
  }

  // ===== createSimplifyLambda =====
  test("createSimplifyLambda creates lambda") {
    val itt = new IterationTransformer()
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES)
    val cv = new TraceClassVisitor(cw, new Textifier(), null)

    val lambdaName = "lambda$INVOKEVIRTUAL$simplifyCtxList"
    val lambdaDesc = s"(${itt.ctxListClassType})V"
    itt.createSimplifyLambda(cw, lambdaName, lambdaDesc)
    // todo: verify that class has the added method ...
    val cr = new ClassReader(cw.toByteArray)
  }


  // ===== transformBodyStartBlock =====
  test("transformBodyStartBlock works") {
    
  }
}