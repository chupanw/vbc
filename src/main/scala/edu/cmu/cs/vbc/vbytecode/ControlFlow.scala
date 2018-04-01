package edu.cmu.cs.vbc.vbytecode

import edu.cmu.cs.vbc.utils.{InstrLOAD_LOOP_CTX, LiftUtils, loadUtil}
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree.TypeAnnotationNode
import org.objectweb.asm.{Label, MethodVisitor}


/**
  * for design rationale, see https://github.com/ckaestne/vbc/wiki/ControlFlow
  */

object Block {
  def apply(instrs: Instruction*): Block = Block(instrs, Nil)
}

case class Block(instr: Seq[Instruction], exceptionHandlers: Seq[VBCHandler]) {

  import LiftUtils._

  def toByteCode(mv: MethodVisitor, env: MethodEnv) = {
    //    validate()

    mv.visitLabel(env.getBlockLabel(this))
    instr.foreach(_.toByteCode(mv, env, this))
    writeExceptions(mv, env)
  }

  def toVByteCode(mv: MethodVisitor, env: VMethodEnv, isFirstBlockOfInit: Boolean = false) = {
    vvalidate(env)
    mv.visitLabel(env.getBlockLabel(this))

    //possibly jump over VBlocks and load extra stack variables (if this is the VBLock head)
    //a unique first block always has a satisfiable condition and no stack variables
    //exception blocks have no stack variables and always a satisfiable condition
    if (env.isVBlockHead(this) && !isUniqueFirstBlock(env)) {
      vblockSkipIfCtxContradition(mv, env)
      loadUnbalancedStackVariables(mv, env)
    }

    //generate block code
    instr.foreach({
      // respect instruction tags in env
      case loadLoopCtx: InstrLOAD_LOOP_CTX => loadLoopCtx.toByteCode(mv, env, this) // todo: make this be based on tags as well
      case insertedInsn if env.getTag(insertedInsn, env.TAG_PRESERVE) => insertedInsn.toByteCode(mv, env, this)
      case other => other.toVByteCode(mv, env, this)
    })

    //if this block ends with a jump to a different VBlock (always all jumps are to the same or to
    //different VBlocks, never mixed)
    if (env.isVBlockEnd(this)) {
      storeUnbalancedStackVariables(mv, env)
      variationalJump(mv, env)
    } else {
      nonvariationalJump(mv, env)
    }

    mv.visitLabel(env.getBlockEndLabel(this))
//    writeExceptions(mv, env)
  }


  def validate(): Unit = {
    // ensure last statement is the only jump instruction, if any
    instr.dropRight(1).foreach(i => {
      assert(!i.isJumpInstr, "only the last instruction in a block may be a jump instruction (goto, if)")
      assert(!i.isReturnInstr, "only the last instruction in a block may be a return instruction")
    })
  }

  def vvalidate(env: VMethodEnv): Unit = {
    validate()
    //additionally ensure that the last block is the only one that contains a return statement
//    if (this != env.getLastBlock())
//      assert(!instr.last.isReturnInstr, "only the last block may contain a return instruction in variational byte code")
  }


  override def equals(that: Any): Boolean = that match {
    case t: Block => t eq this
    case _ => false
  }

  /**
    * writing exception table for every block separately.
    * this may produce larger than necessary tables when two consecutive blocks
    * have the same or overlapping handlers, but it's easier to write and shouldn't
    * really affect runtime performance in practice
    *
    * atomic exceptions that can be thrown by instructions in the block
    * are handled as follows:
    * if there is already an exception catching them, great, nothing to do.
    * otherwise, add a handler that just uses the ATHROW mechanism and then
    * jumps back to the first block (after updating this blocks condition).
    * we handling the prioritization by adding the atomic exception handlers to
    * the end.
    */
  private def writeExceptions(mv: MethodVisitor, env: MethodEnv) = {
    if (exceptionHandlers.nonEmpty) {
      val blockStartLabel = env.getBlockLabel(this)
      val blockEndLabel = new Label()
      mv.visitLabel(blockEndLabel)

      for (handler <- exceptionHandlers) {
        mv.visitTryCatchBlock(blockStartLabel, blockEndLabel, env.getBlockLabel(env.getBlock(handler.handlerBlockIdx)), handler.exceptionType)
        for (an <- handler.visibleTypeAnnotations)
          an.accept(mv.visitTryCatchAnnotation(an.typeRef, an.typePath, an.desc, true))
        for (an <- handler.invisibleTypeAnnotations)
          an.accept(mv.visitTryCatchAnnotation(an.typeRef, an.typePath, an.desc, true))
      }
    }
  }

  /**
    * do not need the possibility to jump over the first block if
    * is not a jump target within the method, as it can only be executed
    * at the method beginning, where we assume satisfiable contexts.
    */
  private def isUniqueFirstBlock(env: VMethodEnv) =
    env.vblocks.head.firstBlock == this && env.getPredecessors(this).isEmpty


  private def loadUnbalancedStackVariables(mv: MethodVisitor, env: VMethodEnv): Unit = {
    //load local variables if this block is expecting some values on stack
    val expectingVars = env.getExpectingVars(this)
    if (expectingVars.nonEmpty) {
      expectingVars.foreach(
        (v: Variable) => {
          mv.visitVarInsn(ALOAD, env.getVarIdx(v))
        }
      )
    }
  }

  /**
    * each VBlock as a unique entry point. At this entry point, we check whether
    * we should jump over this VBlock to the next.
    */
  private def vblockSkipIfCtxContradition(mv: MethodVisitor, env: VMethodEnv): Unit = {
    assert(env.isVBlockHead(this))
    val nextVBlock = env.getNextVBlock(env.getVBlock(this))
    val thisVBlockConditionVar = env.getVBlockVar(this)

    //load block condition (local variable for each block)
    //jump to next block if condition is contradictory
    if (nextVBlock.isDefined) {
      loadFExpr(mv, env, thisVBlockConditionVar)
      //            mv.visitInsn(DUP)
      //            mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/test/TestOutput", "printFE", "(Lde/fosd/typechef/featureexpr/FeatureExpr;)V", false)
      callFExprIsContradiction(mv)
      //            mv.visitInsn(DUP)
      //            mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/test/TestOutput", "printI", "(I)V", false)
      mv.visitJumpInsn(IFNE, env.getVBlockLabel(nextVBlock.get))
    }
  }

  private def storeUnbalancedStackVariables(mv: MethodVisitor, env: VMethodEnv): Unit = {
    //store local variables if this block is leaving some values on stack
    val leftVars = env.getLeftVars(this)
    if (leftVars.nonEmpty) {
      var hasFEOnTop = false
      if (instr.last.isJumpInstr) {
        val j = instr.last.asInstanceOf[JumpInstruction]
        val (uncond, cond) = j.getSuccessor()
        if (cond.isDefined) {
          // conditional jump, which means there is a FE on the stack right now
          hasFEOnTop = true
        }
      }
      leftVars.reverse.foreach(
        (s: Set[Variable]) => {
          if (hasFEOnTop) mv.visitInsn(SWAP)
          s.size match {
            case 1 =>
              val v = s.toList.head
              loadFExpr(mv, env, env.getVBlockVar(this))
              mv.visitInsn(SWAP)
              mv.visitVarInsn(ALOAD, env.getVarIdx(v))
              callVCreateChoice(mv)
              mv.visitVarInsn(ASTORE, env.getVarIdx(v))
            case 2 =>
              val list = s.toList
              val v1 = list.head
              val v2 = list.last
              mv.visitInsn(DUP)
              loadFExpr(mv, env, env.getVBlockVar(this))
              mv.visitInsn(SWAP)
              mv.visitVarInsn(ALOAD, env.getVarIdx(v1))
              callVCreateChoice(mv)
              mv.visitVarInsn(ASTORE, env.getVarIdx(v1))
              loadFExpr(mv, env, env.getVBlockVar(this))
              mv.visitInsn(SWAP)
              mv.visitVarInsn(ALOAD, env.getVarIdx(v2))
              callVCreateChoice(mv)
              mv.visitVarInsn(ASTORE, env.getVarIdx(v2))
            case v => throw new RuntimeException(s"size of Set[Variable] is $v, but expected 1 or 2")
          }
        }
      )
    }
  }

  private def nonvariationalJump(mv: MethodVisitor, env: VMethodEnv): Unit = {
    //nothing to do. already handled as part of the normal instruction
    val (unconditional, conditional) = env.getJumpTargets(this)
    assert(conditional.isEmpty, "Non-variational jump could not jump to a conditional target")
    if (unconditional.isDefined) {
      val jumpTarget: Block = unconditional.get
      if (env.isVBlockHead(jumpTarget))
        storeUnbalancedStackVariables(mv, env)
      mv.visitJumpInsn(GOTO, env.getBlockLabel(jumpTarget))
    } else {
      // last block, do nothing
    }
  }

  private def variationalJump(mv: MethodVisitor, env: VMethodEnv): Unit = {
    val jumpTargets = env.getVJumpTargets(this)
    val thisVBlockConditionVar = env.getVBlockVar(this)

    if (jumpTargets._1.isEmpty) {
      // last block, nothing to do
    } else if (jumpTargets._2.isEmpty) {
      val targetBlock = jumpTargets._1.get
      val targetBlockConditionVar = env.getVBlockVar(targetBlock)
      //if non-conditional jump
      //- update next block's condition (disjunction with prior value)
      loadFExpr(mv, env, thisVBlockConditionVar)
      loadFExpr(mv, env, targetBlockConditionVar)
      callFExprOr(mv)
      storeFExpr(mv, env, targetBlockConditionVar)

      //- set this block's condition to FALSE
      if (thisVBlockConditionVar != targetBlockConditionVar) {
        pushConstantFALSE(mv)
        storeFExpr(mv, env, thisVBlockConditionVar)
      }

      //- if backward jump, jump there (target condition is satisfiable, because this block's condition is and it's propagated)
      if (env.isVBlockBefore(targetBlock, env.getVBlock(this))) {
        mv.visitJumpInsn(GOTO, env.getVBlockLabel(targetBlock))
      } else if (Some(targetBlock) == env.getNextBlock(this)) {
        //forward jump to next block is leaving this block; then the next block must be the next vblock. do nothing.
      } else {
        //found some forward jump, that's leaving this vblock
        //jump to next vblock (not next block) that's not an exception handler
        val nextVBlock = env.getNextVBlock(env.getVBlock(this))
        if (nextVBlock.isDefined)
          mv.visitJumpInsn(GOTO, env.getVBlockLabel(nextVBlock.get))
      }


    } else {
      //if conditional jump (then the last instruction left us a featureexpr on the stack)
      val thenBlock = jumpTargets._2.get
      val thenBlockConditionVar = env.getVBlockVar(thenBlock)
      val elseBlock = jumpTargets._1.get
      val elseBlockConditionVar = env.getVBlockVar(elseBlock)
      mv.visitInsn(DUP)
      // -- stack: 2x Fexpr representing if condition

      //- update else-block's condition (ie. next block)
      callFExprNot(mv)
      loadFExpr(mv, env, thisVBlockConditionVar)
      callFExprAnd(mv)
      if (thisVBlockConditionVar != elseBlockConditionVar) {
        loadFExpr(mv, env, elseBlockConditionVar)
        callFExprOr(mv)
      }
      storeFExpr(mv, env, elseBlockConditionVar)


      val needToJumpBack = env.isVBlockBefore(thenBlock, env.getVBlock(this)) || env.isSameVBlock(thenBlock, env.getVBlock(this))
      //- update then-block's condition to "then-successor.condition or (thisblock.condition and A)"
      loadFExpr(mv, env, thisVBlockConditionVar)
      callFExprAnd(mv)
      if (thisVBlockConditionVar != thenBlockConditionVar) {
        loadFExpr(mv, env, thenBlockConditionVar)
        callFExprOr(mv)
      }
      if (needToJumpBack)
        mv.visitInsn(DUP)
      storeFExpr(mv, env, thenBlockConditionVar)

      //- set this block's condition to FALSE
      if (thisVBlockConditionVar != thenBlockConditionVar && thisVBlockConditionVar != elseBlockConditionVar) {
        pushConstantFALSE(mv)
        storeFExpr(mv, env, thisVBlockConditionVar)
      }

      //- if then-block is behind and its condition is satisfiable, jump there
      if (needToJumpBack) {
        //value remembered with DUP up there to avoid loading it again
        callFExprIsSatisfiable(mv)
        mv.visitJumpInsn(IFNE, env.getVBlockLabel(thenBlock))
      }
    }
  }
}


