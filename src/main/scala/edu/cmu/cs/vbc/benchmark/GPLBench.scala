package edu.cmu.cs.vbc.benchmark

import java.io.FileWriter

/**
  * One VM invocation runs the same program in iterations, using
  * a sliding window to keep track of execution time of each iteration.
  * Once the sliding window is "stable", it terminates and report the mean
  * of all values in the sliding window.
  */
object GPLVMInvocation extends App with VBench {
  var start: Double = 0.0
  var end: Double = 0.0

  val measurements = new SlidingWindown(10)

  val m = getMainMethod("edu.cmu.cs.vbc.prog.gpl.Main", "gpl.conf", useModel = false)

  while (!(measurements.isFull && measurements.cov() < 0.02)) {
    start = System.nanoTime().toDouble
    m.invoke(null, Array[String]())
    end = System.nanoTime().toDouble
    measurements.add(end - start)
  }

  val meanTimeInMS = measurements.mean() / 1000000
  val writer = new FileWriter("gpl.result", true)
  writer.append(meanTimeInMS + " ms\n")
  writer.close()
}

object GPLVMInvocation_Model extends App with VBench {
  var start: Double = 0.0
  var end: Double = 0.0

  val measurements = new SlidingWindown(10)

  val m = getMainMethod("edu.cmu.cs.vbc.prog.gpl.Main", "gpl-model.conf", useModel = true)

  while (!(measurements.isFull && measurements.cov() < 0.02)) {
    start = System.nanoTime().toDouble
    m.invoke(null, Array[String]())
    end = System.nanoTime().toDouble
    measurements.add(end - start)
  }

  val meanTimeInMS = measurements.mean() / 1000000
  val writer = new FileWriter("gpl-model.result", true)
  writer.append(meanTimeInMS + " ms\n")
  writer.close()
}
