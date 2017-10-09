package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.DiffMethodTestInfrastructure
import edu.cmu.cs.vbc.vbytecode.instructions._
import edu.cmu.cs.vbc.vbytecode.{Block, MethodDesc}
import org.scalatest.{FunSuite, Matchers}


class CFGSplitBlockTest extends FunSuite with Matchers with DiffMethodTestInfrastructure {

  import edu.cmu.cs.vbc.vbytecode.{CFG, SplitInfo}

  test("splitBlock") {
    val cfg = CFG(List(
      Block(InstrPOP(),InstrDUP()),
      Block(InstrDUP(), InstrICONST(1)),
      Block(InstrSWAP(), InstrPOP(), InstrDUP()),
      Block(InstrDUP(), InstrPOP())
    ))
    val info = SplitInfo(2, InstrPOP(), dest => InstrIFEQ(dest), Block(InstrPOP()))
    val result = cfg.splitBlock(info)
    result._1 should equal(1)
  }

}