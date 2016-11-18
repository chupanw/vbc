package edu.cmu.cs.vbc

import java.io.{File, FileWriter}

import edu.cmu.cs.vbc
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._

/**
  * compares the execution of a class (via main method, lifted using a special class loader)
  * between variational and brute-force execution
  */
trait DiffLaunchTestInfrastructure {

  /**
    * instrument byte code to log every method invocation and
    * every access to a field and the value of a return statement
    *
    * also change the way that conditional fields are initialized
    */
  def instrumentMethod(method: VBCMethodNode): VBCMethodNode = {
    instrumentCustomInit(
      // we introduce extra method invocation instructions and return instructions while handling <clinit>.
      if (!method.name.contains("clinit"))
        method.copy(body = CFG(method.body.blocks.map(instrumentBlock)))
      else
        method
    )
  }

  def instrumentBlock(block: Block): Block =
    Block((
      for (instr <- block.instr) yield instr match {
        case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"), MethodName("println"), MethodDesc("(Ljava/lang/String;)V"), _) => List(vbc.TraceInstr_Print(), instr)
        case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"), MethodName("println"), MethodDesc("(Ljava/lang/Object;)V"), _) => List(vbc.TraceInstr_Print(), instr)
        case InstrINVOKEVIRTUAL(owner, name, desc, _) => List(vbc.TraceInstr_S("INVK_VIRT: " + owner + ";" + name + ";" + desc), instr)
        case InstrGETFIELD(owner, name, desc) if desc.contentEquals("I") || desc.contentEquals("Z") => List(instr, vbc.TraceInstr_GetField("GETFIELD: " + owner + ";" + name + ";" + desc, desc))
//        case InstrRETURN() => List(vbc.TraceInstr_S("RETURN"), instr)
//        case InstrIRETURN() => List(vbc.TraceInstr_S("IRETURN"), instr)
        case instr => List(instr)
      }
      ).flatten, block.exceptionHandlers
    )


  def instrumentCustomInit(method: VBCMethodNode): VBCMethodNode = method.copy(body = CFG(method.body.blocks.map(instrumentCustomInit)))

  def instrumentCustomInit(block: Block): Block =
    Block(
      (for (instr <- block.instr) yield instr match {
        //replace initialization of conditional fields
        case InstrINIT_CONDITIONAL_FIELDS() => vbc.TraceInstr_ConfigInit()
        case instr => instr
      }), block.exceptionHandlers
    )


  def checkCrash(clazz: Class[_]): Unit = testMain(clazz, false)

  def testMain(clazz: Class[_], compareTraceAgainstBruteForce: Boolean = true, runBenchmark: Boolean = true): Unit = {
    //test uninstrumented variational execution to see whether it crashes
    val classname = clazz.getName
    VBCLauncher.launch(classname)

    //test instrumented version, executed variationally
    TestTraceOutput.trace = Nil
    TraceConfig.options = Set()
    val vloader: VBCClassLoader = new VBCClassLoader(this.getClass.getClassLoader, true, instrumentMethod)
    val vcls: Class[_] = vloader.loadClass(classname)
    VBCLauncher.invokeMain(vcls, new Array[String](0))

    val vtrace = TestTraceOutput.trace
    val usedOptions = TraceConfig.options.map(_.feature)

    println("Used Options: " + TraceConfig.options.mkString(", "))

    if (compareTraceAgainstBruteForce) {
      val loader: VBCClassLoader = new VBCClassLoader(this.getClass.getClassLoader, false, instrumentMethod, toFileDebugging = false)
      val cls: Class[_] = loader.loadClass(classname)
      //run against brute force instrumented execution and compare traces
      for ((sel, desel) <- scala.util.Random.shuffle(explode(usedOptions.toList)).take(1000)) {
        println("executing config [" + sel.mkString(", ") + "]")
        TestTraceOutput.trace = Nil
        TraceConfig.config = configToMap((sel, desel))
        VBCLauncher.invokeMain(cls, new Array[String](0))
        val atrace = TestTraceOutput.trace

        //get the trace from the v execution relevant for this config and compare
        val filteredvtrace = vtrace.filter(_._1.evaluate(sel.toSet))
        compareTraces(sel, atrace.map(_._2).reverse, filteredvtrace.map(_._2).reverse)
      }
    }

    if (runBenchmark) {
      //run benchmark (without instrumentation)
      val vbenchmarkloader: VBCClassLoader = new VBCClassLoader(this.getClass.getClassLoader, true, instrumentCustomInit)
      val vbenchmarkcls: Class[_] = vbenchmarkloader.loadClass(classname)
      val benchmarkloader: VBCClassLoader = new VBCClassLoader(this.getClass.getClassLoader, false, instrumentCustomInit)
      val benchmarkcls: Class[_] = benchmarkloader.loadClass(classname)
      benchmark(classname, vbenchmarkcls, benchmarkcls, usedOptions)
    }
  }

  type Feature = String
  type Config = (List[Feature], List[Feature])

  def explode(fs: List[Feature]): List[Config] = {
    if (fs.isEmpty) List((Nil, Nil))
    else if (fs.size == 1) List((List(fs.head), Nil), (Nil, List(fs.head)))
    else {
      val r = explode(fs.tail)
      r.map(x => (fs.head :: x._1, x._2)) ++ r.map(x => (x._1, fs.head :: x._2))
    }
  }

  def configToMap(c: Config): Map[String, Boolean] = {
    var r = Map[String, Boolean]()
    for (sel <- c._1)
      r += (sel -> true)
    for (desel <- c._2)
      r += (desel -> false)
    r
  }

  def compareTraces(ctx: List[Feature], expected: List[String], actual: List[String]): Unit = {
    if (expected != actual) {
      val expectedOut = new FileWriter(new File("expected"))
      val foundOut = new FileWriter(new File("found"))
      expectedOut.write("EXPECTED (plain execution): \n")
      expectedOut.write(expected.mkString("\n"))
      foundOut.write("FOUND (variational execution in [" + ctx.mkString(", ") + "]):\n")
      foundOut.write(actual.mkString("\n"))
      expectedOut.close()
      foundOut.close()
      throw new RuntimeException("mismatch between plain execution and variational execution in config [" + ctx.mkString(", ") + "], check the log files")
    }
  }


  def benchmark(classname: String, testVClass: Class[_], testClass: Class[_], configOptions: Set[String]): Unit = {
    import org.scalameter._

    //measure V execution
    val vtime = config(
      Key.exec.benchRuns -> 20
    ) withWarmer {
      new Warmer.Default
    } setUp { _ =>
      TestTraceOutput.trace = Nil
      TraceConfig.config = Map()
    } measure {
      VBCLauncher.invokeMain(testVClass, new Array[String](0))
    }
    //        println(s"Total time V: $time")


    //measure brute-force execution
    val configs = explode(configOptions.toList)
    val bftimes = for ((sel, desel) <- scala.util.Random.shuffle(configs).take(1000))
      yield config(
        Key.exec.benchRuns -> 20
      ) withWarmer {
        new Warmer.Default
      } setUp { _ =>
        TestTraceOutput.trace = Nil
        TraceConfig.config = configToMap((sel, desel))
      } measure {
        VBCLauncher.invokeMain(testClass, new Array[String](0))
      }


    val avgTime = bftimes.map(_.value).sum / bftimes.size
    println(s"VExecution time [$classname]: " + vtime)
    println(s"Execution time [$classname]: " + avgTime + bftimes.mkString(" (", ",", ")"))
    println(s"Slowdown [$classname]: " + vtime.value / avgTime)

  }

}
