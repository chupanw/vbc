package edu.cmu.cs.varex.mtbdd

import edu.cmu.cs.vbc.GlobalConfig

import scala.collection.immutable.Queue
import scala.collection.mutable
import scala.ref.WeakReference

/**
  * Scala implementation of the MTBDD library
  *
  **/
object MTBDDFactory {
  private val valueCache: mutable.WeakHashMap[Any, WeakReference[Value[Any]]] = new mutable.WeakHashMap()
  private val notCache: mutable.WeakHashMap[MTBDD[Boolean], WeakReference[MTBDD[Boolean]]] = new mutable.WeakHashMap()
  private val boolOpCache: mutable.WeakHashMap[(String, MTBDD[Boolean], MTBDD[Boolean]), WeakReference[MTBDD[Boolean]]] = new mutable.WeakHashMap()

  /**
    * Clear all cache of BDDs
    *
    * Need to clear cache after changing [[GlobalConfig.maxInteractionDegree]] at runtime, which
    * should not happen in most cases. Mainly useful for testing.
    */
  private[cs] def clearCache(): Unit = {
    notCache.clear()
    boolOpCache.clear()
    BooleanNode.clearCache()
  }

  abstract class MTBDDImpl[+T] extends MTBDD[T] {

    override def select(ctx: MTBDD[Boolean]): MTBDD[T] = apply[Boolean, T, T]((ctx, v) => ctx match {
      case NOVALUE => NOVALUE
      case ctx: Value[Boolean] => if (ctx.value) v else NOVALUE
    }, ctx, this)

    override def configSpace: MTBDD[Boolean] = mapValue[T, Boolean](this, x => createValue(x != NOVALUE))

    override def map[U](f: T => U): MTBDD[U] = MTBDDFactory.map(this, f)

    override def flatMap[U](f: T => MTBDD[U]): MTBDD[U] = MTBDDFactory.flatMap(this, f)

    //update a value in a context
    override def set[U >: T](ctx: MTBDD[Boolean], value: U): MTBDD[U] = overwrite(createValue[U](value).select(ctx))

    //overwrites the current value with the given value in the (partial) configuration space in which value is defined
    override def overwrite[U >: T](value: MTBDD[U]): MTBDD[U] = apply[T, U, U](
      (oldV, newV) => if (newV == NOVALUE) oldV else newV,
      this,
      value
    )

    override def whenCondition(f: T => Boolean): MTBDD[Boolean] =
      mapValue[T, Boolean](this, x => if (x == NOVALUE) FALSE else createValue(f(x.value)))

    override def toDot: String = {
      def nodeId(b: MTBDD[T]): String  = Math.abs(b.hashCode).toString + System.identityHashCode(b).toString
      val sb = new mutable.StringBuilder()

      sb.append("digraph G {\n")

      foreachNode[T](this, {
        case b: Value[T] =>
          val content = if (b == NOVALUE) "EMPTY" else b.value
          sb.append(nodeId(b) + " [shape=box, label=\"" + content + "\", style=filled, shape=box, height=0.3];\n")
        case b: Node[T] =>
          sb.append(nodeId(b) + " [label=\"" + features.find(_._2 == b.v).map(_._1).getOrElse("unknown_" + b.v) + "\"];\n")
          sb.append(nodeId(b) + " -> " + nodeId(b.low) + " [style=dotted];\n")
          sb.append(nodeId(b) + " -> " + nodeId(b.high) + " [style=filled];\n")
      })
      sb.append("}")
      sb.toString()
    }
  }

  /**
    * Concrete implementation of [[Node]]
    *
    * @param v  Index of the variable that represents current decision
    * @param low  Subtree if this variable is false
    * @param high Subtree if this variable is true
    */
  private class NodeImpl[+T](val v: Int, val low: MTBDD[T], val high: MTBDD[T]) extends MTBDDImpl[T] with Node[T] {
    /**
      * Used for debugging or testing, no paths contain more than [[GlobalConfig.maxInteractionDegree]] true edges
      */
    def checkDegree: Boolean = {
      def go(n: MTBDD[T], degree: Int): Boolean = {
        n match {
          case n: NodeImpl[T] => degree <= GlobalConfig.maxInteractionDegree && go(n.low, degree) && go(n.high, degree + 1)
          case v: ValueImpl[T] => if (degree <= GlobalConfig.maxInteractionDegree) true else !v.value.asInstanceOf[Boolean]
        }
      }
      go(this, 0)
    }

