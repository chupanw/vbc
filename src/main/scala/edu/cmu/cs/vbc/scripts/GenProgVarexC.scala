package edu.cmu.cs.vbc.scripts

import java.io.{File, FileWriter}
import java.nio.file.{FileSystems, Files, Path, StandardCopyOption}
import java.util.concurrent.{Executors, FutureTask, TimeUnit}

import edu.cmu.cs.varex.VCache
import edu.cmu.cs.varex.mtbdd.MTBDDFactory
import edu.cmu.cs.vbc.VBCClassLoader
import edu.cmu.cs.vbc.testutils.{ApacheMathLauncher, IntroClassLauncher, VTestStat}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.TimeoutException
import scala.sys.process._

object ScriptConfig {
  val tmpConfigPath    = "/tmp/tmp.config"
  val maxAttempts: Int = 1
  val popSize          = 300
  val timeout: Long    = 3600 // in seconds
}

/**
  * Automate the process of using VarexC to find GenProg patches
  *
  * Note:
  *   Run `mvn package` to make sure the GenProg jar is up-to-date
  *   See below for a list of paths required
  */
trait PatchRunner {
  def genprogPath: String      // e.g., /Users/.../genprog4java/
  def projects4GenProg: String // e.g., /Users/.../Math-GenProg/
  def projects4VarexC: String  // e.g., /Users/.../Math-VarexC/
  def project: String          // e.g., checksum/e9c74e27/000/
  def launch(args: Array[String]): Unit
  def compileCMD: Seq[String]
  def template(project: String, seed: Long): String

  val logger: Logger = LoggerFactory.getLogger("genprog")

  def go(attempt: Int): Unit = {
    val seed = System.currentTimeMillis()
    logger.info(s"Project: $project")
    logger.info(s"Attempt: $attempt")
    logger.info(s"Seed: $seed")

    logger.info("Cleaning up...")
    step1_CleanUp()
    logger.info("Generating config file for GenProg")
    step2_GenerateGenProgConfigFile(seed)
    logger.info("Running GenProg...")
    step3_RunGenProg()
    logger.info("Running VarexC")
    val succeeded = step4_RunVarexC()
    if (!succeeded && attempt < ScriptConfig.maxAttempts) go(attempt + 1)
  }

  def step1_CleanUp(): Unit = {
    VTestStat.clear()
    VCache.clearAll()
    VBCClassLoader.clearCache()
    MTBDDFactory.clearCache()
  }

  def step2_GenerateGenProgConfigFile(seed: Long): Unit = {
    val writer = new FileWriter(new File(ScriptConfig.tmpConfigPath))
    writer.write(template(project, seed))
    writer.close()
  }

  def step3_RunGenProg(): Unit = {
    val serCache = new File("testcache.ser")
    assert(!serCache.exists() || serCache.delete())
    val jar    = genprogPath + "target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar"
    val jvmOps = s"-ea -Dlog4j.configuration=${genprogPath}src/log4j.properties"
    assert(s"java $jvmOps -jar $jar ${ScriptConfig.tmpConfigPath}".! == 0)
  }

  def mkPath(elems: String*): Path         = FileSystems.getDefault.getPath(elems.head, elems.tail: _*)
  def mkPathString(elems: String*): String = mkPath(elems: _*).toFile.getAbsolutePath

  def copyMutatedCode(variantFolder: Path /*e.g., tmp/variant999/ */ ): Unit = {
    def copy(absolute: Path): Unit = {
      val relative = mkPath(projects4GenProg).resolve(variantFolder).relativize(absolute)
      val dst      = mkPath(projects4VarexC, project, "src/main/java").resolve(relative)
      val folder   = dst.getParent.toFile
      if (!folder.exists()) folder.mkdirs()
      logger.info(s"Copying ${absolute} to ${dst}")
      Files.copy(absolute, dst, StandardCopyOption.REPLACE_EXISTING)
    }
    val variantFolderAbsolutePath = mkPath(projects4GenProg).resolve(variantFolder)
    Files.walk(variantFolderAbsolutePath).forEach(x => if (!x.toFile.isDirectory) copy(x))
  }

  def getLastVariant(path2Variants: Path): Path = {
    val dir = path2Variants.toFile
    val lastVariant: File = dir
      .listFiles()
      .filter(
        x => x.isDirectory && x.getName.startsWith("variant")
      )
      .sortWith((x, y) =>
        x.getName.substring("variant".length).toInt < y.getName.substring("variant".length).toInt)
      .last
    val lastVariantPath = mkPath(lastVariant.getAbsolutePath)
    mkPath(projects4GenProg).relativize(lastVariantPath)
  }

