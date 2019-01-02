package edu.cmu.cs.vbc.testutils

import java.io.FileWriter

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.varex.V

import scala.collection.mutable

/**
  * Execute the specified test case (as name) in a separate JVM
  *
  * After execution, we write the overall passing condition to a temp file.
  *
  */
class ForkTestCase(c: Class[_], mName: String) extends TestClass(c) {

  def run(): Unit = {
    require(checkAnnotations, s"Unsupported annotation in $c")
    val m = getTestCases.find(x => x.getName.equals(mName))
    if (m.isDefined && !isSkipped(m.get)) {
      if (!isParameterized)
        executeOnce(None, m.get, FeatureExprFactory.True, mutable.ArrayBuffer[FeatureExpr]())
      else
        for (x <- getParameters) {
          executeOnce(Some(x.asInstanceOf[Array[V[_]]]), m.get, FeatureExprFactory.True, mutable.ArrayBuffer[FeatureExpr]())
        }
//      writePassingConditionToFile()
    }
  }

  /**
    * Too slow
    */
  def writePassingConditionToFile(): Unit = {
    val fileName = "passingCond/" + c.getName + "." + mName + ".txt"
    val writer = new FileWriter(fileName)
    writer.write(VTestStat.classes.head._2.getOverallPassingCondition().toString())
    writer.close()
  }
}


