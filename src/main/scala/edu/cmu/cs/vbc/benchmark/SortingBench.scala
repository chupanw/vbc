import edu.cmu.cs.vbc.benchmark.VBench

object SortArrayVMInvocation extends App with VBench {
  benchmark("edu.cmu.cs.vbc.prog.benchmark.SortArray", "util.conf", useModel = false, "sort_array.result")
}
