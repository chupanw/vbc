package edu.cmu.cs.vbc.benchmark

/**
  * One VM invocation runs the same program in iterations, using
  * a sliding window to keep track of execution time of each iteration.
  * Once the sliding window is "stable", it terminates and report the mean
  * of all values in the sliding window.
  */
object QuEvalVMInvocation_Model extends App with VBench {
  benchmark("edu.cmu.cs.vbc.prog.queval.queval.MainClass", "queval-model.conf", useModel = true, "queval-model.result")
}

object QuEvalVMInvocation extends App with VBench {
  benchmark("edu.cmu.cs.vbc.prog.queval.queval.MainClass", "queval.conf", useModel = false, "queval.result")
}