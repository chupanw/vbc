package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.analysis.VBCFrame
import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.loader.{BasicBlock, MethodAnalyzer}
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm.tree._
import org.objectweb.asm._

import PartialFunction.cond

class IterationTransformer {
  import edu.cmu.cs.vbc.utils.LiftUtils.{fexprclasstype, fexprclassname, vclassname, vclasstype}
  val fePairClassName = "model/java/util/FEPair"
  val fePairClassType = s"L$fePairClassName;"
  val ctxListClassName = "model/java/util/CtxList"
  val ctxListClassType = s"L$ctxListClassName;"
  val objectClassType = "Ljava/lang/Object;"


  def transformListIteration(cfg: CFG, env: VMethodEnv, cw: ClassVisitor): (CFG, VMethodEnv) = {
    val loops = env.loopAnalysis.loops.filter(isListIteration(_, env))
    val loopBodyBlocks = loops.flatMap(_.body)
    // the last body block(s) of loops are technically predecessors of the loop, but we are not interested in them
    val loopPredecessors = loops.flatMap(l => env.getPredecessors(l.entry)) diff loopBodyBlocks

    // todo: modify newBlocks and add newVars & newInsns here
    var blockTransformations = cfg.blocks map {
      // todo: re-enable simplify invocation
//      case entryPred if loopPredecessors contains entryPred =>
//        transformEntryPred(entryPred, env, cw)

      case bodyBlock if loopBodyBlocks contains bodyBlock =>
        transformBodyBlock(bodyBlock, env, loops.find(_.body.contains(bodyBlock)).get)

      case block => BlockTransformation(block, List(), List())
    }

    val collectTransformations =
      blockTransformations.foldRight((List.empty[Block], List.empty[Int], List.empty[Variable])) _
    val (newBlocks, newInsns, newVars) = collectTransformations((bt, collected) =>
      (bt.newBlock :: collected._1,
        bt.newInsnIndeces ++ collected._2,
        bt.newVars ++ collected._3))


    val newCFG: CFG = CFG(newBlocks)
    val newMN = VBCMethodNode(env.method.access, env.method.name, env.method.desc, env.method.signature,
      env.method.exceptions, newCFG, env.method.localVar ++ newVars)
    val newEnv = new VMethodEnv(env.clazz, newMN)

    newInsns.foreach(newEnv.instructionTags(_) |= env.TAG_PRESERVE)

    (newCFG, newEnv)
  }


  def transformEntryPred(entryPred: Block, env: VMethodEnv, cw: ClassVisitor): BlockTransformation = {
    val lambdaName = "lambda$INVOKEVIRTUAL$simplifyCtxList"
    val lambdaDesc = s"($ctxListClassType)V"
    createSimplifyLambda(cw, lambdaName, lambdaDesc)

    // Add an invocation to ctxlist.simplify before the iterator invocation
    var newInsns = List.empty[Int]
    BlockTransformation(
      Block(entryPred.instr flatMap {
        case itInvoke: InstrINVOKEVIRTUAL if isIteratorInvocation(itInvoke) =>
          val simplifyInsns = invokeSimplify(env.clazz.name, lambdaName, lambdaDesc)

          val iteratorIndex = env.getInsnIdx(itInvoke)
          // Range from index because new instructions come before simplify
          newInsns ++= List.range(iteratorIndex, iteratorIndex + simplifyInsns.size)

          simplifyInsns :+ itInvoke

        case block => List(block)
      }, entryPred.exceptionHandlers),
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
    // todo: this needs to map over the V wrapping the CtxList
    List(
      InstrDUP(),
      // Could assume the V is a One. It should be, but I'm not certain.
      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("getOne"), MethodDesc("()Ljava/lang/Object;"), true),
      InstrCHECKCAST(Owner(ctxListClassName)),
      InstrINVOKEVIRTUAL(Owner(ctxListClassName), MethodName("simplify____V"), MethodDesc("()V"), true) // todo: changed this, haven't tested
//
//      InstrINVOKEDYNAMIC(Owner(consumerName), MethodName("accept"), MethodDesc(s"()$consumerType"),
//        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
//          "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
//        Type.getType("(Ljava/lang/Object;)V"),
//        new Handle(Opcodes.H_INVOKESTATIC, className, lambdaName, lambdaDesc),
//        Type.getType(lambdaDesc)),
//      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("foreach"), MethodDesc(s"($consumerType)V"), true)
    )
  }

