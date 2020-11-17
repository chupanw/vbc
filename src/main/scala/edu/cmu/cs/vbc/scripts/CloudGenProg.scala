package edu.cmu.cs.vbc.scripts

import java.io.{File, FileWriter}
import java.nio.file.Path

import scala.sys.process.Process

object IntroClassCloudPatchGenerator extends App with CloudPatchGenerator {
  override def genprogPath: String = args(0)
  override def projects4GenProg: String = args(1)
  override def projects4VarexC: String = args(2)
  override def project: String = args(3)
  override def numMut: NumMutations = Eight
  override def relevantTestFilePathString: String = {
    val split = project.split('/')
    mkPathString(projects4VarexC, split(0), split(1), "RelevantTests", split(2) + ".txt")
  }
  override def mongoCollectionName = {
    val split = project.split('/')
    s"${split(0)}-$numMut"
  }
  override def template(project: String, seed: Long): String = {
    val mainClass = "introclassJava." + project.init.replace('/', '_')
    s"""
       |javaVM = /usr/bin/java
       |popsize = 500
       |editMode = pre_compute
       |generations = 20
       |regenPaths = true
       |continue = true
       |seed = $seed
       |classTestFolder = target/test-classes
       |workingDir = ${mkPathString(projects4GenProg, project)}
       |outputDir = ${mkPathString(projects4GenProg, project, "tmp")}
       |cleanUpVariants = true
       |libs=${mkPathString(genprogPath, "lib", "hamcrest-core-1.3.jar")}:${mkPathString(genprogPath, "lib", "junit-4.12.jar")}:${mkPathString(genprogPath, "lib", "junittestrunner.jar")}:${mkPathString(genprogPath, "lib", "varexc.jar")}
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
       |edits = ${edits()}
      """.stripMargin
  }
  assert(project.endsWith("/"))
  start()

}

object IntroClassBatch extends App {
  println("Input kind (median, smallest, grade, digits, checksum, syllables): ")
  val kind = scala.io.StdIn.readLine().trim.toLowerCase()
  val projects = kind match {
    case "median" => Median.runnable
    case "smallest" => Smallest.runnable
    case "grade" => Grade.runnable
    case "digits" => Digits.runnable
    case "checksum" => Checksum.runnable
    case "syllables" => Syllables.runnable
  }
  for (p <- projects) {
    IntroClassCloudPatchGenerator.main(Array(args(0), args(1), args(2), p))
  }
}

object IntroClassGenProgCloudPatchRunner extends App with CloudPatchRunner {
  override def launch(args: Array[String]): Unit = {
    val collectionName = args(0)
    val projectName = args(1)
    val threeHoursInMS: Long = 3 * 3600 * 1000 // three hours limit
    val genprogJarPath = mkPath(System.getProperty("java.io.tmpdir"), projectName, "uber-GenProg4Java-0.0.1-SNAPSHOT.jar")
    downloadJar(
      "https://github.com/chupanw/genprog4java/blob/master/uber-GenProg4Java-0.0.1-SNAPSHOT.jar?raw=true",
      genprogJarPath
    )
    val genprogCMD = Seq("java", "-jar", "uber-GenProg4Java-0.0.1-SNAPSHOT.jar", "tmp.config")
    var attempt = 0
    val startTime = System.currentTimeMillis()
    while (attempt < 20) {  // at most 20 different seeds
      val template = genTemplate(collectionName, projectName, System.currentTimeMillis())
      val writer = new FileWriter(new File(mkPathString(System.getProperty("java.io.tmpdir"), projectName, "tmp.config")))
      writer.write(template)
      writer.close()
      val testCacheFile = new File(mkPathString(System.getProperty("java.io.tmpdir"), projectName, "testcache.ser"))
      if (testCacheFile.exists()) testCacheFile.delete()
      Process(genprogCMD, cwd = mkPath(System.getProperty("java.io.tmpdir"), projectName).toFile).lazyLines.foreach(printlnAndLog)
      val duration = System.currentTimeMillis() - startTime
      if (duration >= threeHoursInMS) return
      attempt += 1
    }
  }
  override def bfLaunch(args: Array[String]): Unit = {}

  override def compileCMD(projectName: String): Seq[String] = Seq("mvn", "-DskipTests=true", "-Dmaven.repo.local=/tmp/.m2/repository", "package")

  def downloadJar(url: String, path: Path): Unit = {
    import sys.process._
    import java.net.URL
    (new URL(url) #> path.toFile).!!
  }

  /**
    * Generate a template that works in Docker
    *
    * @param collectionName e.g., median-8mut-genprog
    * @param projectName  e.g., median-0cdfa335-003
    */
  def genTemplate(collectionName: String, projectName: String, seed: Long): String = {
    val edits = if (collectionName.contains("-8mut-")) "append;delete;replace;aor;ror;lcr;uoi;abs;" else "append;delete;replace;"
    val mainClass = "introclassJava." + projectName.replace('-', '_')
    s"""
       |javaVM = /usr/bin/java
       |popsize = 40
       |editMode = existing
       |generations = 20
       |regenPaths = false
       |continue = true
       |seed = $seed
       |classTestFolder = target/test-classes
       |workingDir = ${mkPathString("/tmp", projectName)}
       |outputDir = ${mkPathString("/tmp", projectName, "tmp")}
       |cleanUpVariants = false
       |libs=${mkPathString("/tmp", projectName, "hamcrest-core-1.3.jar")}:${mkPathString("/tmp", projectName, "junit-4.12.jar")}:${mkPathString("/tmp", projectName, "junittestrunner.jar")}:${mkPathString("/tmp", projectName, "varexc.jar")}
       |sanity = yes
       |sourceDir = src/main/java
       |positiveTests = ${mkPathString("/tmp", projectName, "pos.tests")}
       |negativeTests = ${mkPathString("/tmp", projectName, "neg.tests")}
       |jacocoPath = ${mkPathString("/tmp", projectName, "jacocoagent.jar")}
       |srcClassPath = ${mkPathString("/tmp", projectName, "target", "classes")}
       |classSourceFolder = ${mkPathString("/tmp", projectName, "target", "classes")}
       |testClassPath = ${mkPathString("/tmp", projectName, "target", "test-classes")}
       |testGranularity = method
       |targetClassName = $mainClass
       |sourceVersion=1.8
       |sample = 0.1
       |edits = ${edits}
      """.stripMargin
  }

  run()
}