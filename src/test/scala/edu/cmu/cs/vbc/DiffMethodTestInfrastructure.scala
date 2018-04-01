package edu.cmu.cs.vbc

import de.fosd.typechef.conditional.{ConditionalLib, Opt}
import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.TestOutput.TOpt
import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.vbytecode.instructions._
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes._

/**
  * compares the execution of two methods
  */
trait DiffMethodTestInfrastructure {

  class MyClassLoader extends VBCClassLoader(this.getClass.getClassLoader) {
    def defineClass(name: String, b: Array[Byte]): Class[_] = {
      return defineClass(name, b, 0, b.length)
    }
  }


  case class TestClass(m: VBCMethodNode) {
    private def createClass(testmethod: VBCMethodNode): VBCClassNode = {
      val constr = new VBCMethodNode(ACC_PUBLIC, "<init>", "()V", Some("()V"), Nil,
        CFG(List(
          Block(InstrALOAD(new Parameter(0, "this", TypeDesc("LTest;"))),
            InstrINVOKESPECIAL(Owner("java/lang/Object"), MethodName("<init>"), MethodDesc("()V"), false),
            InstrRETURN())
        )))
      new VBCClassNode(V1_8, ACC_PUBLIC, "Test", None, "java/lang/Object", Nil, Nil,
        List(constr, testmethod))

    }

    def toByteCode(cw: ClassWriter) = {
      createClass(m).toByteCode(cw)
    }

    def toVByteCode(cw: ClassWriter) = {
      createClass(m).toVByteCode(cw)
    }

  }


  def loadTestClass(clazz: TestClass): Class[_] = {
    val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
    clazz.toByteCode(cw)
    val byte = cw.toByteArray
//    val printer = new TraceClassVisitor(new PrintWriter(System.out))
//    println(
//      """
//        |  ------------------
//        | | Unlifted version |
//        |  ------------------
//      """.stripMargin)
//    new ClassReader(byte).accept(printer, 0)
    val myClassLoader = new MyClassLoader
    myClassLoader.defineClass("Test", byte)
  }

  def loadVTestClass(clazz: TestClass): Class[_] = {
    val vcw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
    clazz.toVByteCode(vcw)
    val vbyte = vcw.toByteArray

//    val printer = new TraceClassVisitor(new PrintWriter(System.out))
//    println(
//      """
//        |  ----------------
//        | | Lifted version |
//        |  ----------------
//      """.stripMargin)
//    new ClassReader(vbyte).accept(printer, 0)
    val myVClassLoader = new MyClassLoader
    myVClassLoader.defineClass("Test", vbyte)
  }


  def testMethod(m: VBCMethodNode, compareBruteForce: Boolean = true): Unit = {

    val clazz = new TestClass(m)


    val configOptions: Set[String] = getConfigOptions(m)


    //        val resource: String = "edu.cmu.cs.vbc.Tmp".replace('.', '/') + ".class"
    //        val is: InputStream = myClassLoader.getResourceAsStream(resource)
    //        new ClassReader(is).accept(printer, 0)


    //        val testClass = myClassLoader.defineClass("Test", byte)

    val testClass = loadTestClass(clazz)
    val testVClass = loadVTestClass(clazz)

    val vresult: List[TOpt[String]] = executeV(testVClass, m.name)
    println("found    " + vresult.reverse)
    if (compareBruteForce) {
      val bruteForceResult = executeBruteForce(testClass, m.name, configOptions)
      println("expected " + bruteForceResult.reverse)
      compare(bruteForceResult.reverse, vresult.reverse)
      benchmark(testVClass, testClass, m.name, configOptions)
    }
  }

  def getConfigOptions(m: VBCMethodNode): Set[String] = {
    val configOptions: Set[String] =
      (for (block <- m.body.blocks; instr <- block.instr; if instr.isInstanceOf[InstrLoadConfig]) yield instr.asInstanceOf[InstrLoadConfig].config).toSet
    configOptions
  }

