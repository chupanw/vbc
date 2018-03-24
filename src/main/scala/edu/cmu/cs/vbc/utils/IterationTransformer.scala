package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.analysis.{REF_TYPE, VBCFrame, V_TYPE}
import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm._

import PartialFunction.cond

class IterationTransformer {
  import edu.cmu.cs.vbc.utils.LiftUtils.{fexprclasstype, fexprclassname, vclassname, vclasstype}
  val fePairClassName = "model/java/util/FEPair"
  val fePairClassType = s"L$fePairClassName;"
  val ctxListClassName = "model/java/util/CtxList"
  val ctxListClassType = s"L$ctxListClassName;"
  val objectClassType = "Ljava/lang/Object;"

  type BlockIndex = Int
  type InstructionIndex = Int

  def transformListIteration(cfg: CFG, env: VMethodEnv, cw: ClassVisitor): (CFG, VMethodEnv) = {
    val loops = env.loopAnalysis.loops.filter(isListIteration(_, env))

    if (loops.isEmpty) return (cfg, env)

    val (newCFG, blockUpdates) = insertElementSatisfiabilityConditional(cfg, loops)

    val loopBodyBlocks = loops.flatMap(_.body)
    def toUpdatedIndex(block: Block): BlockIndex = blockUpdates(cfg.blocks.indexOf(block))
    // the last body block(s) of loops are technically predecessors of the loop, but we are not interested in them
    val loopPredecessors = loops.flatMap(l => env.getPredecessors(l.entry)) diff loopBodyBlocks
    val loopPredecessorIndices = loopPredecessors map toUpdatedIndex
    val loopBodyStartBlocks = loopBodyBlocks collect {
      case block if block.instr.exists(isIteratorNextInvocation) => block
    }
    val loopBodyStartBlockIndices = loopBodyStartBlocks map toUpdatedIndex
    val bodyAfterSplitIndices = newCFG.blocks.zipWithIndex collect {
      case (bodyStartBlock, index) if loopBodyStartBlockIndices contains index => index + 1
    }

    // Get the index of INSN in newCFG
    val cfgInsnIdx: Instruction => InstructionIndex = {
      // This is the same way that VMethodEnv calculates insn index
      val newCFGInsns = newCFG.blocks.flatMap(_.instr)
      insn => newCFGInsns.indexWhere(_ eq insn)
    }

    val elementOneVar = new LocalVar("element$one$var", vclasstype)

    def pairWithRelativeOrder(bt: BlockTransformation, block: Block) = (bt, bt -> cfgInsnIdx(block.instr.head))
    val (blockTransformations, blockTransformOrdering) = newCFG.blocks.zipWithIndex.map({
      case (loopPredecessor, index) if loopPredecessorIndices contains index =>
        // Modify loopPredecessor to call CtxList.Simplify()
        val bt = transformLoopPredecessor(loopPredecessor, env, cw, cfgInsnIdx)
        pairWithRelativeOrder(bt, loopPredecessor)

      case (bodyStartBlock, index) if loopBodyStartBlockIndices contains index =>
        // Modify bodyStartBlock to unpack FEPair and check satisfiability
        // No need to jump, the jump insn was inserted by splitBlock
        val bt = transformBodyStartBlock(bodyStartBlock, cfgInsnIdx, elementOneVar)
        pairWithRelativeOrder(bt, bodyStartBlock)

      case (bodyAfterSplit, index) if bodyAfterSplitIndices contains index =>
//         Modify the second split half of the bodyStartBlock - after the satisfiability check - to wrap value in One
        val bt = transformBodyStartBlockAfterSplit(bodyAfterSplit, cfgInsnIdx, elementOneVar)
        pairWithRelativeOrder(bt, bodyAfterSplit)

      case (otherBlock, _) =>
        val bt = BlockTransformation(List(otherBlock), List(), List())
        pairWithRelativeOrder(bt, otherBlock)
    }).unzip

    // build a map from blocktransforms to the number that the newInsns need to be offset by
    val blockTransformsSortedByInstruction = blockTransformOrdering.sortBy(_._2).map(_._1)
    val blockTransformInsnOffset =
      blockTransformsSortedByInstruction.zipWithIndex.foldLeft(List.empty[(BlockTransformation, Int)])({
        case (offsetsSoFar, (bt, 0)) =>
          (bt -> 0) +: offsetsSoFar

        case (offsetsSoFar, (bt, index)) =>
          val offsetOfPrevTransform = offsetsSoFar.head._2
          val newInsnsInPrevTransform = offsetsSoFar.head._1.newInsnIndeces.length
          (bt -> (offsetOfPrevTransform + newInsnsInPrevTransform)) +: offsetsSoFar
    }).toMap
    // then, map the transformations to update the newInsnIndices using those offsets
    val correctedTransformations = blockTransformations.map(bt => {
      val offset = blockTransformInsnOffset(bt)
      val correctedNewInsnIndices = bt.newInsnIndeces.map(_ + offset)
      BlockTransformation(bt.newBlocks, correctedNewInsnIndices, bt.newVars)
    })
    val collectTransformations =
      correctedTransformations.foldRight((List.empty[Block], List.empty[Int], List.empty[Variable])) _
    val (newBlocks, newInsns, newVars) = collectTransformations((bt, collected) =>
      (bt.newBlocks ++ collected._1,
        bt.newInsnIndeces ++ collected._2,
        bt.newVars ++ collected._3))
    // Remove duplicate entries of new variables shared for multiple loops
    val uniqueNewVars = newVars.distinct


    val finalCFG: CFG = CFG(newBlocks)
    val newMN = VBCMethodNode(env.method.access, env.method.name, env.method.desc, env.method.signature,
      env.method.exceptions, finalCFG, env.method.localVar ++ uniqueNewVars)
    val newEnv = new VMethodEnv(env.clazz, newMN)

    newInsns.foreach(newEnv.instructionTags(_) |= env.TAG_PRESERVE)

    (finalCFG, newEnv)
  }
  def isIteratorNextInvocation(insn: Instruction): Boolean = insn match {
    case invoke: InstrINVOKEINTERFACE => invoke.name.name == "next" && invoke.owner.contains("Iterator")
    case _ => false
  }

