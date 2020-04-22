package edu.cmu.cs.vbc.scripts

import java.io._
import java.nio.file.{Files, Path}
import java.util.Date
import java.util.zip.Deflater

import ch.qos.logback.classic.LoggerContext
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.{ReceiveMessageRequest, SendMessageRequest}
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.model.{Filters, Updates}
import com.mongodb.client.{MongoClients, MongoCollection, MongoDatabase}
import edu.cmu.cs.vbc.testutils.{ApacheMathLauncher, VTestStat}
import org.apache.commons.compress.archivers.zip.{ZipArchiveEntry, ZipArchiveInputStream, ZipArchiveOutputStream}
import org.apache.commons.compress.utils.IOUtils
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

import scala.sys.process.Process

trait CloudPatchGenerator extends PatchRunner {
  val sqsURI: String   = System.getProperty("sqsURI")
  val mongoURI: String = System.getProperty("mongoURI")

  private def zipPathString = mkPathString("/tmp", s"$project.zip")
  private def relevantTestFilePathString =
    mkPathString(projects4VarexC, "RelevantTests", project + ".txt")
  private def genprogConfigPathString = mkPathString("/tmp", "tmp.config")
  private def mongoCollectionName =
    if (project contains "-") project.split("-")(0).toLowerCase()
    else project.split("/")(0).toLowerCase()

  override def launch(args: Array[String]): Unit = notAvailable("launch")
  override def compileCMD: Seq[String]           = notAvailable("compileCMD")

  def start(): Unit = {
    logger.info(s"Project: $project")
    logger.info("Generating config file for GenProg")
    step2_GenerateGenProgConfigFile(seed)
    logger.info("Running GenProg...")
    step3_RunGenProg()
    step3_1_SetupMongoAndKafka()
  }

  /**
    * Called by the patch generator
    */
  def step3_1_SetupMongoAndKafka(): Unit = {
    zip()
    val attemptObjectId = putInMongo()
    sqsSend(attemptObjectId)
  }

  def connectMongo(): (MongoDatabase, MongoCollection[Document]) = {
    val mongoClient = MongoClients.create(mongoURI)
    val varexpDB    = mongoClient.getDatabase("varexp")
    (varexpDB, varexpDB.getCollection(mongoCollectionName))
  }

  /**
    * Assuming that an attempt has the following fields:
    * {
    *   bug: String (e.g., Math-1b)
    *   status: String (CREATED | RUNNING | EXECUTED)
    *   date: java.util.Date
    *   approach: String (varexc | genprog)
    *   genprogConfig: String
    *   setupZip: ObjectId  (reference to a zip file)
    *   relevantTests: ObjectId (reference to a txt file)
    *   canFix: Boolean
    *   solutions: Array[String]
    *   log: ObjectId (reference to a execution log for debugging)
    *   executionStartTime: java.util.Date
    *   executionEndTime: java.util.Date
    * }
    */
  def putInMongo(): ObjectId = {

    case class UploadedObjects(zipObjectID: ObjectId,
                               relevantTestsObjectId: ObjectId,
                               genprogConfigObjectId: ObjectId)

    def uploadFiles(db: MongoDatabase): UploadedObjects = {
      val gridFSBucket          = GridFSBuckets.create(db)
      val toUploadZip           = new FileInputStream(new File(zipPathString))
      val toUploadRelevantTests = new FileInputStream(new File(relevantTestFilePathString))
      val toUploadGenProgConfig = new FileInputStream(new File(genprogConfigPathString))
      println("Uploading relevant test file")
      val relevantTestObjectId =
        gridFSBucket.uploadFromStream(s"$project.txt", toUploadRelevantTests)
      println("Uploading GenProg config to MongoDB")
      val genprogConfigObjectId =
        gridFSBucket.uploadFromStream(s"$project.zip", toUploadGenProgConfig)
      println("Uploading zip to MongoDB")
      val zipObjectId = gridFSBucket.uploadFromStream(s"$project.zip", toUploadZip)
      UploadedObjects(zipObjectId, relevantTestObjectId, genprogConfigObjectId)
    }

    val (mongoDB, mongoCollection) = connectMongo()
    val uploadedObjects            = uploadFiles(mongoDB)
    val attemptObjectId            = new ObjectId()

    val attempt = new Document("_id", attemptObjectId)
    attempt
      .append("bug", project)
      .append("status", "CREATED")
      .append("date", new java.util.Date())
      .append("approach", "varexc")
      .append("genprogConfig", uploadedObjects.genprogConfigObjectId)
      .append("setupZip", uploadedObjects.zipObjectID)
      .append("relevantTests", uploadedObjects.relevantTestsObjectId)
    mongoCollection.insertOne(attempt)

    attemptObjectId
  }

