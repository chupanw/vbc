package edu.cmu.cs.vbc.testutils

object ApacheMathLauncher extends TestLauncher {
  override val configFile: String = "apache-math.conf"
  override val useModel: Boolean = false
  override val reuseLifted: Boolean = false

  override def genProject(args: Array[String]): Project = new ApacheMathProject(args)
}

class ApacheMathProject(args: Array[String]) extends Project(args) {
  override def getRelevantTestFilePath: String = mkPath(project, "RelevantTests", version + ".txt").toFile.getAbsolutePath

  override val libJars: Array[String] = getLibJars

  def getLibJars: Array[String] = {
    val libPath = mkPath(project, version, "lib")
    libPath.toFile.listFiles().
      filter(_.getName.endsWith(".jar")).
      filterNot(_.getName.contains("varexc")).
      filterNot(x => x.getName.contains("junit") && !x.getName.contains("recompiled")).
      map(_.getAbsolutePath)
  }
}

object ApacheMathBugs {
  val all: List[String] = (1 to 106).toList map {x => s"Math-${x}b"}
  val debug: List[String] = List(
    60, 61, 62, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74
  ).map{x => s"Math-${x}b"}
}