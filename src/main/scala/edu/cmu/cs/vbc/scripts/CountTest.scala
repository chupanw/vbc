package edu.cmu.cs.vbc.scripts

import java.io.File
import java.nio.file.{FileSystems, Path}

import scala.io.Source.fromFile

object CountTest extends App {
  val relTestFolderStr = args(0)

  var countMap: Map[String, Int] = Map()

  for (f <- new File(relTestFolderStr).listFiles()) {
    val lines = io.Source.fromFile(f).getLines().toList.filterNot(x => x.startsWith("*") || x.startsWith("//"))
    for (l <- lines) {
      if (countMap.contains(l))
        countMap = countMap + (l -> (countMap(l) + 1))
      else
        countMap = countMap + (l -> 1)
    }
  }
  println(countMap.toSeq.sortBy(_._2).reverse.mkString("\n"))

}

object printCP extends App {
  for (i <- (1 to 106)) {
    println(s"cp junit-4.12-recompiled.jar Math-${i}b/lib/")
  }
}

object CountSolutions extends App {
  def getSolutions(p: Path): List[List[String]] = {
    val line = fromFile(p.toFile).getLines().toList.head
    if (line == "List()") Nil
    else
      line
        .split(',')
        .toList
        .map(e => e.dropWhile(_ != '{').takeWhile(_ != '}').tail.split('&').map(_.trim).toList)
  }

  def getSolutionsOld(p: Path): List[List[String]] = {
    val line = fromFile(p.toFile).getLines().toList.head
    line.substring(5).split("\\),").toList.map(_.dropWhile(_ != '(').tail).map(_.takeWhile(_ != ')')).map(_.split(',').toList.map(_.trim))
  }

  def minimize(l: List[List[String]], min: collection.mutable.ListBuffer[List[String]]): List[List[String]] = {
    if (l.isEmpty) min.toList
    else {
      val head =l.head
      min.append(head)
      val filtered = l.filterNot(x => head.diff(x).isEmpty)
      minimize(filtered, min)
    }
  }

  def mkPath(elems: String*): Path = FileSystems.getDefault.getPath(elems.head, elems.tail:_*)

  val solutions = getSolutions(mkPath("/tmp", "Math-28b-solutions.txt"))  // 82, 85, 5, 80
//  val solutions = getSolutionsOld(mkPath("/tmp", "Math-53b-solutions.txt")) // 95
  val minimizedSolutions = minimize(solutions.sortBy(_.size), collection.mutable.ListBuffer[List[String]]())
  println(minimizedSolutions.mkString("\n"))
  val degree1 = solutions.filter(_.size == 1)
  val degree2 = solutions.filter(_.size == 2)
  val degree2Min = minimizedSolutions.filter(_.size == 2)
  val degree3 = solutions.filter(_.size == 3)
  val degree3Min = minimizedSolutions.filter(_.size == 3)
  println("degree 1: " + degree1.size)
  println("degree 2: " + degree2.size)
  println("degree 2 min: " + degree2Min.size)
  println("degree 3: " + degree3.size)
  println("degree 3 min: " + degree3Min.size)
}