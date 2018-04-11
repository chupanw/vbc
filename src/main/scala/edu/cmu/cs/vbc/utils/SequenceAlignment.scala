package edu.cmu.cs.vbc.utils

object SequenceAlignment {
  object Align2 {
    val GAP_COST = 2
    val MATCH_COST = 0
    val NONMATCH_COST = 1
    type CostMatrix = Array[Array[Int]]

    def align(seq1: List[String], seq2: List[String]): (Int, List[(String, String, Int)]) = {
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

    def alignment(seq1: List[String], seq2: List[String], cost: CostMatrix): List[(String, String, Int)] = {
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
  def format(alignment: (Int, List[(String, String, Int)])): String = {
    alignment._2.foldLeft("")((s, el) => s + s"${el._1}\t${el._2}\t${el._3}\n") + s"-----\n${alignment._1}\n"
  }

  def alignAll(seqs: Set[List[String]], to: List[String]): Set[(Int, List[(String, String, Int)])] = {
    seqs.map(Align2.align(_, to))
  }
  def alignAll(seqs: Set[List[String]]): Set[(Int, List[(String, String, Int)])] = {
    for { s1 <- seqs
          s2 <- seqs.diff(Set(s1)) }
      yield Align2.align(s1, s2)
  }

  def splitToList(s: String): List[String] = {
    s.split(" ").toList
  }

  type TraceSet = (String, Int, List[List[String]])

  def matchToTraceSet(groups: List[String]): TraceSet = {
    (groups(0), groups(1).toInt, groups(2).split("\n").map(splitToList).toList)
  }
  def parseTraces(where: String): Iterator[TraceSet] = {
    val pat = """(?m)([^\n]+)\n(\d+)\n(B[B\s\d]+\n)+\n""".r
    val source = scala.io.Source.fromFile(where)
    // getLines removes blank lines, mkString doesn't add trailing newline
    val content = try source.getLines.mkString("\n") + "\n" finally source.close()
    pat.findAllIn(content).matchData.map(m => matchToTraceSet(m.subgroups))
  }

  def main(args: Array[String]): Unit = {
    val test1 = List("a", "c", "a", "cc", "aa", "c", "a", "c")
    val test2 = List("c", "a", "cc", "aa", "c", "a", "c", "a")
//    alignAll(Set(splitToList("a c a cc a c a c"), splitToList("c a cc aa c a c"), splitToList("c a cc aa c aa c a"),
//      splitToList("c a c a c a c a cc aa")), test2).foreach(res => println(format(res)))
    for { t <- parseTraces("/home/lukas/projects/cmu/varexc-paper/gpl.txt")
          alignment <- alignAll(t._3.toSet)
    } {
      // (trace, smaller?, variational len, alignment len, alignment)
      (t._1, t._2 < alignment._1,t._2, alignment._1, alignment._2)
    }
  }
}

