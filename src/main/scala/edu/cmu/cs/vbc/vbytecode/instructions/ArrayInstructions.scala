package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis._
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.utils.{InvokeDynamicUtils, VCall}
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm._

/**
  * Our first attempt is to implement array as array of V. If array length is different, then arrayref itself
  * is also a V.
  */
trait ArrayCreationInstructions extends Instruction {
  def createVArray(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    InvokeDynamicUtils.invoke(VCall.smap, mv, env, loadCurrentCtx(_, env, block), "anewarray", s"$IntType()[$vclasstype") {
      (visitor: MethodVisitor) => {
        visitor.visitVarInsn(ALOAD, 1)
        visitor.visitVarInsn(ALOAD, 0)
        visitor.visitMethodInsn(
          INVOKESTATIC,
          Owner.getArrayOps,
          MethodName("initArray"),
          MethodDesc(s"(${TypeDesc.getInt}${fexprclasstype})[$vclasstype"),
          false
        )
        visitor.visitInsn(ARETURN)
      }
    }
  }

  def createPrimitiveVArray(mv: MethodVisitor, env: VMethodEnv, block: Block, pType: PrimitiveType.Value): Unit = {
    InvokeDynamicUtils.invoke(VCall.smap, mv, env, loadCurrentCtx(_, env, block), s"newarray$pType", s"$IntType()[$vclasstype") {
      (visitor: MethodVisitor) => {
        visitor.visitVarInsn(ALOAD, 1) // int
        visitor.visitVarInsn(ALOAD, 0) // FE
        visitor.visitMethodInsn(
          INVOKESTATIC,
          Owner.getArrayOps,
          MethodName(s"init${pType}Array"),
          MethodDesc(s"(${TypeDesc.getInt}${fexprclasstype})[$vclasstype"),
          false
        )
        visitor.visitInsn(ARETURN)
      }
    }
  }
}

trait ArrayStoreInstructions extends Instruction {
  /**
    * Common assumption on the operand stack:
    *
    * ..., arrayref, index, value -> ...,
    */
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (vType, vPrev, frame1) = s.pop()
    val (idxType, idxPrev, frame2) = frame1.pop()
    val (refType, refPrev, frame3) = frame2.pop()
    // values must be V
    if (!vType.isInstanceOf[V_TYPE]) return (frame3, vPrev)
    if (refType == V_TYPE(false)) {
      env.setLift(this)
      if (idxType != V_TYPE(false)) return (frame3, idxPrev)
    } else {
      if (idxType == V_TYPE(false)) env.setTag(this, env.TAG_HAS_VARG)
    }
    (frame3, Set())
  }

  override def doBacktrack(env: VMethodEnv): Unit = {
    // do nothing, lifting or not depends on array ref type
  }

  def storeOperation(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    InvokeDynamicUtils.invoke(VCall.sforeach, mv, env, loadCurrentCtx(_, env, block), "aastore", s"[$vclasstype($IntType$vclasstype)V", nExplodeArgs = 1) {
      (visitor: MethodVisitor) => {
        visitor.visitVarInsn(ALOAD, 1) //array ref
        visitor.visitVarInsn(ALOAD, 3) //index
        visitor.visitMethodInsn(INVOKEVIRTUAL, IntClass, "intValue", "()I", false)
        visitor.visitVarInsn(ALOAD, 0) //new value
        visitor.visitVarInsn(ALOAD, 2) // FE
        visitor.visitMethodInsn(
          INVOKESTATIC,
          Owner.getArrayOps,
          MethodName("aastore"),
          MethodDesc(s"([${vclasstype}I${vclasstype}${fexprclasstype})V"),
          false
        )
        visitor.visitInsn(RETURN)
      }
    }
  }

  def storeVArray(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    loadCurrentCtx(mv, env, block)
    if (env.getTag(this, env.TAG_HAS_VARG)) {
      // index is a V
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getArrayOps,
        MethodName("aastore"),
        MethodDesc(s"([${vclasstype}${vclasstype}${vclasstype}${fexprclasstype})V"),
        false
      )
    } else {
      // index is a primitive int
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getArrayOps,
        MethodName("aastore"),
        MethodDesc(s"([${vclasstype}I${vclasstype}${fexprclasstype})V"),
        false
      )
    }
  }

  /** Store value of the following type to array: byte, boolean, char, short, int
    *
    * JVM assumes the new value to be of type int. We need some truncating before storing byte,
    * boolean, char and short.
    */
  def storeBCSI(mv: MethodVisitor, env: VMethodEnv, block: Block, pType: PrimitiveType.Value): Unit = {
    InvokeDynamicUtils.invoke(
      VCall.sforeach,
      mv,
      env,
      loadCurrentCtx(_, env, block),
      s"${pType}astore",
      s"[$vclasstype(${TypeDesc.getInt}${TypeDesc.getInt})V",
      nExplodeArgs = 2
    ) {
      (visitor: MethodVisitor) => {
        visitor.visitVarInsn(ALOAD, 0) //array ref
        visitor.visitVarInsn(ALOAD, 1) //index
        Integer2int(visitor)

        // load original value in the array and create a choice between new value and old value
        visitor.visitVarInsn(ALOAD, 2) // FE
        visitor.visitVarInsn(ALOAD, 3) // new value
        // Truncate int to char, short, boolean, byte
        pType match {
          case PrimitiveType.char | PrimitiveType.short | PrimitiveType.boolean | PrimitiveType.byte =>
            visitor.visitMethodInsn(
              INVOKESTATIC,
              Owner.getVOps,
              s"trunc$pType",
              MethodDesc(s"(${TypeDesc.getInt})${TypeDesc.getInt}"),
              false
            )
          case _ => // do nothing
        }
        callVCreateOne(visitor, (m) => m.visitVarInsn(ALOAD, 2))
        visitor.visitVarInsn(ALOAD, 0)
        visitor.visitVarInsn(ALOAD, 1)
        Integer2int(visitor)
        visitor.visitInsn(AALOAD)
        callVCreateChoice(visitor)

        visitor.visitInsn(AASTORE)
        visitor.visitInsn(RETURN)
      }
    }
  }

}


