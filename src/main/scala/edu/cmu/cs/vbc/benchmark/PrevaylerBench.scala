package edu.cmu.cs.vbc.benchmark

/**
  * One VM invocation runs the same program in iterations, using
  * a sliding window to keep track of execution time of each iteration.
  * Once the sliding window is "stable", it terminates and report the mean
  * of all values in the sliding window.
  */
object PrevaylerVMInvocation extends App with VBench {
  benchmark("edu.cmu.cs.vbc.prog.prevayler.RunNumberKeeper", "prevayler.conf", useModel = false, "prevayler.result")
}