  def transformBodyBlock(bodyBlock: Block, env: VMethodEnv, loop: Loop): BlockTransformation = {
    // Unpack FEPair iterator after the iterator.next invocation, and add condition testing context of FEPair
    var newInsns = List.empty[Int]
    BlockTransformation(
      Block(bodyBlock.instr flatMap {
        case nextInvocation: InstrINVOKEINTERFACE if
        nextInvocation.name.name == "next" && nextInvocation.owner.contains("Iterator") =>
          val unpackInsns = List(InstrUNPACK_FEPAIR(loop))

          val nextInvIndex = env.getInsnIdx(nextInvocation)
          val condInsnIndex = unpackInsns.indexWhere(_.isJumpInstr)
          // Range from index + 1 because new instructions come after nextInvocation
          // Do not mark conditional as a "new instruction" so that it will be lifted (thereby narrowing context)
          newInsns ++= List.range(nextInvIndex + 1, nextInvIndex + 1 + condInsnIndex)
          newInsns ++= List.range(nextInvIndex + 1 + condInsnIndex + 1, nextInvIndex + 1 + unpackInsns.size)

          nextInvocation :: unpackInsns

        case otherInsn => List(otherInsn)
      }, bodyBlock.exceptionHandlers),
      newInsns,
      List())
  }



  def isListIteration(loop: Loop, env: VMethodEnv): Boolean =
    env.getPredecessors(loop.entry).exists(_.instr.exists(isIteratorInvocation))

  def isIteratorInvocation(insn: Instruction): Boolean = cond(insn) {
    case inv: InstrINVOKEVIRTUAL => isIteratorInvocation(inv)
  }
  def isIteratorInvocation(insn: InstrINVOKEVIRTUAL): Boolean =
    insn.name.name.equals("iterator") && insn.owner.name.contains("List")





  def transformListIterationLoops(node: ClassNode): Unit = {
    import scala.collection.JavaConversions._ // for map over node.methods

    def createMethodAnalyzer(mn: MethodNode) = {
      val ma  = new MethodAnalyzer(node.name, mn)
      ma.analyze()
      ma
    }

    for { ma                     <- node.methods.map(createMethodAnalyzer).filter(_.loops.nonEmpty)
          loop                   <- ma.loops.filter(isListIteration(_, ma))
          block                  <- firstBodyBlock(loop)
          loopCtxVar             <- findCtxVar(block)

          iteratorInvocation     <- findIteratorInvocation(loop)

          itNextInvocation       <- loadUtil.findSome(loop.body, findIteratorNextInvocation)

          loopCtxStoreBeforeLoop <- findStoreBefore(loop, loopCtxVar, ma) }
      yield {
        addSimplifyInvocationBefore(iteratorInvocation, node, ma)

        saveMethodCtxBefore(loopCtxStoreBeforeLoop, ma)

        unpackFEPair(itNextInvocation, loopCtxVar, ma)

        for { methodCtxVarAfterLoop  <- findFalseFEBefore(loop, ma).map(_.`var`)
              methodCtxLoadAfterLoop <- findLoadAfter(loop, methodCtxVarAfterLoop, ma) }
          yield {
            restoreMethodCtxBefore(methodCtxLoadAfterLoop, ma)
          }
      }
  }


  def firstBodyBlock(loop: edu.cmu.cs.vbc.loader.Loop): Option[BasicBlock] = {
    // Sort body blocks so that find acts deterministically
    // Given 2 or more entry successors to choose from, select the latest one by sorting in descending order
    loop.body.toList.sortWith(_.startLine > _.startLine).find(block => loop.entry.successors contains block.startLine)
  }

