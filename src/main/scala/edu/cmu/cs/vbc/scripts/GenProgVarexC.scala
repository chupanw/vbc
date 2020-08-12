package edu.cmu.cs.vbc.scripts

import java.io.{File, FileWriter}
import java.nio.file.{FileSystems, Files, Path, StandardCopyOption}

import edu.cmu.cs.varex.VCache
import edu.cmu.cs.varex.mtbdd.MTBDDFactory
import edu.cmu.cs.vbc.VBCClassLoader
import edu.cmu.cs.vbc.testutils.{ApacheMathBugs, ApacheMathLauncher, IntroClassLauncher, VTestStat}
import org.slf4j.{Logger, LoggerFactory}

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

  protected val seed: Long = System.currentTimeMillis()
  protected def genProgConfig: String = template(project, seed)

  val logger: Logger = LoggerFactory.getLogger("varexc")

  def go(attempt: Int): Unit = {
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
    writer.write(genProgConfig)
    writer.close()
  }

  def step3_RunGenProg(): Unit = {
    val serCache = new File("testcache.ser")
    assert(!serCache.exists() || serCache.delete())
    val jar          = mkPathString(genprogPath, "target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar")
    val jvmOps       = s"-ea -Dlog4j.configuration=${genprogPath}src/log4j.properties"
    val retCode      = s"java $jvmOps -jar $jar ${ScriptConfig.tmpConfigPath}".!
    val variantsPath = mkPath(projects4GenProg, project, "tmp")
    copyMutatedCode(getLastVariant(variantsPath))
    assert(retCode == 0, "Error running GenProg")
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
    val destProject = mkPath(projects4VarexC, project)
    Process(compileCMD, cwd = destProject.toFile).lazyLines.foreach(println)
    launch(Array(projects4VarexC, project))
    VTestStat.hasOverallSolution
}

  def notAvailable(fieldName: String): Nothing = throw new RuntimeException(s"The $fieldName field should not be used")
}

object IntroClassPatchRunner extends App with PatchRunner {
  override def genprogPath: String               = args(0)
  override def projects4GenProg: String          = args(1)
  override def projects4VarexC: String           = args(2)
  override def project: String                   = args(3)
  override def launch(args: Array[String]): Unit = IntroClassLauncher.main(args)

  override def compileCMD = Seq("mvn", "-DskipTests=true", "package")

  go(1)

  def template(project: String, seed: Long) = {
    val mainClass = "introclassJava." + project.init.replace('/', '_')
    s"""
       |javaVM = /usr/bin/java
       |popsize = ${ScriptConfig.popSize}
       |editMode = pre_compute
       |generations = 50
       |regenPaths = true
       |seed = $seed
       |classTestFolder = target/test-classes
       |workingDir = ${mkPathString(projects4GenProg, project)}
       |outputDir = ${mkPathString(projects4GenProg, project, "tmp")}
       |cleanUpVariants = true
       |libs=${mkPathString(genprogPath, "lib", "hamcrest-core-1.3.jar")}:${mkPathString(
         genprogPath,
         "lib",
         "junit-4.12.jar")}:${mkPathString(genprogPath, "lib", "junittestrunner.jar")}:${mkPathString(
         genprogPath,
         "lib",
         "varexc.jar")}
       |sanity = yes
       |sourceDir = src/main/java
       |positiveTests = ${mkPathString(projects4GenProg, project, "pos.tests")}
       |negativeTests = ${mkPathString(projects4GenProg, project, "neg.tests")}
       |jacocoPath = ${mkPathString(genprogPath, "lib", "jacocoagent.jar")}
       |srcClassPath = ${mkPathString(projects4GenProg, project, "target", "classes")}
       |classSourceFolder = ${mkPathString(projects4GenProg, project, "target", "classes")}
       |testClassPath = ${mkPathString(projects4GenProg, project, "target", "test-classes")}
       |testGranularity = method
       |targetClassName = $mainClass
       |sourceVersion=1.8
       |sample = 0.1
       |edits = append;delete;replace;aor;ror;lcr;uoi;abs;
      """.stripMargin
  }
}

object MathPatchRunner extends App with PatchRunner {
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
       |editMode = pre_compute
       |generations = 50
       |regenPaths = true
       |seed = ${seed}
       |classTestFolder = target/test-classes
       |workingDir = ${mkPathString(projects4GenProg, project)}
       |outputDir = ${mkPathString(projects4GenProg, project, "tmp")}
       |libs=${mkPathString(genprogPath, "lib", "hamcrest-core-1.3.jar")}:${mkPathString(
         genprogPath,
         "lib",
         "junit-4.12.jar")}:${mkPathString(genprogPath, "lib", "junittestrunner.jar")}:
       |sanity = yes
       |sourceDir = src/main/java
       |positiveTests = ${mkPathString(projects4GenProg, project, "pos.tests")}
       |negativeTests = ${mkPathString(projects4GenProg, project, "neg.tests")}
       |jacocoPath = ${mkPathString(genprogPath, "lib", "jacocoagent.jar")}
       |srcClassPath = ${mkPathString(projects4GenProg, project, "target", "classes")}
       |classSourceFolder = ${mkPathString(projects4GenProg, project, "target", "classes")}
       |testClassPath= ${mkPathString(projects4GenProg, project, "target", "test-classes")}
       |testGranularity = method
       |targetClassName = ${mkPathString(projects4GenProg, project, "targetClasses.txt")}
       |sourceVersion=1.8
       |sample=0.1
       |compileCommand=python3 ${mkPathString(projects4GenProg, project, "compile.py")}
       |edits=append;delete;replace;
       |
       |""".stripMargin

  go(1)
}

object GenProgVarexCBatch extends App {
//  val exit = "sbt assembly".!
//  assert(exit == 0, "Something wrong with sbt assembly")

  val pathGenProg          = args(0)
  val pathProjects4GenProg = args(1)
  val pathProjects4VarexC  = args(2)

  for (p <- ApacheMathBugs.debug) {
//    s"timelimit -t${ScriptConfig.timeout} -T${60} java -cp target/scala-2.13/vbc-assembly-0.1.0-SNAPSHOT.jar edu.cmu.cs.vbc.scripts.IntroClassPatchRunner $pathGenProg $pathProjects4GenProg $pathProjects4VarexC $p".!
    MathPatchRunner.main(args :+ p)
  }
}