    /**
      * @todo Not used
      */
    lazy val degree: Int = maxHighDegree

    /**
      * Maximum number of high edges among all paths.
      *
      * This is also how bounding is done right now in [[BooleanNode.boundedApplyBoolean()]]
      */
    lazy val maxHighDegree: Int = (low, high) match {
      case (l: NodeImpl[Boolean], h: NodeImpl[Boolean]) => Math.max(low.degree, high.degree + 1)
      case (l: ValueImpl[Boolean], h: ValueImpl[Boolean]) => if (h.value) 1 else 0
      case (l: NodeImpl[Boolean], h: ValueImpl[Boolean]) => if (h.value) Math.max(l.degree, 1) else low.degree
      case (l: ValueImpl[Boolean], h: NodeImpl[Boolean]) => h.degree + 1
      case _ => throw new RuntimeException("Only support bounding on MTBDD[Boolean]")
    }

    /**
      * Minimum number of high edges toward TRUE
      *
      * Not sure if this definition is useful.
      * This allows complicated paths toward FALSE. Those paths are unlikely unnecessary, as we do not
      * care about configuration spaces that need more than [[GlobalConfig.maxInteractionDegree]] options.
      *
      * Work only for MTBDD[Boolean]
      */
    lazy val min2TrueDegree: Int = (low, high) match {
      case (l: NodeImpl[Boolean], h: NodeImpl[Boolean]) => Math.min(low.degree, high.degree + 1)
      case (l: ValueImpl[Boolean], h: ValueImpl[Boolean]) => if (h.value) 1 else 0
      case (l: NodeImpl[Boolean], h: ValueImpl[Boolean]) => if (h.value) Math.min(l.degree, 1) else low.degree
      case (l: ValueImpl[Boolean], h: NodeImpl[Boolean]) => if (l.value) 0 else high.degree + 1
      case _ => throw new RuntimeException("Only support bounding on MTBDD[Boolean]")
    }

    /**
      * Maximum number of high edges among all paths
      */
    def maxHighEdges(mtbdd: MTBDD[Boolean]) : Int = {
      def go(n: MTBDD[Boolean], degree: Int): Int = {
        n match {
          case n: NodeImpl[Boolean] => Math.max(go(n.low, degree), go(n.high, degree + 1))
          case v: ValueImpl[Boolean] =>
            // it's okay if the last high edges point to FALSE, that's how bounding works
            if (!v.value) degree - 1 else degree
        }
      }
      go(mtbdd, 0)
    }

//    assert(degree <= GlobalConfig.maxInteractionDegree, "True degree exceeded")
    override def union[U >: T](that: MTBDD[U]): MTBDD[U] = that match {
      case that: Value[U] => throw new RuntimeException("Not suppose to call union on VValue")
      case that: Node[U] =>
        apply[T, U, U](
          (left, right) => if (left == NOVALUE) right else if (right == NOVALUE) left else throw new VNodeException("attempting union of two overlapping configuration spaces"),
          this,
          that
        )
    }
    /**
      * Hashing could be more complicated if there are too many collisions. Check out
      * the discussion in Henrik Reif Andersen's lecture note
      */
    lazy val hash: Int = v + 31 * (if (low eq null) 0 else low.hashCode) + 27 * (if (high eq null) 0 else high.hashCode)

    override def equals(that: Any): Boolean = that match {
      case that: NodeImpl[T] => (this.v == that.v) && (this.low eq that.low) && (this.high eq that.high)
      case _ => false
    }

    /**
      * Transform the MTBDD to a mapping of values to conditions
      *
      * O(|V|) implementation, where |V| is the number of vertex
      *
      * @return mapping of concrete values to conditions
      */
    def toMap[U >: T]: Map[Value[U], String] = {
      def go(r: MTBDD[T], enabled: Set[Int], ordered: Queue[Int]): Map[Value[U], String] = r match {
        case v: Value[T] =>
          val cond = ordered.map(x => if (enabled contains x) lookupVarName(x) else "!" + lookupVarName(x)) mkString "&"
          Map(v -> s"($cond)")
        case n: Node[T] =>
          val ordered2 = ordered enqueue n.v
          val lowMap = go(n.low, enabled, ordered2)
          val highMap = go(n.high, enabled + n.v, ordered2)
          lowMap ++ highMap.map {case (key, value) => key -> {
            val x = lowMap.get(key)
            if (x.isDefined) x.get + "|" + value else value
          }}
      }
      go(this, Set(), Queue())
    }