  // Insert a conditional jump skipping the loop body if the element has an unsatisfiable context
  // Returns a new CFG containing the cleanup blocks, a mapping for each loop's cleanup block, and a
  // map for translating old references
  def insertElementSatisfiabilityConditional(cfg: CFG, loops: Iterable[Loop]): (CFG, Map[BlockIndex, BlockIndex]) = {
    val insertBlocks = loops.foldLeft((cfg, cfg.blocks.indices.map(i => i -> i).toMap)) _

    insertBlocks((collected, loop) => {
      val (collectedCFG, collectedBlockUpdates) = collected

      val loopEntryIdx = collectedBlockUpdates(cfg.blocks.indexOf(loop.entry))

      // Insert a block after the loop body that just jumps to the loop entry
      val lastLoopBodyBlockIdx = collectedBlockUpdates(loop.body.map(cfg.blocks.indexOf).max)
      val (workingCFG, insertedBlockUpdates) =
        collectedCFG.insertBlock(lastLoopBodyBlockIdx, Block(InstrGOTO(loopEntryIdx)))
      val insertedJumpBlockIdx = lastLoopBodyBlockIdx + 1

      val newWorkingCFG = CFG(workingCFG.blocks.zipWithIndex map {
        case (block, idx) if idx == lastLoopBodyBlockIdx =>
          block.copy(block.instr map {
            case insn: InstrGOTO => InstrGOTO(insertedJumpBlockIdx)
            case insn => insn
          })
        case (block, _) => block
      })

      val workingBlockUpdates = newWorkingCFG.mergeIndexMaps(collectedBlockUpdates, insertedBlockUpdates)

      // Want to split the block with the Iterator.next invocation, right after the invocation
      val findBlockToSplit =
        (block: Block) => loadUtil.findSome(block.instr.zipWithIndex, (pair: (Instruction, Int)) =>
          pair._1 match {
            case nextInvocation if isIteratorNextInvocation(nextInvocation) => Some((pair._2, block))
            case _ => None
          })
      val result = for {
        (nextInvocationIdx, block) <- loadUtil.findSome(loop.body, findBlockToSplit)
        blockToSplit = workingBlockUpdates(cfg.blocks.indexOf(block))
        splitInfo = SplitInfo(blockToSplit, nextInvocationIdx,
          InstrIFEQ,
          insertedJumpBlockIdx,
          Seq.empty, newWorkingCFG.blocks(blockToSplit).exceptionHandlers)
        (newCFG, newIndices) = newWorkingCFG.splitBlock(splitInfo)
      } yield {
        (newCFG,
          newCFG.mergeIndexMaps(workingBlockUpdates, newIndices))
      }
      result.get // refactor to remove get possible?
    })
  }

