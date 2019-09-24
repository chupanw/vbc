package edu.cmu.cs.vbc.scripts

import java.io.{File, FileWriter}
import java.util.concurrent.{FutureTask, TimeUnit}

import edu.cmu.cs.varex.VCache
import edu.cmu.cs.varex.mtbdd.MTBDDFactory
import edu.cmu.cs.vbc.VBCClassLoader
import edu.cmu.cs.vbc.testutils.{TestLauncher, VTestStat}
import org.slf4j.LoggerFactory

import scala.concurrent.TimeoutException

/**
  * Automate the process of using VarexC to find GenProg patches
  *
  * To setup a project
  *   1. Record project name and main class name in the programs field
  *   2. Create pos.tests and neg.tests for the project
  *   3. Create the RelevantTest file for VarexC
  *   4. Copy varexc.jar (for the annotation) and modify the build file
  */
object GenProgVarexC extends App {

  // Configurable stuff
  val tmpConfigPath = "/tmp/tmp.config"
  val maxAttempts: Int = 1
  val osBase = args(0) // "Users" for the Mac and "home" for the Linux
  val popSize = 300
  val timeoutDigit: Long = -1
  val timeoutUnit: TimeUnit = TimeUnit.MINUTES

  val logger = LoggerFactory.getLogger("genprog")

  val programs: List[String] = Digits.debug

  programs.foreach(x => go(x, 1))


  def go(project: String, i: Int): Unit = {
    val mainClass = "introclassJava." + project.init.replace('/', '_')
    val seed = System.currentTimeMillis()
    logger.info("Cleaning up...")
    cleanUp()
    logger.info(s"Project: $project")
    logger.info(s"Attempt: $i")
    logger.info(s"Seed: $seed")
    logger.info("Generating config file for GenProg")
    generateGenProgConfigFile(project, mainClass, seed)
    logger.info("Running GenProg...")
    runGenProg()
    logger.info("Running VarexC")
    val succeeded = runVarexC(project)
    if (!succeeded && i < maxAttempts) go(project, i + 1)
  }

  def cleanUp(): Unit = {
    VTestStat.clear()
    VCache.clearAll()
    VBCClassLoader.clearCache()
    MTBDDFactory.clearCache()
  }

  def runGenProg(): Unit = {
    import scala.sys.process._
    val baseDir = s"/$osBase/chupanw/Projects/genprog4java"
    val serCache = new File("testcache.ser")
    assert(!serCache.exists() || serCache.delete() == true)
    val jar = baseDir + "/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar"
    val jvmOps = s"-ea -Dlog4j.configuration=/$osBase/chupanw/Projects/genprog4java/src/log4j.properties"
    assert(s"java $jvmOps -jar $jar $tmpConfigPath".! == 0)
  }

  def runVarexC(project: String): Boolean = {
    import scala.sys.process._
    def getLastVariant(path: String): File = {
      val dir = new File(path)
      dir.listFiles().filter(
        x => x.isDirectory && x.getName.startsWith("variant")
      ).sortWith((x, y) => x.getName.substring("variant".length).toInt < y.getName.substring("variant".length).toInt).last
    }
    // copy source files to the working directory
    val variantsPath = s"/$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/${project}tmp/"
    val destProject = s"/$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava-VarexC/dataset/${project}"
    val mergedDir = getLastVariant(variantsPath).getAbsolutePath
    s"cp -r $mergedDir/introclassJava ${destProject}src/main/java/".!
    s"cp -r $mergedDir/varexc ${destProject}src/main/java/".!
    try {
      Process(Seq("mvn", "test"), new File(destProject)).lineStream.foreach(println)
    } catch {
      case x: RuntimeException if x.getMessage.contains("Nonzero exit code") => // we expect maven test to fail
    }
    val args = destProject.splitAt(destProject.init.lastIndexOf('/'))
    if (timeoutDigit > 0) {
      try {
        val res = new FutureTask[Boolean](new Runnable {
          override def run(): Unit = TestLauncher.main(Array(args._1 + "/", args._2.init.substring(1)))
        }, true)
        res.get(timeoutDigit, timeoutUnit)
      } catch {
        case _: TimeoutException =>
          logger.info(s"Terminating after $timeoutDigit $timeoutUnit...")
          false
        case e => throw e
      }
    } else {
      TestLauncher.main(Array(args._1 + "/", args._2.init.substring(1)))
      VTestStat.hasOverallSolution
    }
  }

  def generateGenProgConfigFile(project: String, mainClass: String, seed: Long): Unit = {
    val template =
      s"""
        |javaVM = /usr/bin/java
        |popsize = $popSize
        |seed = $seed
        |classTestFolder = target/test-classes
        |workingDir = /$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/$project
        |outputDir = /$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/${project}tmp
        |cleanUpVariants = true
        |libs=/$osBase/chupanw/Projects/genprog4java/lib/hamcrest-core-1.3.jar:/$osBase/chupanw/Projects/genprog4java/lib/junit-4.12.jar:/$osBase/chupanw/Projects/genprog4java/lib/junittestrunner.jar:/$osBase/chupanw/Projects/genprog4java/lib/varexc.jar
        |sanity = yes
        |sourceDir = src/main/java
        |positiveTests = /$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/${project}pos.tests
        |negativeTests = /$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/${project}neg.tests
        |jacocoPath = /$osBase/chupanw/Projects/genprog4java/lib/jacocoagent.jar
        |testClassPath=/$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/${project}target/test-classes/
        |testGranularity = method
        |targetClassName = $mainClass
        |sourceVersion=1.8
        |generations=1
        |edits = append;delete;replace;expadd;exprem;exprep;boundswitch,5.0;
        |regenPaths = true
      """.stripMargin

    val writer = new FileWriter(new File(tmpConfigPath))
    writer.write(template)
    writer.close()
  }
}

