package edu.cmu.cs.vbc.testutils

import java.io.File

import de.fosd.typechef.featureexpr.{FeatureExprFactory, FeatureExprParser}
import edu.cmu.cs.varex.V
import edu.cmu.cs.vbc.config.{Settings, VERuntime}

object MonopoliTestLoader {
//  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/monopoli100.jar"
//  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/monopoli100.jar"

    val main = "/Users/chupanw/Projects/Data/Monopooly/bin/"
    val test = "/Users/chupanw/Projects/Data/Monopooly/bin/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test, config = Some("monopoli.conf"), useModel = true, reuseLifted = true)
}

object MonopoliTestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  VERuntime.classloader = Some(MonopoliTestLoader.testLoader)
  VERuntime.loadFeatures("monopoly.txt")

  val tests = List(
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsMatarCubiertos"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTest"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida1"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestPartida3"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.DadosTest"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosYo"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosMuchasTarjetas"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.grupos.GrupoATests"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida2"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsParaCubrirMutantes"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestCubrirTodos"

    // original problematic order, get fixed after proper reinitialization
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsMatarCubiertos",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTest",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida1",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestPartida3",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.DadosTest",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosYo",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosMuchasTarjetas",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.grupos.GrupoATests",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida2",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsParaCubrirMutantes",
//        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestCubrirTodos"

    // working order
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.DadosTest",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTest",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida1",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida2",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestCubrirTodos",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosMuchasTarjetas",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosYo",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestPartida3",
//      "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsMatarCubiertos",
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsParaCubrirMutantes",
//      "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.grupos.GrupoATests"
  )

  tests.foreach {x =>
    val testClass = new TestClass(MonopoliTestLoader.testLoader.loadClass(x))
    testClass.runTests()
  }

  if (Settings.printTestResults) VTestStat.printToConsole()
}


/**
  * Execute a specified test case
  *
  * This will get called from the commandline
  */
object MonopoliForkTestCaseLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
  VERuntime.classloader = Some(MonopoliTestLoader.testLoader)
  VERuntime.loadFeatures("monopoly.txt")
  val cName = args(0)
  val mName = args(1)
  val t = new ForkTestCase(MonopoliTestLoader.testLoader.loadClass(cName), mName)
  t.run()
}

/**
  * Execute all test cases of Monopoli
  *
  * Using IntelliJ to build the whole project as one jar, so that we don't need to
  * call sbt.
  */
object MonopoliForkTestLauncher extends App {
  import scala.sys.process._
  val testClasses = List(
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsMatarCubiertos",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida1",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestPartida3",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.DadosTest",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosYo",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosMuchasTarjetas",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.grupos.GrupoATests",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida2",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsParaCubrirMutantes",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestCubrirTodos",
    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTest"
  )
  val jarFile = "out/artifacts/all_jar/vbc.jar"

  testClasses.foreach(c => {
    val clazz = new TestClass(MonopoliTestLoader.testLoader.loadClass(c))
    clazz.getTestCases.foreach(m => {
      Process(Seq("java", "-Xmx12g", "-cp", jarFile, "edu.cmu.cs.vbc.testutils.MonopoliForkTestCaseLauncher", c, m.getName)).lineStream.foreach(println)
    })
  })

//  printSolutions()

  /**
    * Too slow
    */
  def printSolutions(): Unit = {
    val dir = new File("passingCond/")
    assert(dir.exists(), "please create the passingCond directory")
    var overallPassing = FeatureExprFactory.True
    val parser = new FeatureExprParser(FeatureExprFactory.bdd)
    for (f <- dir.listFiles()) {
      overallPassing = overallPassing.and(parser.parseFile(f))
    }
    println(V.getAllLowDegreeSolutions(overallPassing))
  }
}
