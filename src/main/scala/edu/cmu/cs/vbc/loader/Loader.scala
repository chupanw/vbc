package edu.cmu.cs.vbc.loader

import java.io.InputStream

import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import org.objectweb.asm.{ClassReader, Opcodes, Type}

import scala.collection.JavaConversions._

/**
  * My class adapter using the tree API
  */
class Loader {

  def loadClass(is: InputStream): VBCClassNode = {

    val cr = new ClassReader(is)
    val classNode = new ClassNode(ASM5)
    cr.accept(classNode, 0)

    adaptClass(classNode)
  }

  def adaptClass(cl: ClassNode): VBCClassNode = new VBCClassNode(
    cl.version,
    cl.access,
    cl.name,
    if (cl.signature == null) None else Some(cl.signature),
    cl.superName,
    if (cl.interfaces == null) Nil else cl.interfaces.toList,
    if (cl.fields == null) Nil else cl.fields.map(adaptField).toList,
    if (cl.methods == null) Nil else cl.methods.map(
      m => adaptMethod(cl.name,
        TernaryOperatorRewriter.extractAllTernaryOperator(cl.name,
          InitRewriter.extractInitSeq(
              transformSwitches(LocalVariableTransformer.transform(m)), cl
          )
        )
      )
    ).toList,
    if (cl.sourceDebug != null && cl.sourceFile != null) Some(cl.sourceFile, cl.sourceDebug) else None,
    if (cl.outerClass != null) Some(cl.outerClass, cl.outerMethod, cl.outerMethodDesc) else None,
    if (cl.visibleAnnotations == null) Nil else cl.visibleAnnotations.toList,
    if (cl.invisibleAnnotations == null) Nil else cl.invisibleAnnotations.toList,
    if (cl.visibleTypeAnnotations == null) Nil else cl.visibleTypeAnnotations.toList,
    if (cl.invisibleTypeAnnotations == null) Nil else cl.invisibleTypeAnnotations.toList,
    if (cl.attrs == null) Nil else cl.attrs.toList,
    if (cl.innerClasses == null) Nil else cl.innerClasses.map(adaptInnerClass).toList
  )

  /**
    * Transform LookupSwitch and TableSwitch to IFEQ and GOTO
    *
    * In case of errors, we modify instruction list instead of creating a new MethodNode
    *
    * @param m  origin MethodNode
    * @return new MethodNode that could not have any switch
    */
  def transformSwitches(m: MethodNode): MethodNode = {
    val instructions: List[AbstractInsnNode] = m.instructions.toArray.toList
    val newInstructions: List[AbstractInsnNode] = instructions.flatMap(i => i match {
      case table: TableSwitchInsnNode =>
        val switchValueIdx = m.maxLocals
        m.localVariables.add(new LocalVariableNode(s"switch${util.Random.nextInt()}", "I", "I", null, null, switchValueIdx))
        m.maxLocals += 1
        val ifs: List[AbstractInsnNode] = (table.min to table.max).toList.flatMap(num => {
          List(
            new VarInsnNode(ILOAD, switchValueIdx),
            new LdcInsnNode(num),
            new InsnNode(ISUB),
            new JumpInsnNode(IFEQ, table.labels(num - table.min))
          )
        })
        List(new VarInsnNode(ISTORE, switchValueIdx)) ::: ifs ::: List(new JumpInsnNode(GOTO, table.dflt))
      case lookup: LookupSwitchInsnNode =>
        val switchValueIdx = m.maxLocals
        m.localVariables.add(new LocalVariableNode(s"switch${util.Random.nextInt()}", "I", "I", null, null, switchValueIdx))
        m.maxLocals += 1
        val ifs: List[AbstractInsnNode] = (0 until lookup.keys.length).toList.flatMap(index => {
          List(
            new VarInsnNode(ILOAD, switchValueIdx),
            new LdcInsnNode(lookup.keys(index)),
            new InsnNode(ISUB),
            new JumpInsnNode(IFEQ, lookup.labels(index))
          )
        })
        List(new VarInsnNode(ISTORE, switchValueIdx)) ::: ifs ::: List(new JumpInsnNode(GOTO, lookup.dflt))
      case _ => List(i)
    })
    m.instructions.clear()
    newInstructions foreach {i => m.instructions.add(i)}
    if (m.maxStack < 2) m.maxStack = 2  // rare but possible
    m
  }

