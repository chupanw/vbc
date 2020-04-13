package edu.cmu.cs.vbc.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ExceptionHandlerAnalyzerTest extends AnyFlatSpec with Matchers {

  "The analyzer" should "return nothing for Thread#setPriority" in {
    assertResult(Set()) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.lang.Thread", "setPriority", false)
    }
  }

  it should "return URISyntaxException for File#toURI" in {
    assertResult(Set("java.net.URISyntaxException")) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.io.File", "toURI", false)
    }
  }

  it should "return NoSuchMethodException and InvocationTargetException for Class#newInstance" in {
    assertResult(Set("java.lang.NoSuchMethodException", "java.lang.reflect.InvocationTargetException")) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.lang.Class", "newInstance", false)
    }
  }

  it should "return InvocationTargetException, NoSuchMethodException, and IllegalAccessException for Class#getEnumConstantsShared" in {
    assertResult(Set("java.lang.reflect.InvocationTargetException", "java.lang.NoSuchMethodException", "java.lang.IllegalAccessException")) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.lang.Class", "getEnumConstantsShared", false)
    }
  }

  it should "return Throwable for Thread#start because of finally" in {
    assertResult(Set("java.lang.Throwable")) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.lang.Thread", "start", false)
    }
  }

  it should "return Throwable for Thread#start because it can handle two Throwable" in {
    assertResult(Set("java.lang.Throwable")) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.lang.Thread", "start", true)
    }
  }

  it should "return Throwable for Thread#interrupt because of synchronized" in {
    assertResult(Set("java.lang.Throwable")) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.lang.Thread", "interrupt", false)
    }
  }

  it should "return nothing for Thread#stop even though the method declaration has synchronized" in {
    assertResult(Set()) {
      ExceptionHandlerAnalyzer.analyzeMethod("java.lang.Thread", "stop", false)
    }
  }
}
