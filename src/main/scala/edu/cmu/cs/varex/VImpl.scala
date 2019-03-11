package edu.cmu.cs.varex
import java.util.function
import java.util.function.{BiConsumer, BiFunction, Consumer, Predicate}

import de.fosd.typechef.featureexpr.FeatureExpr
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr
import edu.cmu.cs.varex.mtbdd.MTBDDFactory.NOVALUE
import edu.cmu.cs.varex.mtbdd.{MTBDD, MTBDDFactory}

import scala.collection.immutable.Queue


class MTBDDVImpl[T] (val values: MTBDD[T]) extends V[T] {

  override def getOne: T = {
    val allValues = MTBDDFactory.valueIterator(values)
    val ret = allValues.head
    if (allValues.size > 1) throw new RuntimeException("calling getOne on a choice")
    else ret
  }

  override def map[U](fun: function.Function[_ >: T, _ <: U]): V[_ <: U] = {
    MTBDDVImpl.mapCnt += 1
    if (values == NOVALUE) new MTBDDVImpl[U](NOVALUE) else new MTBDDVImpl[U](values.map(x => fun.apply(x)))
  }

  //perf: hopefully faster now?
  override def map[U](fun: BiFunction[FeatureExpr, _ >: T, _ <: U]): V[_ <: U] = {
    MTBDDVImpl.mapCtxCnt += 1
    if (values == NOVALUE) new MTBDDVImpl[U](NOVALUE) else new MTBDDVImpl[U](values.map(x => fun.apply(new BDDFeatureExpr(values.whenCondition(y => y == x)), x)))
  }
//  override def map[U](fun: BiFunction[FeatureExpr, _ >: T, _ <: U]): V[_ <: U] = new VImpl[U](mapValueWithContext(values, fun))

  override def flatMap[U](fun: function.Function[_ >: T, V[_ <: U]]): V[_ <: U] = {
    MTBDDVImpl.flatMapCnt += 1
    if (values == NOVALUE) new MTBDDVImpl[U](NOVALUE) else new MTBDDVImpl[U](values.flatMap(x => fun.apply(x).asInstanceOf[MTBDDVImpl[U]].values))
  }

  //perf
//  override def flatMap[U](fun: BiFunction[FeatureExpr, _ >: T, V[_ <: U]]): V[_ <: U] = {
//    if (values == NOVALUE) new VImpl[U](NOVALUE) else new VImpl[U](values.flatMap(x => fun.apply(new BDDFeatureExpr(values.whenCondition(y => y == x)), x).asInstanceOf[VImpl[U]].values))
//  }
  override def flatMap[U](fun: BiFunction[FeatureExpr, _ >: T, V[_ <: U]]): V[_ <: U] = {
    MTBDDVImpl.flatMapCtxCnt += 1
    if (values == NOVALUE) new MTBDDVImpl[U](NOVALUE) else flatMapWithContext[U](this.values, fun)
  }

  override def foreach(fun: Consumer[T]): Unit = {
    MTBDDVImpl.foreachCnt += 1
    if (values != NOVALUE) MTBDDFactory.foreachValue[T](values, x => fun.accept(x))
  }

  //perf: let's hope its faster now
//  override def foreach(fun: BiConsumer[FeatureExpr, T]): Unit =
//    {VImpl.foreachCtxCnt += 1;if (values != NOVALUE) MTBDDFactory.foreachValue[T](values, x => fun.accept(new BDDFeatureExpr(values.whenCondition(y => y == x)), x))}
  override def foreach(fun: BiConsumer[FeatureExpr, T]): Unit = {
    MTBDDVImpl.foreachCtxCnt += 1
    foreachWithContext(this.values, fun)
  }

  //perf
  override def foreachExp(fun: BiConsumerExp[FeatureExpr, T]): Unit =
    if (values != NOVALUE) MTBDDFactory.foreachValue[T](values, x => fun.accept(new BDDFeatureExpr(values.whenCondition(y => y == x)), x))

  override def when(condition: Predicate[T], filterNull: Boolean): FeatureExpr = {
    new BDDFeatureExpr(values.whenCondition(x => if (filterNull) x != null && condition.test(x) else condition.test(x)))
  }

  override def select(configSpace: FeatureExpr): V[T] = reduce(configSpace)

  override def reduce(reducedConfigSpace: FeatureExpr): V[T] =
    if (values == NOVALUE) new MTBDDVImpl[T](NOVALUE) else new MTBDDVImpl[T](values.select(reducedConfigSpace.bdd))

  override def getConfigSpace: FeatureExpr = new BDDFeatureExpr(values.configSpace)

  override def equalValue(o: Any): Boolean = ???

