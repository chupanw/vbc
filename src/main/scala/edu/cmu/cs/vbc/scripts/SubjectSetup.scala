package edu.cmu.cs.vbc.scripts

import java.io.{File, FileWriter}
import java.net.URLClassLoader
import java.nio.file.{FileSystems, Files, StandardCopyOption}

import scala.reflect.io.Directory
import java.nio.file.Path
import org.apache.commons.io.FileUtils
import ch.qos.logback.core.util.FileUtil

/**
  * To setup a project
  *   1. Record project name and main class name in the programs field  (automatic, see [[ProjectLister]])
  *   2.1 Change the number of black and white tests below (manual)
  *   2.2 Create pos.tests and neg.tests for the project (automatic)
  *   3. Create the RelevantTest file for VarexC  (automatic)
  *   4. Copy varexc.jar (for the annotation) and modify the build file (i.e., add dependency and change to JDK 8)
  */
object SubjectSetup extends App {

  val osBase  = args(0)
  val base    = s"/$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/"
  val baseVBC = s"/$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava-VarexC/dataset/"

  val projects = Syllables.runnable

  /**
    * Unless we have a better way to get passing tests
    *
    * The following only works for IntroClass projects, need to tweak the number of black/white tests though
    */
  val blackTests = (1 to 6).toList
  val whiteTests = (1 to 10).toList
  def getPosFromNeg(project: String, neg: List[String]): List[String] = {
    val blackClass  = getBlackTestClass(project)
    val whiteClass  = getWhiteTestClass(project)
    val blackFailed = neg.filter(_.contains("Black")).map(_.split("::")(1).substring(4).toInt)
    val whiteFailed = neg.filter(_.contains("White")).map(_.split("::")(1).substring(4).toInt)
    val blackPos    = for (i <- blackTests if !blackFailed.contains(i)) yield blackClass + s"::test$i"
    val whitePos    = for (i <- whiteTests if !whiteFailed.contains(i)) yield whiteClass + s"::test$i"
    blackPos ::: whitePos
  }
  def getBlackTestClass(project: String): String = {
    val p = project.substring(0, project.length - 1).replace('/', '_')
    "introclassJava." + p + "BlackboxTest"
  }
  def getWhiteTestClass(project: String): String = {
    val p = project.substring(0, project.length - 1).replace('/', '_')
    "introclassJava." + p + "WhiteboxTest"
  }
  def createRelevantTests(project: String): Unit = {
    val pd      = project.substring(0, project.init.lastIndexOf("/") + 1)
    val version = project.substring(pd.length, project.length - 1)
    val rtDir   = new File(baseVBC + pd + "RelevantTests")
    if (!rtDir.exists()) rtDir.mkdir()
    val writer = new FileWriter(new File(rtDir.getPath + "/" + version + ".txt"))
    println(s"Generating ${rtDir.getPath}/$version.txt")
    writer.write(getBlackTestClass(project) + "\n")
    writer.write(getWhiteTestClass(project) + "\n")
    writer.close()
  }

  var failed: List[String]  = Nil
  var skipped: List[String] = Nil

  run()

  def run(): Unit = {
    var current: String = ""
    try {
      for (p <- projects) {
        println(s"Project: $p")
        current = p
        initProject(p)
//        generatePosNeg(p)
//        createRelevantTests(p)
//        setupVBC(p)
//        println()
      }
    } catch {
      case a: AssertionError => a.printStackTrace(); failed = current :: failed
      case e                 => throw e
    }
    println("Failed: ")
    println(failed)
    println("Skipped: ")
    println(skipped)
  }

  def initProject(project: String): Unit = {
    def mkPath(elems: String*): java.nio.file.Path = java.nio.file.FileSystems.getDefault.getPath(elems.head, elems.tail:_*)
    val projectPath = mkPath(base, project)
    val varexcProjectPath = mkPath(baseVBC, project)
    scala.sys.process.Process(Seq("mvn", "clean"), projectPath.toFile).lazyLines.foreach(println)
    try {
      scala.sys.process.Process(Seq("mvn", "test"), projectPath.toFile).lazyLines.foreach(println)
    } catch {
      case _: Throwable => // expected test fail
    }
    scala.sys.process.Process(Seq("mvn", "clean"), varexcProjectPath.toFile).lazyLines.foreach(println)
    val tmpFolder = mkPath(projectPath.toFile.getAbsolutePath, "tmp").toFile
    if (tmpFolder.exists() || tmpFolder.isDirectory)
      new Directory(tmpFolder).deleteRecursively()
  }

