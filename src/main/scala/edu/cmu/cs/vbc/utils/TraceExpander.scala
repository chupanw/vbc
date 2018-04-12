package edu.cmu.cs.vbc.utils

import java.io.FileWriter

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory, FeatureExprParser}

import scala.collection.mutable
import scala.io.Source

object TraceExpander extends App {
  val allLines = Source.fromFile("vtrace.txt").getLines().toList
  val feParser = new FeatureExprParser(featureFactory = FeatureExprFactory.bdd)
  var counter = 0
  var counter2 = 0
  val writer = new FileWriter("traces.txt")

  def getOneVTraces(lines: List[String]): (VTrace, List[String]) = {
    assert(lines(0).length == 0, "each trace should start with an empty line")
    assert(lines(1).length != 0, "second line of each trace should be non empty")
    assert(lines(2).startsWith("B"), "Third line of each trace should start with B")
    val (current, rest) = lines.tail.span(_.length != 0)
    val elements = current.tail.map(getVTraceElement)
    (VTrace(current(0), elements), rest)
  }

  def getVTraceElement(l: String): VTraceElement = {
    assert(l.startsWith("B"), s"vtrace line should start with B, not $l")
    val split = l.split(" ")
    assert(split.size == 2)
    val fe = feParser.parse(split(1))
    VTraceElement(split(0), fe)
  }

  type Config = (List[FeatureExpr], List[FeatureExpr])
  def explode(fs: Set[FeatureExpr]): List[Config] = {
    if (fs.isEmpty) List((Nil, Nil))
    else if (fs.size == 1) List((List(fs.head), Nil), (Nil, List(fs.head)))
    else {
      val r = explode(fs.tail)
      r.map(x => (fs.head :: x._1, x._2)) ++ r.map(x => (x._1, fs.head :: x._2))
    }
  }

  def explodeCombinations(FEs: Set[FeatureExpr]): List[FeatureExpr] = {
    if (FEs.size > 10)
      return List()
    val configs = explode(FEs)
    configs.map {x =>
      val enabled: FeatureExpr = x._1.foldLeft(FeatureExprFactory.True)((disjunction, f) => disjunction.and(f))
      val disabled: FeatureExpr = x._2.foldLeft(FeatureExprFactory.True)((disjunction, f) => disjunction.and(f.not()))
      enabled.and(disabled)
    }
  }

  def explodeVTrace(vtrace: VTrace, configs: List[FeatureExpr], writer: FileWriter): Unit = {
    println(s"exploding the $counter2-th vtrace")
    counter2 += 1
    if (configs.isEmpty) {
      System.err.println("error: too many options, giving up...")
      println(vtrace)
      return
    }
    val traces: List[String] = configs.map { c =>
      val sb = new mutable.StringBuilder()
      vtrace.elements.map(x => if (x.fe.and(c).isSatisfiable) x.b else "").filter(_.length > 0).mkString(" ")
    }.distinct.filter(_.trim.length > 0)
    if (traces.size > 1) {
      writer.write("\n")
      writer.write(vtrace.mn + "\n")
      writer.write(vtrace.elements.size + "\n")
      for (t <- traces) {
        writer.write(t + "\n")
      }
    }
    else {
      System.err.println("error: " + vtrace.mn)
    }
  }

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)


//  val fe1 = feParser.parse("(def(TRANSPOSE)&def(DFS)&!def(MSTKRUSKAL)&def(Src)&!def(CONNECTED)&!def(UNDIRECTED)&((def(WEIGHTED)&def(DIRECTED)&def(BASE)&def(SHORTEST)&!def(BFS)&def(Src2))|(!def(SHORTEST)&def(DIRECTED)&def(BASE)&!def(BFS)&def(Src2)))&def(SrcProg)&def(NUMBER)&def(SEARCH)&!def(MSTPRIM)&def(STRONGLYCONNECTED))")
//  val feBase = feParser.parse("(((def(BFS)&def(WEIGHTED)&def(DIRECTED)&def(BASE)&def(SHORTEST)&def(Src2))|(!def(SHORTEST)&def(BFS)&def(DIRECTED)&def(BASE)&def(Src2)))&!def(DFS)&!def(MSTKRUSKAL)&def(Src)&!def(CONNECTED)&!def(UNDIRECTED)&!def(CYCLE)&def(SrcProg)&def(NUMBER)&def(SEARCH)&!def(MSTPRIM)&!def(STRONGLYCONNECTED))")
//  println(fe1.and(feBase).isContradiction)

  val allVTraces = scala.collection.mutable.ListBuffer[VTrace]()
  var lines = allLines
  while (lines.nonEmpty) {
    println(s"reading the $counter-th vtrace")
    counter += 1
    val (vt, rest) = getOneVTraces(lines)
    allVTraces += vt
    lines = rest
  }

  val validVTraces = allVTraces.filter(vt => !vt.isSameContext && !vt.isAllSingleContext)
  println(s"Needs to explode ${validVTraces.size} vTraces")
  validVTraces.foreach(t => explodeVTrace(t, explodeCombinations(t.FEs), writer))
  writer.close()
}

case class VTrace(mn: String, elements: List[VTraceElement]) {
  val simplifiedElements: List[VTraceElement] = elements.map(x => VTraceElement(x.b, x.fe.simplify(elements.head.fe)))
  val isSameContext: Boolean = simplifiedElements.forall(_.fe.equivalentTo(simplifiedElements.head.fe))
  val isAllSingleContext: Boolean = simplifiedElements.forall(_.fe.collectDistinctFeatureObjects.size <= 1)
//  val isContradictingMethodCtx: Boolean = simplifiedElements.exists(_.fe.equivalentTo(FeatureExprFactory.False))
  val FEs: Set[FeatureExpr] = simplifiedElements.map(_.fe).flatMap(_.collectDistinctFeatureObjects).toSet
}

case class VTraceElement(b: String, fe: FeatureExpr)
