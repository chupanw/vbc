package edu.cmu.cs.vbc.scripts

object IntroClassCloudPatchGenerator extends App with CloudPatchGenerator {
  override def genprogPath: String = args(0)
  override def projects4GenProg: String = args(1)
  override def projects4VarexC: String = args(2)
  override def project: String = args(3)
  override def numMut: NumMutations = Eight
  override def relevantTestFilePathString: String = {
    val split = project.split('/')
    mkPathString(projects4GenProg, split(0), split(1), "RelevantTests", split(2) + ".txt")
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

object IntroClassGenProgCloudPatchRunner extends App with CloudPatchRunner {
  override def launch(args: Array[String]): Unit = ???

  override def compileCMD = ???
}