trait ArrayLoadInstructions extends Instruction {
  /**
    * Common assumption on the operand stack:
    *
    * ..., arrayref, index -> ..., value
    */
  def updateStackWithReturnType(s: VBCFrame, env: VMethodEnv, is64Bit: Boolean): (VBCFrame, Set[Instruction]) = {
    val (idxType, idxPrev, frame1) = s.pop()
    val (refType, refPrev, frame2) = frame1.pop()
    if (refType == V_TYPE(false)) {
      env.setLift(this)
      if (idxType != V_TYPE(false)) return (frame2, idxPrev)
    } else {
      if (idxType == V_TYPE(false))
        env.setTag(this, env.TAG_HAS_VARG)
    }
    (frame2.push(V_TYPE(is64Bit), Set(this)), Set())
  }

  override def doBacktrack(env: VMethodEnv): Unit = {
    // do nothing, lifting or not depends on ref type
  }

  def loadOperation(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    InvokeDynamicUtils.invoke(VCall.sflatMap, mv, env, loadCurrentCtx(_, env, block), "aaload", s"[$vclasstype($IntType)$vclasstype", nExplodeArgs = 1) {
      (visitor: MethodVisitor) => {
        visitor.visitVarInsn(ALOAD, 0) // array ref
        visitor.visitVarInsn(ALOAD, 2) // index
        visitor.visitMethodInsn(INVOKEVIRTUAL, IntClass, "intValue", "()I", false)
        visitor.visitInsn(AALOAD)
        visitor.visitInsn(ARETURN)
      }
    }
  }

  def loadVArray(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.getTag(this, env.TAG_HAS_VARG)) {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getArrayOps,
        MethodName("aaload"),
        MethodDesc(s"([$vclasstype$vclasstype$fexprclasstype)$vclasstype"),
        false
      )
    }
    else
      mv.visitInsn(AALOAD)
  }
}

object PrimitiveType extends Enumeration {
  val boolean = Value("Z")
  val char = Value("C")
  val byte = Value("B")
  val short = Value("S")
  val int = Value("I")
  val float = Value("F")
  val long = Value("J")
  val double = Value("D")