    override def toString: String = toMap[T].toString
    override def hashCode: Int = hash
  }

  object BooleanNode {
    private val andCache: mutable.WeakHashMap[(MTBDD[Boolean], MTBDD[Boolean], Int), MTBDD[Boolean]] = mutable.WeakHashMap()
    private val andOp = (a: Value[Boolean], b: Value[Boolean]) => if (a.value && b.value) TRUE else FALSE
    private val orCache: mutable.WeakHashMap[(MTBDD[Boolean], MTBDD[Boolean], Int), MTBDD[Boolean]] = mutable.WeakHashMap()
    private val orOp = (a: Value[Boolean], b: Value[Boolean]) => if (a.value || b.value) TRUE else FALSE
    private val internalNotCache: mutable.WeakHashMap[(MTBDD[Boolean], Int), MTBDD[Boolean]] = mutable.WeakHashMap()
    private[cs] def clearCache(): Unit = {
      andCache.clear()
      orCache.clear()
      internalNotCache.clear()
    }
  }

  class BooleanNode(val s: MTBDD[Boolean]) {
    import BooleanNode._

    def not: MTBDD[Boolean] = lookupCache(notCache, s, boundedNot(s))

    def and(that: MTBDD[Boolean]): MTBDD[Boolean] =
      lookupCache(boolOpCache, ("AND", s, that), { boundedApplyBoolean(true, s, that) })

    def or(that: MTBDD[Boolean]): MTBDD[Boolean] =
      lookupCache(boolOpCache, ("OR", s, that), { boundedApplyBoolean(false, s, that) })

    override def toString: String = s match {
      case n: NodeImpl[Boolean] => n.toMap(TRUE)
      case v: ValueImpl[Boolean] => v.toString
    }

    def boundedNot(mtbdd: MTBDD[Boolean]): MTBDD[Boolean] = {
      val cache = internalNotCache

      def go(n: MTBDD[Boolean], degree: Int): MTBDD[Boolean] =
        if (cache.contains((n, degree))) cache((n, degree))
        else if (degree > GlobalConfig.maxInteractionDegree) FALSE
        else {
          val newNode = n match {
            case nt: Node[Boolean] => mk(nt.v, go(nt.low, degree), go(nt.high, degree + 1))
            case t: Value[Boolean] => createValue(!t.value)
          }
          cache += ((n, degree) -> newNode)
          newNode
        }

      go(mtbdd, 0)
    }


    def boundedApplyBoolean(isAnd: Boolean, left: MTBDD[Boolean], right: MTBDD[Boolean]): MTBDD[Boolean] = {
      val (cache, op) = if (isAnd) (andCache, andOp) else (orCache, orOp)

      def app(u1: MTBDD[Boolean], u2: MTBDD[Boolean], degree: Int): MTBDD[Boolean] = {
        val cached = cache.get((u1, u2, degree))
        if (cached.nonEmpty)
          return cached.get

        if (degree > GlobalConfig.maxInteractionDegree) FALSE
        else {
          val u = (u1, u2) match {
            case (u1: Value[Boolean], u2: Value[Boolean]) => op(u1, u2)
            case (u1: Node[Boolean], u2: Value[Boolean]) => mk(u1.v, app(u1.low, u2, degree), app(u1.high, u2, degree + 1))
            case (u1: Value[Boolean], u2: Node[Boolean]) => mk(u2.v, app(u1, u2.low, degree), app(u1, u2.high, degree + 1))
            case (u1: Node[Boolean], u2: Node[Boolean]) =>
              if (u1.v < u2.v) mk(u1.v, app(u1.low, u2, degree), app(u1.high, u2, degree + 1))
              else if (u1.v > u2.v) mk(u2.v, app(u1, u2.low, degree), app(u1, u2.high, degree + 1))
              else mk(u1.v, app(u1.low, u2.low, degree), app(u1.high, u2.high, degree + 1))
          }
          cache += ((u1, u2, degree) -> u)
          u
        }
      }

      app(left, right, 0)
    }

