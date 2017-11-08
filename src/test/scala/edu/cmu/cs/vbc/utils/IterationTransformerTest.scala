package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode.instructions._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.tree._
import org.objectweb.asm.util.{CheckClassAdapter, Textifier, TraceClassVisitor}
import org.objectweb.asm._
import org.objectweb.asm.Opcodes._
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConversions._
import PartialFunction.cond

import edu.cmu.cs.vbc.utils.LiftUtils.{vclassname, fexprclassname, fexprclasstype}

class IterationTransformerTest extends FunSuite with Matchers {

  def equal(a: Block, b: Block): Boolean = {
    // No value equality for instrs by design, so this is an ugly workaround
    val aStr = a.instr.toList.toString
    val bStr = b.instr.toList.toString
    if (aStr == bStr) {
      true
    } else {
      print(aStr + "\n" + bStr)
      false
    }
  }
  def equal(a: List[Block], b: List[Block]): Boolean = {
    val sizeEqual = a.size == b.size
    val blocksEqual = a.zip(b).foldRight(true)((blocks: (Block, Block), matchSoFar: Boolean) => matchSoFar && equal(blocks._1, blocks._2))
    sizeEqual && blocksEqual
  }
  def equal(a: CFG, b: CFG): Boolean = equal(a.blocks, b.blocks)

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
    Block(InstrLDC("orig 10"), InstrDUP(), InstrPOP(), InstrIFEQ(11)), // 9
    Block(InstrLDC("orig 11"), InstrDUP(), InstrPOP(), InstrGOTO(7)), // 10: loop ^
    Block(InstrLDC("orig 12"), InstrDUP(), InstrPOP(), InstrGOTO(11)) // 11
    ))

//  val valid_cfg_2loop = CFG(List(
//    Block(InstrLDC("orig 1"), InstrPOP(), InstrICONST(1), InstrPOP(), InstrRETURN())))
  val valid_cfg_2loop = CFG(List(
    Block(InstrLDC("orig 1"), InstrPOP(), InstrICONST(1), InstrDUP(), InstrGOTO(1)), // 0
    Block(InstrLDC("orig 2"), InstrPOP(), InstrPOP(), InstrICONST(2), InstrGOTO(2)), // 1
    Block(InstrLDC("orig 3"), InstrPOP(), InstrSWAP(), InstrPOP(), // 2
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(), InstrGOTO(3)),
    Block(InstrLDC("orig 4"), InstrPOP(), InstrICONST(1), InstrGOTO(4)), // 3: loop entry
    Block(InstrLDC("orig 5"), InstrPOP(), // 4
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(5)),
    Block(InstrLDC("orig 6"), InstrPOP(), InstrIFEQ(3)), // 5: loop ^
    Block(InstrLDC("orig 7"), InstrPOP(), InstrICONST(3), // 6
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(), InstrGOTO(7)),
    Block(InstrLDC("orig 8"), InstrPOP(), InstrICONST(7), InstrGOTO(8)), // 7: loop entry
    Block(InstrLDC("orig 9"), InstrPOP(), // 8
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(9)),
    Block(InstrLDC("orig 10"), InstrPOP(), InstrIFEQ(11)), // 9
    Block(InstrLDC("orig 11"), InstrPOP(), InstrICONST(8), InstrPOP(), InstrGOTO(7)), // 10: loop ^
    Block(InstrLDC("orig 12"), InstrPOP(), InstrICONST(9), InstrPOP(), InstrRETURN()) // 11
  ))
  def valid_cfg_2loop_insnIdx(insn: Instruction) = valid_cfg_2loop.blocks.flatMap(_.instr).indexWhere(_ eq insn)


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
        InstrIFEQ(6)),
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
        InstrIFEQ(6)),
      Block(List(InstrPOP(), InstrPOP(), InstrGOTO(3)), Nil), // 5: cleanup block
      Block(InstrDUP(), InstrPOP(), InstrGOTO(7)), // 6: second split-half
      Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 7: loop ^
      Block(InstrLDC("orig 7"), InstrDUP(), // 8
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrPOP(), InstrGOTO(9)),
      Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(10)), // 9: loop entry
      Block(InstrLDC("orig 9"), // 10
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFEQ(12)),
      Block(List(InstrPOP(), InstrPOP(), InstrGOTO(9)), Nil), // 11: cleanup block
      Block(List(InstrDUP(), InstrPOP(), InstrGOTO(13)), Nil), // 12: second split-half
      Block(InstrLDC("orig 10"), InstrDUP(), InstrPOP(), InstrIFEQ(15)), // 13
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

  // ===== createSimplifyLambda =====
  test("createSimplifyLambda creates lambda") {
    val itt = new IterationTransformer()
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES)

    val lambdaName = "lambda$INVOKEVIRTUAL$simplifyCtxList"
    val lambdaDesc = s"(${itt.ctxListClassType})V"

    itt.createSimplifyLambda(cw, lambdaName, lambdaDesc)

    // todo: verify that class has the added method ...
    val cr = new ClassReader(cw.toByteArray)
    val classNode = new ClassNode(ASM5)
    cr.accept(classNode, 0)
    def isTheLambda(mn: MethodNode) = mn.name == lambdaName && mn.desc == lambdaDesc

    assert(classNode.methods.toList.exists(isTheLambda))

    val insns = new InsnList()
    insns.add(new VarInsnNode(ALOAD, 0))
    val invOwner = itt.ctxListClassName
    val invName = "simplify____V"
    val invDesc = "()V"
    insns.add(new MethodInsnNode(INVOKEVIRTUAL, invOwner, invName, invDesc, false))
    insns.add(new InsnNode(RETURN))

    for { mn <- classNode.methods.find(isTheLambda) }
      yield {
        assert(cond(mn.instructions.get(0)) {
          case v: VarInsnNode => v.getOpcode == ALOAD && v.`var` == 0
        })
        assert(cond(mn.instructions.get(1)) {
          case m: MethodInsnNode =>
            m.getOpcode == INVOKEVIRTUAL && m.owner == invOwner && m.name == invName && m.desc == invDesc
        })
        assert(cond(mn.instructions.get(2)) {
          case r: InsnNode => r.getOpcode == RETURN
        })
      }
    // todo: figure out checking JVM compliance
