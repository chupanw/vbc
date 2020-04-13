package edu.cmu.cs.vbc.prog

import edu.cmu.cs.vbc.DiffLaunchTestInfrastructure
import org.scalatest.funsuite.AnyFunSuite

class ExceptionTest extends AnyFunSuite with DiffLaunchTestInfrastructure {

  test("no throw") {
    method("noThrow")
  }

  test("no throw 2") {
    method("noThrow2")
  }

  test("throw in method") {
    method("throwInMethod")
  }

  test("throw in method 2") {
    method("throwInMethod2")
  }

  test("implicit throw") {
    method("implicitThrow")
  }

  test("should catch in the same method") {
    method("shouldCatchSameMethod")
  }

  test("should catch in the method") {
    method("shouldCatchInMethod")
  }

  test("should catch multiple exceptions in the same method") {
    method("shouldCatchMultiExceptionSameMethod")
  }

  test("should catch multiple exceptions in the method") {
    method("shouldCatchMultiExceptionInMethod")
  }

  test("should catch multiple exceptions in the same catch block in the same method") {
    method("shouldCatchMultiExceptionSameMethodSameCatch")
  }

  test("should catch multiple exceptions in the same catch block") {
    method("shouldCatchMultiExceptionInMethodSameCatch")
  }

  test("should not catch in the same method") {
    method("shouldNotCatchSameMethod")
  }

  test("should not catch in the method") {
    method("shouldNotCatchInMethod")
  }

  test("should catch same method and finally") {
    method("shouldCatchSameMethodWithFinally")
  }

  test("should catch in method with finally") {
    method("shouldCatchInMethodWithFinally")
  }

  test("exception caused by fields") {
    method("exceptionWithField")
  }

  test("test happy path") {
    method("testHappyPath")
  }

  def method(mn: String): Unit = {
    import org.objectweb.asm.Opcodes._
    testStaticMethod("ExceptionTest_" + mn) { mv =>
      mv.visitTypeInsn(NEW, "edu/cmu/cs/vbc/prog/ExceptionExample")
      mv.visitInsn(DUP)
      mv.visitMethodInsn(INVOKESPECIAL,
        "edu/cmu/cs/vbc/prog/ExceptionExample",
        "<init>",
        "()V",
        false)
      mv.visitMethodInsn(INVOKEVIRTUAL, "edu/cmu/cs/vbc/prog/ExceptionExample", mn, "()V", false)
      mv.visitInsn(RETURN)
    }
  }
}
