package edu.cmu.cs.vbc.loader

import edu.cmu.cs.vbc.vbytecode.{MethodDesc, TypeDesc}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._

import scala.collection.JavaConversions._

/**
  * This object transforms methods to avoid reuse of local variables.
  *
  * After transformation, each local variable will be used either for 32-bit variables
  * or 64-bit variables, but not mixed.
  *
  * Note that current version only go through the local variable table at the
  * end of the method. Ideally, we should check each individual instruction
  *
  * @author chupanw
  */
object LocalVariableTransformer {
  def transform(m: MethodNode): MethodNode = {
    if (m.localVariables == null) return m
    val instructions = m.instructions.toArray
    val lvIndexes = m.localVariables.map(_.index)

    if (instructions.nonEmpty && lvIndexes.distinct.lengthCompare(m.localVariables.size) < 0) {
      // seems more efficient to use mutable here

      // get the next usable index
      var lastLabel: Option[LabelNode] = None
      val allLoadAndStore = instructions.filter(isLoadOrStore).map(_.asInstanceOf[VarInsnNode])
      var nextIndex =
        if (allLoadAndStore.isEmpty && MethodDesc(m.desc).getArgCount == 0)
          0
        else
          allLoadAndStore.map(_.`var`).max + 1 // in case the last local variable is 64-bit
      if ((m.access & ACC_STATIC) == 0) nextIndex += 1

      // check local variable table
      val seenLV = collection.mutable.Set[LV]()
      val processedLV = collection.mutable.Map[LV, LV]()
      val usedIndex = collection.mutable.Set[Int]()
      for (lv <- m.localVariables if lvIndexes.count(_ == lv.index) > 1) {
        val x = LV(lv.desc, lv.index)
        if (!seenLV.contains(x)) {
          seenLV add x
          if (!usedIndex.contains(x.index)) {
            usedIndex add x.index
            processedLV put (x, x)
          } else {
            assume(lv.start != null, s"start label does not exist for $lv")
            assume(lv.end != null, s"end label does not exist for $lv")
            update(m, lv.start, lv.end, lv.index, nextIndex)
            lv.index = nextIndex
            usedIndex add nextIndex
            processedLV put (x, LV(x.desc, nextIndex))
            nextIndex = if (TypeDesc(lv.desc).is64Bit) nextIndex + 2 else nextIndex + 1
            m.maxLocals = nextIndex
          }
        }
        else {
          val y = processedLV(x)
          if (x != y) {
            assume(lv.start != null, s"start label does not exist for $lv")
            assume(lv.end != null, s"end label does not exist for $lv")
            update(m, lv.start, lv.end, lv.index, y.index)
            lv.index = y.index
          }
        }
      }
    }
    m
  }

  /**
    * Replace old index with new index in a specific region of the method
    */
  def update(m: MethodNode, start: LabelNode, end: LabelNode, oldI: Int, newI: Int): Unit = {
    val region = m.instructions.toArray.dropWhile {
      case x: LabelNode => x.getLabel != start.getLabel
      case _ => true
    }.takeWhile {
      case x: LabelNode => x.getLabel != end.getLabel
      case _ => true
    }
    val storeIns = m.instructions.toArray.takeWhile {
      case x: LabelNode => x.getLabel != start.getLabel
      case _ => true
    }.reverse.find {
      case x: VarInsnNode if x.`var` == oldI => true
      case _ => false
    }
    assert(storeIns.isDefined, s"No store instruction for $oldI before $start")
    storeIns.foreach(_.asInstanceOf[VarInsnNode].`var` = newI)
    for (i <- region) i match {
      case x: VarInsnNode if x.`var` == oldI => x.`var` = newI
      case x: IincInsnNode if x.`var` == oldI => x.`var` = newI
      case _ => // do nothing
    }
  }

  def isLoadOrStore(i: AbstractInsnNode): Boolean = i match {
    case l: VarInsnNode if l.getOpcode != RET => true
    case _ => false
  }
}

case class LV(desc: String, index: Int) {
  private val is64Bit = TypeDesc(desc).is64Bit

  override def equals(obj: scala.Any): Boolean = {
    val that = obj.asInstanceOf[LV]
    this.is64Bit == that.is64Bit && this.index == that.index
  }

  override def hashCode(): Int = this.is64Bit.hashCode + this.index.hashCode()
}

