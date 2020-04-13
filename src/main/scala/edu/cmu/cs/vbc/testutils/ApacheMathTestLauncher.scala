package edu.cmu.cs.vbc.testutils

object ApacheMathLauncher extends TestLauncher {
  override val configFile: String = "apache-math.conf"
  override val useModel: Boolean = false

  override def genProject(args: Array[String]): Project = new ApacheMathProject(args)
}

class ApacheMathProject(args: Array[String]) extends Project(args) {
  override def getRelevantTestFilePath: String = project + "RelevantTests/" + version + ".txt"
}