  def transformLoopPredecessor(loopPredecessor: Block, env: VMethodEnv, cw: ClassVisitor,
                               cfgInsnIdx: Instruction => InstructionIndex): BlockTransformation = {
    val lambdaName = "lambda$INVOKEVIRTUAL$simplifyCtxList"
    val lambdaDesc = s"($ctxListClassType)V"
    createSimplifyLambda(cw, lambdaName, lambdaDesc)

    // Add an invocation to ctxlist.simplify before the iterator invocation
    var newInsns = List.empty[Int]
    BlockTransformation(
      List(Block(loopPredecessor.instr flatMap {
        case itInvoke if isIteratorInvocation(itInvoke) =>
          val simplifyInsns = invokeSimplify(env.clazz.name, lambdaName, lambdaDesc)

          val iteratorIndex = cfgInsnIdx(itInvoke)
          // Range from index because new instructions come before simplify
          newInsns ++= List.range(iteratorIndex, iteratorIndex + simplifyInsns.size)

          simplifyInsns :+ itInvoke

        case insn => List(insn)
      }, loopPredecessor.exceptionHandlers)),
      newInsns,
      List())
  }

  var createdSimplifyLambdaMtd = false
  def createSimplifyLambda(cw: ClassVisitor, lambdaName: String, lambdaDesc: String): Unit = {
    if (!createdSimplifyLambdaMtd) {
      val mv = cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
        lambdaName, lambdaDesc, lambdaDesc, Array[String]())
      mv.visitCode()
      mv.visitVarInsn(Opcodes.ALOAD, 0)
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ctxListClassName, "simplify____V", "()V", false)
      mv.visitInsn(Opcodes.RETURN)
      mv.visitMaxs(2, 1)
      mv.visitEnd()