  def toEnum(atype: Int): PrimitiveType.Value = (atype: @unchecked) match {
    case T_BOOLEAN => boolean
    case T_CHAR => char
    case T_BYTE => byte
    case T_SHORT => short
    case T_INT => int
    case T_FLOAT => float
    case T_LONG => long
    case T_DOUBLE => double
  }
}

/**
  * NEWARRAY is for primitive type. Since we are boxing all primitive types, all NEWARRAY should
  * be replaced by ANEWARRAY
  *
  * @todo We could keep primitive array, but loading and storing value from/to primitive array must be
  *       handled carefully because values outside array are all boxed.
  * @todo By default, primitive array gets initialized after NEWARRAY, but now we are replacing it with ANEWARRAY,
  *       so we might need to initialize also
  */
case class InstrNEWARRAY(atype: Int) extends ArrayCreationInstructions {
  val pType = PrimitiveType.toEnum(atype)

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitIntInsn(NEWARRAY, atype)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      createPrimitiveVArray(mv, env, block, pType)
    }
    else {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(
        INVOKESTATIC,
        Owner.getArrayOps,
        MethodName(s"init${pType}Array"),
        MethodDesc(s"(I${fexprclasstype})[$vclasstype"),
        false
      )
      if (env.getTag(this, env.TAG_NEED_V)) {
        callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      }
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, f) = s.pop()
    if (env.getTag(this, env.TAG_NEED_V)) {
      // this means the array itself needs to be wrapped into a V
      (f.push(V_TYPE(false), Set(this)), Set())
    }
    else {
      if (v == V_TYPE(false)) {
        // array length is a V, needs invokedynamic to create a V array ref
        env.setLift(this)
        (f.push(V_TYPE(false), Set(this)), Set())
      }
      else {
        (f.push(REF_TYPE(), Set(this)), Set())
      }
    }
  }

  override def doBacktrack(env: VMethodEnv): Unit = env.setTag(this, env.TAG_NEED_V)
}

/**
  * Create new array of reference
  *
  * Operand Stack: ..., count -> ..., arrayref
  *
  * @param owner
  */
case class InstrANEWARRAY(owner: Owner) extends ArrayCreationInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitTypeInsn(ANEWARRAY, owner.toModel)

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v, prev, f) = s.pop()
    if (env.getTag(this, env.TAG_NEED_V)) {
      // this means the array itself needs to be wrapped into a V
      (f.push(V_TYPE(false), Set(this)), Set())
    }
    else {
      if (v == V_TYPE(false)) {
        // array length is a V, needs invokedynamic to create a V array ref
        env.setLift(this)
        (f.push(V_TYPE(false), Set(this)), Set())
      }
      else {
        (f.push(REF_TYPE(), Set(this)), Set())
      }
    }
  }

  /**
    * For ANEWARRAY, lifting means invokedynamic on a V array length
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      createVArray(mv, env, block)
    }
    else {
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, MethodName("initArray"), MethodDesc(s"(I$fexprclasstype)[$vclasstype"), false)
      if (env.getTag(this, env.TAG_NEED_V)) {
        callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      }
    }
  }

  override def doBacktrack(env: VMethodEnv): Unit = env.setTag(this, env.TAG_NEED_V)
}

/**
  * Store into reference array
  *
  * Operand Stack: ..., arrayref, index, value -> ...
  */
case class InstrAASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(AASTORE)

  /**
    * For AASTORE, lifting means invokedynamic on a V object
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      storeOperation(mv, env, block)
    }
    else {
      storeVArray(mv, env, block)
    }
  }
}

/**
  * Load reference from array
  *
  * Operand Stack: ..., arrayref, index -> ..., value
  */
case class InstrAALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(AALOAD)

  /**
    * Lifting means invokeDynamic on V of arrayrefs
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadOperation(mv, env, block)
    }
    else {
      loadVArray(mv, env, block)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv) = updateStackWithReturnType(s, env, false)
}

case class InstrIALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(IALOAD)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadOperation(mv, env, block)
    }
    else {
      loadVArray(mv, env, block)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv) = updateStackWithReturnType(s, env, false)
}

case class InstrIASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(IASTORE)

  /**
    * For IASTORE, lifting means invokedynamic on a V object
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      storeOperation(mv, env, block)
    }
    else {
      storeVArray(mv, env, block)
    }
  }
}

/**
  * Store into char array
  *
  * Operand stack: ..., arrayref, index(int), value(int) -> ...
  */