  def step4_RunVarexC(): Boolean = {
    // copy source files to the working directory
    val variantsPath = mkPath(projects4GenProg, project, "tmp")
    val destProject  = mkPath(projects4VarexC, project)
    copyMutatedCode(getLastVariant(variantsPath))
    Process(compileCMD, cwd = destProject.toFile).lazyLines.foreach(println)
    if (ScriptConfig.timeout > 0) {
      try {
        val executor = Executors.newFixedThreadPool(1)
        val res = new FutureTask[Boolean](
          new Runnable {
            override def run(): Unit =
              launch(Array(projects4VarexC, project.substring(0, project.length - 1))) // we assume project ends with '/'
          },
          VTestStat.hasOverallSolution
        )
        executor.submit(res)
        val ret = res.get(ScriptConfig.timeout, TimeUnit.SECONDS)
        executor.shutdown()
        ret
      } catch {
        case _: TimeoutException =>
          logger.info(s"Terminating after ${ScriptConfig.timeout} ${TimeUnit.SECONDS}...")
          false
        case e => throw e
      }
    } else {
      IntroClassLauncher.main(Array(projects4GenProg, project))
      VTestStat.hasOverallSolution
    }
  }

}

object IntroClassPatchRunner extends App with PatchRunner {
  assume(args.forall(x => x.endsWith("/")))
  override def genprogPath: String               = args(0)
  override def projects4GenProg: String          = args(1)
  override def projects4VarexC: String           = args(2)
  override def project: String                   = args(3)
  override def launch(args: Array[String]): Unit = IntroClassLauncher.main(args)

  override def compileCMD = ???

  go(0)

  def template(project: String, seed: Long) = {
    val mainClass = "introclassJava." + project.init.replace('/', '_')
    s"""
       |javaVM = /usr/bin/java
       |popsize = ${ScriptConfig.popSize}
       |seed = $seed
       |classTestFolder = target/test-classes
       |workingDir = $projects4GenProg$project
       |outputDir = $projects4GenProg${project}tmp
       |cleanUpVariants = true
       |libs=${genprogPath}lib/hamcrest-core-1.3.jar:${genprogPath}lib/junit-4.12.jar:${genprogPath}lib/junittestrunner.jar:${genprogPath}lib/varexc.jar
       |sanity = yes
       |sourceDir = src/main/java
       |positiveTests = ${projects4GenProg}${project}pos.tests
       |negativeTests = ${projects4GenProg}${project}neg.tests
       |jacocoPath = ${genprogPath}lib/jacocoagent.jar
       |testClassPath=${projects4GenProg}${project}target/test-classes/
       |testGranularity = method
       |targetClassName = $mainClass
       |sourceVersion=1.8
       |generations=1
       |edits = append;delete;replace;expadd;exprem;exprep;boundswitch,5.0;
       |regenPaths = true
      """.stripMargin
  }
}

object MathPatchRunner extends App with PatchRunner {
  assume(args.forall(x => x.endsWith("/")))
  override def genprogPath: String               = args(0)
  override def projects4GenProg: String          = args(1)
  override def projects4VarexC: String           = args(2)
  override def project: String                   = args(3)
  override def launch(args: Array[String]): Unit = ApacheMathLauncher.main(args)
  override def compileCMD                        = Seq("ant", "compile.tests")
  override def template(project: String, seed: Long): String =
    s"""
       |javaVM = /usr/bin/java
       |popsize = ${ScriptConfig.popSize}
       |seed = ${seed}
       |classTestFolder = target/test-classes
       |workingDir = ${projects4GenProg}${project}
       |outputDir = ${projects4GenProg}${project}tmp
       |libs=${genprogPath}lib/hamcrest-core-1.3.jar:${genprogPath}lib/junit-4.12.jar:${genprogPath}lib/junittestrunner.jar:
       |sanity = yes
       |sourceDir = src/main/java
       |positiveTests = ${projects4GenProg}${project}pos.tests
       |negativeTests = ${projects4GenProg}${project}neg.tests
       |jacocoPath = ${genprogPath}lib/jacocoagent.jar
       |srcClassPath = ${projects4GenProg}${project}target/classes
       |classSourceFolder = ${projects4GenProg}${project}target/classes
       |testClassPath= ${projects4GenProg}${project}target/test-classes
       |testGranularity = method
       |targetClassName = ${projects4GenProg}${project}targetClasses.txt
       |sourceVersion=1.8
       |generations=1
       |compileCommand=python3 ${projects4GenProg}${project}compile.py
       |edits=append;delete;replace;expadd;exprem;exprep;boundswitch,5.0;
       |debug=true
       |
       |""".stripMargin

  go(0)
}

object GenProgVarexCBatch extends App {
  val exit = "sbt assembly".!
  assert(exit == 0, "Something wrong with sbt assembly")

  val baseDir = args(0)

  for (p <- Checksum.runnable) {
    s"timelimit -t${ScriptConfig.timeout} -T${60} java -cp target/scala-2.11/vbc-assembly-0.1.0-SNAPSHOT.jar edu.cmu.cs.vbc.scripts.GenProgVarexCSingle $baseDir $p".!
  }
}