  private def compare(expected: List[TOpt[String]], result: List[TOpt[String]]): Unit = {
    def compareOne(ctx: FeatureExpr, e: String, f: String): Unit =
      assert(ctx.isContradiction || e.trim == f.trim, s"mismatch between expected output and actual output in context $ctx: \nEXPECTED:\n$e\nFOUND:\n$f\nALL:\n" + render(result))

    val allExpected = ConditionalLib.explodeOptList(expected).map(_.mkString)
    val allFound = ConditionalLib.explodeOptList(result).map(_.mkString)
    ConditionalLib.vmapCombination(allExpected, allFound, FeatureExprFactory.True, compareOne)
  }

  private def render(result: List[TOpt[String]]): String =
    result.map(o => "[#condition " + o.feature + "]" + o.entry).mkString

  def executeBruteForce(testClass: Class[_], method: String, configOptions: Set[String]) = {
    //now all the separate executions
    var bruteForceResult: List[Opt[String]] = Nil
    val configs = explode(configOptions.toList)
    for ((sel, desel) <- configs) {
      TestOutput.output = Nil
      Config.configValues = (sel.map((_ -> 1)) ++ desel.map((_ -> 0))).toMap
      val config = (sel.map(FeatureExprFactory.createDefinedExternal(_))
        ++ desel.map(FeatureExprFactory.createDefinedExternal(_).not)).
        fold(FeatureExprFactory.True)(_ and _)

      val testObject = testClass.newInstance()
      testClass.getMethod(method).invoke(testObject)

      bruteForceResult = TestOutput.output.map(_.and(config)) ++ bruteForceResult
    }
    bruteForceResult
  }

