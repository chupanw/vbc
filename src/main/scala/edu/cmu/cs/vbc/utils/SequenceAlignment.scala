package edu.cmu.cs.vbc.utils

object SequenceAlignment {
  type Alignment = List[(String, String, Int)]
  type AlignmentReport = (Int, Alignment)
  object Align2 {
    val GAP_COST = 2
    val MATCH_COST = 0
    val NONMATCH_COST = 1000000000
    type CostMatrix = Array[Array[Int]]

    def align(seq1: List[String], seq2: List[String]): AlignmentReport = {
      val cost = costMatrix(seq1, seq2)
      (cost(0)(0), alignment(seq1, seq2, cost))
    }

    def emptyCostMatrix(s1: Int, s2: Int): CostMatrix = {
      var m = new CostMatrix(s1 + 1)
      for (i <- s1 to (0, -1)) {
        m(i) = new Array[Int](s2 + 1)
        if (i == s1) {
          // Right side
          for (j <- s2 to (0, -1)) {
            m(i)(j) = (s2 - j)*GAP_COST
          }
        } else {
          // Fill across bottom
          m(i)(s2) = (s1 - i)*GAP_COST
        }
      }
      m
    }
    def costMatrix(seq1: List[String], seq2: List[String]): CostMatrix = {
      var m = emptyCostMatrix(seq1.length, seq2.length)

      for (i <- seq1.length - 1 to (0, -1);
           j <- seq2.length - 1 to (0, -1)) {
        val cell = calc_cell((seq1(i), seq2(j)), m(i + 1)(j), m(i)(j + 1), m(i + 1)(j + 1))
        m(i)(j) = cell
      }
      m
    }
    def calc_cell(letters: (String, String), rightCell: Int, downCell: Int, diagCell: Int): Int = {
      List(rightCell + 2, downCell + 2, diagCell + penalty(letters)).min
    }
    def penalty(letters: (String, String)): Int = {
      letters match {
        case (a, b) if a == b => MATCH_COST
        case ("-", _) | (_, "-") => GAP_COST
        case _ => NONMATCH_COST
      }
    }

    def alignment(seq1: List[String], seq2: List[String], cost: CostMatrix): Alignment = {
      val padded_seq1 = seq1 :+ "-"
      val padded_seq2 = seq2 :+ "-"
      var aligned = List.empty[(String, String, Int)]
      var i = 0
      var j = 0
      while (i < seq1.length && j < seq2.length) {
        val a = cost(i)(j)
        val cost_right = cost(i+1)(j) - a + 2
        val cost_down = cost(i)(j+1) - a + 2
        val cost_match = penalty(padded_seq1(i), padded_seq2(j))
        val cost_diag = cost(i+1)(j+1) - a + cost_match
        val min_cost = List(cost_right, cost_down, cost_diag).min

        aligned = aligned :+ (min_cost match {
          case `cost_right` =>
            val res = (seq1(i), "-", GAP_COST)
            i += 1
            res
          case `cost_down` =>
            val res = ("-", seq2(j), GAP_COST)
            j += 1
            res
          case `cost_diag` =>
            val res = (seq1(i), seq2(j), cost_match)
            i += 1
            j += 1
            res
        })
      }
      while (i < seq1.length) {
        aligned = aligned :+ (seq1(i), "-", GAP_COST)
        i += 1
      }
      while (j < seq2.length) {
        aligned = aligned :+ ("-", seq2(j), GAP_COST)
        j += 1
      }
      aligned
    }
  }
  def format(alignment: AlignmentReport): String = {
    alignment._2.foldLeft("")((s, el) => s + s"${el._1}\t${el._2}\t${el._3}\n") + s"-----\n${alignment._1}\n"
  }

  def alignAll(seqs: Set[List[String]], to: List[String]): Set[AlignmentReport] = {
    seqs.map(Align2.align(_, to))
  }




  var serial = 0
  // TraceSet := A set of traces belonging to a single method invocation
  case class TraceSet(file: String, name: String, vTraceLen: Int, traces: List[List[String]]) {
    val id: Int = {serial += 1; serial}
  }
  // TraceAlignmentSet := An alignment of TraceSet TS
  case class TraceAlignmentSet(ts: TraceSet, alignments: Set[AlignmentReport])
  // AlignmentComparison := An alignment comparison of REPORT w.r.t the TraceAlignment to which it belongs
  case class AlignmentComparison(ts: TraceSet, report: AlignmentReport) {
    val alignLen: Int = report._2.size
    val alignmentShorterThanVTrace: Boolean = alignLen < ts.vTraceLen
  }
  case class TraceComparisonSet(ts: TraceSet, comparisons: List[AlignmentComparison], allAlignmentsShorterThanVTrace: Boolean)
  // TraceComparisonSet := Just like TraceAlignmentSet, but instead of a set of alignments have a set of
  // AlignmentComparisons of alignments to the vTrace of the TraceSet
//  case class TraceComparisonSet(ta: TraceAlignmentSet, comparisons: Set[AlignmentComparison])




  def factorial(n: Int, total: Int = 1): Int = {
    if(n < 2) total
    else factorial(n-1, n*total)
  }