  def adaptMethod(owner: String, m: MethodNode): VBCMethodNode = {
    //    println("\tMethod: " + m.name)
    val methodAnalyzer = new MethodCFGAnalyzer(owner, m)
    methodAnalyzer.analyze()
    methodAnalyzer.validate()
    val ordered = methodAnalyzer.blocks.toArray :+ m.instructions.size()

    var varCache: Map[Int, Variable] = Map()
    val isStatic = (m.access & Opcodes.ACC_STATIC) > 0
    val parameterRange = Type.getArgumentTypes(m.desc).size + // numbers of arguments
        (if (isStatic) 0 else 1) +  // 'this' for nonstatic methods
        Type.getArgumentTypes(m.desc).count(t => t.getDescriptor == "J" || t.getDescriptor == "D") // long and double

    // adding "this" explicitly, because it may not be included if it's the only parameter
    if (!isStatic)
      varCache += (0 -> new Parameter(0, "this", Owner(owner).getTypeDesc))
    if (m.localVariables != null) {
      val localVarList = m.localVariables.toList
      for (i <- 0 until localVarList.size()) {
        val vIdx = localVarList(i).index
        if (vIdx < parameterRange)
          varCache += (vIdx -> new Parameter(
            vIdx,
            localVarList(i).name,
            TypeDesc(localVarList(i).desc),
            is64Bit = TypeDesc(localVarList(i).desc).is64Bit
          ))
        else
          varCache += (vIdx -> new LocalVar(
            localVarList(i).name,
            localVarList(i).desc,
            is64Bit = TypeDesc(localVarList(i).desc).is64Bit
          ))
      }
    }

    // typically we initialize all variables and parameters from the table, but that table is technically optional,
    // so we need a fallback option and generate them on the fly with name "$unknown"
    def lookupVariable(idx: Int, opCode: Int): Variable =
      if (varCache contains idx)
        varCache(idx)
      else {
        val newVar =
          if (idx < parameterRange) {
            opCode match {
              case LLOAD | LSTORE => new Parameter(idx, "$unknown", TypeDesc("J"), is64Bit = true)
              case DLOAD | DSTORE => new Parameter(idx, "$unknown", TypeDesc("D"), is64Bit = true)
              case _ => new Parameter(idx, "$unknown", TypeDesc("Ljava/lang/Object;"))
            }
          }
          else {
            opCode match {
              case LLOAD | LSTORE => new LocalVar("$unknown", TypeDesc("J"), is64Bit = true)
              case DLOAD | DSTORE => new LocalVar("$unknown", TypeDesc("D"), is64Bit = true)
              case _ => new LocalVar("$unknown", "V")
            }
          }
        varCache += (idx -> newVar)
        newVar
      }

    def createBlock(start: Int, end: Int): Block = {
      val instrList = for (instrIdx <- start until end;
                           if m.instructions.get(instrIdx).getOpcode >= 0 || m.instructions.get(instrIdx).isInstanceOf[LineNumberNode])
        yield adaptBytecodeInstruction(m.instructions.get(instrIdx), methodAnalyzer.label2BlockIdx.apply, lookupVariable)
      Block(instrList, methodAnalyzer.getBlockException(start))
    }



    val blocks = for (i <- 0 to ordered.length - 2) yield createBlock(ordered(i), ordered(i + 1))
    val nonEmptyBlocks = blocks.filter(_.instr.nonEmpty)

    VBCMethodNode(
      m.access,
      m.name,
      m.desc,
      if (m.signature == null) None else Some(m.signature),
      if (m.exceptions == null) Nil else m.exceptions.toList,
      new CFG(nonEmptyBlocks.toList),
      varCache.values.toList
    )
  }

