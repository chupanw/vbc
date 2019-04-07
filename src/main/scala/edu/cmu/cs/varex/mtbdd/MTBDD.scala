package edu.cmu.cs.varex.mtbdd

/**
  * Base type of all nodes in a multi-terminal BDD
  *
  * Covariance so that we can use MTBDD[Nothing] to represent non-existing values
  */
trait MTBDD[+T] extends Serializable {
  val degree: Int
  def configSpace: MTBDD[Boolean]
  /**
    * Restrict to a partial configuration space
    *
    * @param ctx Restricting context
    * @return
    */
  def select(ctx: MTBDD[Boolean]): MTBDD[T]
  def map[U](f: T => U): MTBDD[U]
  def flatMap[U](f: T => MTBDD[U]): MTBDD[U]
  def when(f: T => Boolean): MTBDD[Boolean] = map(f)
  /**
    * Unlike [[when]], this method maps NOVALUE to false instead of NOVALUE
    */
  def whenCondition(f: T => Boolean): MTBDD[Boolean]

  //update a value in a context
  def set[U >: T](ctx: MTBDD[Boolean], value: U): MTBDD[U]

  //overwrites the current value with the given value in the (partial) configuration space in which value is defined
  def overwrite[U >: T](value: MTBDD[U]): MTBDD[U]
  /**
    * Merge two MTBDDs into one, assuming there is no conflict
    *
    * @param that The other MTBDD to be merged
    * @tparam U Reflexive supertype of current type
    * @return The merged MTBDD
    */
  def union[U >: T](that: MTBDD[U]): MTBDD[U]

  def toDot: String
}

/**
  * Non-terminal node
  */
trait Node[+T] extends MTBDD[T] {
  val v: Int
  val degree: Int
  def low: MTBDD[T]
  def high: MTBDD[T]
}

/**
  * Terminal nodes
  *
  * @todo should all VValues have the config space of TRUE?
  */
trait Value[+T] extends MTBDD[T] {
  val degree: Int = 0
  val value: T
}
