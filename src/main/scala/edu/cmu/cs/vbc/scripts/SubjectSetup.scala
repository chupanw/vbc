package edu.cmu.cs.vbc.scripts

import java.io.{File, FileWriter}

/**
  * To setup a project
  *   1. Record project name and main class name in the programs field
  *   2. Create pos.tests and neg.tests for the project
  *   3. Create the RelevantTest file for VarexC
  *   4. Copy varexc.jar (for the annotation) and modify the build file
  */
object SubjectSetup extends App {

  val osBase = args(0)
  val base = s"/$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava/dataset/"
  val baseVBC = s"/$osBase/chupanw/Projects/Data/PatchStudy/IntroClassJava-VarexC/dataset/"

  /**
    * Unless we have a better way to get passing tests
    *
    * The following only works for median projects
    */
  val blackTests = (1 to 7).toList
  val whiteTests = (1 to 6).toList
  def getPosFromNeg(project: String, neg: List[String]): List[String] = {
    val blackClass = getBlackTestClass(project)
    val whiteClass = getWhiteTestClass(project)
    val blackFailed = neg.filter(_.contains("Black")).map(_.split("::")(1).substring(4).toInt)
    val whiteFailed = neg.filter(_.contains("White")).map(_.split("::")(1).substring(4).toInt)
    val blackPos = for (i <- blackTests if !blackFailed.contains(i)) yield blackClass + s"::test$i"
    val whitePos = for (i <- whiteTests if !whiteFailed.contains(i)) yield whiteClass + s"::test$i"
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
    val pd = project.substring(0, project.init.lastIndexOf("/") + 1)
    val version = project.substring(pd.length, project.length - 1)
    val rtDir = new File(baseVBC + pd + "RelevantTests")
    if (!rtDir.exists()) rtDir.mkdir()
    val writer = new FileWriter(new File(rtDir.getPath + "/" + version + ".txt"))
    println(s"Generating ${rtDir.getPath}/$version.txt")
    writer.write(getBlackTestClass(project) + "\n")
    writer.write(getWhiteTestClass(project) + "\n")
    writer.close()
  }

  var failed: List[String] = Nil
  var skipped: List[String] = Nil
  /**
    * Configurable
    */
  val projects = List(
    "median/2c155667/000/"
//    "median/317aa705/000/",
//    "median/317aa705/002/",
//    "median/317aa705/003/",
//    "median/36d8008b/000/",
//    "median/3b2376ab/003/",
//    "median/3b2376ab/006/",
//    "median/3cf6d33a/007/",
//    "median/48b82975/000/",
//    "median/68eb0bb0/000/",
//    "median/6aaeaf2f/000/",
//    "median/6e464f2b/003/",
//    "median/89b1a701/003/",
//    "median/89b1a701/007/",
//    "median/89b1a701/010/",
//    "median/9013bd3b/000/",
//    "median/90834803/003/",
//    "median/90834803/010/",
//    "median/90834803/015/",
//    "median/90a14c1a/000/",
//    "median/93f87bf2/010/",
//    "median/93f87bf2/012/",
//    "median/93f87bf2/015/",
//    "median/95362737/000/",
//    "median/95362737/003/",
//    "median/9c9308d4/003/",
//    "median/9c9308d4/007/",
//    "median/9c9308d4/012/",
//    "median/aaceaf4a/003/",
//    "median/af81ffd4/004/",
//    "median/af81ffd4/007/",
//    "median/b6fd408d/000/",
//    "median/b6fd408d/001/",
//    "median/c716ee61/000/",
//    "median/c716ee61/001/",
//    "median/c716ee61/002/",
//    "median/cd2d9b5b/010/",
//    "median/d009aa71/000/",
//    "median/d120480a/000/",
//    "median/d2b889e1/000/",
//    "median/d43d3207/000/",
//    "median/d4aae191/000/",
//    "median/e9c6206d/000/",
//    "median/e9c6206d/001/",
//    "median/fcf701e8/000/",
//    "median/fcf701e8/002/",
//    "median/fcf701e8/003/",
//    "median/fe9d5fb9/000/",
//    "median/fe9d5fb9/002/"
  )


  run()

  def run(): Unit = {
    var current: String = ""
    try {
      for (p <- projects) {
        println(s"Project: $p")
        current = p
        generatePosNeg(p)
        createRelevantTests(p)
        setupVBC(p)
        println()
      }
    } catch {
      case a: AssertionError => a.printStackTrace(); failed = current :: failed
      case e => throw e
    }
    println("Failed: ")
    println(failed)
    println("Skipped: ")
    println(skipped)
  }

  def generatePosNeg(project: String): Unit = {
    val projectPath = base + project
    println("Running Maven...")
    val mavenOutput = runMaven(projectPath)
    println("Parsing Maven output...")
    val resultsLineOpt = mavenOutput.find(x => x == "Results :")
    assert(resultsLineOpt.isDefined)
    val restMavenOutput = mavenOutput.dropWhile(_ != resultsLineOpt.get)
    val statLineOpt = restMavenOutput.find(s => s.startsWith("Tests run: "))
    assert(statLineOpt.isDefined, "No stat line when running maven")
    val statLine = statLineOpt.get
    println(statLine)
    val nums = statLine.split(",").map(x => x.substring(x.indexOf(":") + 1).trim).map(_.toInt)
    assert(nums.length == 4)  // total, failures, errors, skipped
    val nPass = nums(0) - nums(1) - nums(2) - nums(3)
    if (nPass == 0) {
      println("No positive test, skipping...")
      skipped = project :: skipped
    } else {
      val failLines = restMavenOutput.drop(2).takeWhile(x => x != statLine).init
      val neg = for (l <- failLines) yield {
        val ll = if (l.startsWith("Failed tests:")) l.substring(13) else l
        val tC = ll.split(":")(0).trim
        val testCase = tC.substring(0, tC.indexOf("("))
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
      scala.sys.process.Process(Seq("mvn", "test"), new File(projectPath)).lineStream.foreach(x => lines += x)
    } catch {
      case r: RuntimeException if r.getMessage.contains("Nonzero exit code:") => // do noting, maven returns nonzero exit code when tests fail
      case e: Exception => throw e
    }
    lines.toList
  }

  def setupVBC(project: String): Unit = {
    import scala.sys.process._
    val pn = project.substring(0, project.indexOf("/") + 1)
    println("Copying varexc.jar...")
    val jar = s"${baseVBC}${pn}varexc.jar"
    val pom = s"${baseVBC}${pn}pom.xml"
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
