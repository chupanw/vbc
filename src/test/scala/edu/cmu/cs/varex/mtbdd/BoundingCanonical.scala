package edu.cmu.cs.varex.mtbdd

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.GlobalConfig
import org.scalatest.FunSuite

class BoundingCanonical extends FunSuite {

  val options: List[FeatureExpr] = List("A", "B", "C", "D").map(feature)

  def feature(s: String): FeatureExpr = FeatureExprFactory.createDefinedExternal(s)

  def changeDegree(d: Int): Unit = {
    GlobalConfig.maxInteractionDegree = d
    MTBDDFactory.clearCache()
  }

  def exprGen(): List[FeatureExpr] = {
    val e1 = options ::: options.map(_.not())
    val e2 = (for (a <- e1; b <- e1) yield List(a & b, a | b)).flatten
    val e3 = (for (a <- e2; b <- e2) yield List(a & b, a | b)).flatten
    val e4 = e3 ::: e3.map(!_)
    val e5 = (for (a <- e4; b <- e1) yield List(a & b, a | b)).flatten
    e1 ::: e2 ::: e3 ::: e4 ::: e5
  }

  def cnfExprGen(): Seq[FeatureExpr] = {
    def exp(opts: List[FeatureExpr]): Seq[FeatureExpr] = {
      if (opts.size == 1) Seq(opts.head, !opts.head)
      else {
        val head = opts.head
        val ret = exp(opts.tail)
        ret.map(head & _) ++ ret.map(!head & _)
      }
    }
    val clauses = exp(options)
    (for (i <- 1 to clauses.size) yield clauses.combinations(i).map(_.reduce(_ or _))).flatten
  }

  /**
    * Stole from @ckaestne
    */
  test("All expressions with same truth table within bound share exactly same bdd structure") {
    for (i <- 1 to 4) {
      println(s"checking bound $i")
      changeDegree(i)
      val exprs = exprGen() ++ cnfExprGen()
      val bookkeeping: collection.mutable.Map[String, FeatureExpr] = collection.mutable.Map()

      for (e <- exprs) {
        val allSolutionsAsString = e.getAllSolutionsSortedScala.mkString(",")
        val existing = bookkeeping.get(allSolutionsAsString)
        if (existing.isDefined)
          assert(existing.get.bdd eq e.bdd, s"Equivalence check failed for ${existing.get} and $e")
        bookkeeping += allSolutionsAsString -> e
      }
    }
  }

}
