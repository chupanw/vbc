package edu.cmu.cs.vbc.testutils

import java.io.FileWriter

/**
  * Execute the specified test case (as name) in a separate JVM
  *
  * After execution, we write the overall passing condition to a temp file.
  *
  * cpwTODO: this class needs a lot of fixing after exception handling is changed
  */
class ForkTestCase(c: Class[_], mName: String) extends TestClass(c) {

  def run(): Unit = {
    require(checkAnnotations, s"Unsupported annotation in $c")
    if (isAbstract) {
      VTestStat.skipClass(className)
      return
    }
    val m = getTestCases.find(x => x.getName.equals(mName))
    if (m.isDefined && !isSkipped(m.get)) {
      if (!isParameterized) {
        //        executeOnce(None, m.get, FeatureExprFactory.True, FeatureExprFactory.False)
        writeBDD(c.getName, m.get.getName)
      }
      else
        for (x <- getParameters) {
          //          executeOnce(Some(x.asInstanceOf[Array[V[_]]]), m.get, FeatureExprFactory.True, FeatureExprFactory.False)
        }
//      writePassingConditionToFile()
      writeOneSolutionToFile()
    }
  }

  /**
    * Too slow
    */
  def writePassingConditionToFile(): Unit = {
    val fileName = "passingCond/" + c.getName + "." + mName + ".txt"
    val writer = new FileWriter(fileName)
    writer.write(VTestStat.classes.head._2.getOverallPassingCondition.toString)
    writer.close()
  }

  def writeOneSolutionToFile(): Unit = {
    ???
    //    val fileName = "passingCond/" + c.getName + ".txt"
    //    val writer = new FileWriter(fileName, true)
    //    if (VTestStat.classes(c.getName).failedMethods.contains(mName)) {
    //      val stat = VTestStat.classes(c.getName).failedMethods(mName)
    //      val oneSolution = stat.oneSolution(stat.failingCtx.not())
    //      writer.write(mName + " " + oneSolution + "\n")
    //    } else if (VTestStat.classes(c.getName).succeededMethods.contains(mName) || VTestStat.classes(c.getName).skippedMethods.contains(mName)) {
    //      writer.write(mName + " succeed or skipped" + "\n")
    //    } else {
    //      writer.write(mName + " something went wrong" + "\n")
    //    }
    //    writer.close()
  }
}


