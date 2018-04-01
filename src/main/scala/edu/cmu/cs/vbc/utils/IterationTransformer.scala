package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.analysis.{REF_TYPE, VBCFrame, V_TYPE}
import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm._

import PartialFunction.cond

/**
  * Class encapsulating the operations to transform loops iterating over `CtxList`s.
  * This transformation allows those loops to leverage the optimized structure of `CtxList`s.
  *
  * Its entry point is `transformListIteration`.
  */
class IterationTransformer {
  import edu.cmu.cs.vbc.utils.LiftUtils.{fexprclasstype, fexprclassname, vclassname, vclasstype}
  val fePairClassName = "model/java/util/FEPair"
  val fePairClassType = s"L$fePairClassName;"
  val ctxListClassName = "model/java/util/CtxList"
  val ctxListClassType = s"L$ctxListClassName;"
  val objectClassType = "Ljava/lang/Object;"

  type BlockIndex = Int
  type InstructionIndex = Int

  /**
    * Transform the given CFG (with the given VMethodEnv) for the given ClassVisitor
    * to optimize list iteration loops to take advantage of CtxLists. This only makes sense
    * if `ctxListEnabled` is true.
    * @param cfg The CFG of the method to transform.
    * @param env The VMethodEnv of the method to transform.
    * @param cw The ClassVisitor of the class in which `cfg`'s method exists.
    * @return A CFG and VMethodEnv in which all list iteration loops are transformed.
    */
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
        // Modify the second split half of the bodyStartBlock - after the satisfiability check - to wrap value in One
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

  /**
    * Insert a conditional jump skipping the body of loop iterations when the iteration element has an
    * unsatisfiable context. This involves splitting a block and inserting an entirely new block in the
    * given CFG.
    * @param cfg The CFG containing the `loops`.
    * @param loops The loops for which to insert the conditional jump.
    * @return A pair of updated CFG, and map from old CFG block indices to new indices.
    */
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

  /**
    * Transform the block directly preceding the loop. This block should contain a `iterator` method
    * invocation.
    * The transformation consists of inserting a call to `CtxList.simplify` before calling `iterator`.
    * @param loopPredecessor The block preceding the loop.
    * @param env The VMethodEnv of this method.
    * @param cw The ClassVisitor of this class.
    * @param cfgInsnIdx A function mapping `Instruction`s to their indices in the CFG of this method.
    * @return
    */
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

  // So that multiple simplify lambdas are not created per class.
  var createdSimplifyLambdaMtd = false
  /**
    * Create the lambda for invoking `CtxList.simplify` (necessary to map over the V wrapping the CtxList)
    * by adding a method to the current class.
    * @param cw The ClassVisitor for the current class.
    * @param lambdaName The name of the lambda.
    * @param lambdaDesc The signature/descriptor of the lambda.
    */
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

  /**
    * Return the sequence of instructions to invoke `CtxList.simplify` on a CtxList.
    * Note: Assumes that the top of the stack contains a reference to the CtxList wrapped in a One.
    *
    * @param className The name of the current class.
    * @param lambdaName The name of the simplify lambda.
    * @param lambdaDesc The signature/descriptor of the simplify lambda.
    * @return The instructions.
    */
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

  /**
    * Transform the first block of a loop body.
    * The transformation consists of unpacking the `FEPair` into its value and context and then checking the context's
    * satisfiability. The check leaves the satisfiability result on the stack to be used by the jump inserted by
    * `insertElementSatisfiabilityConditional`.
    * The `elementOneVar` is used to store the element value because the stack must be empty at the end of a block.
    * @param bodyStartBlock The first block of the loop body.
    * @param cfgInsnIdx A function mapping `Instruction`s to their indices in the CFG of this method.
    * @param elementOneVar The Variable in which to store the element value.
    * @return
    */
  def transformBodyStartBlock(bodyStartBlock: Block, cfgInsnIdx: Instruction => InstructionIndex,
                              elementOneVar: Variable): BlockTransformation = {
    // Unpack FEPair iterator after the iterator.next invocation, and test satisfiability of FEPair context
    // the jump using the result of the satisfiability test is already present after the next invocation
    // -- it was inserted by `insertElementSatisfiabilityConditional`
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

  /**
    * Return the sequence of instructions to unpack the `FEPair` on the stack, check the satisfiability
    * of its context, and store the value into `elementVar`.
    * @param elementVar The Variable in which to store the element value.
    * @return The instructions.
    */
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

  /**
    * Transform the block immediately after the first body block of a loop. This block was created by splitting
    * the original first body block with the element satisfiability conditional check.
    * The transformation consists of getting the element value from `elementOneVar`, so that it is ready to be used
    * by the loop body.
    * @param bodyStartBlockAfterSplit The block immediately after the first body block of the loop.
    * @param cfgInsnIdx A function mapping `Instruction`s to their indices in the CFG of this method.
    * @param elementOneVar The Variable the element value is in.
    * @return
    */
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

  /**
    * Return the instruction sequence to get the element value onto the stack, so that the loop body can use it.
    * @param elementOneVar The Variable the element value is in.
    * @return The instructions.
    */
  def loadElement(elementOneVar: Variable): List[Instruction] = {
    List(
      InstrALOAD(elementOneVar)
    )
  }


  /**
    * Determines if the given `loop` is iterating over a `CtxList` (and therefore needs to be transformed).
    * @param loop The loop in question.
    * @param env The VMethodEnv of the method containing `loop`.
    * @return Is `loop` iterating over a `CtxList`?
    */
  def isListIteration(loop: Loop, env: VMethodEnv): Boolean = {
    val predecessors = env.getPredecessors(loop.entry)
    predecessors.exists(_.instr.exists(isIteratorInvocation))
  }

  /**
    * @param insn Any Instruction.
    * @return Is `insn` an invocation of `iterator`?
    */
  def isIteratorInvocation(insn: Instruction): Boolean = cond(insn) {
    case inv: InstrINVOKEVIRTUAL => isIteratorInvocation(inv)
    case inv: InstrINVOKEINTERFACE => isIteratorInvocation(inv)
  }
  def isIteratorInvocation(insn: InstrINVOKEVIRTUAL): Boolean =
    insn.name.name.equals("iterator") && insn.owner.name.contains("List")
  def isIteratorInvocation(insn: InstrINVOKEINTERFACE): Boolean =
    insn.name.name.equals("iterator") && insn.owner.name.contains("List")

  /**
    * @param insn Any Instruction.
    * @return Is `insn` an invocation of `Iterator.next`?
    */
  def isIteratorNextInvocation(insn: Instruction): Boolean = insn match {
    case invoke: InstrINVOKEINTERFACE => invoke.name.name == "next" && invoke.owner.contains("Iterator")
    case _ => false
  }
}

/**
  * Class representing a transformed block.
  * @param newBlocks The blocks that should take the place of the old block.
  * @param newInsnIndeces The indices in the CFG of newly inserted Instructions.
  * @param newVars New Variables required by `newBlocks`.
  */
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

/**
  * Dummy instruction that puts the Variable containing the current loop ctx var onto the stack.
  * This dummy instruction is necessary because the loop ctx var is not known until after the new VMethodEnv
  * is created for the transformed CFG.
  */
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
      val newFrame = s.push(V_TYPE(false), Set(this))
      val backtrack = Set[Instruction]()
      (newFrame, backtrack)
    }
  }
}