case class InstrCASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(CASTORE)
  }

  /**
    * Lifting means performing operations on a V object
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      storeBCSI(mv, env, block, PrimitiveType.char)
    }
    else {
      storeVArray(mv, env, block)
    }
  }
}

/**
  * Load char from array
  *
  * Operand stack: ..., arrayref, index -> ..., value
  *
  * @todo Treating char array as V array lose the ability to print the char, sys.out could not
  *       tell whether this is a char or simply an integer
  */
case class InstrCALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(CALOAD)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadOperation(mv, env, block)
    }
    else {
      loadVArray(mv, env, block)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv) = updateStackWithReturnType(s, env, false)
}

/** Get length of array
  *
  * ..., arrayref -> ..., length (int)
  */
case class InstrARRAYLENGTH() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(ARRAYLENGTH)
  }

  /** Lifting means invoking on V arrayref */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      InvokeDynamicUtils.invoke(
        VCall.smap,
        mv,
        env,
        loadCtx = loadCurrentCtx(_, env, block),
        lambdaName = "arraylength",
        desc = TypeDesc("[" + vclasstype) + "()" + TypeDesc.getInt
      ) {
        mv: MethodVisitor => {
          mv.visitVarInsn(ALOAD, 1) // arrayref
          mv.visitInsn(ARRAYLENGTH)
          int2Integer(mv)
          mv.visitInsn(ARETURN)
        }
      }
    }
    else
      mv.visitInsn(ARRAYLENGTH)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (vt, _, frame) = s.pop()
    val newFrame =
      if (vt == V_TYPE(false)) {
        env.setLift(this)
        frame.push(V_TYPE(false), Set(this))
      } else {
        frame.push(INT_TYPE(), Set(this))
      }
    (newFrame, Set())
  }
}

/**
  * Load byte OR boolean from array
  *
  * ..., arrayref, index (int) -> ..., value (int)
  */
case class InstrBALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(BALOAD)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadOperation(mv, env, block)
    }
    else {
      mv.visitInsn(AALOAD)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv) = updateStackWithReturnType(s, env, false)
}

case class InstrBASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(BASTORE)
  }

  /**
    * Lifting means arrayref is V
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      storeBCSI(mv, env, block, PrimitiveType.byte)
    }
    else {
      mv.visitInsn(AASTORE)
    }
  }
}

case class InstrSASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(SASTORE)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      storeBCSI(mv, env, block, PrimitiveType.short)
    }
    else {
      mv.visitInsn(AASTORE)
    }
  }
}

case class InstrSALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(SALOAD)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadOperation(mv, env, block)
    }
    else {
      mv.visitInsn(AALOAD)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv) = updateStackWithReturnType(s, env, false)
}

case class InstrDASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DASTORE)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      storeOperation(mv, env, block)
    }
    else {
      ??? // should not happen because currently everything is V
      mv.visitInsn(AASTORE)
    }
  }
}

case class InstrDALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DALOAD)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      loadOperation(mv, env, block)
    else
      mv.visitInsn(AALOAD)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, is64Bit = true)
}

case class InstrLASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LASTORE)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      storeOperation(mv, env, block)
    }
    else {
      ??? // should not happen because currently everything is V
      mv.visitInsn(AASTORE)
    }
  }
}

case class InstrLALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LALOAD)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      loadOperation(mv, env, block)
    }
    else {
      mv.visitInsn(AALOAD)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv) = updateStackWithReturnType(s, env, true)
}


case class InstrFALOAD() extends ArrayLoadInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FALOAD)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      loadOperation(mv, env, block)
    else
      mv.visitInsn(AALOAD)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = updateStackWithReturnType(s, env, is64Bit = false)
}

case class InstrFASTORE() extends ArrayStoreInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitInsn(FASTORE)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      storeOperation(mv, env, block)
    else
      mv.visitInsn(AASTORE)
  }
}

/**
  * Create a multidimensional array
  *
  * ..., count1, [count2, ...] -> ..., arrayref
  */