  def findCtxLoadInsn(mInsn: MethodInsnNode): Option[VarInsnNode] = {
    // Get the ctx loading insn that precedes the given method invocation.
    // Where to find that depends on the method being invoked:
    val varInsn = (mInsn.owner, mInsn.name) match {
      case ("edu/cmu/cs/varex/VOps", "IADD") |
           ("edu/cmu/cs/varex/VOps", "IINC") |
           ("edu/cmu/cs/varex/VOps", "ISUB") |
           ("edu/cmu/cs/varex/VOps", "IMUL") |
           ("de/fosd/typechef/featureexpr/FeatureExpr", "isContradiction") => Some(mInsn.getPrevious)
      case ("edu/cmu/cs/varex/V", "one") => Some(mInsn.getPrevious.getPrevious)
      case ("edu/cmu/cs/varex/V", "choice") => Some(mInsn.getPrevious.getPrevious.getPrevious)
      case _ => None
    }
    val varInsnNode = varInsn.flatMap(loadUtil.narrow[VarInsnNode])
    varInsnNode
  }

  def findCtxVar(block: BasicBlock): Option[Int] = {
    val ctxLoadingInsns = Set(Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE)
    loadUtil.findSome(block.instructions, (insn: AbstractInsnNode) =>
      if (ctxLoadingInsns.contains(insn.getOpcode) && cond(insn) {
        case m: MethodInsnNode => m.owner.contains("edu/cmu/cs/varex/V") || m.name.contains("isContradiction")
      }) {
        for { mInsn <- loadUtil.narrow[MethodInsnNode](insn)
              ctxLoadInsn <- findCtxLoadInsn(mInsn) }
          yield ctxLoadInsn.`var`
      }
      else
        None)
  }

  def findIteratorNextInvocation(block: BasicBlock): Option[MethodInsnNode] = {
    loadUtil.findSome(block.instructions, (insn: AbstractInsnNode) => {
      insn match {
        case invokeDynamicNode: InvokeDynamicInsnNode =>
          invokeDynamicNode.bsmArgs(1) match {
            case h: Handle if h.getName contains "INVOKEINTERFACE$next" => Some(invokeDynamicNode)
            case _ => None
          }
        case _ => None
      }
    }).flatMap(idInsn =>
      idInsn.getNext.getNext match {
      case ii: MethodInsnNode if ii.name contains "sflatMap" => Some(ii)
      case _ => None
    })
  }

  def unpackFEPair(after: AbstractInsnNode, at: Int, ma: MethodAnalyzer): Unit = {
    import edu.cmu.cs.vbc.utils.LiftUtils.{fexprclasstype, vclassname, vclasstype}
    val methodCtxVar = mtdCtxVars(ma.mNode.name)
    ma.insertInsns(after, List(
      // stack =
      // ..., One(FEPair)
      new MethodInsnNode(Opcodes.INVOKEINTERFACE, vclassname, "getOne", "()Ljava/lang/Object;", true),
      new TypeInsnNode(Opcodes.CHECKCAST, "model/java/util/FEPair"),
      // ..., FEPair
      new InsnNode(Opcodes.DUP),
      // ..., FEPair, FEPair
      new FieldInsnNode(Opcodes.GETFIELD, "model/java/util/FEPair", "v", "Ljava/lang/Object;"),
      // ..., FEPair, value
      new InsnNode(Opcodes.SWAP),
      // ..., value, FEPair
      new FieldInsnNode(Opcodes.GETFIELD, "model/java/util/FEPair", "ctx", fexprclasstype),
      // ..., value, FE
      new InsnNode(Opcodes.DUP),
      // ..., value, FE, FE
      new VarInsnNode(Opcodes.ALOAD, methodCtxVar),
      // ..., value, FE, FE, mCtx
      new MethodInsnNode(Opcodes.INVOKEINTERFACE, "de/fosd/typechef/featureexpr/FeatureExpr", "and",
        "(Lde/fosd/typechef/featureexpr/FeatureExpr;)Lde/fosd/typechef/featureexpr/FeatureExpr;", true),
      // ..., value, FE, FE^mCtx
      new VarInsnNode(Opcodes.ASTORE, at),

      // ..., value, FE
      new InsnNode(Opcodes.SWAP),
      // ..., FE, value
      new MethodInsnNode(Opcodes.INVOKESTATIC, vclassname, "one",
        s"(${fexprclasstype}Ljava/lang/Object;)$vclasstype", true)
      // ..., One(value)
    ))
  }

