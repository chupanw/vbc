package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.vbytecode.Owner
import org.objectweb.asm.{ClassVisitor, MethodVisitor}
import org.objectweb.asm.Opcodes._

class ReplaceCallsClassVisitor(next: ClassVisitor) extends ClassVisitor(ASM5, next) {
  var className = ""

  override def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]): Unit = {
    className = name
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor = {
    val mv = super.visitMethod(access, name, desc, signature, exceptions)
    if (mv != null && className == "org/apache/commons/digester/SetPropertiesRule")
      new ReplaceCallsMethodVisitor(mv)
    else
      mv
  }
}


class ReplaceCallsMethodVisitor(next: MethodVisitor) extends MethodVisitor(ASM5, next) {
  override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean): Unit = {
    (owner, name, desc) match {
      case ("org/apache/commons/beanutils/BeanUtils", "populate", "(Ljava/lang/Object;Ljava/util/Map;)V") =>
        mv.visitMethodInsn(INVOKESTATIC, Owner.getVOps, "populate", "(Ljava/lang/Object;Ljava/util/Map;)V", false)
      case _ => super.visitMethodInsn(opcode, owner, name, desc, itf)
    }
  }
}