  def zip(): Unit = {
    val folder = mkPath(projects4VarexC, project)
    try {
      val archive = new ZipArchiveOutputStream(new FileOutputStream(zipPathString))
      archive.setLevel(Deflater.BEST_COMPRESSION)
      Files
        .walk(folder)
        .forEach(p => {
          val f            = p.toFile
          val relativePath = folder.relativize(p)
          if (!f.isDirectory && !shouldFilter(relativePath)) {
            println(s"Zipping file - ${relativePath.toFile.toString}")
            val entry = new ZipArchiveEntry(f, relativePath.toFile.toString)
            archive.putArchiveEntry(entry)
            val fis = new FileInputStream(f)
            IOUtils.copy(fis, archive)
            archive.closeArchiveEntry()
          }
        })
      archive.finish()
    } catch {
      case x: IOException =>
        x.printStackTrace()
        System.exit(2)
    }

    def shouldFilter(path: Path): Boolean = {
      path.startsWith("target") || path.startsWith(".git")
    }
  }

  def sqsSend(attemptObjectId: ObjectId): Unit = {
    val sqs = AmazonSQSClientBuilder.defaultClient()
    val send_msg_request = new SendMessageRequest()
      .withQueueUrl(sqsURI)
      .withMessageBody(mongoCollectionName + " " + attemptObjectId.toString)
      .withMessageGroupId(mongoCollectionName)
      .withMessageDeduplicationId(System.currentTimeMillis().toString)
    sqs.sendMessage(send_msg_request)
  }
}

trait CloudPatchRunner extends PatchRunner {
  val sqsURI: String   = System.getProperty("sqsURI")
  val mongoURI: String = System.getProperty("mongoURI")

  override def genprogPath: String                           = notAvailable("genprogPath")
  override def projects4GenProg: String                      = notAvailable("projects4GenProg")
  override def projects4VarexC: String                       = notAvailable("projects4VarexC")
  override def template(project: String, seed: Long): String = notAvailable("template")
  override def project: String                               = notAvailable("project")

  /**
    * Called by the patch executioner
    *
    * @return start time, end time, can fix or not, array of solutions
    */
  def runVarexC(): Unit = {
    val (collectionName, attemptObjectId) = sqsReceive()
    val (db, collection)                  = connectMongo(collectionName)
    updateStatusTo("RUNNING", collection, attemptObjectId)
    val project = downloadSetupFromMongo(db, collection, attemptObjectId)
    unzip(project)
    val destProject = mkPath("/tmp", project)
    Process(compileCMD, cwd = destProject.toFile).lazyLines.foreach(println)
    val startTime = new Date()
    launch(Array("/tmp", project))
    val endTime = new Date()

    val logObjectId = uploadLog(db)

    val startTimeUpdate = Updates.set("executionStartTime", startTime)
    val endTimeUpdate   = Updates.set("executionEndTime", endTime)
    val canFixUpdate    = Updates.set("canFix", VTestStat.hasOverallSolution)
    val solutionsUpdate = Updates.set("solutions", VTestStat.getOverallPassingCond.getAllSolutions)
    val logUpdate       = Updates.set("log", logObjectId)

    collection.updateOne(
      Filters.eq(attemptObjectId),
      Updates.combine(startTimeUpdate, endTimeUpdate, canFixUpdate, solutionsUpdate, logUpdate))
    updateStatusTo("EXECUTED", collection, attemptObjectId)
  }

