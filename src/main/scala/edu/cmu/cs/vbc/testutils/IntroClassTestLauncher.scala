package edu.cmu.cs.vbc.testutils

object IntroClassLauncher extends TestLauncher {
  override val configFile: String = "intro-class.conf"
  override val useModel: Boolean = true

  override def genProject(args: Array[String]): Project = new IntroClassProject(args)
}

class IntroClassProject(args: Array[String]) extends Project(args) {
  override def getRelevantTestFilePath: String = {
    // assume that version looks like median/0cdfa335/000
    val split = version.split('/')
    mkPath(project, split(0), split(1), "RelevantTests", split(2) + ".txt").toFile.getAbsolutePath
  }
}