  def generatePosNeg(project: String): Unit = {
    val projectPath = base + project
    println("Running Maven...")
    val mavenOutput = runMaven(projectPath)
    println("Parsing Maven output...")
    val resultsLineOpt = mavenOutput.find(x => x == "Results :")
    assert(resultsLineOpt.isDefined)
    val restMavenOutput = mavenOutput.dropWhile(_ != resultsLineOpt.get)
    val statLineOpt     = restMavenOutput.find(s => s.startsWith("Tests run: "))
    assert(statLineOpt.isDefined, "No stat line when running maven")
    val statLine = statLineOpt.get
    println(statLine)
    val nums = statLine.split(",").map(x => x.substring(x.indexOf(":") + 1).trim).map(_.toInt)
    assert(nums.length == 4) // total, failures, errors, skipped
    val nPass = nums(0) - nums(1) - nums(2) - nums(3)
    if (nPass == 0) {
      println("No positive test, skipping...")
      skipped = project :: skipped
    } else {
      val failLines = restMavenOutput.drop(2).takeWhile(x => x != statLine).init
      val neg = for (l <- failLines) yield {
        val ll        = if (l.startsWith("Failed tests:")) l.substring(13) else l
        val tC        = ll.split(":")(0).trim
        val testCase  = tC.substring(0, tC.indexOf("("))
        val testClass = tC.substring(testCase.length + 1, tC.length - 1)
        testClass + "::" + testCase
      }
      val pos = getPosFromNeg(project, neg)
      println("Generating neg.tests...")
      val negFile = new FileWriter(new File(projectPath + "neg.tests"))
      neg.foreach(x => negFile.write(x + "\n"))
      negFile.close()
      println("Generating pos.tests...")
      val posFile = new FileWriter(new File(projectPath + "pos.tests"))
      pos.foreach(x => posFile.write(x + "\n"))
      posFile.close()
    }
  }

  private def runMaven(projectPath: String): List[String] = {
    val lines = collection.mutable.ListBuffer[String]()
    try {
      scala.sys.process
        .Process(Seq("mvn", "test"), new File(projectPath))
        .lineStream
        .foreach(x => lines += x)
    } catch {
      case r: RuntimeException if r.getMessage.contains("Nonzero exit code:") => // do noting, maven returns nonzero exit code when tests fail
      case e: Exception                                                       => throw e
    }
    lines.toList
  }

  def setupVBC(project: String): Unit = {
    import scala.sys.process._
    val pn = project.substring(0, project.indexOf("/") + 1)
    println("Copying varexc.jar...")
    val jar  = s"${baseVBC}${pn}varexc.jar"
    val pom  = s"${baseVBC}${pn}pom.xml"
    val dest = s"${baseVBC}${project}"
    assert(s"cp $jar $dest".! == 0)
    val oldPOM = s"${baseVBC}${project}pom.xml"
    val newPOM = s"${baseVBC}${project}pom.bak"
    if (!new File(newPOM).exists()) {
      println("Copying pom.xml...")
      assert(s"mv $oldPOM $newPOM".! == 0)
      assert(s"cp $pom $dest".! == 0)
    }
  }

}

object ProjectLister extends App {
  val datasetDir = "/Users/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/"
  val project    = "syllables"

  /**
    * Assume the project structure like this: .../dataset/digit/07045530/000
    * @return a list of versions used in [[SubjectSetup]] and [[GenProgVarexC]]
    */
  def listProjects: List[String] = {
    val dir = new File(datasetDir + project)
    (for (d <- dir.listFiles().toList if d.isDirectory && d.getName != "ref") yield {
      d.listFiles()
        .toList
        .filter(_.isDirectory)
        .map(x => "\"" + s"$project/${d.getName}/${x.getName}/" + "\"")
    }).flatten
  }

