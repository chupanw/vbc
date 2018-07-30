package edu.cmu.cs.vbc.testutils

import de.fosd.typechef.featureexpr.{FeatureExpr, FeatureExprFactory}
import edu.cmu.cs.vbc.VERuntime

/**
  * Launch JUnit test cases
  *
  * For example, this can be used to launch test cases from Defects4J
  */
object TestLauncher extends App {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  // turn this two into parameters
  val testClasspath = "/Users/chupanw/Projects/Data/defects4j-math/6f/target/test-classes/"
  val mainClasspath = "/Users/chupanw/Projects/Data/defects4j-math/6f/target/classes/"

  val testLoader = new VBCTestClassLoader(this.getClass.getClassLoader, mainClasspath, testClasspath)
  VERuntime.classloader = Some(testLoader)

//  val allTests = testLoader.findTestClassFiles()
//  allTests.foreach {x =>
//    val testClass = TestClass(testLoader.loadClass(x))
//    testClass.runTests()
//  }

  val tests: List[String] = List(
    // problematic:
//    "org.apache.commons.math3.analysis.integration.gauss.BaseRuleFactoryTest" // concurrency

//    "org.apache.commons.math3.linear.CholeskyDecompositionTest"  // Array casting, [V to [[D
//    "org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatorTest" // Array casting, needs to expand multi-dimensional arrays
//    "org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunctionTest",  // Array casting, needs to expand multi-dimensional arrays
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

//    "org.apache.commons.math3.stat.regression.GLSMultipleLinearRegressionTest"  // frame merge error

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


    // 1f
//    "org.apache.commons.math3.analysis.interpolation.FieldHermiteInterpolatorTest",  // ok
//    "org.apache.commons.math3.analysis.polynomials.PolynomialsUtilsTest",  // ok
//    "org.apache.commons.math3.distribution.KolmogorovSmirnovDistributionTest",  // ok
//    "org.apache.commons.math3.fraction.BigFractionFieldTest",  //ok
//    "org.apache.commons.math3.fraction.BigFractionFormatTest",  //ok
//    "org.apache.commons.math3.fraction.BigFractionTest",  //ok
//    "org.apache.commons.math3.fraction.FractionFieldTest",  //ok
//    "org.apache.commons.math3.fraction.FractionFormatTest",//ok
//    "org.apache.commons.math3.fraction.FractionTest",//ok
//    "org.apache.commons.math3.linear.ArrayFieldVectorTest",//ok
//    "org.apache.commons.math3.linear.BlockFieldMatrixTest",//ok
//    "org.apache.commons.math3.linear.FieldLUDecompositionTest",//ok
//    "org.apache.commons.math3.linear.FieldLUSolverTest",  //ok
//    "org.apache.commons.math3.linear.FieldMatrixImplTest",//ok
//    "org.apache.commons.math3.linear.MatrixUtilsTest",//ok
//    "org.apache.commons.math3.linear.SparseFieldMatrixTest",//ok
//    "org.apache.commons.math3.linear.SparseFieldVectorTest",//ok
//    "org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegratorTest",//ok
//    "org.apache.commons.math3.ode.nonstiff.AdamsMoultonIntegratorTest",//ok
//    "org.apache.commons.math3.ode.sampling.NordsieckStepInterpolatorTest",//ok
//    "org.apache.commons.math3.util.OpenIntToFieldTest"//ok

    // 6f
    "org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest",
    "org.apache.commons.math3.fitting.CurveFitterTest",
    "org.apache.commons.math3.fitting.GaussianFitterTest",
    "org.apache.commons.math3.fitting.HarmonicFitterTest",
    "org.apache.commons.math3.fitting.PolynomialFitterTest",
//    "org.apache.commons.math3.optim.linear.SimplexSolverTest", // method code too large
    "org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizerTest",
    "org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapterTest",
    "org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapterTest",
    "org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest",
//    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest"
//    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest"
    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest",
    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest",
    "org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest",
    "org.apache.commons.math3.optim.nonlinear.vector.MultiStartMultivariateVectorOptimizerTest",
    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest",
    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest",
    "org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest",
    "org.apache.commons.math3.optim.univariate.BrentOptimizerTest",
    "org.apache.commons.math3.optim.univariate.MultiStartUnivariateOptimizerTest"
  )

    tests.foreach {x =>
      val testClass = TestClass(testLoader.loadClass(x))
      testClass.runTests()
    }

  VTestStat.printToConsole()
}
