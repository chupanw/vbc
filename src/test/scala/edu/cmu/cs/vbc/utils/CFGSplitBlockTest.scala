package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode.instructions._
import edu.cmu.cs.vbc.vbytecode.{Block, CFG, SplitInfo}
import org.scalatest.{FunSuite, Matchers}


class CFGSplitBlockTest extends FunSuite with Matchers {

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

  val cfg = CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)),
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)),
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrDUP(), InstrGOTO(3)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(3))
    ))

  test("Basic structure of cfg with split block works") {
    val info = SplitInfo(2, 2, dest => InstrIFNE(dest), 1)
    val (newCFG, newIndices) = cfg.splitBlock(info)
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)),
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)),
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(1)),
      Block(InstrDUP(), InstrGOTO(4)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4))
      ))), "Block splitting doesn't work as expected")
  }

  test("newIndices maps old indices correctly") {
    val info = SplitInfo(2, 2, dest => InstrIFNE(dest), 1)
    val (newCFG, newIndices) = cfg.splitBlock(info)
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(newIndices(1))),
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(newIndices(2))),
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(newIndices(1))),
      Block(InstrDUP(), InstrGOTO(newIndices(3))),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(newIndices(3)))
    ))), "newIndices doesn't map old indices to new ones correctly")
  }

  test("Inserted jump has index updated") {
    val info = SplitInfo(2, 2, dest => InstrIFNE(dest), 3)
    val (newCFG, newIndices) = cfg.splitBlock(info)
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)),
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)),
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(4)),
      Block(InstrDUP(), InstrGOTO(4)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4))
      ))), "Inserted block doesn't have jump index updated")
  }

  test("Splitting multiple blocks works correctly") {
    val info = SplitInfo(2, 2, dest => InstrIFNE(dest), 1)
    val (newCFG, newIndices) = cfg.splitBlock(info)
    val newInfo = SplitInfo(1, 1, dest => InstrIFEQ(dest), 0)
    val (newCFG2, newIndices2) = newCFG.splitBlock(newInfo)
    assert(equal(newCFG2, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)),
      Block(InstrLDC("orig 2"), InstrDUP(), InstrIFEQ(0)),
      Block(InstrICONST(1), InstrGOTO(3)),
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(1)),
      Block(InstrDUP(), InstrGOTO(5)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(5))
    ))))
  }

  test("newIndices correctly updates with multiple splits") {
    val info = SplitInfo(2, 2, dest => InstrIFNE(dest), 1)
    val (newCFG, newIndices) = cfg.splitBlock(info)
    val newInfo = SplitInfo(1, 1, dest => InstrIFEQ(dest), 0)
    val (newCFG2, newIndices2) = newCFG.splitBlock(newInfo)
    assert(equal(newCFG2, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(newIndices2(1))),
      Block(InstrLDC("orig 2"), InstrDUP(), InstrIFEQ(newIndices2(0))),
      Block(InstrICONST(1), InstrGOTO(newIndices2(2))),
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(newIndices2(1))),
      Block(InstrDUP(), InstrGOTO(newIndices2(4))),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(newIndices2(4)))
    ))))
    val newNewInfo = SplitInfo(0, 1, dest => InstrIFEQ(dest), 3)
    val (newCFG3, newIndices3) = newCFG2.splitBlock(newNewInfo)
    assert(equal(newCFG3, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrIFEQ(newIndices3(3))),
      Block(InstrDUP(), InstrGOTO(newIndices3(1))),
      Block(InstrLDC("orig 2"), InstrDUP(), InstrIFEQ(newIndices3(0))),
      Block(InstrICONST(1), InstrGOTO(newIndices3(3))),
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), InstrIFNE(newIndices3(1))),
      Block(InstrDUP(), InstrGOTO(newIndices3(5))),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(newIndices3(5)))
    ))))
  }
}