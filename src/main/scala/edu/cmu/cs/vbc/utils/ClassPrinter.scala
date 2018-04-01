package edu.cmu.cs.vbc.utils

import java.io.{File, PrintWriter}

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.{ASMifier, TraceClassVisitor}
import org.objectweb.asm.{ClassReader, Opcodes}

/**
  * @author chupanw
  */
object ClassPrinter extends App {

  val cr = new ClassReader(args(0))
  cr.accept(new TraceClassVisitor(null, new PrintWriter(System.out)), 0)

}

object ASMifierPrinter extends App {

  val cr = new ClassReader(args(0))
  cr.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)), 0)
}

object DotifierPrinter extends App {

  val cr = new ClassReader(args(0))
  val cn = new ClassNode(Opcodes.ASM5)
  cr.accept(cn, 0)
  val dot = new Dotifier()
  cr.accept(new TraceClassVisitor(null, dot, new PrintWriter(System.out)), 0)
  val writer = new PrintWriter(new File("/tmp/cfg.gv"))
  writer.write(dot.textBuf.mkString(""))
  writer.close()
  import scala.sys.process._
  val content = Process("dot -Tpdf -O /tmp/cfg.gv").lineStream
  println(content)
}
