package edu.cmu.cs.vbc.utils

import java.util.Optional

import edu.cmu.cs.vbc.DiffMethodTestInfrastructure
import edu.cmu.cs.vbc.vbytecode.instructions._
import edu.cmu.cs.vbc.vbytecode.{Block, CFG, MethodDesc, SplitInfo}
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

  test("splitBlock") {
    val cfg = CFG(List(
      Block(InstrPOP(), InstrDUP(), InstrGOTO(1)),
      Block(InstrDUP(), InstrICONST(1), InstrGOTO(2)),
      Block(InstrSWAP(), InstrPOP(), InstrDUP(), InstrGOTO(3)),
      Block(InstrDUP(), InstrPOP(), InstrGOTO(3))
    ))
    val info = SplitInfo(2, 1, dest => InstrIFNE(dest), Block(InstrPOP(), InstrGOTO(1)))
    val (newCFG, newBlock, newIndices) = cfg.splitBlock(info)
    assert(equal(newCFG, CFG(List(
      Block(InstrPOP(), InstrDUP(), InstrGOTO(1)),
      Block(InstrDUP(), InstrICONST(1), InstrGOTO(2)),
      Block(InstrSWAP(), InstrPOP(), InstrIFNE(4)),
      Block(InstrPOP(), InstrGOTO(1)),
      Block(InstrDUP(), InstrGOTO(5)),
      Block(InstrDUP(), InstrPOP(), InstrGOTO(5))
      ))), "Block splitting doesn't work as expected")
    assert(equal(newCFG, CFG(List(
      Block(InstrPOP(), InstrDUP(), InstrGOTO(newIndices(1))),
      Block(InstrDUP(), InstrICONST(1), InstrGOTO(newIndices(2))),
      Block(InstrSWAP(), InstrPOP(), InstrIFNE(4)),
      Block(InstrPOP(), InstrGOTO(newIndices(1))),
      Block(InstrDUP(), InstrGOTO(newIndices(3))),
      Block(InstrDUP(), InstrPOP(), InstrGOTO(newIndices(3)))
    ))), "newIndices doesn't map old indices to new ones correctly")
    val info2 = SplitInfo(2, 1, dest => InstrIFNE(dest), Block(InstrPOP(), InstrGOTO(3)))
    val (newCFG2, newBlock2, newIndices2) = cfg.splitBlock(info2)
    assert(equal(newCFG2, CFG(List(
      Block(InstrPOP(), InstrDUP(), InstrGOTO(1)),
      Block(InstrDUP(), InstrICONST(1), InstrGOTO(2)),
      Block(InstrSWAP(), InstrPOP(), InstrIFNE(4)),
      Block(InstrPOP(), InstrGOTO(5)),
      Block(InstrDUP(), InstrGOTO(5)),
      Block(InstrDUP(), InstrPOP(), InstrGOTO(5))
      ))), "Inserted block doesn't have jump index updated")
  }

}