  def adaptBytecodeInstruction(inst: AbstractInsnNode, labelLookup: LabelNode => Int, variables: (Int, Int) => Variable): Instruction =
    inst.getOpcode match {
      case NOP => UNKNOWN()
      case ACONST_NULL => InstrACONST_NULL()
      case ICONST_M1 => InstrICONST(-1)
      case ICONST_0 => InstrICONST(0)
      case ICONST_1 => InstrICONST(1)
      case ICONST_2 => InstrICONST(2)
      case ICONST_3 => InstrICONST(3)
      case ICONST_4 => InstrICONST(4)
      case ICONST_5 => InstrICONST(5)
      case LCONST_0 => InstrLCONST(0)
      case LCONST_1 => InstrLCONST(1)
      case FCONST_0 => InstrFCONST_0()
      case FCONST_1 => InstrFCONST_1()
      case FCONST_2 => UNKNOWN(FCONST_2)
      case DCONST_0 => InstrDCONST_0()
      case DCONST_1 => InstrDCONST_1()
      case BIPUSH => {
        val i = inst.asInstanceOf[IntInsnNode]
        InstrBIPUSH(i.operand)
      }
      case SIPUSH => {
        val i = inst.asInstanceOf[IntInsnNode]
        InstrSIPUSH(i.operand)
      }
      case LDC => {
        val i = inst.asInstanceOf[LdcInsnNode]
        InstrLDC(i.cst)
      }
      case ILOAD => {
        val i = inst.asInstanceOf[VarInsnNode]
        InstrILOAD(variables(i.`var`, ILOAD))
      }
      case LLOAD =>
        val i = inst.asInstanceOf[VarInsnNode]
        InstrLLOAD(variables(i.`var`, LLOAD))
      case FLOAD =>
        val i = inst.asInstanceOf[VarInsnNode]
        InstrFLOAD(variables(i.`var`, FLOAD))
      case DLOAD =>
        val i = inst.asInstanceOf[VarInsnNode]
        InstrDLOAD(variables(i.`var`, DLOAD))
      case ALOAD => {
        val i = inst.asInstanceOf[VarInsnNode]
        InstrALOAD(variables(i.`var`, ALOAD))
      }
      case IALOAD => InstrIALOAD()
      case LALOAD => InstrLALOAD()
      case FALOAD => InstrFALOAD()
      case DALOAD => InstrDALOAD()
      case AALOAD => InstrAALOAD()
      case BALOAD => InstrBALOAD()
      case CALOAD => InstrCALOAD()
      case SALOAD => InstrSALOAD()
      case ISTORE => {
        val i = inst.asInstanceOf[VarInsnNode]
        InstrISTORE(variables(i.`var`, ISTORE))
      }
      case LSTORE => {
        val i = inst.asInstanceOf[VarInsnNode]
        InstrLSTORE(variables(i.`var`, LSTORE))
      }
      case FSTORE =>
        val i = inst.asInstanceOf[VarInsnNode]
        InstrFSTORE(variables(i.`var`, FSTORE))
      case DSTORE =>
        val i = inst.asInstanceOf[VarInsnNode]
        InstrDSTORE(variables(i.`var`, DSTORE))
      case ASTORE => {
        val i = inst.asInstanceOf[VarInsnNode]
        InstrASTORE(variables(i.`var`, ASTORE))
      }
      case IASTORE => InstrIASTORE()
      case LASTORE => InstrLASTORE()
      case FASTORE => InstrFASTORE()
      case DASTORE => InstrDASTORE()
      case AASTORE => InstrAASTORE()
      case BASTORE => InstrBASTORE()
      case CASTORE => InstrCASTORE()
      case SASTORE => InstrSASTORE()
      case POP => InstrPOP()
      case POP2 => InstrPOP2()
      case DUP => InstrDUP()
      case DUP_X1 => InstrDUP_X1()
      case DUP_X2 => InstrDUP_X2()
      case DUP2 => InstrDUP2()
      case DUP2_X1 => InstrDUP2_X1()
      case DUP2_X2 => UNKNOWN(DUP2_X2)
      case SWAP => InstrSWAP()
      case IADD => InstrIADD()
      case LADD => InstrLADD()
      case FADD => InstrFADD()
      case DADD => InstrDADD()
      case ISUB => InstrISUB()
      case LSUB => InstrLSUB()
      case FSUB => UNKNOWN(FSUB)
      case DSUB => UNKNOWN(DSUB)
      case IMUL => InstrIMUL()
      case LMUL => InstrLMUL()
      case FMUL => InstrFMUL()
      case DMUL => InstrDMUL()
      case IDIV => InstrIDIV()
      case LDIV => InstrLDIV()
      case FDIV => InstrFDIV()
      case DDIV => InstrDDIV()
      case IREM => InstrIREM()
      case LREM => InstrLREM()
      case FREM => UNKNOWN(FREM)
      case DREM => UNKNOWN(DREM)
      case INEG => InstrINEG()
      case LNEG => InstrLNEG()
      case FNEG => UNKNOWN(FNEG)
      case DNEG => UNKNOWN(DNEG)
      case ISHL => InstrISHL()
      case LSHL => InstrLSHL()
      case ISHR => InstrISHR()
      case LSHR => InstrLSHR()
      case IUSHR => InstrIUSHR()
      case LUSHR => InstrLUSHR()
      case IAND => InstrIAND()
      case LAND => InstrLAND()
      case IOR => InstrIOR()
      case LOR => InstrLOR()
      case IXOR => InstrIXOR()
      case LXOR => InstrLXOR()
      case IINC => {
        val i = inst.asInstanceOf[IincInsnNode]
        InstrIINC(variables(i.`var`, IINC), i.incr)
      }
      case I2L => InstrI2L()
      case I2F => InstrI2F()
      case I2D => InstrI2D()
      case L2I => InstrL2I()
      case L2F => InstrL2F()
      case L2D => InstrL2D()
      case F2I => InstrF2I()
      case F2L => UNKNOWN(F2L)
      case F2D => InstrF2D()
      case D2I => UNKNOWN(D2I)
      case D2L => InstrD2L()
      case D2F => InstrD2F()
      case I2B => InstrI2B()
      case I2C => InstrI2C()
      case I2S => InstrI2S()
      case LCMP => InstrLCMP()
      case FCMPL => InstrFCMPL()
      case FCMPG => InstrFCMPG()
      case DCMPL => InstrDCMPL()
      case DCMPG => InstrDCMPG()
      case IFEQ => {
        val insIFEQ = inst.asInstanceOf[JumpInsnNode]
        val label = insIFEQ.label
        InstrIFEQ(labelLookup(label))
      }
      case IFNE => {
        val i = inst.asInstanceOf[JumpInsnNode]
        val label = i.label
        InstrIFNE(labelLookup(label))
      }
      case IFLT => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIFLT(labelLookup(i.label))
      }
      case IFGE => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIFGE(labelLookup(i.label))
      }
      case IFGT => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIFGT(labelLookup(i.label))
      }
      case IFLE =>
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIFLE(labelLookup(i.label))
      case IF_ICMPEQ => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ICMPEQ(labelLookup(i.label))
      }
      case IF_ICMPNE => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ICMPNE(labelLookup(i.label))
      }
      case IF_ICMPLT => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ICMPLT(labelLookup(i.label))
      }
      case IF_ICMPGE => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ICMPGE(labelLookup(i.label))
      }
      case IF_ICMPGT => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ICMPGT(labelLookup(i.label))
      }
      case IF_ICMPLE => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ICMPLE(labelLookup(i.label))
      }
      case IF_ACMPEQ =>
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ACMPEQ(labelLookup(i.label))
      case IF_ACMPNE =>
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIF_ACMPNE(labelLookup(i.label))
      case GOTO => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrGOTO(labelLookup(i.label))
      }
      case JSR => UNKNOWN(JSR)
      case RET => UNKNOWN(RET)
      case TABLESWITCH => UNKNOWN(TABLESWITCH)
      case LOOKUPSWITCH => UNKNOWN(LOOKUPSWITCH)
      case IRETURN => InstrIRETURN()
      case LRETURN => InstrLRETURN()
      case FRETURN => InstrFRETURN()
      case DRETURN => InstrDRETURN()
      case ARETURN => InstrARETURN()
      case RETURN => InstrRETURN()
      case GETSTATIC => {
        val i = inst.asInstanceOf[FieldInsnNode]
        InstrGETSTATIC(Owner(i.owner), FieldName(i.name), TypeDesc(i.desc))
      }
      case PUTSTATIC => {
        val i = inst.asInstanceOf[FieldInsnNode]
        InstrPUTSTATIC(Owner(i.owner), FieldName(i.name), TypeDesc(i.desc))
      }
      case GETFIELD => {
        val i = inst.asInstanceOf[FieldInsnNode]
        InstrGETFIELD(Owner(i.owner), FieldName(i.name), TypeDesc(i.desc))
      }
      case PUTFIELD => {
        val i = inst.asInstanceOf[FieldInsnNode]
        InstrPUTFIELD(Owner(i.owner), FieldName(i.name), TypeDesc(i.desc))
      }
      case INVOKEVIRTUAL => {
        val i = inst.asInstanceOf[MethodInsnNode]
        InstrINVOKEVIRTUAL(Owner(i.owner), MethodName(i.name), MethodDesc(i.desc), i.itf)
      }
      case INVOKESPECIAL => {
        val i = inst.asInstanceOf[MethodInsnNode]
        InstrINVOKESPECIAL(Owner(i.owner), MethodName(i.name), MethodDesc(i.desc), i.itf)
      }
      case INVOKESTATIC => {
        val i = inst.asInstanceOf[MethodInsnNode]
        InstrINVOKESTATIC(Owner(i.owner), MethodName(i.name), MethodDesc(i.desc), i.itf)
      }
      case INVOKEINTERFACE => {
        val i = inst.asInstanceOf[MethodInsnNode]
        InstrINVOKEINTERFACE(Owner(i.owner), MethodName(i.name), MethodDesc(i.desc), i.itf)
      }
      case INVOKEDYNAMIC => {
        val i = inst.asInstanceOf[InvokeDynamicInsnNode]
        InstrINVOKEDYNAMIC(MethodName(i.name), MethodDesc(i.desc), i.bsm, i.bsmArgs)
      }
      case NEW => {
        val i = inst.asInstanceOf[TypeInsnNode]
        InstrNEW(i.desc)
      }
      case NEWARRAY => {
        val i = inst.asInstanceOf[IntInsnNode]
        InstrNEWARRAY(i.operand)
      }
      case ANEWARRAY => {
        val i = inst.asInstanceOf[TypeInsnNode]
        InstrANEWARRAY(Owner(i.desc))
      }
      case ARRAYLENGTH => InstrARRAYLENGTH()
      case ATHROW => InstrATHROW()
      case CHECKCAST => {
        val i = inst.asInstanceOf[TypeInsnNode]
        InstrCHECKCAST(Owner(i.desc))
      }
      case INSTANCEOF =>
        val i = inst.asInstanceOf[TypeInsnNode]
        InstrINSTANCEOF(Owner(i.desc))
      case MONITORENTER => InstrMONITORENTER()
      case MONITOREXIT => InstrMONITOREXIT()
      case MULTIANEWARRAY =>
        val i = inst.asInstanceOf[MultiANewArrayInsnNode]
        InstrMULTIANEWARRAY(i.desc, i.dims)
      case IFNULL => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIFNULL(labelLookup(i.label))
      }
      case IFNONNULL => {
        val i = inst.asInstanceOf[JumpInsnNode]
        InstrIFNONNULL(labelLookup(i.label))
      }
      case -1 =>
        // special nodes in ASM such as LineNumberNode and LabelNode
        inst match {
          case ln: LineNumberNode => InstrLINENUMBER(ln.line)
          case _ => InstrNOP()
        }
      case _ => {
        UNKNOWN()
      }
    }

  def adaptField(field: FieldNode): VBCFieldNode = new VBCFieldNode(
    field.access,
    field.name,
    field.desc,
    field.signature,
    field.value,
    if (field.visibleAnnotations == null) Nil else field.visibleAnnotations.toList,
    if (field.invisibleAnnotations == null) Nil else field.invisibleAnnotations.toList,
    if (field.visibleTypeAnnotations == null) Nil else field.visibleTypeAnnotations.toList,
    if (field.invisibleTypeAnnotations == null) Nil else field.invisibleTypeAnnotations.toList,
    if (field.attrs == null) Nil else field.attrs.toList
  )

  def adaptInnerClass(m: InnerClassNode): VBCInnerClassNode = new VBCInnerClassNode(
    m.name,
    m.outerName,
    m.innerName,
    m.access
  )

  def loadClass(bytes: Array[Byte]): VBCClassNode = {
    val cr = new ClassReader(bytes)
    val classNode = new ClassNode(ASM5)
    cr.accept(classNode, 0)
    adaptClass(classNode)
  }


}