    /**
      * Get all bounded solutions, including variables that do not appear in the BDD
      *
      * We might get too many solutions this way, especially if there are a lot of unused variables
      */
    def allSat: List[String] = {
      def go(r: MTBDD[Boolean], enabled: Queue[Int], currentV: Int): List[List[String]] = if (enabled.size > GlobalConfig.maxInteractionDegree) Nil else r match {
        case v: Value[Boolean] =>
          if (currentV == varNum)
            if (v.value) List(enabled.map(lookupVarName).toList) else Nil
          else
            go(r, enabled, currentV + 1) ::: go(r, enabled enqueue currentV, currentV + 1)
        case n: Node[Boolean] =>
          if (n.v == currentV)
            go(n.low, enabled, currentV + 1) ::: go(n.high, enabled enqueue n.v, currentV + 1)
          else
            go(n, enabled, currentV + 1) ::: go(n, enabled enqueue currentV, currentV + 1)
      }
      val res = go(s, Queue(), 0)
      val sorted = res.sortBy(_.size).map(_.mkString("{", "&", "}"))
      sorted
    }

    def evaluate(enabled: Set[String]): Boolean = {
      def go(mtbdd: MTBDD[Boolean]): Boolean = mtbdd match {
        case TRUE => true
        case FALSE => false
        case node: Node[Boolean] => if (enabled contains lookupVarName(node.v)) go(node.high) else go(node.low)
      }
      go(s)
    }
  }

  implicit def boolOps(s: MTBDD[Boolean]): BooleanNode = new BooleanNode(s)

  class ValueImpl[T](val value: T) extends MTBDDImpl with Value[T] {
    override def configSpace: MTBDD[Boolean] = TRUE
    override def hashCode: Int = if (value != null) value.hashCode() else -1
    override def equals(that: Any): Boolean = that match {
      case that: Value[T] => (that != NOVALUE) && (this.value == that.value)
      case _ => super.equals(that)
    }
    override def union[U >: Nothing](that: MTBDD[U]): MTBDD[U] =
      throw new RuntimeException("Not suppose to call union on VValue")

    override def toString: String = if (value == null) "<NULL>" else value.toString
  }

  val TRUE: Value[Boolean] = createValue(true)
  val FALSE: Value[Boolean] = createValue(false)

  /**
    * Special [[Value]] object to indicate empty value
    *
    * Usually used when calling [[Node.select()]]
    */
  object NOVALUE extends Value[Nothing] {
    def error() = throw new RuntimeException("method does not exist for NOVALUE")
    lazy val value: Nothing = error()
    override def configSpace: MTBDD[Boolean] = error()
    override def select(ctx: MTBDD[Boolean]): MTBDD[Nothing] = error()
    override def map[U](f: Nothing => U): MTBDD[U] = error()
    override def flatMap[U](f: Nothing => MTBDD[U]): MTBDD[U] = error()
    override def set[U >: Nothing](ctx: MTBDD[Boolean], value: U): MTBDD[U] = error()
    override def overwrite[U >: Nothing](value: MTBDD[U]): MTBDD[U] = error()
    override def union[U >: Nothing](that: MTBDD[U]): MTBDD[U] = error()
    override def when(f: Nothing => Boolean): MTBDD[Boolean] = error()
    override def whenCondition(f: Nothing => Boolean): MTBDD[Boolean] = error()
    override def toDot: String = error()
    override def toString: String = "<EMPTY>"
  }


  private def lookupCache[K, V <: AnyRef](cache: mutable.WeakHashMap[K, WeakReference[V]], k: K, gen: => V): V = {
    val v = cache.get(k).flatMap(_.get)
    if (v.isDefined) v.get
    else {
      val x: V = gen
      cache.put(k, WeakReference.apply(x))
      x
    }
  }


  def createValue[T](x: T): Value[T] = {
    val v = valueCache.get(x).flatMap(_.get)
    if (v.isDefined) v.get.asInstanceOf[Value[T]]
    else {
      val xv = new ValueImpl[T](x)
      valueCache.put(x, WeakReference.apply[Value[Any]](xv))
      xv
    }
  }

  def createChoice[T](ctx: MTBDD[Boolean], a: MTBDD[T], b: MTBDD[T]): MTBDD[T] = ite(ctx, a, b)
  def createChoice[T](ctx: MTBDD[Boolean], a: T, b: T): MTBDD[T] = ite(ctx, createValue(a), createValue(b))