case class CFG(blocks: List[Block]) {
  type BlockIndex = Int

  def toByteCode(mv: MethodVisitor, env: MethodEnv) = {
    blocks.foreach(_.toByteCode(mv, env))
  }

  def writeHandler(mv: MethodVisitor,
                   env: VMethodEnv,
                   handler: VBCHandler,
                   startIdx: Int,
                   endIdx: Int,
                   remaining: List[Int]): Unit = {
    def write(): Unit = {
      mv.visitTryCatchBlock(
        env.getBlockLabel(env.getBlock(startIdx)),  // start label
        env.getBlockEndLabel(env.getBlock(endIdx)), // end label
        env.getBlockLabel(env.getBlock(handler.handlerBlockIdx)), // handler block label
        handler.exceptionType // exception type
      )
      for (an <- handler.visibleTypeAnnotations)
        an.accept(mv.visitTryCatchAnnotation(an.typeRef, an.typePath, an.desc, true))
      for (an <- handler.invisibleTypeAnnotations)
        an.accept(mv.visitTryCatchAnnotation(an.typeRef, an.typePath, an.desc, true))
    }
    if (remaining.isEmpty)
      write()
    else {
      val next = remaining.head
      if (next == endIdx + 1)
        writeHandler(mv, env, handler, startIdx, next, remaining.tail)
      else {
        write()
        writeHandler(mv, env, handler, remaining.head, remaining.head, remaining.tail)
      }
    }
  }

  def toVByteCode(mv: MethodVisitor, env: VMethodEnv) = {
    // Write exception handler table
    val allHandlers = blocks.flatMap(_.exceptionHandlers).distinct
    for (handler <- allHandlers) {
      val bs = blocks.filter(_.exceptionHandlers.contains(handler))
      val indexes = bs.map(blocks.indexOf(_))
      writeHandler(mv, env, handler, indexes.head, indexes.head, indexes.tail)
    }

    var initializeVars: List[LocalVar] = Nil

    //initialize all fresh variables (e.g., used for result, unbalanced stacks, exceptionCond, blockCondition)
    initializeVars ++= env.getFreshVars()

    //there might be a smarter way, but as we need to load an old value when
    //conditionally storing an updated value, we need to initialize all lifted
    //fields. here setting them all to One(null)
    //the same process occurs (not actually but as a potential case for the
    //analysis when jumping over unsatisfiable blocks)
    initializeVars ++= env.getLocalVariables()

    for (v <- initializeVars.distinct)
      v.vinitialize(mv, env, v)

    //serialize blocks, but keep the last vblock in one piece at the end (requires potential reordering of blocks
    val lastVBlock = env.getLastVBlock().allBlocks
    blocks.filterNot(lastVBlock.contains).foreach(_.toVByteCode(mv, env))
    blocks.filter(lastVBlock.contains).foreach(_.toVByteCode(mv, env))
  }


  /**
    * Perform the block split identified by `info`.
    *
    * Note that the blocks in the new CFG will not be the same Block objects in `this` because
    * all jump references in the passed in blocks will be updated to reflect the new CFG's indexing.
    *
    * @param info A `SplitInfo` identifying the CFG, block to split, etc.
    * @return An updated CFG with the desired block split, and a map from old cfg block indices to new ones.
    */
  def splitBlock(info: SplitInfo): (CFG, Map[BlockIndex, BlockIndex]) = {
    // assume that block indices are just literally their indices in the cfg.blocks list
    // therefore adding two blocks after splitting will just shift all blocks after splitting
    // back two.

    // Need to build a map from the old block references of cfg to the new ones returned
    // so that references to the old blocks can be updated easily.
    val (updatedBlocks, newIndices) = updateJumpsToBlocksAfter(info.blockToSplit)

    val newBlocks = updatedBlocks.zipWithIndex.flatMap {
      case (theBlock, index) if index == info.blockToSplit =>
        val instrsBeforeSplit = theBlock.instr.take(info.splitAfterInstrIdx + 1)
        val instrsAfterSplit = theBlock.instr.takeRight(theBlock.instr.size - info.splitAfterInstrIdx - 1)
        val before = Block(instrsBeforeSplit :+ info.jump(newIndices(info.jumpDestinatinBlockIdx)),
          info.beforeSplitExceptionHandlers)
        val after = Block(instrsAfterSplit, info.afterSplitExceptionHandlers)
        List(before, after)

      case (b, _) => List(b)
    }

    (CFG(newBlocks), newIndices)
  }

  /**
    * Like `splitBlock`, but instead of splitting a block just insert a new block after an existing block.
    * Also updates jump references in the enture CFG so a new CFG and mapping of index changes are returned.
    *
    * Note: Assumes that jumps inside `block` reference block indices in the *current* CFG (i.e. they will be
    * updated).
    * @param after The block after which to insert `block`.
    * @param block The new block to insert.
    * @return An updated CFG and map of block index changes.
    */
  // Like splitBlock, but instead of splitting a block just insert a new one
  // Still updates the jump references and so a new CFG and mapping of old to new
  // references are returned.
  def insertBlock(after: BlockIndex, block: Block): (CFG, Map[BlockIndex, BlockIndex]) = {
    val (updatedBlocks, newIndices) = updateJumpsToBlocksAfter(after)
    def blockIndexChange(index: BlockIndex) = if (index > after) index + 1 else index
    val updateJumps = updateJumpIndices(blockIndexChange, _: Instruction)
    val updatedBlock = Block(block.instr.map(updateJumps), Seq.empty)
    val newBlocks = updatedBlocks.slice(0, after + 1) ++
      List(updatedBlock) ++
      updatedBlocks.slice(after + 1, updatedBlocks.size)

    val indexUpdate = List.range(0, updatedBlocks.size).map(index => index -> blockIndexChange(index)).toMap

    (CFG(newBlocks), indexUpdate)
  }

  private def updateJumpsToBlocksAfter(thisBlock: BlockIndex) = {
    // maps old indices to what the new indices will be
    val newIndex = this.blocks.indices.zipWithIndex.map({
      case (old, aboveSplit) if aboveSplit > thisBlock => old -> (aboveSplit + 1)
      case (old, belowOrOnSplit) => old -> belowOrOnSplit
    }).toMap

    // map over the blocks and change all the jump insns to refer to the new indices
    val blocksWithNewJumps =
      this.blocks.map(b => Block(b.instr.map(updateJumpIndices(newIndex, _: Instruction)), b.exceptionHandlers))
    (blocksWithNewJumps, newIndex)
  }

  private def updateJumpIndices(updater: BlockIndex => BlockIndex, insn: Instruction): Instruction = insn match {
   case jump: JumpInstruction =>
        val jumpSucc = jump.getSuccessor()
        val oldJumpDest = jumpSucc._1.getOrElse(jumpSucc._2.get)
        val newJumpDest = updater(oldJumpDest)
        jump.copy(newJumpDest)

      case other => other
  }

  /**
    * Merge the two given maps from old indices to new indices (as those returned by `insertBlock`).
    * I.e. this method connects the index maps from two sequential `insertBlock` invocations.
    * @param oldMap The index map from the first `insertBlock` invocation.
    * @param newMap The index map from the second invocation.
    * @return A map from the domain of `oldMap` to the range of `newMap`.
    */
  def mergeIndexMaps(oldMap: Map[BlockIndex, BlockIndex],
                     newMap: Map[BlockIndex, BlockIndex]): Map[BlockIndex, BlockIndex] =
    oldMap.map(idxChg => idxChg._1 -> newMap(idxChg._2))
}

