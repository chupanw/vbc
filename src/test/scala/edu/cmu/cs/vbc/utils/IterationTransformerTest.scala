package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode.instructions._
import edu.cmu.cs.vbc.vbytecode._
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

//  test("insertCleanupBlocks works for one loop") {
//    val itt = new IterationTransformer()
//    val loopEntry = cfg_1loop.blocks(3)
//    val loopBody = Set(cfg_1loop.blocks(4), cfg_1loop.blocks(5))
//    val (newCFG, cleanupBlocks, blockUpdates) = itt.insertCleanupBlocks(cfg_1loop, List(Loop(loopEntry, loopBody)))
//    assert(equal(newCFG, CFG(List(
//      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
//      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
//      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
//        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
//        InstrDUP(), InstrGOTO(3)),
//      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
//      Block(InstrLDC("orig 5"), // 4
//        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
//        InstrIFNE(6)),
//      Block(List(InstrPOP(), InstrPOP(), InstrGOTO(3)), Nil), // 5: cleanup block
//      Block(InstrDUP(), InstrPOP(), InstrGOTO(7)), // 6: second split-half
//      Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 7: loop ^
//      Block(InstrLDC("orig 7"), InstrDUP(), InstrPOP(), InstrGOTO(9)), // 8
//      Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(9)) // 9
//    ))), "Block splitting doesn't work as expected")
//  }

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

  test("Splitting multiple blocks works correctly") {
//    val info = SplitInfo(2, 2, dest => InstrIFNE(dest), Block(InstrLDC("inserted 1"), InstrPOP(), InstrGOTO(1)))
//    val (newCFG, newBlock, newIndices) = cfg.splitBlock(info)
//    val newInfo = SplitInfo(1, 1, dest => InstrIFEQ(dest), Block(InstrLDC("inserted 2"), InstrICONST(5), InstrIF_ACMPEQ(4)))
//    val (newCFG2, newBlock2, newIndices2) = newCFG.splitBlock(newInfo)
//    assert(equal(newCFG2, CFG(List(
//      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)),
//      Block(InstrLDC("orig 2"), InstrDUP(), InstrIFEQ(3)),
//      Block(InstrLDC("inserted 2"), InstrICONST(5), InstrIF_ACMPEQ(6)),
//      Block(InstrICONST(1), InstrGOTO(4)),
//      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(6)),
//      Block(InstrLDC("inserted 1"), InstrPOP(), InstrGOTO(1)),
//      Block(InstrDUP(), InstrGOTO(7)),
//      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(7))
//    ))))
  }

  test("newIndices correctly updates with multiple splits") {
//    val info = SplitInfo(2, 2, dest => InstrIFNE(dest), Block(InstrLDC("inserted 1"), InstrPOP(), InstrGOTO(1)))
//    val (newCFG, newBlock, newIndices) = cfg.splitBlock(info)
//    val newInfo = SplitInfo(1, 1, dest => InstrIFEQ(dest), Block(InstrLDC("inserted 2"), InstrICONST(5), InstrIF_ACMPEQ(4)))
//    val (newCFG2, newBlock2, newIndices2) = newCFG.splitBlock(newInfo)
//    assert(equal(newCFG2, CFG(List(
//      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(newIndices2(1))),
//      Block(InstrLDC("orig 2"), InstrDUP(), InstrIFEQ(3)),
//      Block(InstrLDC("inserted 2"), InstrICONST(5), InstrIF_ACMPEQ(newIndices2(4))),
//      Block(InstrICONST(1), InstrGOTO(newIndices2(2))),
//      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(newIndices2(4))),
//      Block(InstrLDC("inserted 1"), InstrPOP(), InstrGOTO(newIndices2(1))),
//      Block(InstrDUP(), InstrGOTO(newIndices2(5))),
//      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(newIndices2(5)))
//    ))))
//    val newNewInfo = SplitInfo(4, 1, dest => InstrIFEQ(dest), Block(InstrLDC("inserted 3"), InstrICONST(22), InstrIF_ICMPLE(0)))
//    val (newCFG3, newBlock3, newIndices3) = newCFG2.splitBlock(newNewInfo)
//    assert(equal(newCFG3, CFG(List(
//      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(newIndices3(1))),
//      Block(InstrLDC("orig 2"), InstrDUP(), InstrIFEQ(newIndices3(3))),
//      Block(InstrLDC("inserted 2"), InstrICONST(5), InstrIF_ACMPEQ(newIndices3(6))),
//      Block(InstrICONST(1), InstrGOTO(newIndices3(4))),
//      Block(InstrLDC("orig 3"), InstrSWAP(), InstrIFEQ(6)),
//      Block(InstrLDC("inserted 3"), InstrICONST(22), InstrIF_ICMPLE(newIndices3(0))),
//      Block(InstrPOP(), InstrIFNE(newIndices3(6))),
//      Block(InstrLDC("inserted 1"), InstrPOP(), InstrGOTO(newIndices3(1))),
//      Block(InstrDUP(), InstrGOTO(newIndices3(7))),
//      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(newIndices3(7)))
//    ))))
  }
}