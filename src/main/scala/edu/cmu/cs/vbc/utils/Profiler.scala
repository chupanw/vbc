package edu.cmu.cs.vbc.utils

import scala.collection.mutable

/**
  * @author chupanw
  */
object Profiler {

  val methodCallCount: mutable.Map[String, Int] = mutable.Map[String, Int]().withDefaultValue(0)
  // in nanoseconds
  val methodCallDuration: mutable.Map[String, Long] = mutable.Map[String, Long]().withDefaultValue(0)
  val startTime: mutable.Map[String, Long] = mutable.Map[String, Long]()

  def stopTimer(id: String): Unit = {
//    def removeN(s: String): String = s.substring(0, s.lastIndexOf("#"))
//    val end = System.nanoTime()
//    val duration = end - startTime(id)
//    methodCallCount(removeN(id)) += 1
//    methodCallDuration(removeN(id)) += duration
  }

  def startTimer(id: String): Unit = {
//    startTime(id) = System.nanoTime()
  }

  def reset(): Unit = {
    methodCallCount.clear()
    methodCallDuration.clear()
    startTime.clear()
  }

  def report(): Unit = {
    println("********** Profiling **********")
    methodCallCount.toSeq.sortWith(_._2 > _._2).foreach(p =>
      println(f"${p._2} \t ${methodCallDuration(p._1).toDouble / 1000000}%.2f ms \t ${methodCallDuration(p._1).toDouble / p._2 / 1000000}%.2f ms \t ${p._1}")
    )
    println("*******************************")
  }
}
