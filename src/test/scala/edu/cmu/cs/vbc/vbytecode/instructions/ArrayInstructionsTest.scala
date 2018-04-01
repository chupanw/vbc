package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.{DiffMethodTestInfrastructure, InstrDBGIPrint}
import org.objectweb.asm.Opcodes._
import org.scalatest.FlatSpec

/**
  * @author chupanw
  */
class ArrayInstructionsTest extends FlatSpec with DiffMethodTestInfrastructure {
  "ARRAYLENGTH" can "get the length of primitive array" in {
    methodWithBlocks(
      createVPrimitiveArray(T_INT, 0, 10, 20) :::
      Block(InstrARRAYLENGTH(), InstrDBGIPrint(), InstrRETURN()) ::
      Nil
    )
  }

  it can "get the length of Integer array" in {
    methodWithBlocks(
      createVObjectArray(Owner("java/lang/Integer"), 0, 20, 30) :::
      Block(InstrARRAYLENGTH(), InstrDBGIPrint(), InstrRETURN()) ::
      Nil
    )
  }

  "BASTORE" can "store byte values to V[]" in {
    val arrayref = new LocalVar("arrayref", "[B")
    methodWithBlocks(
      Block(InstrBIPUSH(2), InstrNEWARRAY(T_BYTE), InstrASTORE(arrayref)) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrBASTORE(), InstrRETURN()) ::
        Nil
    )
  }

  it can "store byte values to V<V[]>" in {
    val arrayref = new LocalVar("arrayref", "[B")
    methodWithBlocks(
      createVPrimitiveArray(T_BYTE, 0, tLength = 2, fLength = 4, localVar = Some(arrayref)) :::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrBASTORE(), InstrRETURN()) ::
        Nil
    )
  }

  it can "store byte values to V<V[]> with V index" in {
    val arrayref = new LocalVar("arrayref", "[B")
    val index = new LocalVar("index", "I")
    methodWithBlocks(
      createVPrimitiveArray(T_BYTE, 0, tLength = 2, fLength = 4, localVar = Some(arrayref), config = "A") :::
        createVint(tValue = 0, fValue = 1, startBlockIdx = 3, localVar = Some(index), config = "B") :::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrBIPUSH(1), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrBIPUSH(2), InstrBASTORE(), InstrRETURN()) ::
        Nil
    )
  }

  "BALOAD" can "load byte values from V[]" in {
    val arrayref = new LocalVar("arrayref", "[B")
    methodWithBlocks(
      Block(InstrBIPUSH(2), InstrNEWARRAY(T_BYTE), InstrASTORE(arrayref)) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrBALOAD(), InstrDBGIPrint()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrBALOAD(), InstrDBGIPrint(), InstrRETURN()) ::
        Nil
    )
  }

  it can "load byte values from V<V[]>" in {
    val arrayref = new LocalVar("arrayref", "[B")
    methodWithBlocks(
      createVPrimitiveArray(T_BYTE, 0, tLength = 2, fLength = 4, localVar = Some(arrayref)) :::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrBALOAD(), InstrDBGIPrint()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrBALOAD(), InstrDBGIPrint(), InstrRETURN()) ::
        Nil
    )
  }

  it can "load byte values from V<V[]> with V index" in {
    val arrayref = new LocalVar("arrayref", "[B")
    val index = new LocalVar("index", "I")
    methodWithBlocks(
      createVPrimitiveArray(T_BYTE, 0, tLength = 2, fLength = 4, localVar = Some(arrayref), config = "A") :::
        createVint(tValue = 0, fValue = 1, startBlockIdx = 3, localVar = Some(index), config = "B") :::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrICONST(1), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrICONST(2), InstrBASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrBALOAD(), InstrDBGIPrint(), InstrRETURN()) ::
        Nil
    )
  }

  "SASTORE" can "store short values to V[]" in {
    val arrayref = new LocalVar("arrayref", "[S")
    methodWithBlocks(
      Block(InstrBIPUSH(2), InstrNEWARRAY(T_SHORT), InstrASTORE(arrayref)) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrSASTORE(), InstrRETURN()) ::
        Nil
    )
  }

  it can "store short values to V<V[]>" in {
    val arrayref = new LocalVar("arrayref", "[S")
    methodWithBlocks(
      createVPrimitiveArray(T_SHORT, 0, tLength = 2, fLength = 4, localVar = Some(arrayref)) :::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrSASTORE(), InstrRETURN()) ::
        Nil
    )
  }

  it can "store short values to V<V[]> with V index" in {
    val arrayref = new LocalVar("arrayref", "[S")
    val index = new LocalVar("index", "I")
    methodWithBlocks(
      createVPrimitiveArray(T_SHORT, 0, tLength = 2, fLength = 4, localVar = Some(arrayref), config = "A") :::
        createVint(tValue = 0, fValue = 1, startBlockIdx = 3, localVar = Some(index), config = "B") :::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrBIPUSH(1), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrBIPUSH(2), InstrSASTORE(), InstrRETURN()) ::
        Nil
    )
  }

  "SALOAD" can "load short values from V[]" in {
    val arrayref = new LocalVar("arrayref", "[S")
    methodWithBlocks(
      Block(InstrBIPUSH(2), InstrNEWARRAY(T_SHORT), InstrASTORE(arrayref)) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrSALOAD(), InstrDBGIPrint()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrSALOAD(), InstrDBGIPrint(), InstrRETURN()) ::
        Nil
    )
  }

  it can "load short values from V<V[]>" in {
    val arrayref = new LocalVar("arrayref", "[S")
    methodWithBlocks(
      createVPrimitiveArray(T_SHORT, 0, tLength = 2, fLength = 4, localVar = Some(arrayref)) :::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrICONST(1), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrICONST(2), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrSALOAD(), InstrDBGIPrint()) ::
        Block(InstrALOAD(arrayref), InstrICONST(1), InstrSALOAD(), InstrDBGIPrint(), InstrRETURN()) ::
        Nil
    )
  }

  it can "load short values from V<V[]> with V index" in {
    val arrayref = new LocalVar("arrayref", "[S")
    val index = new LocalVar("index", "I")
    methodWithBlocks(
      createVPrimitiveArray(T_SHORT, 0, tLength = 2, fLength = 4, localVar = Some(arrayref), config = "A") :::
        createVint(tValue = 0, fValue = 1, startBlockIdx = 3, localVar = Some(index), config = "B") :::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrICONST(1), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrILOAD(index), InstrICONST(2), InstrSASTORE()) ::
        Block(InstrALOAD(arrayref), InstrICONST(0), InstrSALOAD(), InstrDBGIPrint(), InstrRETURN()) ::
        Nil
    )
  }
}
