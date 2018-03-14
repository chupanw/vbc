package edu.cmu.cs.vbc.benchmark

import java.io.FileWriter

/**
  * One VM invocation runs the same program in iterations, using
  * a sliding window to keep track of execution time of each iteration.
  * Once the sliding window is "stable", it terminates and report the mean
  * of all values in the sliding window.
  */
object QuEvalVMInvocation_Model extends App with VBench {
  var start: Double = 0.0
  var end: Double = 0.0

  val measurements = new SlidingWindown(10)

  val m = getMainMethod("edu.cmu.cs.vbc.prog.queval.queval.MainClass", "queval-model.conf", useModel = true)

  while (!(measurements.isFull && measurements.cov() < 0.02)) {
    start = System.nanoTime().toDouble
    m.invoke(null, Array[String]())
    end = System.nanoTime().toDouble
    measurements.add(end - start)
  }

  val meanTimeInMS = measurements.mean() / 1000000
  val writer = new FileWriter("queval-model.result", true)
  writer.append(meanTimeInMS + " ms\n")
  writer.close()
}

object QuEvalVMInvocation extends App with VBench {
  var start: Double = 0.0
  var end: Double = 0.0

  val measurements = new SlidingWindown(10)

  val m = getMainMethod("edu.cmu.cs.vbc.prog.queval.queval.MainClass", "queval.conf", useModel = false)

  while (!(measurements.isFull && measurements.cov() < 0.02)) {
    start = System.nanoTime().toDouble
    m.invoke(null, Array[String]())
    end = System.nanoTime().toDouble
    measurements.add(end - start)
  }

  val meanTimeInMS = measurements.mean() / 1000000
  val writer = new FileWriter("queval.result", true)
  writer.append(meanTimeInMS + " ms\n")
  writer.close()
}