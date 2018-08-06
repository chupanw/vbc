package edu.cmu.cs.vbc.scripts

import java.io.File
import scala.sys.process._

/**
  * Checkout projects from the Defects4J dataset
  *
  * @note Needs defects4j tool chain in PATH
  */
object Checkout extends App {

  val usage =
    """
      |Usage: <project> <version> <destination> <overwrite>
      |<project> = math | chart | closure | lang | mockito | time
      |<overwrite> = true | false
    """.stripMargin

  assume(args.length == 4, s"Incorrect number of parameters\n$usage")

  val project: String = args(0) match {
    case "math" => "Math"
    case "chart" => "Chart"
    case "closure" => "Closure"
    case "lang" => "Lang"
    case "mockito" => "Mockito"
    case "time" => "Time"
    case _ => throw new UnsupportedOperationException(s"Wrong project name: ${args(0)}\n$usage")
  }

  val version: Int = args(1).init.toInt
  assume(version > 0 && (args(0) match {
    case "math" => version < 107
    case "chart" => version < 27
    case "closure" => version < 134
    case "lang" => version < 66
    case "mockito" => version < 39
    case "time" => version < 28
  }), s"Unsupported version number for $project: $version")
  val versionPostfix = args(1).last match {
    case 'b' => 'b'
    case 'f' => 'f'
    case _ => throw new UnsupportedOperationException(s"Wrong postfix verison: ${args(1)}\n$usage")
  }

  assume(args(2).endsWith("/"), s"Wrong destination, expecting a directory: ${args(2)}")
  val destination = new File(args(2) + project + "-" + args(1))

  val overwrite = args(3) match {
    case "true" => true
    case "false" => false
    case _ => throw new UnsupportedOperationException(s"Wrong overwrite parameter: ${args(3)}\n$usage")
  }

  if (destination.exists() && destination.listFiles().nonEmpty && !overwrite) {
    println(s"$destination already exists, aborting...")
  } else {
    val output = Seq(
      "defects4j", "checkout",
      "-p", project,
      "-v", args(1),
      "-w", destination.getAbsoluteFile.toString
    ).lineStream
    output foreach println
  }
}

object CheckOutAll extends App {
  val usage =
    """
      |Usage: <project> <destination> <overwrite>
      |<project> = math | chart | closure | lang | mockito | time
      |<overwrite> = true | false
    """.stripMargin

  assume(args.length == 3, s"Incorrect number of parameters\n$usage")

  args(0) match {
    case "math" =>
      1 to 106 foreach {i => Checkout.main(Array(args(0), i + "f", args(1), args(2)))}
    case "chart" =>
      1 to 26 foreach {i => Checkout.main(Array(args(0), i + "f", args(1), args(2)))}
    case "closure" =>
      1 to 133 foreach {i => Checkout.main(Array(args(0), i + "f", args(1), args(2)))}
    case "lang" =>
      1 to 65 foreach {i => Checkout.main(Array(args(0), i + "f", args(1), args(2)))}
    case "mockito" =>
      1 to 38 foreach {i => Checkout.main(Array(args(0), i + "f", args(1), args(2)))}
    case "time" =>
      1 to 27 foreach {i => Checkout.main(Array(args(0), i + "f", args(1), args(2)))}
    case _ =>
    case _ => throw new UnsupportedOperationException(s"Wrong project name: ${args(0)}\n$usage")
  }
}