/**
  * A class containing information for `CFG.splitBlock`.
  * @param blockToSplit The index of the block to split.
  * @param splitAfterInstrIdx The index of the instruction after which to split the block.
  * @param jump The jump instruction to insert.
  * @param jumpDestinatinBlockIdx The destination of the jump to insert.
  * @param beforeSplitExceptionHandlers The VBCHandlers to assign to the first half of the split block.
  * @param afterSplitExceptionHandlers The VBCHandlers to assign to the second half of the split block.
  */
case class SplitInfo(blockToSplit: Int,
                     splitAfterInstrIdx: Int,
                     jump: Int => JumpInstruction,
                     jumpDestinatinBlockIdx: Int,
                     beforeSplitExceptionHandlers: Seq[VBCHandler] = Seq.empty,
                     afterSplitExceptionHandlers: Seq[VBCHandler] = Seq.empty)

// All of the Vs that get introduced are inserted to store intermediate values
// used between VBlocks which in a normal program would just be stored in the stack
//  but with variational execution it is not guaranteed that the next block will be executed
//  immediately

case class VBCHandler(
                     exceptionType: String,
                     handlerBlockIdx: Int,
                     visibleTypeAnnotations: List[TypeAnnotationNode] = Nil,
                     invisibleTypeAnnotations: List[TypeAnnotationNode] = Nil
                     )