  //    a.select(ctx) union b.select(ctx.not)

  /**
    * Perform specified operation on two MTBDDs and get a new one as result
    *
    * @tparam A type of left operand
    * @tparam B type of right operand
    * @tparam C type of result
    */
  def apply[A, B, C](op: (Value[A], Value[B]) => Value[C], left: MTBDD[A], right: MTBDD[B]): MTBDD[C] = {
    var cache: Map[(MTBDD[A], MTBDD[B]), MTBDD[C]] = Map()

    def app(u1: MTBDD[A], u2: MTBDD[B]): MTBDD[C] = {
      val cached = cache.get((u1, u2))
      if (cached.nonEmpty)
        return cached.get

      val u = (u1, u2) match {
        case (u1: Value[A], u2: Value[B]) => op(u1, u2)
        case (u1: Node[A], u2: Value[B]) => mk(u1.v, app(u1.low, u2), app(u1.high, u2))
        case (u1: Value[A], u2: Node[B]) => mk(u2.v, app(u1, u2.low), app(u1, u2.high))
        case (u1: Node[A], u2: Node[B]) =>
          if (u1.v < u2.v) mk(u1.v, app(u1.low, u2), app(u1.high, u2))
          else if (u1.v > u2.v) mk(u2.v, app(u1, u2.low), app(u1, u2.high))
          else mk(u1.v, app(u1.low, u2.low), app(u1.high, u2.high))
      }

      cache += ((u1, u2) -> u)
      u
    }

    app(left, right)
  }


  def ite[T](f: MTBDD[Boolean], g: MTBDD[T], h: MTBDD[T]): MTBDD[T] = {
    var cache: Map[(MTBDD[Boolean], MTBDD[T], MTBDD[T]), MTBDD[T]] = Map()

    def ite_(f: MTBDD[Boolean], g: MTBDD[T], h: MTBDD[T]): MTBDD[T] = {
      val cached = cache.get((f, g, h))
      if (cached.nonEmpty)
        return cached.get
      if (g eq h) h
      else {
        f match {
          case f: Value[Boolean] => if (f == TRUE) g else h
          case f: Node[Boolean] =>
            // PERF Could have just do two select calls and then a union, but that is likely to be slow
            //  - if this happen too often, maybe we can make the references to leave nodes mutable and update them
            val result = (g, h) match {
              case (g: Value[T], h: Value[T]) => mk(f.v, ite_(f.low, g, h), ite_(f.high, g, h))
              case (g: Value[T], h: Node[T]) =>
                val min = Math.min(f.v, h.v)
                val (fa, fb) = if (f.v == min) (f.low, f.high) else (f, f)
                val (ha, hb) = if (h.v == min) (h.low, h.high) else (h, h)
                mk(min, ite_(fa, g, ha), ite_(fb, g, hb))
              case (g: Node[T], h: Value[T]) =>
                val min = Math.min(f.v, g.v)
                val (fa, fb) = if (f.v == min) (f.low, f.high) else (f, f)
                val (ga, gb) = if (g.v == min) (g.low, g.high) else (g, g)
                mk(min, ite_(fa, ga, h), ite_(fb, gb, h))
              case (g: Node[T], h: Node[T]) =>
                val min = List(f, g, h).map(_.v).min
                val (fa, fb) = if (f.v == min) (f.low, f.high) else (f, f)
                val (ga, gb) = if (g.v == min) (g.low, g.high) else (g, g)
                val (ha, hb) = if (h.v == min) (h.low, h.high) else (h, h)
                mk(min, ite_(fa, ga, ha), ite_(fb, gb, hb))
            }
            cache += ((f, g, h) -> result)
            result
        }
      }
    }
    ite_(f, g, h)
  }

  private def flatMap[T, U](node: MTBDD[T], f: T => MTBDD[U]): MTBDD[U] = flatMap[T, U](node, (_: MTBDD[Boolean], x: T) => f(x))

