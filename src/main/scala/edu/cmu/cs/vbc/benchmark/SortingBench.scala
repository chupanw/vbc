import java.io.FileWriter

import edu.cmu.cs.vbc.benchmark.{SlidingWindown, VBench}

object SortArrayVMInvocation extends App with VBench {
  var start: Double = 0.0
  var end: Double = 0.0

  val measurements = new SlidingWindown(10)

  val m = getMainMethod("edu.cmu.cs.vbc.prog.benchmark.SortArray", "util.conf", useModel = false)

  while (!(measurements.isFull && measurements.cov() < 0.02)) {
    start = System.nanoTime().toDouble
    m.invoke(null, Array[String]())
    end = System.nanoTime().toDouble
    measurements.add(end - start)
  }

  val meanTimeInMS = measurements.mean() / 1000000
  val writer = new FileWriter("sort_array.result", true)
  writer.append(meanTimeInMS + " ms\n")
  writer.close()
}
