package edu.cmu.cs.vbc.benchmark

import java.io.FileWriter

/**
  * @author chupanw
  */
object VGPLBench extends App {
  import scala.sys.process._

  1 to 10 foreach {_ => Seq("sbt", "runMain edu.cmu.cs.vbc.benchmark.VGPLVMInvocation").!}
}

object VGPLVMInvocation extends App with VBench {
  var start: Double = 0.0
  var end: Double = 0.0

  val measurements = new SlidingWindown(10)

  val m = getMainMethod("edu.cmu.cs.vbc.prog.gpl.Main", "gpl.conf")

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
