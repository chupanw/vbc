package edu.cmu.cs.varex.mtbdd

import edu.cmu.cs.varex.mtbdd.MTBDDFactory._
import org.scalatest.FlatSpec

class ValueSpec extends FlatSpec {
  "A VValue" should "have a somewhat unique hashcode"

  it should "have a value"

  it should "override the equals method"

  it should "have a configuration space of TRUE"

  "NOVALUE" should "only equals to itself" in {
    assert(NOVALUE == NOVALUE)
    assert(NOVALUE != createValue(1))
    assert(NOVALUE != createValue("a"))
    assert(NOVALUE != createValue(true))
    assert(createValue(1) != NOVALUE)
    assert(createValue("a") != NOVALUE)
    assert(createValue(true) != NOVALUE)
  }
}
