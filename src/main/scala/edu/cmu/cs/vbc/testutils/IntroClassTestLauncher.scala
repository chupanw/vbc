package edu.cmu.cs.vbc.testutils

import scala.io.Source.fromFile

object IntroClassLauncher extends TestLauncher {
  override val configFile: String = "intro-class.conf"
  override val useModel: Boolean = true
  override val reuseLifted: Boolean = false

  override def genProject(args: Array[String]): Project = new IntroClassProject(args)
}

class IntroClassProject(args: Array[String]) extends Project(args) {
  override def getRelevantTestFilePath: String = {
    // assume that version looks like median/0cdfa335/000
    val split = version.split('/')
    mkPath(project, split(0), split(1), "RelevantTests", split(2) + ".txt").toFile.getAbsolutePath
  }

  override val libJars: Array[String] = Array("lib/junit-4.12-recompiled.jar")
}


object IntroClassCloudLauncher extends TestLauncher {
  override val configFile: String = "intro-class.conf"
  override val useModel: Boolean = true
  override val reuseLifted: Boolean = false

  override def genProject(args: Array[String]): Project = new IntroClassCloudProject(args)
}

class IntroClassCloudProject(args: Array[String]) extends Project(args) {
  override def getRelevantTestFilePath: String = {
    // project would be "/tmp" and version would be something like median-0cdfa335-003
    mkPath(project, "RelevantTests", version + ".txt").toFile.getAbsolutePath
  }

  override val libJars: Array[String] = Array("lib/junit-4.12-recompiled.jar")

  override def parseRelevantTests(file: String): (List[String], List[TestString], List[TestString]) = {
    val f = fromFile(file)
    val validLines = f.getLines().toList.filterNot(_.startsWith("//"))
    val testClasses = validLines.filterNot(_.startsWith("*"))
    val failingTests = validLines.filter(_.startsWith("*")).map(x => TestString(x.substring(1).trim))
    // prioritize test classes that have failing tests
    val orderedTestClasses = (failingTests.map(_.className) ::: testClasses).distinct
    (orderedTestClasses, failingTests, Nil)
  }
}