  println(listProjects.mkString("List(\n  ", ",\n  ", "\n)"))
}

object ProjectRenamer extends App {
  val datasetDir = "/Users/chupanw/Projects/Data/PatchStudy/IntroClassJava-VarexC/dataset/"
  val project    = "syllables"

  def rename(): Unit = {
    val dir = new File(datasetDir + project)
    for (d <- dir.listFiles() if d.isDirectory && d.getName.length > 8) {
      import scala.sys.process._
      println(s"Renaming $project ${d.getName}")
      Process(s"git mv ${d.getName} ${d.getName.substring(0, 8)}", dir).!!
    }
  }

  rename()
}

/**
  * Setup Apache Math projects for VarexGenProg
  *
  * Takes two paths of the folders that contain all math projects as parameters, one for GenProg and one for VarexC
  *
  * 1. Extract relevant tests from Defects4J
  * 2. Copy varexc.jar into each project  (expect a varexc.jar in the varexc folder)
  * 3. Prepare pos and neg tests
  */
object MathSetup extends App {
  import scala.sys.process._

  assume(args.length == 3 && args(0).endsWith("/") && args(1).endsWith("/"),
         "Please provide the folder that contains all math projects as parameter")

  val genprogFolder     = args(0)
  val varexcFolder      = args(1)
  val compileScriptPath = args(2)

  // main
//  writeRelevantTests()
//  copyVarexCJar()
//  compileProjects()
//  extractPosNegTests()
//  genTargetClasses()
  // setupCompileScript()
//  restoreAntBuild()
//  modifyAntBuild()
// addVarexCJartoBuild()
// checkTargetClasses()
// copyJars()
// cleanupProjects()

  def listProjects(path: String): List[File] = {
    val folder = new File(path)
    folder.listFiles().filter(_.isDirectory).filterNot(_.getName == "RelevantTests").toList
  }

  def writeRelevantTests(): Unit = {
    val projects       = listProjects(varexcFolder)
    val relTestsFolder = new File(varexcFolder + "RelevantTests/")
    if (!relTestsFolder.exists()) {
      assert(relTestsFolder.mkdir(), s"failed to create folder: $relTestsFolder")
    }
    for (p <- projects) {
      val fp = new File(relTestsFolder.getAbsolutePath + "/" + p.getName + ".txt")
      val f  = new FileWriter(fp)
      println(s"Writing relevant tests to ${fp}")
      Process(Seq("defects4j", "export", "-p", "tests.relevant"), cwd = Some(p)).lineStream
        .foreach(l => f.write(l + "\n"))
      f.close()
    }
  }

  def copyVarexCJar(): Unit = {
    val projects = listProjects(varexcFolder)
    for (p <- projects) {
      val libFolder = new File(p.getAbsolutePath + "/lib/")
      if (!libFolder.exists())
        assert(libFolder.mkdir(), s"failed to create folder: ${libFolder.getAbsoluteFile}")
      println(s"Copying varexc.jar to ${libFolder.getAbsolutePath}")
      val cmd = s"cp ${varexcFolder}/varexc.jar ${libFolder.getAbsolutePath}"
      assert(cmd.! == 0, s"Error in command: $cmd")
    }
  }

  def cleanupProjects(): Unit = {
    val projects = listProjects(varexcFolder).sortBy(_.getName().split('-')(1).init.toInt)
    for (p <- projects) {
      println(s"Running ant clean in ${p.getAbsolutePath}")
      Process(Seq("ant", "clean"), cwd = Some(p)).!!
    }

    val genprogProjects = listProjects(genprogFolder).sortBy(_.getName().split('-')(1).init.toInt)
    for (p <- genprogProjects) {
      println(s"Running ant clean in ${p.getAbsolutePath}")
      Process(Seq("ant", "clean"), cwd = Some(p)).!!

      val tmpDir = mkPath(p.getAbsolutePath(), "tmp").toFile()
      if (tmpDir.exists()) {
        println("deleting " + p.getAbsolutePath())
        FileUtils.deleteDirectory(tmpDir)
      }
    }
  }

