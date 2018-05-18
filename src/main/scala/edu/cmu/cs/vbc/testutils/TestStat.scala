package edu.cmu.cs.vbc.testutils

object TestStat {
  val buf = new StringBuilder()

  private val succCount = collection.mutable.Map[String, Int]()
  private val failCount = collection.mutable.Map[String, Int]()
  private val skipCount = collection.mutable.Map[String, Int]()
  private val all = collection.mutable.Set[String]()
  private val skippedClasses = collection.mutable.ListBuffer[String]()

  def succeed(c: String): Unit = {
    all += c
    if (succCount.contains(c)) succCount(c) = succCount(c) + 1 else succCount(c) = 1
  }
  def fail(c: String): Unit = {
    all += c
    if (failCount.contains(c)) failCount(c) = failCount(c) + 1 else failCount(c) = 1
  }
  def skip(c: String): Unit = {
    all += c
    if (skipCount.contains(c)) skipCount(c) = skipCount(c) + 1 else skipCount(c) = 1
  }

  def skipClass(c: String): Unit = {
    skippedClasses += c
  }

  def add(i: Int, c: String): Unit = {
    val succ = succCount.getOrElse(c, 0)
    val fail = failCount.getOrElse(c, 0)
    val skip = skipCount.getOrElse(c, 0)
    val total = succ + fail + skip
    buf ++= s"[$i] " + c + "\n"
    buf ++= s"Tests run: $total, Succeed: $succ, Failed: $fail, Skipped: $skip" + "\n"
  }

  def printToConsole(): Unit = {
    all.toList.zipWithIndex.sortBy(_._2).foreach(x => add(x._2, x._1))
    println(
      """
        |********** Test Report **********
      """.stripMargin)
    println(buf.toString)

    println(
      """
        |****** Skipped Classes ******
      """.stripMargin)
    println(skippedClasses.mkString("\n"))
  }
}