      createdSimplifyLambdaMtd = true
    }
  }
  def invokeSimplify(className: String, lambdaName: String, lambdaDesc: String): List[Instruction] = {
    val consumerName = "java/util/function/Consumer"
    val consumerType = s"L$consumerName;"
    // todo: this should map over the V wrapping the CtxList, but doing so causes a stack error for some reason
    // the assumption that the V wrapping the CtxList is a One should always be true: the whole
    // point of CtxList is that there is only one list despite having variations in elements
    List(
      InstrDUP(),

      // Assume the V is a One. It should be.
      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("getOne"), MethodDesc("()Ljava/lang/Object;"), true),
      InstrCHECKCAST(Owner(ctxListClassName)),
      InstrINVOKEVIRTUAL(Owner(ctxListClassName), MethodName("simplify____V"), MethodDesc("()V"), false)

      // Alternatively map over the V. This causes some sort of stack error; not sure why.
//      InstrINVOKEDYNAMIC(Owner(consumerName), MethodName("accept"), MethodDesc(s"()$consumerType"),
//        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
//          "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
//        Type.getType("(Ljava/lang/Object;)V"),
//        new Handle(Opcodes.H_INVOKESTATIC, className, lambdaName, lambdaDesc),
//        Type.getType(lambdaDesc)),
//      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("foreach"), MethodDesc(s"($consumerType)V"), true)
    )
  }

  def transformBodyStartBlock(bodyStartBlock: Block, cfgInsnIdx: Instruction => InstructionIndex,
                              elementOneVar: Variable): BlockTransformation = {
    // Unpack FEPair iterator after the iterator.next invocation, and test satisfiability of FEPair context
    // the jump using the result of the satisfiability test is already present after the next invocation
    // -- it was inserted by insertCleanupBlocks
    var newInsns = List.empty[Int]
    BlockTransformation(
      List(Block(bodyStartBlock.instr flatMap {
        case nextInvocation if isIteratorNextInvocation(nextInvocation) =>
          val unpackInsns = unpackFEPair(elementOneVar)
          val nextInvIndex = cfgInsnIdx(nextInvocation)
          newInsns ++= List.range(nextInvIndex + 1, nextInvIndex + 1 + unpackInsns.size)
          nextInvocation :: unpackInsns

        case otherInsn => List(otherInsn)
      }, bodyStartBlock.exceptionHandlers)),
      newInsns,
      List(elementOneVar))
  }
  def unpackFEPair(elementVar: Variable): List[Instruction] = {
    List(
      // stack: ..., One(FEPair)
      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("getOne"), MethodDesc("()Ljava/lang/Object;"), true),
      // ..., FEPair
      InstrCHECKCAST(Owner(fePairClassName)),
      // ..., FEPair
      InstrDUP(),
      // ..., FEPair, FEPair
      InstrGETFIELD(Owner(fePairClassName), FieldName("v"), TypeDesc(objectClassType)),
      // ..., FEPair, v
      InstrSWAP(),
      // ..., v, FEPair
      InstrGETFIELD(Owner(fePairClassName), FieldName("ctx"), TypeDesc(fexprclasstype)),
      // ..., v, FEctx
      InstrDUP(),
      // ..., v, FEctx, FEctx
      InstrLOAD_LOOP_CTX(),
      // ..., v, FEctx, FEctx, loopCtx
      InstrINVOKEINTERFACE(Owner(fexprclassname), MethodName("and"),
        MethodDesc(s"($fexprclasstype)$fexprclasstype"), true),
      // ..., v, FEctx, FEctx&loopCtx
      InstrDUP(),
      // ..., v, FEctx, FEctx&loopCtx, FEctx&loopCtx
      InstrINVOKEINTERFACE(Owner(fexprclassname), MethodName("isSatisfiable"), MethodDesc("()Z"), true),
      // ..., v, FEctx, FEctx&loopCtx, isSat?
      InstrINVOKESTATIC(Owner("java/lang/Integer"), MethodName("valueOf"), MethodDesc("(I)Ljava/lang/Integer;"), true),
      // ..., v, FEctx, FEctx&loopCtx, Integer<isSat?>
      InstrICONST(0),
      // ..., v, FEctx, FEctx&loopCtx, Integer<isSat?>, 0
      InstrINVOKESTATIC(Owner("java/lang/Integer"), MethodName("valueOf"), MethodDesc("(I)Ljava/lang/Integer;"), true),
      // ..., v, FEctx, FEctx&loopCtx, Integer<isSat?>, Integer<0>
      InstrINVOKESTATIC(Owner(vclassname), MethodName("choice"), MethodDesc(s"($fexprclasstype$objectClassType$objectClassType)$vclasstype"), true),
      // ..., v, FEctx, V<isSat?>
      InstrDUP_X2(),
      // ..., V<isSat?>, v, FEctx, V<isSat?>
      InstrPOP(),
      // ..., V<isSat?>, v, FEctx
      InstrSWAP(),
      // ..., V<isSat?>, FEctx, v
      InstrINVOKESTATIC(Owner(vclassname), MethodName("one"), MethodDesc(s"($fexprclasstype$objectClassType)$vclasstype"), true),
      // ..., V<isSat?>, One<v>
      InstrASTORE(elementVar)
      // ..., V<isSat?> -- to be checked on the jump inserted by insertElementSatisfiabilityConditional()
    )
  }
  def transformBodyStartBlockAfterSplit(bodyStartBlockAfterSplit: Block, cfgInsnIdx: Instruction => InstructionIndex,
                                        elementOneVar: Variable): BlockTransformation = {
    val loadElInsns = loadElement(elementOneVar)
    val firstInsnOfBlockIdx = cfgInsnIdx(bodyStartBlockAfterSplit.instr.head)
    val newInsns = List.range(firstInsnOfBlockIdx, firstInsnOfBlockIdx + loadElInsns.size)
    BlockTransformation(
      List(Block(loadElInsns ++ bodyStartBlockAfterSplit.instr, bodyStartBlockAfterSplit.exceptionHandlers)),
      newInsns,
      List())
  }
  def loadElement(elementOneVar: Variable): List[Instruction] = {
    List(
      InstrALOAD(elementOneVar)
    )
  }


  def isListIteration(loop: Loop, env: VMethodEnv): Boolean = {
    val predecessors = env.getPredecessors(loop.entry)
    predecessors.exists(_.instr.exists(isIteratorInvocation))
  }

  def isIteratorInvocation(insn: Instruction): Boolean = cond(insn) {
    case inv: InstrINVOKEVIRTUAL => isIteratorInvocation(inv)
    case inv: InstrINVOKEINTERFACE => isIteratorInvocation(inv)
  }
  def isIteratorInvocation(insn: InstrINVOKEVIRTUAL): Boolean =
    insn.name.name.equals("iterator") && insn.owner.name.contains("List")
  def isIteratorInvocation(insn: InstrINVOKEINTERFACE): Boolean =
    insn.name.name.equals("iterator") && insn.owner.name.contains("List")
}

