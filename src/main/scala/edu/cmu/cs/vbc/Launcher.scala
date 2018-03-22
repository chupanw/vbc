package edu.cmu.cs.vbc

import java.lang.reflect.{Method, Modifier}

import de.fosd.typechef.featureexpr.FeatureExprFactory
import edu.cmu.cs.vbc.utils.Statistics

/**
  * @author chupanw
  *
  *         Simple launcher class that executes a program variationally after lifting it.
  *
  *         Provide the main class as a parameter
  */
object Launcher extends App {
  if (args.size < 1)
    throw new RuntimeException("provide main class as parameter")

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  VBCLauncher.launch(args(0), args(1) == "true", args(2), args.drop(3))
}


object VBCLauncher {
  def launch(classname: String, liftBytecode: Boolean = true, configFile: String, args: Array[String] = new Array[String](0)) {
    val loader: VBCClassLoader = new VBCClassLoader(this.getClass.getClassLoader, liftBytecode, configFile = Some(configFile))
    Thread.currentThread().setContextClassLoader(loader)
    val cls: Class[_] = loader.loadClass(classname)
    invokeMain(cls, args)
        if (liftBytecode) Statistics.printStatistics()
//    Profiler.report()
  }

  def invokeMain(cls: Class[_], args: Array[String]): Unit = {
    try {
      val mtd: Method = cls.getMethod("main", classOf[Array[String]])
      val modifiers = mtd.getModifiers
      if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
        mtd.invoke(null, args)
    } catch {
      case e: NoSuchMethodException => println("No main method found in $classname, aborting...")
    }
  }
}