//    cr.accept(new CheckClassAdapter(cw), 0) // throws null exception
  }


  // ===== transformLoopPredecessor =====
  test("transformLoopPredecessor works") {
    val className = "testclass"
    val vbcMtdNode = VBCMethodNode(0, "test", "()V", None, List.empty, valid_cfg_2loop)
    val vbcClazz = VBCClassNode(0, 0, className, None, "java/util/Object", List.empty, List.empty, List(vbcMtdNode))
    val env = new VMethodEnv(vbcClazz, vbcMtdNode)

    val itt = new IterationTransformer()
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES)

    val loopPredecessor = valid_cfg_2loop.blocks(2)



    val blockTrans = itt.transformLoopPredecessor(loopPredecessor, env, cw, valid_cfg_2loop_insnIdx)

    val lambdaName = "lambda$INVOKEVIRTUAL$simplifyCtxList"
    val lambdaDesc = s"(${itt.ctxListClassType})V"
    val consumerName = "java/util/function/Consumer"
    val consumerType = s"L$consumerName;"

    assert(blockTrans.newVars.isEmpty)
    assert(equal(blockTrans.newBlocks, List(Block(
      InstrLDC("orig 3"),
      InstrPOP(),
      InstrSWAP(),
      InstrPOP(), // 13

      InstrDUP(),
      InstrINVOKEDYNAMIC(Owner(consumerName), MethodName("accept"), MethodDesc(s"()$consumerType"),
        new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
          "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
        Type.getType("(Ljava/lang/Object;)V"),
        new Handle(H_INVOKESTATIC, className, lambdaName, lambdaDesc),
        Type.getType(lambdaDesc)),
      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("foreach"), MethodDesc(s"($consumerType)V"), true),

      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(),
      InstrGOTO(3)
    ))))
    val iteratorIndex = 14
    assert(blockTrans.newInsnIndeces == List(iteratorIndex, iteratorIndex + 1, iteratorIndex + 2))
  }



  // ===== transformBodyStartBlock =====
  test("transformBodyStartBlock works") {
    val itt = new IterationTransformer()
    val bodyStartBlock = valid_cfg_2loop.blocks(4)
    val blockTrans = itt.transformBodyStartBlock(bodyStartBlock, valid_cfg_2loop_insnIdx)

    assert(blockTrans.newVars.isEmpty)
    assert(equal(blockTrans.newBlocks, List(Block(
      InstrLDC("orig 5"),
      InstrPOP(),
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),

      // stack: ..., One(FEPair)
      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("getOne"), MethodDesc("()Ljava/lang/Object;"), true),
      // ..., FEPair
      InstrCHECKCAST(Owner(itt.fePairClassName)),
      InstrDUP(),
      // ..., FEPair, FEPair
      InstrGETFIELD(Owner(itt.fePairClassName), FieldName("v"), TypeDesc(itt.objectClassType)),
      // ..., FEPair, v
      InstrSWAP(),
      // ..., v, FEPair
      InstrGETFIELD(Owner(itt.fePairClassName), FieldName("ctx"), TypeDesc(fexprclasstype)),
      // ..., v, ctx
      InstrDUP(),
      // ..., v, ctx, ctx
      InstrLOAD_LOOP_CTX(),
//      InstrALOAD(loopCtxVar),
      // ..., v, ctx, ctx, loopCtx
      InstrINVOKEINTERFACE(Owner(fexprclassname), MethodName("and"),
        MethodDesc(s"(${fexprclasstype})${fexprclasstype}"), true),
      // ..., v, ctx, FE
      InstrINVOKEINTERFACE(Owner(fexprclassname), MethodName("isSatisfiable"), MethodDesc("()Z"), true),
      // ..., v, ctx, isSat?

      InstrDUP(),
      InstrPOP(),
      InstrGOTO(5)
    ))))
    val nextInvIndex = 23
    assert(blockTrans.newInsnIndeces == List.range(nextInvIndex + 1, nextInvIndex + 11))
  }
}