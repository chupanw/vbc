package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.{GlobalConfig, VERuntime}

object MonopoliTestLauncher extends App {
  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

//  val main = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/monopoli100.jar"
//  val test = "/Users/chupanw/Projects/mutationtest-varex/code-ut/jars/monopoli100.jar"

  val main = "/tmp/Monopooly/bin/"
  val test = "/tmp/Monopooly/bin/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, main, test, config = Some("monopoli.conf"), useModel = true)
  VERuntime.classloader = Some(testLoader)

  val tests = List(
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsMatarCubiertos"
//    "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTest",
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
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsMatarCubiertos",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTest",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida1",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestPartida3",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.DadosTest",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosYo",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestEjerciciosMuchasTarjetas",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.grupos.GrupoATests",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TableroTestPartida2",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestsParaCubrirMutantes",
        "edu.uclm.esi.iso5.juegos.monopoly.dominio.tests.TestCubrirTodos"

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
    val testClass = new TestClass(testLoader.loadClass(x))
    testClass.runTests()
  }

  if (GlobalConfig.printTestResults) VTestStat.printToConsole()
}