  private def flatMap[T, U](node: MTBDD[T], f: (MTBDD[Boolean], T) => MTBDD[U]): MTBDD[U] = {
    // PERF This is probably expensive
    val oldValueNodes = valueNodeIterator(node)
    var result: MTBDD[U] = null
    for (oldValueNode <- oldValueNodes; if oldValueNode != NOVALUE) {
      val ctx = node.when(_ == oldValueNode.value)
      val newValue = f(ctx, oldValueNode.value)
      result = if (result == null) newValue.select(ctx) else result union newValue.select(ctx)
    }
    result
  }

  private val bddTable: mutable.WeakHashMap[Node[_], WeakReference[Node[_]]] = new mutable.WeakHashMap()

  def mk[T](v: Int, low: MTBDD[T], high: MTBDD[T]): MTBDD[T] =
    if (low eq high)  // since we are caching all nodes, eq should be sufficient and faster
      low
    else {
      assert(v < features.size, "unknown variable id")
      val newNode = new NodeImpl[T](v, low, high)
      lookupCache(bddTable, newNode, newNode).asInstanceOf[Node[T]]
    }


  def not(bdd: MTBDD[Boolean]): MTBDD[Boolean] = lookupCache(notCache, bdd, map[Boolean, Boolean](bdd, x => !x))

  def map[T, U](bdd: MTBDD[T], f: T => U): MTBDD[U] =
    mapValue[T, U](bdd, x => if (x == NOVALUE) NOVALUE else createValue(f(x.value)))

  private def mapValue[T, U](bdd: MTBDD[T], f: Value[T] => Value[U]): MTBDD[U] = {
    var rewritten: Map[MTBDD[T], MTBDD[U]] = Map()

    def map_(n: MTBDD[T]): MTBDD[U] =
      if (rewritten contains n) rewritten(n)
      else {
        val newNode = n match {
          case nt: Node[T] => mk(nt.v, map_(nt.low), map_(nt.high))
          case t: Value[T] => f(t)
        }
        rewritten += (n -> newNode)
        newNode
      }

    map_(bdd)
  }

  def mapPair[A, B, C](a: MTBDD[A], b: MTBDD[B], f: (A, B) => C): MTBDD[C] =
    apply[A, B, C]((aa, bb) => createValue[C](f(aa.value, bb.value)), a, b)

  var features: mutable.Map[String, Int] = mutable.Map()
  private val featureIDs: mutable.Map[Int, String] = mutable.Map()
  def lookupVarName(id: Int): String = featureIDs.getOrElseUpdate(id, {
    val varNameOpt = features.find(_._2 == id)
    assert(varNameOpt.isDefined, s"Unknown variable id: $id")
    varNameOpt.get._1
  })

  def varNum = features.size

  private def getFeatureId(s: String) = features.getOrElseUpdate(s, features.size)

  def feature(s: String):Node[Boolean] = mk[Boolean](getFeatureId(s), FALSE, TRUE).asInstanceOf[Node[Boolean]]

  def foreachNode[T](bdd: MTBDD[T], f: MTBDD[T] => Unit): Unit = allNodes(bdd).foreach(f)

  def foreachValue[T](bdd: MTBDD[T], f: T => Unit): Unit = valueIterator(bdd).foreach(f)

  def allNodes[T](bdd: MTBDD[T]): List[MTBDD[T]] = new VNodeIterator[T](bdd).toList

  def valueIterator[T](bdd: MTBDD[T]): List[T] = allNodes(bdd).filter(x => x.isInstanceOf[Value[T]] && x != NOVALUE).map(_.asInstanceOf[Value[T]].value)

  def valueNodeIterator[T](bdd: MTBDD[T]): List[Value[T]] = allNodes(bdd).filter(_.isInstanceOf[Value[T]]).map(_.asInstanceOf[Value[T]])

  private class VNodeIterator[T](bdd: MTBDD[T]) extends Iterator[MTBDD[T]] {
    var seen: Set[MTBDD[T]] = Set()
    var queue: Queue[MTBDD[T]] = Queue() enqueue bdd

    override def hasNext = {
      while (queue.nonEmpty && (seen contains queue.head))
        queue = queue.dequeue._2
      queue.nonEmpty
    }

    override def next() = {
      while (queue.nonEmpty && (seen contains queue.head))
        queue = queue.dequeue._2

      val b = queue.dequeue._1
      seen = seen + b
      b match {
        case b: Node[T] => queue = queue enqueue List(b.low, b.high)
        case _ => // nothing
      }
      b
    }
  }
}

class VNodeException(msg: String) extends Exception(msg)