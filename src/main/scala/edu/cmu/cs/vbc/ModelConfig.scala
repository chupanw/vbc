package edu.cmu.cs.vbc

import com.typesafe.config._

import scala.collection.JavaConversions._
/**
  * Project specific configuration file for model classes.
  *
  * @author chupanw
  */
class ModelConfig(fileName: String) {
  val config: Config = ConfigFactory.load(fileName)
  val defaultConfig: Option[ModelConfig] = if (fileName == "default.conf") None else Some(ModelConfig.defaultConfig)

  /**
    * List of JDK classes that we are not lifting while executing this program
    */
  val jdkNotLiftingClasses: List[String] = {
    try {
      val classes: List[String] = config.getStringList("jdk-not-lifting").toList
      if (defaultConfig.isDefined)
        defaultConfig.get.jdkNotLiftingClasses ::: classes
      else
        classes
    } catch {
      case _: Throwable => defaultConfig.get.jdkNotLiftingClasses
    }
  }

  /**
    * List of library classes that we are not lifting while executing this program
    */
  val libraryNotLiftingClasses: List[String] = {
    try {
      config.getStringList("library-not-lifting").toList
    } catch {
      case _: Throwable => List()
    }
  }

  /**
    * List of program classes that we are not lifting while executing this program
    */
  val programNotLiftingClasses: List[String] = {
    try {
      config.getStringList("program-not-lifting").toList
    } catch {
      case _: Throwable => List()
    }
  }

  /**
    * All classes that we DON'T lift
    */
  val notLiftingClasses: List[String] = jdkNotLiftingClasses ::: libraryNotLiftingClasses ::: programNotLiftingClasses

  /**
    * All JDK classes that we DO lift
    */
  val jdkLiftingClasses: List[String] = {
    try {
      val classes: List[String] = config.getStringList("jdk-lifting").toList
      if (defaultConfig.isDefined)
        defaultConfig.get.jdkLiftingClasses ::: classes
      else
        classes
    } catch {
      case _: Throwable => defaultConfig.get.jdkLiftingClasses
    }
  }

  val libraryLiftingClasses: List[String] = {
    try {
      config.getStringList("library-lifting").toList
    } catch {
      case _: Throwable => List()
    }
  }

  require(jdkNotLiftingClasses intersect jdkLiftingClasses isEmpty, "Conflicting model class configuration")
}

object ModelConfig {
  val defaultConfig: ModelConfig = new ModelConfig("default.conf")
}

