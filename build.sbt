scalaVersion := "2.11.7"

//libraryDependencies += "org.ow2.asm" % "asm" % "5.0.4"

libraryDependencies += "de.fosd.typechef" % "featureexprlib_2.11" % "0.4.1"

libraryDependencies += "de.fosd.typechef" % "conditionallib_2.11" % "0.4.1"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test"

libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.7" % "test"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "net.sf.trove4j" % "trove4j" % "3.0.3"

parallelExecution in Test := false

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

scalacOptions += "-unchecked"

initialize := {
    val _ = initialize.value
    if (sys.props("java.specification.version") != "1.8")
        sys.error("Java 8 is required for this project.")
}
