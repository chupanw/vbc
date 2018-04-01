package edu.cmu.cs.vbc.vbytecode

import org.objectweb.asm.{Label, MethodVisitor, Type}

class MethodEnv(val clazz: VBCClassNode, val method: VBCMethodNode) extends CFGAnalysis {
  //find all local variables
  protected val localVars: List[LocalVar] =
    (for (block <- blocks; instr <- block.instr; v <- instr.getVariables) yield v).distinct

  protected var labels: List[Label] = Nil
  //local variables refer to variables that are used in the byte code,
  //not to variables that are generated in the tranformation process;
  //the latter are stored separately as freshVars
  //(localVars and freshVars behave as sorted sets)
  protected def parameterCount: Int = Type.getArgumentTypes(method.desc).size + (if (method.isStatic) 0 else 1) +
    Type.getArgumentTypes(method.desc).count(t => t.getDescriptor == "J" || t.getDescriptor == "D") // long and double
  protected var freshVars: List[LocalVar] = Nil

  def getFreshVars(): List[LocalVar] = freshVars

  protected var blockLabels: Map[Block, Label] = Map()

  val thisParameter: Parameter = new Parameter(0, "this", Owner(clazz.name).getTypeDesc)

  def freshLabel(name: String = "<unknown>") = {
    val l = new Label(name)
    labels ::= l
    l
  }

  def freshLocalVar(name: String, desc: String, init: (MethodVisitor, VMethodEnv, LocalVar) => Unit): LocalVar = {
    val l = new LocalVar(name, desc, vinitialize = init)
    freshVars ::= l
    l
  }

  def countSlots(l: List[LocalVar]): Int = {
    val ll = l.map(v => if (v.is64Bit) 2 else 1)
    (0 /: ll) (_ + _)
  }

  def getVarIdx(variable: Variable): Int = variable match {
    case p: Parameter =>
      assert(p.idx >= 0, "Parameter with negative index")
      p.idx
    case l: LocalVar =>
      val localIdxPos = localVars.reverse.indexOf(l)
      if (localIdxPos >= 0) {
        parameterCount + countSlots(localVars.splitAt(localVars.size - 1 - localIdxPos)._1)
      }
      else {
        val freshIdxPos = freshVars.reverse.indexOf(l)
        assert(freshIdxPos >= 0, "variable not found in environment")
        parameterCount + countSlots(localVars) + countSlots(freshVars.splitAt(freshIdxPos)._1)
      }
  }

  /**
    * returns a label for a block
    */
  def getBlockLabel(block: Block): Label = {
    if (!(blockLabels contains block))
      blockLabels += (block -> freshLabel("L" + blocks.indexOf(block)))
    blockLabels(block)
  }

  /**
    * returns whether the first block is before the second block
    * in the current method
    */
  def isBlockBefore(first: Block, second: Block): Boolean =
    blocks.indexOf(first) < blocks.indexOf(second)


  def isMain = {
    //todo oversimplified; also I'd rather create a new main method that just calls the lifted main method,
    //such that the (lifted) main method is not different from any other (lifted) method
    method.name == "main"
  }

  // indices of first instructions in each block
  lazy val instructionBlockBoundaries = blocks.foldLeft(List(0))((l, block) => (l.head + block.instr.size) :: l).reverse

  // lookup the block for an instruction
  def blockForInstruction(instrIdx: Int): Block = {
    var idx = instrIdx
    for (b <- blocks) {
      idx -= b.instr.size
      if (idx < 0)
        return b
    }
    null
  }

  def maxLocals = parameterCount + localVars.size + freshVars.size

  val instructions = for (b <- blocks; i <- b.instr) yield i

  def getBlockStart(blockIdx: Int): Int = {
    var sum = 0
    blocks.take(blockIdx).foreach(sum += _.instr.length)
    sum
  }

}

