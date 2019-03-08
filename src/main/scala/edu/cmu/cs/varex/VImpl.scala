package edu.cmu.cs.varex
import java.util.function
import java.util.function.{BiConsumer, BiFunction, Consumer, Predicate}

import de.fosd.typechef.featureexpr.FeatureExpr
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr
import edu.cmu.cs.varex.mtbdd.MTBDDFactory.NOVALUE
import edu.cmu.cs.varex.mtbdd.{MTBDD, MTBDDFactory}


class VImpl[T] (val values: MTBDD[T]) extends V[T] {

  override def getOne: T = {
    val allValues = MTBDDFactory.valueIterator(values)
    val ret = allValues.head
    if (allValues.size > 1) throw new RuntimeException("calling getOne on a choice")
    else ret
  }

  override def map[U](fun: function.Function[_ >: T, _ <: U]): V[_ <: U] =
    if (values == NOVALUE) new VImpl[U](NOVALUE) else new VImpl[U](values.map(x => fun.apply(x)))

  //perf
  override def map[U](fun: BiFunction[FeatureExpr, _ >: T, _ <: U]): V[_ <: U] =
    if (values == NOVALUE) new VImpl[U](NOVALUE) else new VImpl[U](values.map(x => fun.apply(new BDDFeatureExpr(values.whenCondition(y => y == x)), x)))

  override def flatMap[U](fun: function.Function[_ >: T, V[_ <: U]]): V[_ <: U] = {
    if (values == NOVALUE) new VImpl[U](NOVALUE) else new VImpl[U](values.flatMap(x => fun.apply(x).asInstanceOf[VImpl[U]].values))
  }

  //perf
  override def flatMap[U](fun: BiFunction[FeatureExpr, _ >: T, V[_ <: U]]): V[_ <: U] = {
    if (values == NOVALUE) new VImpl[U](NOVALUE) else new VImpl[U](values.flatMap(x => fun.apply(new BDDFeatureExpr(values.whenCondition(y => y == x)), x).asInstanceOf[VImpl[U]].values))
  }

  override def foreach(fun: Consumer[T]): Unit = if (values != NOVALUE) MTBDDFactory.foreachValue[T](values, x => fun.accept(x))

  //perf
  override def foreach(fun: BiConsumer[FeatureExpr, T]): Unit =
    if (values != NOVALUE) MTBDDFactory.foreachValue[T](values, x => fun.accept(new BDDFeatureExpr(values.whenCondition(y => y == x)), x))

  //perf
  override def foreachExp(fun: BiConsumerExp[FeatureExpr, T]): Unit =
    if (values != NOVALUE) MTBDDFactory.foreachValue[T](values, x => fun.accept(new BDDFeatureExpr(values.whenCondition(y => y == x)), x))

  override def when(condition: Predicate[T], filterNull: Boolean): FeatureExpr = {
    new BDDFeatureExpr(values.whenCondition(x => if (filterNull) x != null && condition.test(x) else condition.test(x)))
  }

  override def select(configSpace: FeatureExpr): V[T] = reduce(configSpace)

  override def reduce(reducedConfigSpace: FeatureExpr): V[T] =
    if (values == NOVALUE) new VImpl[T](NOVALUE) else new VImpl[T](values.select(reducedConfigSpace.bdd))

  override def getConfigSpace: FeatureExpr = new BDDFeatureExpr(values.configSpace)

  override def equalValue(o: Any): Boolean = ???

  override def hasThrowable: Boolean = {
    var found = false
    MTBDDFactory.foreachValue[T](values, x => found = found || x.isInstanceOf[Throwable])
    found
  }

  override def simplified(): V[T] = ???

  override def restrictInteractionDegree(): V[T] = ???

  def getOneValue(): One[T] = ???

  /* Debugging */
  override def toString: String = values.toString
}

object VImpl {
  def choice[T](context: FeatureExpr, a: T, b: T): VImpl[T] = new VImpl[T](MTBDDFactory.createChoice(context.bdd, a, b))
  def choiceV[T](context: FeatureExpr, a: V[T], b: V[T]): VImpl[T] =
    new VImpl[T](MTBDDFactory.createChoice(context.bdd, a.asInstanceOf[VImpl[T]].values, b.asInstanceOf[VImpl[T]].values))
}

class One[T] (val value: MTBDD[T]) extends VImpl[T](value) {
  def this(context: FeatureExpr, v: T) = this(MTBDDFactory.createValue(v).select(context.bdd))
}
