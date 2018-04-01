package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.vbytecode.{Block, MethodDesc, MethodName, Owner}
import edu.cmu.cs.vbc.{DiffMethodTestInfrastructure, InstrDBGIPrint, InstrDBGStrPrint}
import org.scalatest.FlatSpec

/**
  * @author chupanw
  */
class StackInstructionsTest extends FlatSpec with DiffMethodTestInfrastructure {

  "DUP2" can "duplicate two ints" in {
    methodWithBlocks(
      Block(InstrICONST(1),
            InstrICONST(2),
            InstrDUP2(),
            InstrDBGIPrint(),
            InstrDBGIPrint(),
            InstrDBGIPrint(),
            InstrDBGIPrint(),
            InstrRETURN()) ::
        Nil
    )
  }

  it can "duplicate two vints" in {
    methodWithBlocks(
      createVint(tValue = 1, fValue = 2, startBlockIdx = 0, config = "A") :::
        createVint(tValue = 3, fValue = 4, startBlockIdx = 3, config = "B") :::
        Block(InstrDUP2(),
              InstrDBGIPrint(),
              InstrDBGIPrint(),
              InstrDBGIPrint(),
              InstrDBGIPrint(),
              InstrRETURN()) ::
        Nil
    )
  }

  it can "duplicate one long" in {
    methodWithBlocks(
      Block(
        InstrINVOKESTATIC(Owner.getRuntime,
                          MethodName("getRuntime"),
                          MethodDesc("()Ljava/lang/Runtime;"),
                          false),
        InstrINVOKEVIRTUAL(Owner.getRuntime,
                           MethodName("maxMemory"),
                           MethodDesc("()J"),
                           false),
        InstrDUP2(),
        InstrINVOKESTATIC(Owner.getLong,
                          MethodName("valueOf"),
                          MethodDesc("(J)Ljava/lang/Long;"),
                          false),
        InstrINVOKEVIRTUAL(Owner.getLong,
                           MethodName("toString"),
                           MethodDesc("()Ljava/lang/String;"),
                           false),
        InstrDBGStrPrint(),
        InstrINVOKESTATIC(Owner.getLong,
                          MethodName("valueOf"),
                          MethodDesc("(J)Ljava/lang/Long;"),
                          false),
        InstrINVOKEVIRTUAL(Owner.getLong,
                           MethodName("toString"),
                           MethodDesc("()Ljava/lang/String;"),
                           false),
        InstrDBGStrPrint(),
        InstrRETURN()
      ) ::
        Nil
    )
  }

  "DUP_X2" can "handle three ints" in {
    methodWithBlocks(
      Block(InstrICONST(1),
        InstrICONST(2),
        InstrICONST(3),
        InstrDUP_X2(),
        InstrDBGIPrint(),
        InstrDBGIPrint(),
        InstrDBGIPrint(),
        InstrDBGIPrint(),
        InstrRETURN()) ::
        Nil
    )
  }

  it can "handle one int and one long" in {
    methodWithBlocks(
      Block(
        InstrINVOKESTATIC(Owner.getRuntime,
          MethodName("getRuntime"),
          MethodDesc("()Ljava/lang/Runtime;"),
          false),
        InstrINVOKEVIRTUAL(Owner.getRuntime,
          MethodName("maxMemory"),
          MethodDesc("()J"),
          false),
        InstrICONST(1),
        InstrDUP_X2(),
        InstrDBGIPrint(),
        InstrINVOKESTATIC(Owner.getLong,
          MethodName("valueOf"),
          MethodDesc("(J)Ljava/lang/Long;"),
          false),
        InstrINVOKEVIRTUAL(Owner.getLong,
          MethodName("toString"),
          MethodDesc("()Ljava/lang/String;"),
          false),
        InstrDBGStrPrint(),
        InstrDBGIPrint(),
        InstrRETURN()) ::
        Nil
    )
  }

  it can "handle three vints" in {
    methodWithBlocks(
      createVint(tValue = 1, fValue = 2, startBlockIdx = 0, config = "A") :::
        createVint(tValue = 3, fValue = 4, startBlockIdx = 3, config = "B") :::
        createVint(tValue = 5, fValue = 6, startBlockIdx = 6, config = "C") :::
        Block(InstrDUP_X2(),
          InstrDBGIPrint(),
          InstrDBGIPrint(),
          InstrDBGIPrint(),
          InstrDBGIPrint(),
          InstrRETURN()) ::
        Nil
    )
  }

  "DUP2_X1" can "handle three ints" in {
    methodWithBlocks(
      Block(InstrICONST(1),
        InstrICONST(2),
        InstrICONST(3),
        InstrDUP2_X1(),
        InstrDBGIPrint(),
        InstrDBGIPrint(),
        InstrDBGIPrint(),
        InstrDBGIPrint(),
        InstrDBGIPrint(),
        InstrRETURN()) ::
        Nil
    )
  }

  it can "handle three vints" in {
    methodWithBlocks(
      createVint(tValue = 1, fValue = 2, startBlockIdx = 0, config = "A") :::
        createVint(tValue = 3, fValue = 4, startBlockIdx = 3, config = "B") :::
        createVint(tValue = 5, fValue = 6, startBlockIdx = 6, config = "C") :::
        Block(InstrDUP2_X1(),
          InstrDBGIPrint(),
          InstrDBGIPrint(),
          InstrDBGIPrint(),
          InstrDBGIPrint(),
          InstrDBGIPrint(),
          InstrRETURN()) ::
        Nil
    )
  }

  it can "handle one long and one int" in {
    methodWithBlocks(
      Block(
        InstrICONST(1),
        InstrINVOKESTATIC(Owner.getRuntime,
          MethodName("getRuntime"),
          MethodDesc("()Ljava/lang/Runtime;"),
          false),
        InstrINVOKEVIRTUAL(Owner.getRuntime,
          MethodName("maxMemory"),
          MethodDesc("()J"),
          false),
        InstrDUP2_X1(),
        InstrINVOKESTATIC(Owner.getLong,
          MethodName("valueOf"),
          MethodDesc("(J)Ljava/lang/Long;"),
          false),
        InstrINVOKEVIRTUAL(Owner.getLong,
          MethodName("toString"),
          MethodDesc("()Ljava/lang/String;"),
          false),
        InstrDBGStrPrint(),
        InstrDBGIPrint(),
        InstrINVOKESTATIC(Owner.getLong,
          MethodName("valueOf"),
          MethodDesc("(J)Ljava/lang/Long;"),
          false),
        InstrINVOKEVIRTUAL(Owner.getLong,
          MethodName("toString"),
          MethodDesc("()Ljava/lang/String;"),
          false),
        InstrDBGStrPrint(),
        InstrRETURN()) ::
        Nil
    )
  }
}
