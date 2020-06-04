package edu.cmu.cs.vbc.testutils

object ClosureTestLauncher extends TestLauncher {
  override val configFile: String = "closure.conf"
  override val useModel: Boolean = true

  override def genProject(args: Array[String]): Project = new ClosureProject(args)
}

class ClosureProject(args: Array[String]) extends Project(args) {
  override def getRelevantTestFilePath: String = mkPath(project, "RelevantTests", version + ".txt").toFile.getAbsolutePath

  override val testClassPath: String = mkPath(project, version, "build", "test").toFile.getAbsolutePath
  override val mainClassPath: String = mkPath(project, version, "build", "classes").toFile.getAbsolutePath
  override val libJars: Array[String] = getLibJars :+ mkPath(project, version, "build", "lib", "rhino.jar").toFile.getAbsolutePath

  def getLibJars: Array[String] = {
    val libPath = mkPath(project, version, "lib")
    libPath.toFile.listFiles().filter(_.getName.endsWith(".jar")).map(_.getAbsolutePath)
  }
}