  /**
    * For some reasons this can fail for some bugs if non-ASCII characters are used.
    *
    * If that's the case, manually execute `defects4j compile` in console or execute this program in console via sbt
    */
  def compileProjects(): Unit = {
    val projects = listProjects(genprogFolder)
    for (p <- projects) {
      println(s"Running defects4j test in ${p.getAbsolutePath}")
      Process(Seq("defects4j", "test"), cwd = Some(p)).!
    }
  }

  def copyJars(): Unit = {
    val projects = listProjects(varexcFolder).sortBy(_.getName().split('-')(1).init.toInt)
    for (p <- projects.filterNot(_.getName().endsWith("-1b"))) {
      val args4jPath = mkPath("/home/chupanw/Projects/Data/PatchStudy/Closure-VarexC", "Closure-1b", "lib", "args4j.jar")
      val args4jNPath = mkPath(p.getAbsolutePath(), "lib", "args4j.jar")
      FileUtils.copyFile(args4jPath.toFile(), args4jNPath.toFile())

      val guavaPath = mkPath("/home/chupanw/Projects/Data/PatchStudy/Closure-VarexC", "Closure-1b", "lib", "guava.jar")
      val guavaNPath = mkPath(p.getAbsolutePath(), "lib", "guava.jar")
      FileUtils.copyFile(guavaPath.toFile(), guavaNPath.toFile())

      val junitPath = mkPath("/home/chupanw/Projects/Data/PatchStudy/Closure-VarexC", "Closure-1b", "lib", "junit.jar")
      val junitNPath = mkPath(p.getAbsolutePath(), "lib", "junit.jar")
      FileUtils.copyFile(junitPath.toFile(), junitNPath.toFile())
    }
  }

  def mkPath(elems: String*): Path = FileSystems.getDefault().getPath(elems.head, elems.tail:_*)

  def checkTargetClasses(): Unit = {
    val projects = listProjects(genprogFolder).sortBy(_.getName().split('-')(1).init.toInt)
    val modifiedClassesPath = FileSystems.getDefault().getPath("closure_modified_classes.txt")
    val modifiedClasses: List[String] = io.Source.fromFile(modifiedClassesPath.toFile()).getLines().toList.map(l => l.trim())
    for (p <- projects.filterNot(x => x.getName().endsWith("-3b"))) {
      val targetClassesPath = FileSystems.getDefault().getPath(p.getAbsolutePath(), "targetClasses.txt")
      val targetClasses: List[String] = io.Source.fromFile(targetClassesPath.toFile()).getLines().toList.map(l => l.trim())
      if (modifiedClasses.intersect(targetClasses).nonEmpty) {
        println(p.getName() + " " + modifiedClasses.intersect(targetClasses))
        // val relPath = mkPath(genprogFolder).relativize(mkPath(p.getAbsolutePath()))

        // println("\t" + "copying src")
        // val varexcSourcePath = mkPath(varexcFolder, relPath.toString(), "src")
        // val genprogSourcePath = mkPath(p.getAbsolutePath(), "src")
        // FileUtils.copyDirectory(varexcSourcePath.toFile(), genprogSourcePath.toFile())

        // println("\t" + "copying test")
        // val varexcTestPath = mkPath(varexcFolder, relPath.toString(), "test")
        // val genprogTestPath = mkPath(p.getAbsolutePath(), "test")
        // FileUtils.copyDirectory(varexcTestPath.toFile(), genprogTestPath.toFile())

        // val hasRhino = mkPath(p.getAbsolutePath(), "lib", "rhino").toFile().exists()
        // if (hasRhino) {
        //   println("\t" + "copying lib/rhino")
        //   val varexcRhinoPath = mkPath(varexcFolder, relPath.toString(), "lib", "rhino")
        //   val genprogRhinoPath = mkPath(p.getAbsolutePath(), "lib", "rhino")
        //   FileUtils.copyDirectory(varexcRhinoPath.toFile(), genprogRhinoPath.toFile())
        // } else {
        //   println("\t" + "copying rhino.jar")
        //   val varexcRhinoPath = mkPath(varexcFolder, relPath.toString(), "lib", "libtrunk_rhino_parser_jarjared.jar")
        //   val genprogRhinoPath = mkPath(p.getAbsolutePath(), "lib", "libtrunk_rhino_parser_jarjared.jar")
        //   FileUtils.copyFile(varexcRhinoPath.toFile(), genprogRhinoPath.toFile())
        // }
        
        // Process(Seq("ant", "compile-tests"), cwd = Some(p)).!
        // Process(Seq("defects4j", "test"), cwd = Some(p)).!
        // println("\t git commit changes")
        // if (hasRhino) 
        //   Process(Seq("git", "add", "src", "test", "lib/rhino"), cwd = Some(p)).! 
        // else 
        //   Process(Seq("git", "add", "src", "test"), cwd = Some(p)).!
        // Process(Seq("git", "commit", "-m", "VarexC"), cwd = Some(p)).!
      }
    }
  }