  def uploadLog(db: MongoDatabase): ObjectId = {
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].stop()
    val gridFSBucket = GridFSBuckets.create(db)
    val toUpload     = new FileInputStream(new File("/tmp/varexc.log"))
    println("Uploading log file to MongoDB")
    gridFSBucket.uploadFromStream("varexc.log", toUpload)
  }

  def connectMongo(collectionName: String): (MongoDatabase, MongoCollection[Document]) = {
    val mongoClient = MongoClients.create(mongoURI)
    val varexpDB    = mongoClient.getDatabase("varexp")
    (varexpDB, varexpDB.getCollection(collectionName))
  }

  /**
    * Check kafka event stream and consume exactly one message
    *
    * @return collection name and ObjectId of the fix attempt
    */
  def sqsReceive(): (String, ObjectId) = {
    val sqs      = AmazonSQSClientBuilder.defaultClient()
    val request  = new ReceiveMessageRequest().withQueueUrl(sqsURI).withMaxNumberOfMessages(1)
    val messages = sqs.receiveMessage(request).getMessages
    if (messages.isEmpty) {
      println("Couldn't get any message from AWS SQS")
      System.exit(3)
      (null, null) // dead code
    } else {
      val message = messages.get(0)
      val body    = message.getBody.split(" ")
      println(s"Processing message: collection=${body(0)}, value=${body(1)}")
      val attemptObjectId = new ObjectId(body(1))
      sqs.deleteMessage(sqsURI, message.getReceiptHandle)
      (body(0), attemptObjectId)
    }
  }

  def updateStatusTo(status: String,
                     collection: MongoCollection[Document],
                     attemptObjectId: ObjectId): Unit = {
    collection.updateOne(Filters.eq(attemptObjectId), Updates.set("status", status))
  }

  /**
    *
    * @param db
    * @param collection
    * @param attemptObjectId
    * @return project name, e.g., Math-1b
    */
  def downloadSetupFromMongo(db: MongoDatabase,
                             collection: MongoCollection[Document],
                             attemptObjectId: ObjectId): String = {
    val attempt               = collection.find(Filters.eq(attemptObjectId)).first()
    val project               = attempt.get("bug").asInstanceOf[String]
    val zipObjectId           = attempt.get("setupZip").asInstanceOf[ObjectId]
    val relevantTestsObjectId = attempt.get("relevantTests").asInstanceOf[ObjectId]
    val gridFSBucket          = GridFSBuckets.create(db)
    val zipOutputStream       = new FileOutputStream(mkPathString("/tmp", project + ".zip"))
    val relevantTestsFolder   = new File("/tmp/RelevantTests")
    if (!relevantTestsFolder.exists()) relevantTestsFolder.mkdir()
    val relevantTestsOutputStream = new FileOutputStream(
      mkPathString("/tmp", "RelevantTests", project + ".txt"))
    gridFSBucket.downloadToStream(zipObjectId, zipOutputStream)
    gridFSBucket.downloadToStream(relevantTestsObjectId, relevantTestsOutputStream)
    zipOutputStream.close()
    relevantTestsOutputStream.close()
    project
  }

  def unzip(project: String): Unit = {
    try {
      val archive = new ZipArchiveInputStream(
        new BufferedInputStream(new FileInputStream(mkPathString("/tmp", project + ".zip"))))
      var entry = archive.getNextZipEntry
      while (entry != null) {
        val fp = mkPath("/tmp", project, entry.getName)
        println(s"Unzipping file - ${fp.toFile.toString}")
        val parent = fp.getParent
        if (parent != null) {
          Files.createDirectories(parent)
        }
        IOUtils.copy(archive, new FileOutputStream(fp.toFile))
        entry = archive.getNextZipEntry
      }
    } catch {
      case x: IOException =>
        x.printStackTrace()
        System.exit(2)
    }
  }
}

object MathCloudPatchGenerator extends App with CloudPatchGenerator {
  override def genprogPath: String      = args(0)
  override def projects4GenProg: String = args(1)
  override def projects4VarexC: String  = args(2)
  override def project: String          = args(3)
  override def template(project: String, seed: Long): String =
    s"""
       |javaVM = /usr/bin/java
       |popsize = ${ScriptConfig.popSize}
       |editMode = pre_compute
       |generations = 50
       |regenPaths = true
       |seed = ${seed}
       |classTestFolder = target/test-classes
       |workingDir = ${mkPathString(projects4GenProg, project)}
       |outputDir = ${mkPathString(projects4GenProg, project, "tmp")}
       |libs=${mkPathString(genprogPath, "lib", "hamcrest-core-1.3.jar")}:${mkPathString(
         genprogPath,
         "lib",
         "junit-4.12.jar")}:${mkPathString(genprogPath, "lib", "junittestrunner.jar")}:
       |sanity = yes
       |sourceDir = src/main/java
       |positiveTests = ${mkPathString(projects4GenProg, project, "pos.tests")}
       |negativeTests = ${mkPathString(projects4GenProg, project, "neg.tests")}
       |jacocoPath = ${mkPathString(genprogPath, "lib", "jacocoagent.jar")}
       |srcClassPath = ${mkPathString(projects4GenProg, project, "target", "classes")}
       |classSourceFolder = ${mkPathString(projects4GenProg, project, "target", "classes")}
       |testClassPath= ${mkPathString(projects4GenProg, project, "target", "test-classes")}
       |testGranularity = method
       |targetClassName = ${mkPathString(projects4GenProg, project, "targetClasses.txt")}
       |sourceVersion=1.8
       |sample=0.1
       |compileCommand=python3 ${mkPathString(projects4GenProg, project, "compile.py")}
       |edits=append;delete;replace;
       |
       |""".stripMargin

  start()
}

object MathCloudPatchRunner extends App with CloudPatchRunner {
  override def launch(args: Array[String]): Unit = ApacheMathLauncher.main(args)
  override def compileCMD                        = Seq("ant", "compile.tests")

  runVarexC()
}
