package edu.cmu.cs.vbc

import java.io.{File, FileWriter}

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc
import edu.cmu.cs.vbc.config.VERuntime
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm.{ClassWriter, MethodVisitor}

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
  def instrumentMethod(method: VBCMethodNode, clazz: VBCClassNode): VBCMethodNode = {
    instrumentCustomInit(
      // Avoid comparing java classes because we might have model classes.
      // For brute-force execution, we don't use model class (to avoid writing wrapper methods in model classes),
      // so invokevirtual and getfield will be recorded. However, for V execution, we are using model class, which
      // makes it difficult to compare the traces.
      if (clazz.name.startsWith("model/java"))
        method
      // we introduce extra method invocation instructions and return instructions while handling <clinit>.
      else if (!method.name.contains("clinit"))
        method.copy(body = CFG(method.body.blocks.map(instrumentBlock)))
      else
        method
    )
  }

  def instrumentBlock(block: Block): Block =
    Block(
      (
        for (instr <- block.instr)
          yield
            instr match {
              case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"),
                                      MethodName("println"),
                                      MethodDesc("(Ljava/lang/String;)V"),
                                      _) =>
                // reduce output
                //          List(vbc.TraceInstr_Print(), instr)
                List(vbc.TraceInstr_Print(), InstrPOP(), InstrPOP())
              case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"),
                                      MethodName("println"),
                                      MethodDesc("(Ljava/lang/Object;)V"),
                                      _) =>
                // reduce output
                //          List(vbc.TraceInstr_Print(), instr)
                List(vbc.TraceInstr_Print(), InstrPOP(), InstrPOP())
              case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"),
                                      MethodName("println"),
                                      MethodDesc("(I)V"),
                                      _) =>
                List(vbc.TraceInstr_PrintI(), InstrPOP(), InstrPOP())
              case InstrINVOKEVIRTUAL(owner, name, desc, _) =>
                List(vbc.TraceInstr_S("INVK_VIRT: " + owner + ";" + name + ";" + desc), instr)
              case InstrGETFIELD(owner, name, desc)
                  if desc.contentEquals("I") || desc.contentEquals("Z") =>
                List(instr,
                     vbc.TraceInstr_GetField("GETFIELD: " + owner + ";" + name + ";" + desc, desc))
              //        case InstrRETURN() => List(vbc.TraceInstr_S("RETURN"), instr)
              //        case InstrIRETURN() => List(vbc.TraceInstr_S("IRETURN"), instr)
              case instr => List(instr)
            }
      ).flatten,
      block.exceptionHandlers,
      block.exceptions,
      shouldJumpBack = block.shouldJumpBack
    )

  def prepareBenchmark(method: VBCMethodNode, clazz: VBCClassNode): VBCMethodNode =
    instrumentCustomInit(avoidOutput(method, clazz))

  def avoidOutput(method: VBCMethodNode, clazz: VBCClassNode): VBCMethodNode =
    method.copy(body = CFG(method.body.blocks.map(avoidOutput)))

  def avoidOutput(block: Block): Block =
    Block(
      (
        for (instr <- block.instr)
          yield
            instr match {
              case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"),
                                      MethodName("println"),
                                      MethodDesc("(Ljava/lang/String;)V"),
                                      _) =>
                List(InstrPOP(), InstrPOP())
              case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"),
                                      MethodName("println"),
                                      MethodDesc("(Ljava/lang/Object;)V"),
                                      _) =>
                List(InstrPOP(), InstrPOP())
              case InstrINVOKEVIRTUAL(Owner("java/io/PrintStream"),
                                      MethodName("println"),
                                      MethodDesc("(I)V"),
                                      _) =>
                List(InstrPOP(), InstrPOP())
              case i => List(i)
            }
      ).flatten,
      block.exceptionHandlers,
      block.exceptions,
      shouldJumpBack = block.shouldJumpBack
    )

  def instrumentCustomInit(method: VBCMethodNode): VBCMethodNode =
    method.copy(body = CFG(method.body.blocks.map(instrumentCustomInit)))

  def instrumentCustomInit(block: Block): Block =
    Block(
      for (instr <- block.instr)
        yield
          instr match {
            //replace initialization of conditional fields
            case InstrINIT_CONDITIONAL_FIELDS() => vbc.TraceInstr_ConfigInit()
            case i                              => i
          },
      block.exceptionHandlers,
      block.exceptions,
      shouldJumpBack = block.shouldJumpBack
    )