case class InstrMULTIANEWARRAY(owner: Owner, dims: Int) extends Instruction {
  val returnType: String = "[" * dims + vclasstype
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = mv.visitMultiANewArrayInsn(owner.toModel, dims)

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      val invokeArgs = IntType * (dims - 1)
      InvokeDynamicUtils.invoke(VCall.sflatMap, mv, env, loadCurrentCtx(_, env, block), "multianewarray", s"$IntType($invokeArgs)$returnType", nExplodeArgs = dims - 1) {
        (visitor: MethodVisitor) => {
          // count1, count2, ..., FE, countN
          visitor.visitIntInsn(BIPUSH, dims)
          visitor.visitIntInsn(NEWARRAY, Opcodes.T_INT)
          // first n-1 dimensions
          0 until dims - 1 foreach {i =>
            visitor.visitInsn(DUP)
            visitor.visitIntInsn(BIPUSH, i)
            visitor.visitVarInsn(ALOAD, i)
            visitor.visitMethodInsn(INVOKEVIRTUAL, Owner.getInt, "intValue", MethodDesc("()I"), false)
            visitor.visitInsn(IASTORE)
          }
          // countN
          visitor.visitInsn(DUP)
          visitor.visitIntInsn(BIPUSH, dims - 1)
          visitor.visitVarInsn(ALOAD, dims)
          visitor.visitMethodInsn(INVOKEVIRTUAL, Owner.getInt, "intValue", MethodDesc("()I"), false)
          visitor.visitInsn(IASTORE)

          visitor.visitLdcInsn(owner.toModel.toString)
          visitor.visitVarInsn(ALOAD, dims - 1)
          visitor.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, MethodName("initMultiArray"), MethodDesc(s"([ILjava/lang/String;$fexprclasstype)[$vclasstype"), false)
          callVCreateOne(visitor, m => m.visitVarInsn(ALOAD, dims -1))
          visitor.visitInsn(ARETURN)
        }
      }
    }
    else {
      mv.visitIntInsn(BIPUSH, dims)
      mv.visitIntInsn(NEWARRAY, Opcodes.T_INT)
      val dimsVar = env.freshLocalVar("dimensions", "[I", LocalVar.initNull)
      mv.visitVarInsn(ASTORE, env.getVarIdx(dimsVar))
      dims - 1 to 0 by -1 foreach {i =>
        mv.visitVarInsn(ALOAD, env.getVarIdx(dimsVar))
        mv.visitInsn(SWAP)
        mv.visitIntInsn(BIPUSH, i)
        mv.visitInsn(SWAP)
        mv.visitInsn(IASTORE)
      }
      mv.visitVarInsn(ALOAD, env.getVarIdx(dimsVar))
      mv.visitLdcInsn(owner.toModel.toString)
      loadCurrentCtx(mv, env, block)
      mv.visitMethodInsn(INVOKESTATIC, Owner.getArrayOps, MethodName("initMultiArray"), MethodDesc(s"([ILjava/lang/String;$fexprclasstype)[$vclasstype"), false)
      if (env.getTag(this, env.TAG_NEED_V)) {
        callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      }
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (vs, prevs, f) = s.popN(dims)
    if (env.getTag(this, env.TAG_NEED_V)) {
      // this means the array itself needs to be wrapped into a V
      (f.push(V_TYPE(false), Set(this)), Set())
    }
    else {
      if (vs.contains(V_TYPE(false))) {
        // at least one dimension is V, ensure that all dimensions are V
        val bv = vs.find(!_.isInstanceOf[V_TYPE])
        if (bv.isDefined) return (f, prevs(vs.indexOf(bv.get)))
        // array length is a V, needs invokedynamic to create a V array ref
        env.setLift(this)
        (f.push(V_TYPE(false), Set(this)), Set())
      }
      else {
        (f.push(REF_TYPE(), Set(this)), Set())
      }
    }
  }

  override def doBacktrack(env: VMethodEnv): Unit = env.setTag(this, env.TAG_NEED_V)
}