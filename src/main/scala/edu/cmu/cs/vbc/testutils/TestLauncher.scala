package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.VERuntime
import scala.io.Source._

/**
  * Launch JUnit test cases
  *
  * For example, this can be used to launch test cases from Defects4J
  */
object TestLauncher extends App {

  assume(args.length == 2, s"Wrong number of arguments: ${args.length}")

  assume(args(0).endsWith("/"), s"Not a folder: $repository")
  val repository = args(0)
  val version = args(1)
  val relevantTests = fromFile(args(0) + "RelevantTests/" + version + ".txt")

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  // turn this two into parameters
  val testClasspath = s"$repository$version/target/test-classes/"
  val mainClasspath = s"$repository$version/target/classes/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, mainClasspath, testClasspath)
  VERuntime.classloader = Some(testLoader)

//  val allTests = testLoader.findTestClassFiles()
//  allTests.foreach {x =>
//    val testClass = TestClass(testLoader.loadClass(x))
//    testClass.runTests()
//  }

  val tests = relevantTests.getLines().toList.filterNot(_.startsWith("//"))

//  val tests: List[String] = List(
    // problematic:
//    "org.apache.commons.math3.analysis.integration.gauss.BaseRuleFactoryTest" // concurrency

//    "org.apache.commons.math3.linear.CholeskyDecompositionTest"  // Array casting, [V to [[D
//    "org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest" // Array casting, needs to expand multi-dimensional arrays
//    "org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest",
//    "org.apache.commons.math3.stat.FrequencyTest"
//    "org.apache.commons.math3.stat.CertifiedDataTest"

//    "org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest"  // VBlock, first block not in allBlocks
//    "org.apache.commons.math3.linear.ConjugateGradientTest",  // VBlock, first block not in allBlocks

//    "org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunctionTest"  // todo: unknown, maybe @ignore?
//    "org.apache.commons.math3.util.MathUtilsTest",  // todo: unknown
//    "org.apache.commons.math3.util.MathArraysTest", // todo: unknown

//    "org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest" // need model class for java.text.DecimalFormat
//    "org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest"  // DecimalFormatSymbols
//    "org.apache.commons.math3.random.RandomDataGeneratorTest" // DecimalFormat

//    "org.apache.commons.math3.util.FastMathTest"  // too slow
//    "org.apache.commons.math3.optimization.fitting.PolynomialFitterTest", // too slow
//    "org.apache.commons.math3.dfp.DfpTest" // too slow + conflicting model class
//    "org.apache.commons.math3.optim.linear.SimplexSolverTest",  // too large
//    "org.apache.commons.math3.special.BetaTest" // method code too large
//    "org.apache.commons.math3.random.EmpiricalDistributionTest" // too slow
//    "org.apache.commons.math3.random.ISAACTest",  // method code too large
//    "org.apache.commons.math3.random.MersenneTwisterTest",  // method code too large

//    "org.apache.commons.math3.optimization.linear.SimplexSolverTest"  // no such field

//    "org.apache.commons.math3.linear.EigenDecompositionTest" // VBCAnalyzer
//    "org.apache.commons.math3.linear.EigenSolverTest" // VBCAnalyzer
//    "org.apache.commons.math3.linear.SingularValueDecompositionTest" // VBCAnalyzer

//    "org.apache.commons.math3.linear.SparseRealVectorTest"  // assertion error
//    "org.apache.commons.math3.linear.RealMatrixFormatTest"  // assertion error, StringBuffer
//    "org.apache.commons.math3.linear.UnmodifiableOpenMapRealVectorTest",  // error creating test object
//    "org.apache.commons.math3.linear.UnmodifiableArrayRealVectorTest",  // error creating test object
//    "org.apache.commons.math3.random.RandomAdaptorTest" // assertion error
//    "org.apache.commons.math3.random.Well19937cTest"

//    "org.apache.commons.math3.stat.inference.WilcoxonSignedRankTestTest", // DUPX2

//    "org.apache.commons.math3.stat.descriptive.DescriptiveStatisticsTest" // reflection related
//    "org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatisticsTest"
//    "org.apache.commons.math3.stat.data.LotteryTest"
//    "org.apache.commons.math3.stat.data.LewTest"

//    "org.apache.commons.math3.genetics.GeneticAlgorithmTestBinary"  // internal class extends Comparable
//    "org.apache.commons.math3.genetics.ElitisticListPopulationTest"
//    "org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest" // comparable
//    "org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest"

//    "org.apache.commons.math3.genetics.UniformCrossoverTest", // @BeforeClass

//    "org.apache.commons.math3.random.SynchronizedRandomGeneratorTest",  // unknown yet
    // WIP
//    "org.apache.commons.math3.genetics.GeneticAlgorithmTestPermutations"
//    "org.apache.commons.math3.genetics.FitnessCachingTest"
//    "org.apache.commons.math3.genetics.DummyBinaryChromosome"
//    "org.apache.commons.math3.genetics.CycleCrossoverTest",
//    "org.apache.commons.math3.exception.util.ExceptionContextTest",
//    "org.apache.commons.math3.exception.util.ArgUtilsTest",
//    "org.apache.commons.math3.exception.util.LocalizedFormatsTest"
//    "org.apache.commons.math3.transform.FastFourierTransformerTest",
//    "org.apache.commons.math3.transform.RealTransformerAbstractTest",
//    "org.apache.commons.math3.transform.FastCosineTransformerTest",
//    "org.apache.commons.math3.transform.FastSineTransformerTest"

    // problematic
//    "org.apache.commons.math3.util.CombinatoricsUtilsTest"

//  )

    tests.foreach {x =>
      val testClass = TestClass(testLoader.loadClass(x))
      testClass.runTests()
    }

  VTestStat.printToConsole()
  VTestStat.toMarkdown(version, "org.apache.commons.math3.")
}
