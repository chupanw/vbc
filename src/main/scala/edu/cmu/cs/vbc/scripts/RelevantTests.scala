package edu.cmu.cs.vbc.scripts

import java.io.File

/**
  * Export relevant tests from Defects4J dataset
  *
  * @note Needs defects4j in PATH
  */
object RelevantTests extends App {
  val usage =
    """
      |<versions-dir> <destination>
    """.stripMargin
  val versionDirString = args(0)
  val destinationString = args(1)
  require(args.length == 2, s"Wrong number of arguments\n$usage")
  require(versionDirString.endsWith("/"), s"Expecting a directory: $versionDirString")
  require(destinationString.endsWith("/"), s"Expecting a directory: $destinationString")

  val versionDir = new File(versionDirString)
  val destinationDir = new File(destinationString)
  if (!destinationDir.exists()) destinationDir.mkdirs()
  for (d <- versionDir.listFiles() if isGitRepo(d)) {
    import scala.sys.process._
    val name = d.getName
    Process(Seq("defects4j", "export", "-p", "tests.relevant", "-o", destinationString + name + ".txt"), cwd = Some(d)).lineStream foreach println
  }

  def isGitRepo(f: File): Boolean = {
    f.isDirectory && f.listFiles().exists(_.getName == ".git")
  }
}
