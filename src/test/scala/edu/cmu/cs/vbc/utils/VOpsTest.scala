package edu.cmu.cs.vbc.utils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.varex.{V, VOps}
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class VOpsTest extends FunSuite {

  def getConfig(n: String) = FeatureExprFactory.createDefinedExternal(n)

  def getChoice(n: String, v1: java.lang.Integer, v2: java.lang.Integer) = V.choice(FeatureExprFactory.createDefinedExternal(n), v1, v2)

  test("whenEQ 1") {
    val v1 = getChoice("A", 1, 2)
    assert(VOps.whenEQ(v1) == FeatureExprFactory.False)
  }

  test("whenEQ 2") {
    val v1 = getChoice("A", 1, 0)
    assert(VOps.whenEQ(v1) == getConfig("A").not())
  }

  test("whenIEQ") {
    val v1 = getChoice("A", 2, 3)
    val v2 = getChoice("B", 3, 4)
    assert(VOps.whenIEQ(v1, v2, FeatureExprFactory.True) == getConfig("A").not().and(getConfig("B")))
  }
}
