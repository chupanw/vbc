scalaVersion := "2.13.1"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"
libraryDependencies += "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % "test"

resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/releases"
libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.19" % "test"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.2.0"

parallelExecution in Test := false

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

scalacOptions += "-unchecked"

initialize := {
    val _ = initialize.value
    if (sys.props("java.specification.version") != "1.8")
        sys.error("Java 8 is required for this project.")
}

testOptions in Test ++= Seq(
  Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/html")
)

// checkstyle dependencies
libraryDependencies += "commons-cli" % "commons-cli" % "1.2"
libraryDependencies += "com.google.guava" % "guava" % "18.0"
libraryDependencies += "commons-beanutils" % "commons-beanutils" % "1.8.3"
//libraryDependencies += "antlr" % "antlr" % "2.7.7"
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.3"
libraryDependencies += "org.apache.ant" % "ant" % "1.7.0"

// config
libraryDependencies += "com.typesafe" % "config" % "1.4.0"

unmanagedJars in Compile ~= {uj =>
  Seq(Attributed.blank(file(System.getProperty("java.home").dropRight(3) + "lib/tools.jar"))) ++ uj
}

// for benchmarking
fork := true
(fullClasspath in Runtime) := (fullClasspath in Runtime).value ++ (fullClasspath in Test).value

// assembly
test in assembly := {}
assemblyMergeStrategy in assembly := {
  case PathList("org", "objectweb", "asm", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", "log4j", xs@_*) => MergeStrategy.first
  case x =>
    val default = (assemblyMergeStrategy in assembly).value
    default(x)
}