  override def hasThrowable: Boolean = {
    var found = false
    MTBDDFactory.foreachValue[T](values, x => found = found || x.isInstanceOf[Throwable])
    found
  }

  override def simplified(): V[T] = ???

  override def restrictInteractionDegree(): V[T] = ???

  def getOneValue(): MTBDDOne[T] = ???

  /**
    * Similar to the mapValue private function in [[MTBDDFactory]], but provide context
    *
    * I'm hesitant to put this into [[MTBDDFactory]] as it has some Java stuff
    */
  private def mapValueWithContext[T, U](bdd: MTBDD[T], f: BiFunction[FeatureExpr, _ >: T, _ <: U]): MTBDD[U] = {
    import edu.cmu.cs.varex.mtbdd.{Node, Value, MTBDDFactory}

    def map_(n: MTBDD[T], path: Queue[(Int, Boolean)]): MTBDD[U] = n match {
      case nt: Node[T] => MTBDDFactory.mk(nt.v, map_(nt.low, path enqueue (nt.v, false)), map_(nt.high, path enqueue (nt.v, true)))
      case t: Value[T] =>
        if (t == NOVALUE) NOVALUE
        else {
          val ctx = new BDDFeatureExpr(constructFeatureExpr(path))
          MTBDDFactory.createValue(f.apply(ctx, t.value))
        }
    }
    map_(bdd, Queue())
  }

  private def flatMapWithContext[U](bdd: MTBDD[T], fun: BiFunction[FeatureExpr, _ >: T, V[_ <: U]]): V[_ <: U] = {
    import MTBDDFactory._

    val oldValueNodes = valueNodeIterator(bdd)
    var result: MTBDD[U] = null
    for (oldValueNode <- oldValueNodes; if oldValueNode != NOVALUE) {
      val ctx = bdd.whenCondition(_ == oldValueNode.value)
      val newValue = fun.apply(new BDDFeatureExpr(ctx), oldValueNode.value).asInstanceOf[MTBDDVImpl[U]].values
      result = if (result == null) newValue.select(ctx) else result union newValue.select(ctx)
    }
    new MTBDDVImpl[U](result)
  }

  private def foreachWithContext[T](bdd: MTBDD[T], fun: BiConsumer[FeatureExpr, T]): Unit = {
    import edu.cmu.cs.varex.mtbdd.{Node, Value}
    import scala.collection.mutable

    var delay = mutable.Map[Value[T], FeatureExpr]()

    def foreach_(n: MTBDD[T], path: Queue[(Int, Boolean)]): Unit = n match {
      case nt: Node[T] =>
        foreach_(nt.low, path enqueue (nt.v, false))
        foreach_(nt.high, path enqueue (nt.v, true))
      case t: Value[T] =>
        if (t != NOVALUE)
          delay += (t -> delay.getOrElse(t, new BDDFeatureExpr(MTBDDFactory.FALSE)).or(new BDDFeatureExpr(constructFeatureExpr(path))))
    }
    foreach_(bdd, Queue())
    delay.foreach(p => fun.accept(p._2, p._1.value))
  }

  private def constructFeatureExpr(path: Queue[(Int, Boolean)]): MTBDD[Boolean] = {
    import MTBDDFactory._
    if (path.isEmpty) return TRUE
    var (curVar, enabled) = path.head
    if (path.size > 1) {
      if (enabled) mk(curVar, FALSE, constructFeatureExpr(path.tail))
      else mk(curVar, constructFeatureExpr(path.tail), FALSE)
    } else {
      // last feature
      if (enabled) mk(curVar, FALSE, TRUE) else mk(curVar, TRUE, FALSE)
    }
  }

  /* Debugging */
  override def toString: String = values.toString
}

object MTBDDVImpl {
  def choice[T](context: FeatureExpr, a: T, b: T): MTBDDVImpl[T] = new MTBDDVImpl[T](MTBDDFactory.createChoice(context.bdd, a, b))
  def choiceV[T](context: FeatureExpr, a: V[T], b: V[T]): MTBDDVImpl[T] =
    new MTBDDVImpl[T](MTBDDFactory.createChoice(context.bdd, a.asInstanceOf[MTBDDVImpl[T]].values, b.asInstanceOf[MTBDDVImpl[T]].values))
  var mapCtxCnt = 0
  var mapCnt = 0
  var foreachCtxCnt = 0
  var foreachCnt = 0
  var flatMapCnt = 0
  var flatMapCtxCnt = 0
}

class MTBDDOne[T] (val value: MTBDD[T]) extends MTBDDVImpl[T](value) {
  def this(context: FeatureExpr, v: T) = this(MTBDDFactory.createValue(v).select(context.bdd))
}