  def isInvokeDynamicWith(insn: AbstractInsnNode, nameSubstr: String, descSubstr: String): Boolean = cond(insn) {
    case idInsn: InvokeDynamicInsnNode =>
      cond(idInsn.bsmArgs(1)) {
        case h: Handle => h.getName.contains(nameSubstr) && h.getDesc.contains(descSubstr)
      }
  }
  def isIteratorInvocation(insn: AbstractInsnNode): Boolean =
    isInvokeDynamicWith(insn, "INVOKEVIRTUAL$iterator", "CtxList")

  def isHasNextInvocation(insn: AbstractInsnNode): Boolean =
    isInvokeDynamicWith(insn, "INVOKEINTERFACE$hasNext", "CtxIterator")


  def isListIteration(loop: edu.cmu.cs.vbc.loader.Loop, ma: MethodAnalyzer): Boolean =
    loop.entry.predecessors.map(ma.blockEnds).exists(_.instructions.exists(isIteratorInvocation))

  def findIteratorInvocation(loop: edu.cmu.cs.vbc.loader.Loop): Option[AbstractInsnNode] = {
    var thisInsn = loop.entry.instructions.head.getPrevious
    var prevInsn = thisInsn.getPrevious
    while (thisInsn != prevInsn) {
      if (isIteratorInvocation(thisInsn))
        return Some(thisInsn)

      thisInsn = prevInsn
      prevInsn = prevInsn.getPrevious
    }
    None
  }

  var createdSimplifyLambda: Boolean = false
  def createSimplifyLambda(clazz: ClassNode, ctxListName: String, lambdaName: String, lambdaDesc: String): Unit = {
    if (!createdSimplifyLambda) {
      val lambda = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
        lambdaName, lambdaDesc, lambdaDesc, Array[String]())
      lambda.visitCode()
      lambda.visitVarInsn(Opcodes.ALOAD, 0)
      lambda.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ctxListName, "simplify____V", "()V", false)
      lambda.visitInsn(Opcodes.RETURN)
      lambda.visitMaxs(2, 1)
      lambda.visitEnd()

      clazz.methods.add(lambda)

