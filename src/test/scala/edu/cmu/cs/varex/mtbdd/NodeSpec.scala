package edu.cmu.cs.varex.mtbdd

import org.scalatest.FlatSpec

class NodeSpec extends FlatSpec {
  import MTBDDFactory._

  "A VNode" should "have no value" in {
  }

  it should "have an index"

  it should "have non-null left and right children"

  it should "have a configuration space of not TRUE or FALSE"

  it should "have a somewhat unique hashcode"

  it should "override the equals method"

  it should "have unique values" in {
    def countValue[T](v: MTBDD[T]): Int = valueNodeIterator(v).size

    val a = createChoice(feature("a"), 2, 1)
    assert(countValue(a) == 2)

    val a2 = a.map(x => 1)
    assert(countValue(a2) == 1)

    val ab = a.select(feature("b"))
    assert(countValue(ab) == 3) // including NOVALUE node

    val ab2 = ab.map(x => 1)
    assert(countValue(ab2) == 2)

    val ab3 = ab.map(x => x + 1)
    assert(countValue(ab3) == 3)

    val ab4 = a.flatMap(x => if (x == 1) createValue(1) else createChoice(feature("b"), 1, 2))
    assert(countValue(ab4) == 2)

    val ab5 = a.flatMap(x => if (x == 1) createValue(1) else createChoice(feature("b"), 2, 3))
    assert(countValue(ab5) == 3)

    val ab6 = ab5.map(x => if (x % 2 == 0) 1 else x)
    assert(countValue(ab6) == 2)
  }
}
