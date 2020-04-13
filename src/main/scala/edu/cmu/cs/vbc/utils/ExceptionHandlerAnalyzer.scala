package edu.cmu.cs.vbc.utils

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree.{ClassNode, MethodNode}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object ExceptionHandlerAnalyzer {

  val classNodeCache: mutable.Map[String, ClassNode] = mutable.Map()

  def analyzeMethod(className: String, methodName: String, shouldExcludeOneThrowable: Boolean): Set[String] = {
    val classNode = classNodeCache.getOrElseUpdate(className, {
      val cNode = new ClassNode(ASM5)
      new ClassReader(className).accept(cNode, 0)
      cNode
    })
    val methodNodes = classNode.methods.asScala.toSet.filter(x => x.name == methodName)
    methodNodes.flatMap(m => analyzeMethodNode(m, shouldExcludeOneThrowable))
  }

  def analyzeMethodNode(m: MethodNode, shouldExcludeOneThrowable: Boolean): Set[String] = {
    val l = m.tryCatchBlocks.asScala.toList.map(_.`type`).map {
      case null => "java.lang.Throwable"
      case rest => rest.replace('/', '.')
    }
    val throwableCount = l.count(x => x == "java.lang.Throwable")
    if (shouldExcludeOneThrowable && throwableCount == 1) l.toSet.removedAll(Set("java.lang.Throwable")) else l.toSet
  }
}