  def executeV(testVClass: Class[_], method: String): List[TOpt[String]] = {
    //VExecution first
    TestOutput.output = Nil
    Config.configValues = Map()
    val ctx = FeatureExprFactory.True

    val constructor = testVClass.getConstructor(classOf[FeatureExpr])
    val testVObject = constructor.newInstance(ctx)
    val mn = MethodName(method).rename(MethodDesc("()V"))
    val exceptions = testVClass.getMethod(mn, classOf[FeatureExpr]).invoke(testVObject, ctx)
    val vresult = TestOutput.output
    println("[INFO] Returned exceptions: " + exceptions)
    vresult
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


  def benchmark(testVClass: Class[_], testClass: Class[_], method: String, configOptions: Set[String]): Unit = {
    import org.scalameter._

    //measure V execution
    var testObject: Any = null
    var _ctx: FeatureExpr = null
    val vtime = config(
      Key.exec.benchRuns -> 20
    ) withWarmer {
      new Warmer.Default
      //        } withMeasurer {
      //            new Measurer.IgnoringGC
    } setUp { _ =>
      TestOutput.output = Nil
      Config.configValues = Map()
      _ctx = FeatureExprFactory.True
      val constructor = testVClass.getConstructor(classOf[FeatureExpr])
      testObject = constructor.newInstance(_ctx)
    } measure {
      val mn = MethodName(method).rename(MethodDesc("()V"))
      testVClass.getMethod(mn, classOf[FeatureExpr]).invoke(testObject, _ctx)
    }
    //        println(s"Total time V: $time")


    //measure brute-force execution
    val configs = explode(configOptions.toList)
    val bftimes = for ((sel, desel) <- configs)
      yield config(
        Key.exec.benchRuns -> 20
      ) withWarmer {
        new Warmer.Default
        //        } withMeasurer {
        //            new Measurer.IgnoringGC
      } setUp { _ =>
        TestOutput.output = Nil
        Config.configValues = (sel.map((_ -> 1)) ++ desel.map((_ -> 0))).toMap
        testObject = testClass.newInstance()
      } measure {
        testClass.getMethod(method).invoke(testObject)
      }


    val avgTime = bftimes.map(_.value).sum / bftimes.size
    println("VExecution time: " + vtime)
    println("Execution time: " + avgTime + bftimes.mkString(" (", ",", ")"))
    println("Slowdown: " + vtime.value / avgTime)

  }

  def simpleMethod(instrs: Instruction*) = {
    FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
    testMethod(new VBCMethodNode(ACC_PUBLIC, "test", "()V", Some("()V"), Nil,
      CFG(List(Block(instrs: _*)))))
  }

  def methodWithBlocks(blocks: List[Block], compareBruteForce: Boolean = true) = {
    FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
    testMethod(VBCMethodNode(ACC_PUBLIC, "test", "()V", Some("()V"), Nil, CFG(blocks)), compareBruteForce)
  }

  /** Helper function to create a variational Integer.
    *
    * @param startBlockIdx the index of starting block, used to jump between blocks
    * @param tValue        int value if condition is true
    * @param fValue        int value if condition is false
    * @param localVar      store the new variational Integer to this local variable, otherwise leave it on stack
    * @return A list of bytecode blocks
    */
  def createVInteger(
                      startBlockIdx: Int,
                      tValue: Int,
                      fValue: Int,
                      localVar: Option[LocalVar] = None,
                      config: String = "A"
                    ): List[Block] = {
    createV(startBlockIdx, localVar, config)(
      List( InstrICONST(tValue), InstrINVOKESTATIC(Owner("java/lang/Integer"), MethodName("valueOf"), MethodDesc("(I)Ljava/lang/Integer;"), itf = false) )
    )(
      List( InstrICONST(fValue), InstrINVOKESTATIC(Owner("java/lang/Integer"), MethodName("valueOf"), MethodDesc("(I)Ljava/lang/Integer;"), itf = false) )
    )
  }

  /** Helper function to create a variational String. */
  def createVString(
                     startBlockIdx: Int,
                     tValue: String,
                     fValue: String,
                     localVar: Option[LocalVar] = None,
                     config: String = "A"
                   ): List[Block] = {
    createV(startBlockIdx, localVar, config)(
      List(InstrLDC(tValue))
    )(
      List(InstrLDC(fValue))
    )
  }

  def createVPrimitiveArray(
                    atype: Int,
                    startBlockIdx: Int,
                    tLength: Int,
                    fLength: Int,
                    localVar: Option[LocalVar] = None,
                    config: String = "A"
                  ): List[Block] = {
    createV(startBlockIdx, localVar, config)(
      List(InstrBIPUSH(tLength), InstrNEWARRAY(atype))
    )(
      List(InstrBIPUSH(fLength), InstrNEWARRAY(atype))
    )
  }

  def createVObjectArray(
                          atype: Owner,
                          startBlockIdx: Int,
                          tLength: Int,
                          fLength: Int,
                          localVar: Option[LocalVar] = None,
                          config: String = "A"
                        ): List[Block] = {
    createV(startBlockIdx, localVar, config)(
      List(InstrBIPUSH(tLength), InstrANEWARRAY(atype))
    )(
      List(InstrBIPUSH(fLength), InstrANEWARRAY(atype))
    )
  }

  def createVint(
                tValue: Int,
                fValue: Int,
                startBlockIdx: Int,
                localVar: Option[LocalVar] = None,
                config: String = "A"
                ): List[Block] = {
    createV(startBlockIdx, localVar, config)(
      List(InstrLDC(Integer.valueOf(tValue)))
    )(
      List(InstrLDC(Integer.valueOf(fValue)))
    )
  }

  def createVlong(
                 tValue: Long,
                 fValue: Long,
                 startBlockIdx: Int,
                 localVar: Option[LocalVar] = None,
                 config: String = "A"
                 ): List[Block] = {
    createV(startBlockIdx, localVar, config)(
      List(InstrLDC(new java.lang.Long(tValue)))
    )(
      List(InstrLDC(new java.lang.Long(fValue)))
    )
  }

  def createV(startBlockIdx: Int, localVar: Option[LocalVar] = None, config: String = "A")
             (t: List[Instruction])
             (f: List[Instruction]): List[Block] = {
    Block(InstrLoadConfig(config), InstrIFEQ(startBlockIdx + 2)) ::
    Block(
      t ::: List(
        if (localVar.isDefined)
          if (localVar.get.desc == "I") InstrISTORE(localVar.get) else InstrASTORE(localVar.get)
        else
          InstrNOP(),
        InstrGOTO(startBlockIdx + 3)
      ): _*
    ) ::
    Block(
      f ::: List(
        if (localVar.isDefined)
          if (localVar.get.desc == "I") InstrISTORE(localVar.get) else InstrASTORE(localVar.get)
        else
          InstrNOP(),
        InstrGOTO(startBlockIdx + 3)
      ): _*
    ) ::
    Nil
  }
}