      createdSimplifyLambda = true
    }
  }
  def addSimplifyInvocationBefore(insn: AbstractInsnNode, clazz: ClassNode, ma: MethodAnalyzer): Unit = {
    val ctxListName = "model/java/util/CtxList"
    val lambdaName = "lambda$INVOKEVIRTUAL$CtxListsimplify"
    val lambdaDesc = s"(L$ctxListName;)V"

    createSimplifyLambda(clazz, ctxListName, lambdaName, lambdaDesc)

    ma.insertInsns(insn.getPrevious, List(
      // stack =
      // ..., V<CtxList>
      new InsnNode(Opcodes.DUP),
      // ..., V<CtxList>, V<CtxList>
      new InvokeDynamicInsnNode("accept", "()Ljava/util/function/Consumer;",
        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
          "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
        Type.getType("(Ljava/lang/Object;)V"),
        new Handle(Opcodes.H_INVOKESTATIC, clazz.name, lambdaName, lambdaDesc),
        Type.getType(lambdaDesc)),
      // ..., V<CtxList>, V<CtxList>, lambda
      new MethodInsnNode(Opcodes.INVOKEINTERFACE, "edu/cmu/cs/varex/V", "foreach", "(Ljava/util/function/Consumer;)V",
        true)
      // ..., V<CtxList>
    ))
  }

  def findStoreBefore(loop: edu.cmu.cs.vbc.loader.Loop, varIndex: Int, ma: MethodAnalyzer): Option[VarInsnNode] = {
    /*
    Turns out the only store into the loop ctx var "before" the loop is in the first body block of the loop
    -- that is where the context propogation of the vBlocks is done
     */
    val firstBodyBlock = loop.entry.successors.map(ma.blockStarts) find (_.instructions.exists(isHasNextInvocation))
    firstBodyBlock.flatMap(block => loadUtil.findSome(block.instructions.reverse, (insn: AbstractInsnNode) =>  insn match {
      case v: VarInsnNode if v.`var` == varIndex => Some(v)
      case _ => None
    }))
  }
  def findLoadAfter(loop: edu.cmu.cs.vbc.loader.Loop, varIndex: Int, ma: MethodAnalyzer): Option[VarInsnNode] = {
    // Find the first store to VARINDEX after LOOP
    val blocksAfterLoop = ma.reachableFrom(loop.entry, _.successors.map(ma.blockStarts)) diff loop.body
    val blocksSorted = blocksAfterLoop.toList.sortWith(_.startLine < _.startLine)

    def findStoreIn(block: BasicBlock) = loadUtil.findSome(block.instructions, (insn: AbstractInsnNode) => insn match {
      case vInsn: VarInsnNode if vInsn.`var` == varIndex => Some(vInsn)
      case _ => None
    })

    loadUtil.findSome(blocksSorted, (block: BasicBlock) => findStoreIn(block))
  }


  def findLabel(iterable: Iterable[AbstractInsnNode]): Option[LabelNode] = {
    loadUtil.findSome(iterable, (insn: AbstractInsnNode) => insn match {
      case l: LabelNode => Some(l)
      case _ => None
    })
  }
  def firstLabel(insnList: InsnList): Option[LabelNode] = findLabel(insnList.toArray)
  def lastLabel(insnList: InsnList): Option[LabelNode] = findLabel(insnList.toArray.reverse)

  var mtdCtxVars: Map[String, Int] = Map()
  def createMethodCtxVar(ma: MethodAnalyzer): Unit = {
    for { firstL <- firstLabel(ma.mNode.instructions)
          lastL  <- lastLabel(ma.mNode.instructions) }
        yield {
          val mtdCtxVar = ma.mNode.maxLocals
          mtdCtxVars += (ma.mNode.name -> mtdCtxVar)
          ma.mNode.maxLocals += 1
          ma.mNode.localVariables.add(new LocalVariableNode("pre$loop$mtd$ctx",
            "Lde/fosd/typechef/featureexpr/FeatureExpr;", "Lde/fosd/typechef/featureexpr/FeatureExpr;",
            firstL, lastL, mtdCtxVar))

          // initialize var with False at method start
          ma.insertInsns(firstL, List(
            new MethodInsnNode(Opcodes.INVOKESTATIC, "de/fosd/typechef/featureexpr/FeatureExprFactory", "False",
              "()Lde/fosd/typechef/featureexpr/FeatureExpr;", true),
            new VarInsnNode(Opcodes.ASTORE, mtdCtxVar)))
        }
  }
  def saveMethodCtxBefore(ctxStoreInsn: VarInsnNode, ma: MethodAnalyzer): Unit = {
    if ((mtdCtxVars get ma.mNode.name).isEmpty) createMethodCtxVar(ma)

    for { varIndex <- mtdCtxVars get ma.mNode.name }
      yield {
        val label = new LabelNode()
        ma.insertInsns(ctxStoreInsn.getPrevious,
          List(
            // stack = ..., method ctx
            new VarInsnNode(Opcodes.ALOAD, varIndex),
            // ..., method ctx, methodCtxVar
            new MethodInsnNode(Opcodes.INVOKEINTERFACE, "de/fosd/typechef/featureexpr/FeatureExpr", "isContradiction",
              "()Z", true),
            // ..., method ctx, Z
            new JumpInsnNode(Opcodes.IFEQ, label), // if methodCtxVar is not False, don't store into it again

            // ..., method ctx
            new InsnNode(Opcodes.DUP), // otherwise, store the method ctx into methodCtx
            // ..., methodCtx, methodCtx
            new VarInsnNode(Opcodes.ASTORE, varIndex),

            // ..., method ctx
            label
        ))
    }
  }
  def restoreMethodCtxBefore(insn: VarInsnNode, ma: MethodAnalyzer): Unit = {
    // restore mtdCtxVar into ctxVar at start of block
    ma.mNode.maxStack += 1
    val blockCtxVar = insn.`var`
    for { methodCtxVar <- mtdCtxVars get ma.mNode.name }
      yield ma.insertInsns(insn.getPrevious,
        List(
          new VarInsnNode(Opcodes.ALOAD, methodCtxVar),
          new VarInsnNode(Opcodes.ASTORE, blockCtxVar)
        ))
  }

  def findFalseFEBefore(loop: edu.cmu.cs.vbc.loader.Loop, ma: MethodAnalyzer): Option[VarInsnNode] = {
    def isFEFalse(m: MethodInsnNode) = m.owner == "de/fosd/typechef/featureexpr/FeatureExprFactory" && m.name == "False"
    val firstBodyBlock = loop.entry.successors.map(ma.blockStarts) find (_.instructions.exists(isHasNextInvocation))
    firstBodyBlock.flatMap(block => loadUtil.findSome(block.instructions.reverse, (insn: AbstractInsnNode) =>  insn match {
      case m: MethodInsnNode if isFEFalse(m) => loadUtil.narrow[VarInsnNode](m.getPrevious)
      case _ => None
    }))
  }
}

