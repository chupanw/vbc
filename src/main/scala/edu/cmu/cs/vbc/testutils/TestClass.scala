package edu.cmu.cs.vbc.testutils

import java.lang.reflect.Method

case class TestClass(c: Class[_]) {

  def getTestCases: List[Method] = c.getMethods.toList.filter {x =>
    x.isAnnotationPresent(classOf[org.junit.Test])
  }
}