//  def checkCrash(clazz: Class[_]): Unit = testMain(clazz, false)

  /**
    * Runtime changes relevant to switching class loaders
    * @param loader the new classloader
    */
  def changeClassLoader(loader: ClassLoader): Unit = {
    VBCClassLoader.clearCache()
    Thread.currentThread().setContextClassLoader(loader)
    VERuntime.classloader = Some(loader)
  }

  /**
    * Create a class with the given static method and test it.
    *
    * Useful for cases like [[edu.cmu.cs.vbc.prog.ExceptionExample]] where each method is essentially
    * a test case.
    */
  def testStaticMethod(testClassName: String,
                       compareTraceAgainstBruteForce: Boolean = true,
                       runBenchmark: Boolean = true,
                       fm: (Map[String, Boolean]) => Boolean = _ => true,
                       configFile: Option[String] = None,
                       useModel: Boolean = false,
                       processTrace: List[(FeatureExpr, String)] => List[(FeatureExpr, String)] = x => x)(main: MethodVisitor => Unit): Unit = {
    import org.objectweb.asm.Opcodes._
    val cw = new ClassWriter(0)
    cw.visit(V1_8, ACC_PUBLIC, testClassName, null, "java/lang/Object", Array.empty)
    val mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC,
      "main",
      "([Ljava/lang/String;)V",
      "([Ljava/lang/String;)V",
      Array.empty)
    mv.visitCode()
    main(mv)
    mv.visitMaxs(10, 10) // big enough numbers, will be re-computed by ASM later
    mv.visitEnd()
    cw.visitEnd()

    testMainBase(compareTraceAgainstBruteForce, runBenchmark, fm, configFile, useModel, processTrace) { loader =>
      loader.loadClassFromBytes(testClassName, cw.toByteArray)
    }
  }

  def testMain(clazz: Class[_],
               compareTraceAgainstBruteForce: Boolean = true,
               runBenchmark: Boolean = true,
               fm: (Map[String, Boolean]) => Boolean = _ => true,
               configFile: Option[String] = None,
               useModel: Boolean = false,
               processTrace: List[(FeatureExpr, String)] => List[(FeatureExpr, String)] = x => x): Unit = {
    testMainBase(compareTraceAgainstBruteForce, runBenchmark, fm, configFile, useModel, processTrace) { loader =>
      loader.loadClass(clazz.getName)
    }
  }

  private def testMainBase(
                            compareTraceAgainstBruteForce: Boolean = true,
                            runBenchmark: Boolean = true,
                            fm: (Map[String, Boolean]) => Boolean = _ => true,
                            configFile: Option[String] = None,
                            useModel: Boolean = false,
                            processTrace: List[(FeatureExpr, String)] => List[(FeatureExpr, String)] = x => x)(loadClassWith: VBCClassLoader => Class[_]): Unit = {
    println("Warning: please make sure conditional fields are not initialized! Otherwise brute-force comparison would fail")
    //test uninstrumented variational execution to see whether it crashes
    val origClassLoader = this.getClass.getClassLoader
    val testCrashLoader: VBCClassLoader = new VBCClassLoader(origClassLoader,
      true,
      avoidOutput,
      configFile = configFile,
      useModel = useModel)
    changeClassLoader(testCrashLoader)
    val testCrash = loadClassWith(testCrashLoader)
    VBCLauncher4Test.invokeLiftedMain(testCrash, new Array[String](0))

    //test instrumented version, executed variationally
    TestTraceOutput.trace = Nil
    TraceConfig.options = Set()
    val vloader: VBCClassLoader = new VBCClassLoader(origClassLoader,
                                                     true,
                                                     rewriter = instrumentMethod,
                                                     configFile = configFile,
                                                     useModel = useModel)
    changeClassLoader(vloader)
    val vcls = loadClassWith(vloader)
    VBCLauncher4Test.invokeLiftedMain(vcls, new Array[String](0))

    val usedOptions = TraceConfig.options.map(_.feature)
    val vtraces = TestTraceOutput.extractVTraces(TestTraceOutput.trace)
    val sortedVTraces = TestTraceOutput.sortVTraces(vtraces)

    println("Used Options: " + TraceConfig.options.mkString(", "))

    if (compareTraceAgainstBruteForce) {
      val loader: VBCClassLoader = new VBCClassLoader(origClassLoader,
        false,
        instrumentMethod,
        toFileDebugging = false,
        configFile = configFile,
        useModel = useModel)
      changeClassLoader(loader)
      val cls = loadClassWith(loader)
      //run against brute force instrumented execution and compare traces
      val configs = if (usedOptions.toList.size < 20) explode(usedOptions.toList) else selectOptRandomly(usedOptions.toList, 1000000)
      for ((sel, desel) <- configs if fm(configToMap((sel, desel)))) {
        println("executing config [" + sel.mkString(", ") + "]")
        TestTraceOutput.trace = Nil
        TraceConfig.config = configToMap((sel, desel))
        VBCLauncher4Test.invokeUnliftedMain(cls, new Array[String](0))
        val atrace = TestTraceOutput.trace

        //get the trace from the v execution relevant for this config and compare
        val ctx = genConfig(sel, desel)
        val selectedVTrace = sortedVTraces.find(p => ctx.implies(p._1).isTautology()).get._2
        val filteredvtrace = selectedVTrace.filter(_._1.evaluate(sel.toSet))
        compareTraces(sel, processTrace(atrace).map(_._2).reverse, processTrace(filteredvtrace).map(_._2).reverse)
      }
    }

    if (runBenchmark) {
      //run benchmark (without instrumentation)
      val vbenchmarkloader: VBCClassLoader = new VBCClassLoader(origClassLoader,
                                                                true,
                                                                prepareBenchmark,
                                                                configFile = configFile,
                                                                useModel = useModel)
      val benchmarkloader: VBCClassLoader = new VBCClassLoader(origClassLoader,
                                                               false,
                                                               prepareBenchmark,
                                                               configFile = configFile,
                                                               useModel = useModel)
      benchmark(loadClassWith, vbenchmarkloader, benchmarkloader, usedOptions, fm)
    }
  }

  def genConfig(sel: Seq[Feature], desel: Seq[Feature]): FeatureExpr = {
    val selFE = sel.map(x => FeatureExprFactory.createDefinedExternal(x))
    val deselFE = desel.map(x => FeatureExprFactory.createDefinedExternal(x).not())
    selFE.foldLeft(FeatureExprFactory.True)(_ and _) and deselFE.foldLeft(FeatureExprFactory.True)(_ and _)
  }

  type Feature = String
  type Config  = (List[Feature], List[Feature])

  def explode(fs: List[Feature]): List[Config] = {
    if (fs.isEmpty) List((Nil, Nil))
    else if (fs.size == 1) List((List(fs.head), Nil), (Nil, List(fs.head)))
    else {
      val r = explode(fs.tail)
      r.map(x => (fs.head :: x._1, x._2)) ++ r.map(x => (x._1, fs.head :: x._2))
    }
  }

  /**
    * Randomly assign true or false to each option
    *
    * @param fs  List of options in the program.
    * @param num Number of selections.
    * @return List of pairs, each pair consists of a list selected options and a list of deselected options. Might
    *         contain duplicate pairs because of the randomness.
    */
  def selectOptRandomly(fs: List[Feature], num: Int): List[Config] = {
    import scala.util.Random
    require(num > 0)
    if (fs.isEmpty) List((Nil, Nil))
    else {
      (0 to num).toList.map(_ => Random.shuffle(fs).splitAt(Random.nextInt(fs.size)))
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
      val foundOut    = new FileWriter(new File("found"))
      expectedOut.write("EXPECTED (plain execution): \n")
      expectedOut.write(expected.mkString("\n"))
      foundOut.write("FOUND (variational execution in [" + ctx.mkString(", ") + "]):\n")
      foundOut.write(actual.mkString("\n"))
      expectedOut.close()
      foundOut.close()
      throw new RuntimeException(
        "mismatch between plain execution and variational execution in config [" + ctx.mkString(
          ", ") + "], check the log files")
    }
  }

  def benchmark(loadClassWith: VBCClassLoader => Class[_],
                vloader: VBCClassLoader,
                loader: VBCClassLoader,
                configOptions: Set[String],
                fm: (Map[String, Boolean]) => Boolean): Unit = {
    import org.scalameter._

    //measure V execution
    changeClassLoader(vloader)
    val testVClass = loadClassWith(vloader)
    val vtime = config(
      Key.exec.benchRuns -> 20
    ) withWarmer {
      new Warmer.Default
    } setUp { _ =>
      TestTraceOutput.trace = Nil
      TraceConfig.config = Map()
    } measure {
      //      Profiler.reset()
      VBCLauncher4Test.invokeLiftedMain(testVClass, new Array[String](0))
      //      Profiler.report()
    }
    //        println(s"Total time V: $time")

    changeClassLoader(loader)
    lazy val testClass = loadClassWith(loader)
    //measure brute-force execution
    val configs =
      if (configOptions.size < 20)
        explode(configOptions.toList)
      else
        selectOptRandomly(configOptions.toList, 1000000) // close to 2^20
    val bftimes = for ((sel, desel) <- configs if fm(configToMap((sel, desel))))
      yield
        config(
          Key.exec.benchRuns -> 20
        ) withWarmer {
          new Warmer.Default
        } setUp { _ =>
          TestTraceOutput.trace = Nil
          TraceConfig.config = configToMap((sel, desel))
        } measure {
          VBCLauncher4Test.invokeUnliftedMain(testClass, new Array[String](0))
        }

    val avgTime = bftimes.map(_.value).sum / bftimes.size
    println()
    println(s"VExecution time: " + vtime)
    println(s"Execution time: " + avgTime + bftimes.mkString(" (", ",", ")"))
    println(s"Slowdown: " + vtime.value / avgTime)

  }

}