  def addVarexCJartoBuild(): Unit = {
    val projects = listProjects(varexcFolder).sortBy(_.getName().split('-')(1).init.toInt)
    var count = 0
    for (p <- projects) {
      val buildFile = FileSystems.getDefault().getPath(p.getAbsolutePath(), "build.xml")
      val buildFileBackup = FileSystems.getDefault().getPath(p.getAbsolutePath(), "build.xml.bak")
      if (!buildFileBackup.toFile.exists()) {
        Files.copy(buildFile, buildFileBackup)
      }
      val buildLines = io.Source.fromFile(buildFile.toFile()).getLines().toList
      if (!buildLines.exists(l => l.contains("varexc.jar"))) {
        val id = buildLines.indexWhere(l => l.contains("include name=\"protobuf-java.jar\""))
        if (id > 0) {
          val (pre, post) = buildLines.splitAt(id)
          val newBuildLines = pre ::: "<include name=\"varexc.jar\"/>" :: post
          val writer = new FileWriter(buildFile.toFile())
          writer.write(newBuildLines.mkString("\n"))
          writer.close()
          println("Processed: " + p.getName())
          count += 1
        } else {
          if (buildLines.exists(l => l.contains("classpath.path")) && buildLines.exists(l => l.contains("*.jar"))) {
            println(p.getName() + " ready")
            count += 1
          }
        }
      } else {
        println(p.getName() + " ready")
        count += 1
      }
    }
    println("Count: " + count)
  }

  def extractPosNegTests(): Unit = {
    val projects = listProjects(genprogFolder)
    for (p <- projects.filter(_.getName().endsWith("-35b"))) {
      val relevantTestsPath = s"$varexcFolder/RelevantTests/${p.getName}.txt"
      val relTestClasses    = io.Source.fromFile(relevantTestsPath).getLines().toList.filterNot(_.startsWith("*"))
      // val allTestsPath      = s"$genprogFolder${p.getName}/build/test/"
      // val allClassesPath    = s"$genprogFolder${p.getName}/build/classes/"
      // val rhinoLib = s"$genprogFolder${p.getName}/build/lib/rhino.jar"
      // val classLoader = new URLClassLoader(
        // Array(allClassesPath, allTestsPath, rhinoLib).map(new File(_).toURI.toURL))
      val outputDir = s"$genprogFolder${p.getName}/"

      val neg = genNeg(p)
      val pos = genPos(p, relTestClasses, neg)

      val posFile = new FileWriter(new File(outputDir + "pos.tests"))
      posFile.write(pos.mkString("\n"))

//      val negFile = new FileWriter(new File(outputDir + "neg.tests"))
//      val relTestFile = new FileWriter(new File(s"$varexcFolder/RelevantTests/${p.getName}.txt"),
//                                       true) // used for prioritizing test execution
//      negFile.write(neg.mkString("\n"))
//      relTestFile.write(neg.map(x => "*" + x).mkString("\n"))

      posFile.close()
//      negFile.close()
//      relTestFile.close()
    }
  }