case class BlockTransformation(newBlock: Block, newInsnIndeces: List[Int], newVars: List[Variable])

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

case class InstrUNPACK_FEPAIR(loop: Loop) extends Instruction {
  import edu.cmu.cs.vbc.utils.LiftUtils.{fexprclasstype, fexprclassname, vclassname, vclasstype}
  val fePairClassName = "model/java/util/FEPair"
  val fePairClassType = s"L$fePairClassName;"
  val ctxListClassName = "model/java/util/CtxList"
  val ctxListClassType = s"L$ctxListClassName;"
  val objectClassType = "Ljava/lang/Object;"

  def toByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    val thisBlock = env.getBlockForInstruction(this)
    for {
      thisLoop <- env.loopAnalysis.loops.find(_.body.contains(thisBlock))
      loopCtxVarIdx = env.getVarIdx(env.getVBlockVar(thisLoop.entry))
      entry = thisLoop.entry
    } yield {
      // stack: ..., One(FEPair)
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Owner(vclassname), MethodName("getOne"),
        MethodDesc("()Ljava/lang/Object;"), true)
      // ..., FEPair
      mv.visitTypeInsn(Opcodes.CHECKCAST, Owner(fePairClassName))
      mv.visitInsn(Opcodes.DUP)
      // ..., FEPair, FEPair
      mv.visitFieldInsn(Opcodes.GETFIELD, Owner(fePairClassName), FieldName("v"), TypeDesc(objectClassType))
      // ..., FEPair, v
      mv.visitInsn(Opcodes.SWAP)
      // ..., v, FEPair
      mv.visitFieldInsn(Opcodes.GETFIELD, Owner(fePairClassName), FieldName("ctx"), TypeDesc(fexprclasstype))
      // ..., v, ctx
      mv.visitInsn(Opcodes.DUP)
      // ..., v, ctx, ctx
      mv.visitVarInsn(Opcodes.ALOAD, loopCtxVarIdx)
      // ..., v, ctx, ctx, loopCtx
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Owner(fexprclassname), MethodName("and"),
        MethodDesc(s"($fexprclasstype)$fexprclasstype"), true)
      // ..., v, ctx, FE
      mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Owner(fexprclassname), MethodName("isSatisfiable"), MethodDesc("()Z"),
        true)
      // ..., v, ctx, isSat?
      mv.visitJumpInsn(Opcodes.IFEQ, env.getBlockLabel(entry)) // todo: this won't work because we leave stuff on top of the stack
      // ..., v, ctx
      mv.visitInsn(Opcodes.SWAP)
      // ..., ctx, v
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, Owner(vclassname), MethodName("one"),
        MethodDesc(s"($fexprclasstype$objectClassType)$vclasstype"), true)
      // ..., One(v)
    }
  }
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    assert(false, "invalid override of toByteCode called on fake instruction UNPACK_FEPAIR." +
      "This instruction should be passed a VMethodEnv rather than a MethodEnv.")
  }
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    assert(false, "toVByteCode called on fake instruction UNPACK_FEPAIR. This instruction should not be lifted.")
  }
  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set())
}