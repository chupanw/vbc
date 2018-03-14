package edu.cmu.cs.vbc.benchmark

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
}