case class BlockTransformation(newBlocks: List[Block], newInsnIndeces: List[Int], newVars: List[Variable]) {
  def +(that: BlockTransformation): BlockTransformation =
    BlockTransformation(newBlocks ++ that.newBlocks,
      newInsnIndeces ++ that.newInsnIndeces,
      newVars ++ that.newVars)
}

object loadUtil {
  // Narrow the type of X to TO, if possible
  def narrow[To](x: Any): Option[To] = {
    x match {
      case xTo: To => Some(xTo)
      case _ => None
    }
  }

  // Find the first element e in CONTAINER such that F(e).nonEmpty
  def findSome[A, B](container: Traversable[A], f: (A => Option[B])): Option[B] = {
    for (el <- container) {
      val res = f(el)
      if (res.nonEmpty) return res
    }
    None
  }
}

case class InstrLOAD_LOOP_CTX() extends Instruction {
  def toByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    val thisBlock = env.getBlockForInstruction(this)
    for {
      thisLoop <- env.loopAnalysis.loops.find(_.body.contains(thisBlock))
      loopCtxVarIdx = env.getVarIdx(env.getVBlockVar(thisLoop.entry))
    } yield {
      mv.visitVarInsn(Opcodes.ALOAD, loopCtxVarIdx + 1) // TODO: Not sure why this needs to be +1
    }
  }
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    assert(false, "invalid override of toByteCode called on fake instruction LOAD_LOOP_CTX." +
      "This instruction should be passed a VMethodEnv rather than a MethodEnv.")
  }
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    assert(false, "toVByteCode called on fake instruction LOAD_LOOP_CTX. This instruction should not be lifted.")
  }
  // this is the same as instrALOAD - I just want it to push a new reference
  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    if (!env.shouldLiftInstr(this))
      (s.push(REF_TYPE(), Set(this)), Set())
    else {
      val newFrame = s.push(V_TYPE(), Set(this))
      val backtrack = Set[Instruction]()
      (newFrame, backtrack)
    }
  }
}