  def alignAll(ts: TraceSet): TraceComparisonSet = {
    val seqs = ts.traces
    var aligned = collection.mutable.Set.empty[(List[String], List[String])]
    def alreadyAligned(a: List[String], b: List[String]) = {
      aligned.contains((a, b)) || aligned.contains((b, a))
    }
    var alignCount = seqs.size*(seqs.size - 1)/2
    var allAlignmentsShorterThanVTrace = true
    println(s"$alignCount alignments to perform...")
    val comparisons = for { s1 <- seqs
          s2 <- seqs.diff(List(s1)) if !alreadyAligned(s1, s2) }
      yield {
        aligned.add((s1, s2))
        alignCount -= 1
        val alignment = Align2.align(s1, s2)
//        println(s"Alignment done. $alignCount pairs left")
        val ac = AlignmentComparison(ts, alignment)
        if (!ac.alignmentShorterThanVTrace) allAlignmentsShorterThanVTrace = false
        ac
      }
    TraceComparisonSet(ts, comparisons, allAlignmentsShorterThanVTrace)
  }

  def splitToList(s: String): List[String] = {
    s.split(" ").toList
  }
  def matchToTraceSet(file: String, groups: List[String]): TraceSet = {
    TraceSet(file, groups(0), groups(1).toInt, groups(2).split("\n").map(splitToList).toList)
  }
  def parseTraces(where: String): Iterator[TraceSet] = {
    val pat = """(?m)([^\n]+)\n(\d+)\n(B[B\s\d]+\n)+\n""".r
    val source = scala.io.Source.fromFile(where)
    // getLines removes blank lines, mkString doesn't add trailing newline
    val content = try source.getLines.mkString("\n") + "\n" finally source.close()
    pat.findAllIn(content).matchData.map(m => matchToTraceSet(where, m.subgroups))
  }
  def reportVTraceOptimality(comparisonSets: List[TraceComparisonSet]): List[TraceComparisonSet] = {
    // maybe actually the useful metric to report is not the number of traces that are less than the vtrace,
    // but rather the number of tracesets for which ALL trace alignments are less than the vtrace
    // -> that would be the real problem, right?
    var shorterAlignmentSets = List.empty[TraceComparisonSet]
    for (cSet <- comparisonSets) {
      println(s"${cSet.ts.file}: ${cSet.ts.name} (length ${cSet.ts.vTraceLen})")
      cSet.comparisons.foreach(ac =>
        println(s"Trace pair: ${ac.alignLen}\t<?\t${cSet.ts.vTraceLen}\t- ${ac.alignmentShorterThanVTrace}"))
      println("\n")
      // All of the pairwise alignments are shorter than the vTrace
      if (cSet.allAlignmentsShorterThanVTrace) shorterAlignmentSets :+= cSet
//      if (cSet.comparisons.count(_.alignmentShorterThanVTrace) != 0) shorterAlignmentSets :+= cSet
    }
    shorterAlignmentSets
  }

  def main(args: Array[String]): Unit = {
    val test1 = List("a", "c", "a", "cc", "aa", "c", "a", "c")
    val test2 = List("c", "a", "cc", "aa", "c", "a", "c", "a")
    val files = List(
//      "/home/lukas/projects/cmu/varexc-paper/gpl-traces.txt"
//      "/home/lukas/projects/cmu/varexc-paper/elevator-traces.txt"
//      "/home/lukas/projects/cmu/varexc-paper/queval-traces.txt"
      "/home/lukas/projects/cmu/varexc-paper/checkstyle-traces-truncated.txt"
    )
    var subOptimalCount = 0
    var processedCount = 0
    val aligned = for { file <- files
                        traceSet <- { println(s"Processing file $file"); parseTraces(file) }
    } yield {
      println(s"(${traceSet.id}) Aligning traces for $file: ${traceSet.name}")
      val tcs = alignAll(traceSet)
      processedCount += 1
      if (tcs.allAlignmentsShorterThanVTrace) {
        subOptimalCount += 1
        println(s"\n* Trace set ${traceSet.id} is suboptimal. *\n")
      }
      println(s"> $subOptimalCount/$processedCount trace sets found suboptimal so far. <")
      tcs
    }
    val setsWithOnlyShorterAlignments = reportVTraceOptimality(aligned)
    def containsDups(l: List[Any]): Boolean = {
      l.distinct.size != l.size
    }
    def tracesContainDups(ac: AlignmentComparison): Boolean = {
      val t1 = ac.report._2.map(_._1)
      val t2 = ac.report._2.map(_._2)
      containsDups(t1) || containsDups(t2)
    }
    println("Checking for loop sets..")
    val shorterAlignmentSetsWithLoops = setsWithOnlyShorterAlignments.filter(tcs => tcs.comparisons.exists(tracesContainDups))
    println(s"\n\n----- Summary -----\nNumber of set alignments shorter than vTrace: ${setsWithOnlyShorterAlignments.size}/${aligned.size} (${shorterAlignmentSetsWithLoops.size} are loops)")
    println(s"\n\nSet alignments shorter than vTrace:\n${setsWithOnlyShorterAlignments.map(_.ts.name).mkString("\n")}")
  }
}

