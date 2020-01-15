package edu.cmu.cs.vbc.loader

import edu.cmu.cs.vbc.vbytecode.{MethodDesc, TypeDesc}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._

import scala.jdk.CollectionConverters._

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
    val lvIndexes = m.localVariables.asScala.map(_.index)

    // avoid accidentally update VarInsnNodes that share the same index
    var lastStoreUpdate = collection.mutable.Map[LV, Int]()
    m.localVariables.asScala.foreach { x => lastStoreUpdate(LV(x.name, x.desc, x.index)) = -1 }

    if (instructions.nonEmpty && lvIndexes.distinct.lengthCompare(m.localVariables.size) < 0) {
      // seems more efficient to use mutable here

      // get the next usable index
      var nextIndex = m.maxLocals

      // check local variable table
      val seenLV = collection.mutable.Set[LV]()
      val processedLV = collection.mutable.Map[LV, LV]()
      val usedIndex = collection.mutable.Set[Int]()
      for (lv <- m.localVariables.asScala if lvIndexes.count(_ == lv.index) > 1) {
        val x = LV(lv.name, lv.desc, lv.index)
        if (!seenLV.contains(x)) {
          seenLV add x
          if (!usedIndex.contains(x.index)) {
            usedIndex add x.index
            processedLV put (x, x)
          } else {
            assume(lv.start != null, s"start label does not exist for $lv")
            assume(lv.end != null, s"end label does not exist for $lv")
            update(m, lv, nextIndex, lastStoreUpdate)
            lv.index = nextIndex
            usedIndex add nextIndex
            processedLV put (x, LV(x.name, x.desc, nextIndex))
            nextIndex = if (TypeDesc(lv.desc).is64Bit) nextIndex + 2 else nextIndex + 1
            m.maxLocals = nextIndex
          }
        }
        else {
          val y = processedLV(x)
          if (x != y) {
            assume(lv.start != null, s"start label does not exist for $lv")
            assume(lv.end != null, s"end label does not exist for $lv")
            update(m, lv, y.index, lastStoreUpdate)
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
  def update(m: MethodNode, lv: LocalVariableNode, newI: Int, lastStoreUpdate: collection.mutable.Map[LV, Int]): Unit = {
    val start: LabelNode = lv.start
    val end: LabelNode = lv.end
    val oldI: Int = lv.index
    val myLV = LV(lv.name, lv.desc, lv.index)
    val region = m.instructions.toArray.dropWhile {
      case x: LabelNode => x.getLabel != start.getLabel
      case _ => true
    }.takeWhile {
      case x: LabelNode => x.getLabel != end.getLabel
      case _ => true
    }
    val beforeRegion = m.instructions.toArray.takeWhile {
      case x: LabelNode => x.getLabel != start.getLabel
      case _ => true
    }
    val storeIns = beforeRegion.reverse.find {
      case x: VarInsnNode if isStore(x.getOpcode) && x.`var` == oldI && m.instructions.indexOf(x) > lastStoreUpdate(myLV)  => true
      case _ => false
    }
    storeIns.foreach { x =>
      lastStoreUpdate(myLV) = m.instructions.indexOf(x)
      x.asInstanceOf[VarInsnNode].`var` = newI
    }
    for (i <- region) i match {
      case x: VarInsnNode if x.`var` == oldI => x.`var` = newI
      case x: IincInsnNode if x.`var` == oldI => x.`var` = newI
      case _ => // do nothing
    }
  }

  def isStore(op: Int): Boolean = op match {
    case ASTORE | ISTORE | LSTORE | FSTORE | DSTORE => true
    case _ => false
  }
}

case class LV(name: String, desc: String, index: Int)

