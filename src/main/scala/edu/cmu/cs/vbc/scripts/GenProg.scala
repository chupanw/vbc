package edu.cmu.cs.vbc.scripts

import java.io.{File, FileWriter}

import edu.cmu.cs.varex.VCache
import edu.cmu.cs.vbc.VBCClassLoader
import edu.cmu.cs.vbc.testutils.{TestLauncher, VTestStat}
import org.slf4j.LoggerFactory

/**
  * Automate the process of using VarexC to find GenProg patches
  *
  * To setup a project
  *   1. Record project name and main class name in the programs field
  *   2. Create pos.tests and neg.tests for the project
  *   3. Create the RelevantTest file for VarexC
  *   4. Copy varexc.jar (for the annotation) and modify the build file
  */
object GenProg extends App {

  val tmpConfigPath = "/tmp/tmp.config"
  val maxAttempts: Int = 10
  val osBase = args(0) // "Users" for the Mac and "home" for the Linux
  /**
    * Tuples of project path (e.g., median/0cea42f9/003/) and name of the main class
    */
  val programs: List[(String, String)] = List(
//    ("median/0cea42f9/003/", "introclassJava.median_0cea42f9_003")  // fixed
//    ("median/0cdfa335/003/", "introclassJava.median_0cdfa335_003")  // fixed
//    ("median/15cb07a7/003/", "introclassJava.median_15cb07a7_003")  // filtered, no positive tests
//    ("median/1b31fa5c/000/", "introclassJava.median_1b31fa5c_000")  // fixed by GenProg
//    ("median/1bf73a9c/000/", "introclassJava.median_1bf73a9c_000"), // filtered, no positive tests
//    ("median/1bf73a9c/003/", "introclassJava.median_1bf73a9c_003")  // fixed by GenProg
    ("median/1c2bb3a4/000/", "introclassJava.median_1c2bb3a4_000")  // todo try harder, memory issue
//    ("median/2c155667/000/", "introclassJava.median_2c155667_000")  // todo try harder
//    ("median/30074a0e/000/", "introclassJava.median_30074a0e_000"), // filtered, no positive tests
//    ("median/317aa705/000/", "introclassJava.median_317aa705_000"), // todo scalability, too many ors
//    ("median/317aa705/002/", "introclassJava.median_317aa705_002"),
//    ("median/317aa705/003/", "introclassJava.median_317aa705_003"),
//    ("median/36d8008b/000/", "introclassJava.median_36d8008b_000"),
//    ("median/3b2376ab/003/", "introclassJava.median_3b2376ab_003"),
//    ("median/3b2376ab/006/", "introclassJava.median_3b2376ab_006"),
//    ("median/3cf6d33a/007/", "introclassJava.median_3cf6d33a_007"),
//    ("median/48b82975/000/", "introclassJava.median_48b82975_000"),
//    ("median/68eb0bb0/000/", "introclassJava.median_68eb0bb0_000"),
//    ("median/6aaeaf2f/000/", "introclassJava.median_6aaeaf2f_000"),
//    ("median/6e464f2b/003/", "introclassJava.median_6e464f2b_003")
//    ("median/89b1a701/003/", "introclassJava.median_89b1a701_003"),
//    ("median/89b1a701/007/", "introclassJava.median_89b1a701_007"),
//    ("median/89b1a701/010/", "introclassJava.median_89b1a701_010"),
//    ("median/9013bd3b/000/", "introclassJava.median_9013bd3b_000"),
//    ("median/90834803/003/", "introclassJava.median_90834803_003"),
//    ("median/90834803/010/", "introclassJava.median_90834803_010"),
//    ("median/90834803/015/", "introclassJava.median_90834803_015"),
//    ("median/90a14c1a/000/", "introclassJava.median_90a14c1a_000"),
//    ("median/93f87bf2/010/", "introclassJava.median_93f87bf2_010"),
//    ("median/93f87bf2/012/", "introclassJava.median_93f87bf2_012"),
//    ("median/93f87bf2/015/", "introclassJava.median_93f87bf2_015"),
//    ("median/95362737/000/", "introclassJava.median_95362737_000"),
//    ("median/95362737/003/", "introclassJava.median_95362737_003"),
//    ("median/9c9308d4/003/", "introclassJava.median_9c9308d4_003"),
//    ("median/9c9308d4/007/", "introclassJava.median_9c9308d4_007"),
//    ("median/9c9308d4/012/", "introclassJava.median_9c9308d4_012"),
//    ("median/aaceaf4a/003/", "introclassJava.median_aaceaf4a_003"),
//    ("median/af81ffd4/004/", "introclassJava.median_af81ffd4_004"),
//    ("median/af81ffd4/007/", "introclassJava.median_af81ffd4_007"),
//    ("median/b6fd408d/000/", "introclassJava.median_b6fd408d_000"),
//    ("median/b6fd408d/001/", "introclassJava.median_b6fd408d_001"),
//    ("median/c716ee61/000/", "introclassJava.median_c716ee61_000"),
//    ("median/c716ee61/001/", "introclassJava.median_c716ee61_001"),
//    ("median/c716ee61/002/", "introclassJava.median_c716ee61_002"),
//    ("median/cd2d9b5b/010/", "introclassJava.median_cd2d9b5b_010"),
//    ("median/d009aa71/000/", "introclassJava.median_d009aa71_000"),
//    ("median/d120480a/000/", "introclassJava.median_d120480a_000"),
//    ("median/d2b889e1/000/", "introclassJava.median_d2b889e1_000"),
//    ("median/d43d3207/000/", "introclassJava.median_d43d3207_000"),
//    ("median/d4aae191/000/", "introclassJava.median_d4aae191_000"),
//    ("median/e9c6206d/000/", "introclassJava.median_e9c6206d_000"),
//    ("median/e9c6206d/001/", "introclassJava.median_e9c6206d_001"),
//    ("median/fcf701e8/000/", "introclassJava.median_fcf701e8_000"),
//    ("median/fcf701e8/002/", "introclassJava.median_fcf701e8_002"),
//    ("median/fcf701e8/003/", "introclassJava.median_fcf701e8_003"),
//    ("median/fe9d5fb9/000/", "introclassJava.median_fe9d5fb9_000"),
//    ("median/fe9d5fb9/002/", "introclassJava.median_fe9d5fb9_002")
  )
  val logger = LoggerFactory.getLogger("genprog")

  programs.foreach(pair => go(pair._1, pair._2, 0))


  def go(project: String, mainClass: String, i: Int): Unit = {
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
    if (!succeeded && i < maxAttempts) go(project, mainClass, i + 1)
  }

  def cleanUp(): Unit = {
    VTestStat.clear()
    VCache.clearAll()
    VBCClassLoader.clearCache()
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
    TestLauncher.main(Array(args._1 + "/", args._2.init.substring(1)))
    VTestStat.hasOverallSolution
  }

  def generateGenProgConfigFile(project: String, mainClass: String, seed: Long): Unit = {
    val template =
      s"""
        |javaVM = /usr/bin/java
        |popsize = 500
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

