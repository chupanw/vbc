package edu.cmu.cs.vbc.loader

import java.io.InputStream

import edu.cmu.cs.vbc.VBCClassLoader
import edu.cmu.cs.vbc.config.Settings
import edu.cmu.cs.vbc.utils.ExceptionHandlerAnalyzer
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import org.objectweb.asm.{ClassReader, MethodVisitor, Opcodes, Type}

import scala.jdk.CollectionConverters._

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
    if (cl.interfaces == null) Nil else cl.interfaces.asScala.toList,
    if (cl.fields == null) Nil else cl.fields.asScala.map(adaptField).toList,
    // todo: rewrite this part with trait stacking
    if (cl.methods == null) Nil else cl.methods.asScala.map(
//      m => transformSwitches(LocalVariableTransformer.transform(m))).flatMap(m => new MethodChopper(cl.name, m).chop()).map(m => {
//        adaptMethod(cl.name,
//          TryCatchBlock.wrapMethodBody(
//            TernaryOperatorRewriter.extractAllTernaryOperator(cl.name,
//              InitRewriter.extractInitSeq(m, cl)
//            )
//          )
//        )
//      }
      m => adaptMethod(cl.name,
        TryCatchBlock.wrapMethodBody(
          TernaryOperatorRewriter.extractAllTernaryOperator(cl.name,
            InitRewriter.extractInitSeq(
                transformSwitches(LocalVariableTransformer.transform(m)), cl
            )
          )
        )
      )
    ).toList,
    if (cl.sourceDebug != null && cl.sourceFile != null) Some(cl.sourceFile, cl.sourceDebug) else None,
    if (cl.outerClass != null) Some(cl.outerClass, cl.outerMethod, cl.outerMethodDesc) else None,
    if (cl.visibleAnnotations == null) Nil else cl.visibleAnnotations.asScala.toList,
    if (cl.invisibleAnnotations == null) Nil else cl.invisibleAnnotations.asScala.toList,
    if (cl.visibleTypeAnnotations == null) Nil else cl.visibleTypeAnnotations.asScala.toList,
    if (cl.invisibleTypeAnnotations == null) Nil else cl.invisibleTypeAnnotations.asScala.toList,
    if (cl.attrs == null) Nil else cl.attrs.asScala.toList,
    if (cl.innerClasses == null) Nil else cl.innerClasses.asScala.map(adaptInnerClass).toList
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
        m.maxLocals += 1  // 1 is fine because Java does not allow double or long in switch
        val ifs: List[AbstractInsnNode] = (table.min to table.max).toList.flatMap(num => {
          List(
            new VarInsnNode(ILOAD, switchValueIdx),
            new LdcInsnNode(num),
            new InsnNode(ISUB),
            new JumpInsnNode(IFEQ, table.labels.get(num - table.min))
          )
        })
        List(new VarInsnNode(ISTORE, switchValueIdx)) ::: ifs ::: List(new InsnNode(ICONST_1), new JumpInsnNode(IFNE, table.dflt))
      case lookup: LookupSwitchInsnNode =>
        val switchValueIdx = m.maxLocals
        m.localVariables.add(new LocalVariableNode(s"switch${util.Random.nextInt()}", "I", "I", null, null, switchValueIdx))
        m.maxLocals += 1
        val ifs: List[AbstractInsnNode] = (0 until lookup.keys.size()).toList.flatMap(index => {
          List(
            new VarInsnNode(ILOAD, switchValueIdx),
            new LdcInsnNode(lookup.keys.get(index)),
            new InsnNode(ISUB),
            new JumpInsnNode(IFEQ, lookup.labels.get(index))
          )
        })
        List(new VarInsnNode(ISTORE, switchValueIdx)) ::: ifs ::: List(new InsnNode(ICONST_1), new JumpInsnNode(IFNE, lookup.dflt))
      case _ => List(i)
    })
    m.instructions.clear()
    newInstructions foreach {i => m.instructions.add(i)}
    if (m.maxStack < 2) m.maxStack = 2  // rare but possible
    m
  }

  def pickLVInit(desc: TypeDesc): (MethodVisitor, VMethodEnv, LocalVar) => Unit = {
    desc match {
      case TypeDesc("I") | TypeDesc("S") | TypeDesc("B") | TypeDesc("C") | TypeDesc("Z") =>
        LocalVar.initIntZero
      case TypeDesc("F") => LocalVar.initFloatZero
      case TypeDesc("J") => LocalVar.initLongZero
      case TypeDesc("D") => LocalVar.initDoubleZero
      case _ => LocalVar.initNull
    }
  }

  def recordExceptions(className: String, m: MethodNode): Unit = {
    val exceptions = ExceptionHandlerAnalyzer.analyzeMethodNode(m, shouldExcludeOneThrowable = true)
    VBCClassLoader.putMightHandle(
      className.replace('/', '.'),
      MethodName.getLiftedMethodName(m.name, m.desc),
      exceptions
    )
  }

  def adaptMethod(owner: String, m: MethodNode): VBCMethodNode = {
    if (Settings.enableStackTraceCheck) recordExceptions(owner, m)
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
      val localVarList = m.localVariables.asScala.toList
      for (i <- localVarList.indices) {
        val lv = localVarList(i)
        if (lv.index < parameterRange)
          varCache += (lv.index -> new Parameter(lv.index, lv.name, TypeDesc(lv.desc), is64Bit = TypeDesc(lv.desc).is64Bit))
        else
          varCache += (lv.index -> new LocalVar(lv.name, lv.desc, is64Bit = TypeDesc(lv.desc).is64Bit, vinitialize = pickLVInit(TypeDesc(lv.desc))))
      }
    }

    // typically we initialize all variables and parameters from the table, but that table is technically optional,
    // so we need a fallback option and generate them on the fly with name "$unknown"
    // todo: this looks dangerous because local variables might share the same index
    def lookupVariable(idx: Int, opCode: Int): Variable = {
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
              case LLOAD | LSTORE => new LocalVar("$unknownLong", TypeDesc("J"), is64Bit = true, vinitialize = LocalVar.initLongZero)
              case DLOAD | DSTORE => new LocalVar("$unknownDouble", TypeDesc("D"), is64Bit = true, vinitialize = LocalVar.initDoubleZero)
              case FLOAD | FSTORE => new LocalVar("$unknownFloat", TypeDesc("F"), is64Bit = false, vinitialize = LocalVar.initFloatZero)
              case ILOAD | ISTORE => new LocalVar("$unknown", TypeDesc("I"), is64Bit = false, vinitialize = LocalVar.initIntZero)
              case _ => new LocalVar("$unknown", "V")
            }
          }
        varCache += (idx -> newVar)
        newVar
      }
    }

    def createBlock(start: Int, end: Int): Block = {
      val instrList = for (instrIdx <- start until end;
                           if m.instructions.get(instrIdx).getOpcode >= 0 || m.instructions.get(instrIdx).isInstanceOf[LineNumberNode])
        yield adaptBytecodeInstruction(m.instructions.get(instrIdx), methodAnalyzer.label2BlockIdx.apply, lookupVariable)
      Block(instrList, methodAnalyzer.getBlockException(start), Nil)
    }



    val blocks = for (i <- 0 to ordered.length - 2) yield createBlock(ordered(i), ordered(i + 1))
    val nonEmptyBlocks = blocks.filter(_.instr.nonEmpty)

    VBCMethodNode(
      m.access,
      m.name,
      m.desc,
      Option(m.signature),
      if (m.exceptions == null) Nil else m.exceptions.asScala.toList,
      new CFG(nonEmptyBlocks.toList),
      varCache.values.toList,
      annotationDefault = m.annotationDefault,
      invisibleAnnotations = if (m.invisibleAnnotations == null) Nil else m.invisibleAnnotations.asScala.toList,
      invisibleLocalVariableAnnotations = if (m.invisibleLocalVariableAnnotations == null) Nil else m.invisibleLocalVariableAnnotations.asScala.toList,
      invisibleParameterAnnotations = if (m.invisibleParameterAnnotations == null) Array.empty else m.invisibleParameterAnnotations.filter(_ != null).map(_.asScala.toList),
      invisibleTypeAnnotations = if (m.invisibleTypeAnnotations == null) Nil else m.invisibleTypeAnnotations.asScala.toList,
      visibleAnnotations = if (m.visibleAnnotations == null) Nil else m.visibleAnnotations.asScala.toList,
      visibleLocalVariableAnnotations = if (m.visibleLocalVariableAnnotations == null) Nil else m.visibleLocalVariableAnnotations.asScala.toList,
      visibleParameterAnnotations = if (m.visibleParameterAnnotations == null) Array.empty else m.visibleParameterAnnotations.filter(x => x != null).map(_.asScala.toList),
      visibleTypeAnnotations = if (m.visibleTypeAnnotations == null) Nil else m.visibleTypeAnnotations.asScala.toList
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
      case FCONST_2 => InstrFCONST_2()
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
      case DUP2_X2 => InstrDUP2_X2()
      case SWAP => InstrSWAP()
      case IADD => InstrIADD()
      case LADD => InstrLADD()
      case FADD => InstrFADD()
      case DADD => InstrDADD()
      case ISUB => InstrISUB()
      case LSUB => InstrLSUB()
      case FSUB => InstrFSUB()
      case DSUB => InstrDSUB()
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
      case DREM => InstrDREM()
      case INEG => InstrINEG()
      case LNEG => InstrLNEG()
      case FNEG => InstrFNEG()
      case DNEG => InstrDNEG()
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
      case D2I => InstrD2I()
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
        InstrMULTIANEWARRAY(Owner(i.desc), i.dims)
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
    if (field.visibleAnnotations == null) Nil else field.visibleAnnotations.asScala.toList,
    if (field.invisibleAnnotations == null) Nil else field.invisibleAnnotations.asScala.toList,
    if (field.visibleTypeAnnotations == null) Nil else field.visibleTypeAnnotations.asScala.toList,
    if (field.invisibleTypeAnnotations == null) Nil else field.invisibleTypeAnnotations.asScala.toList,
    if (field.attrs == null) Nil else field.attrs.asScala.toList
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