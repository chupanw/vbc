package edu.cmu.cs.vbc.utils

object SequenceAlignment {
  object Align2 {
    val GAP_COST = 2
    val MATCH_COST = 0
    val NONMATCH_COST = 1
    type CostMatrix = Array[Array[Int]]

    def align(seq1: String, seq2: String): (Int, List[(Char, Char, Int)]) = {
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
    def costMatrix(seq1: String, seq2: String): CostMatrix = {
      var m = emptyCostMatrix(seq1.length, seq2.length)

      for (i <- seq1.length - 1 to (0, -1);
           j <- seq2.length - 1 to (0, -1)) {
        val cell = calc_cell((seq1(i), seq2(j)), m(i + 1)(j), m(i)(j + 1), m(i + 1)(j + 1))
        m(i)(j) = cell
      }
      m
    }
    def calc_cell(letters: (Char, Char), rightCell: Int, downCell: Int, diagCell: Int): Int = {
      List(rightCell + 2, downCell + 2, diagCell + penalty(letters)).min
    }
    def penalty(letters: (Char, Char)): Int = {
      letters match {
        case (a, b) if a == b => MATCH_COST
        case ('-', _) | (_, '-') => GAP_COST
        case _ => NONMATCH_COST
      }
    }

    def alignment(seq1: String, seq2: String, cost: CostMatrix): List[(Char, Char, Int)] = {
      val padded_seq1 = seq1 + "-"
      val padded_seq2 = seq2 + "-"
      var aligned = List.empty[(Char, Char, Int)]
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
            val res = (seq1(i), '-', GAP_COST)
            i += 1
            res
          case `cost_down` =>
            val res = ('-', seq2(j), GAP_COST)
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
        aligned = aligned :+ (seq1(i), '-', GAP_COST)
        i += 1
      }
      while (j < seq2.length) {
        aligned = aligned :+ ('-', seq2(j), GAP_COST)
        j += 1
      }
      aligned
    }
  }
  def format(alignment: List[(Char, Char, Int)]): String = {
    alignment.foldLeft("")((s, el) => s + el._1 + " " + el._2 + " " + el._3 + "\n")
  }

  def main(args: Array[String]): Unit = {
    val test1 = "acacacac"
    val test2 = "cacacaca"
    println(format(Align2.align(test1, test2)._2))
  }
}