  def genNeg(project: File): List[String] = {
    println(s"Generating neg.tests for ${project.getAbsolutePath}")
    Process(Seq("defects4j", "export", "-p", "tests.trigger"), cwd = Some(project)).lineStream.toList
  }

  def genPos(project: File, relTests: List[String], neg: List[String]): List[String] = {
    val allTestsPath: Path = FileSystems.getDefault.getPath(project.getAbsolutePath(), "all_tests")
    val allTests = io.Source.fromFile(allTestsPath.toFile).getLines().toList.map(l => {
      val split = l.split('(')
      split(1).init + "::" + split(0)
    })
    val allRelTests = allTests.filter(l => {
      val split = l.split("::")
      relTests.contains(split(0))
    })
    allRelTests diff neg
  }

  def genPos(cl: ClassLoader, relTests: List[String], neg: List[String]): List[String] = {
    val allTests = relTests.flatMap(x => {
      val cls = cl.loadClass(x)
      val tests =
        cls.getMethods.filter(m => m.isAnnotationPresent(classOf[org.junit.Test])).map(_.getName)
      if (tests.isEmpty) {
        val junit3Tests = cls.getMethods.filter(m => m.getName.startsWith("test") && m.getParameterCount == 0).map(_.getName)
        junit3Tests.map(t => x + "::" + t)
      } else {
        tests.map(t => x + "::" + t)
      }
    })
    allTests.diff(neg)
  }

  def genTargetClasses(): Unit = {
    val projects = listProjects(genprogFolder)
    for (p <- projects) {
      Process(Seq("defects4j", "export", "-p", "classes.modified", "-o", "targetClasses.txt"),
              cwd = Some(p)).lazyLines.foreach(println)
    }
  }

  def setupCompileScript(): Unit = {
    val projects = listProjects(genprogFolder)
    for (p <- projects) {
      println(s"Setting up compile script in ${p.getAbsolutePath}")
      Process(Seq("ln", "-s", "-f", compileScriptPath, "compile.py"), cwd = Some(p)).lazyLines
        .foreach(println)
    }

    def shouldKeep(f: File): Boolean = {
      if (f.getName.startsWith("Math-")) {
        f.getName.substring(5).init.toInt >= 85
      }
      else false
    }
  }

  /**
    * Update compile target to 1.8 and add varexc.jar as dependency
    */
  def modifyAntBuild(): Unit = {
    import scala.xml._
    val projects = listProjects(varexcFolder)
    for (p <- projects) {
      val buildFile = FileSystems.getDefault.getPath(p.getAbsolutePath, "build.xml")
      val backup    = FileSystems.getDefault.getPath(p.getAbsolutePath, "build.xml.backup")
      if (!backup.toFile.exists()) {
        Files.copy(buildFile, backup)
      }
      val xml = XML.loadFile(buildFile.toFile)
      val transChild = xml.child.map {
        case e: Elem
            if e.label == "property" && e
              .attribute("name")
              .map(_.toString)
              .contains("compile.target") =>
          val updatedAttr = new UnprefixedAttribute("value", "1.8", e.attributes.remove("value"))
          e.copy(attributes = updatedAttr)
        case e: Elem
            if e.label == "path" && e
              .attribute("id")
              .map(_.toString)
              .contains("compile.classpath") =>
          e.copy(
            child = (e.child :+ <pathelement location="${download.lib.dir}/varexc.jar"/>).distinct)
        case e => e
      }
      val newXMLString = xml.copy(child = transChild).toString
      println(s"Updating ${buildFile.toFile.getAbsoluteFile}")
      val writer = new FileWriter(buildFile.toFile)
      writer.write(newXMLString.replace("http://", "https://"))
      writer.close()
    }
  }

  def restoreAntBuild(): Unit = {
    val projects = listProjects(varexcFolder)
    for (p <- projects) {
      val backup    = FileSystems.getDefault.getPath(p.getAbsolutePath, "build.xml.backup")
      val buildFile = FileSystems.getDefault.getPath(p.getAbsolutePath, "build.xml")
      Files.copy(backup, buildFile, StandardCopyOption.REPLACE_EXISTING)
    }
  }

}
