package edu.cmu.cs.vbc.benchmark

import java.io.FileWriter
import java.lang.reflect.{Method, Modifier}

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.VBCClassLoader

/**
  * @author chupanw
  */
trait VBench {
  def getMainMethod(clazzName: String, modelConfig: String, useModel: Boolean): Method = {
    FeatureExprFactory.setDefault(FeatureExprFactory.bdd)
    val loader: VBCClassLoader = new VBCClassLoader(
      this.getClass.getClassLoader,
      isLift = true,
      configFile = Some(modelConfig),
      useModel = useModel
    )
    Thread.currentThread().setContextClassLoader(loader)
    val cls: Class[_] = loader.loadClass(clazzName)
    try {
      val mtd: Method = cls.getMethod("main", classOf[Array[String]])
      val modifiers = mtd.getModifiers
      assert(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
      mtd
    } catch {
      case _: Throwable => throw new RuntimeException("Error")
    }
  }

  def benchmark(prog: String,
                config: String,
                useModel: Boolean,
                output: String,
                windowSize: Int = 10,
                cov: Double = 0.02): Unit = {
    var start: Double = 0.0
    var end: Double = 0.0

    val measurements = new SlidingWindow(windowSize)

    val m = getMainMethod(prog, config, useModel = useModel)

    while (!(measurements.isFull && measurements.cov() < cov)) {
      start = System.nanoTime().toDouble
      m.invoke(null, Array[String]())
      end = System.nanoTime().toDouble
      measurements.add(end - start)
    }

    val meanTimeInMS = measurements.mean() / 1000000
    val writer = new FileWriter(output, true)
    writer.append(meanTimeInMS + " ms\n")
    writer.close()
  }
}
