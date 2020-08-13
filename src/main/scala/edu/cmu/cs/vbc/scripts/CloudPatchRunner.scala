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
  sealed trait Approach
  case object VarexC extends Approach {
    override def toString: String = "varexc"
  }
  case object GenProg extends Approach {
    override def toString: String = "genprog"
  }

  sealed trait NumMutations
  case object Three extends NumMutations {
    override def toString: String = "3mut"
  }
  case object Eight extends NumMutations {
    override def toString: String = "8mut"
  }

  val sqsURIVarexC: String   = System.getProperty("sqsURIVarexC")
  val sqsURIGenProg: String = System.getProperty("sqsURIGenProg")
  val mongoURI: String = System.getProperty("mongoURI")

  // Needs to be configured
  def numMut: NumMutations
  def relevantTestFilePathString: String
  def mongoCollectionName: String // e.g., median-8op-genprog, math-3op-varexc

  def varexcSetupZipPathString  = mkPathString("/tmp", s"$projectName.zip")
  def genprogSetupZipPathString = mkPathString("/tmp", s"$projectName-genprog.zip")
  def genprogConfigPathString = mkPathString("/tmp", "tmp.config")

  override def launch(args: Array[String]): Unit = notAvailable("launch")
  override def compileCMD: Seq[String]           = notAvailable("compileCMD")

  def edits(): String = {
    if (numMut == Three) "append;delete;replace;"
    else if (numMut == Eight) "append;delete;replace;aor;ror;lcr;uoi;abs;"
    else throw new RuntimeException("Unexpected number of mutations")
  }

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
    val projectFolder4GenProg = mkPath(projects4GenProg, project)
    zipSetup(projectFolder4GenProg, genprogSetupZipPathString, GenProg)
    val projectFolder4VarexC = mkPath(projects4VarexC, project)
    zipSetup(projectFolder4VarexC, varexcSetupZipPathString, VarexC)
    val (varexcObjectId, genprogObjectId) = putInMongo()
    sqsSend(sqsURIVarexC, mongoCollectionName + s"-$VarexC", varexcObjectId)
    sqsSend(sqsURIGenProg, mongoCollectionName + s"-$GenProg", genprogObjectId)
  }

  def connectMongo(): (MongoDatabase, MongoCollection[Document], MongoCollection[Document]) = {
    val mongoClient = MongoClients.create(mongoURI)
    val varexpDB    = mongoClient.getDatabase("varexp")
    (varexpDB, varexpDB.getCollection(mongoCollectionName + s"-$VarexC"), varexpDB.getCollection(mongoCollectionName + s"-$GenProg"))
  }

  /**
    * Assuming that an attempt has the following fields:
    * {
    *   bug: String (e.g., Math-1b)
    *   status: String (CREATED | RUNNING | EXECUTED)
    *   date: java.util.Date
    *   approach: String (varexc | genprog)
    *   genprogConfig: String
    *   varexcSetup: ObjectId  (reference to a zip file)
    *   genprogSetup: ObjectId (reference to a zip file)
    *   relevantTests: ObjectId (reference to a txt file)
    *   canFix: Boolean
    *   solutions: ObjectId (reference to a txt file)
    *   log: ObjectId (reference to a execution log file for debugging)
    *   executionStartTime: java.util.Date
    *   executionEndTime: java.util.Date
    * }
    */
  def putInMongo(): (ObjectId, ObjectId) = {

    case class UploadedObjects(varexcSetupZipObjectId: ObjectId,
                               genprogSetupZipObjectId: ObjectId,
                               relevantTestsObjectId: ObjectId,
                               genprogConfigObjectId: ObjectId)

    def uploadFiles(db: MongoDatabase): UploadedObjects = {
      val gridFSBucket      = GridFSBuckets.create(db)

      println("Uploading relevant test file to MongoDB")
      val relevantTestsFile = new FileInputStream(new File(relevantTestFilePathString))
      val relevantTestObjectId =
        gridFSBucket.uploadFromStream(s"$projectName-relTests.txt", relevantTestsFile)

      println("Uploading GenProg config to MongoDB")
      val genprogConfigFile = new FileInputStream(new File(genprogConfigPathString))
      val genprogConfigObjectId =
        gridFSBucket.uploadFromStream(s"$projectName.config", genprogConfigFile)

      println("Uploading GenProg setup to MongoDB")
      val genprogSetupZip   = new FileInputStream(new File(genprogSetupZipPathString))
      val genprogSetupObjectId =
        gridFSBucket.uploadFromStream(s"$projectName-genprog.zip", genprogSetupZip)

      println("Uploading VarexC setup to MongoDB")
      val varexcSetupZip    = new FileInputStream(new File(varexcSetupZipPathString))
      val varexcSetupObjectId = gridFSBucket.uploadFromStream(s"$projectName-varexc.zip", varexcSetupZip)

      UploadedObjects(varexcSetupObjectId, genprogSetupObjectId, relevantTestObjectId, genprogConfigObjectId)
    }

    val (mongoDB, varexcCollection, genprogCollection) = connectMongo()
    val uploadedObjects            = uploadFiles(mongoDB)
    val varexcObjectId            = new ObjectId()
    val genprogObjectId = new ObjectId()

    val varexcAttempt = new Document("_id", varexcObjectId)
    varexcAttempt
      .append("bug", projectName)
      .append("status", "CREATED")
      .append("date", new java.util.Date())
      .append("approach", "varexc")
      .append("genprogConfig", uploadedObjects.genprogConfigObjectId)
      .append("varexcSetup", uploadedObjects.varexcSetupZipObjectId)
      .append("genprogSetup", uploadedObjects.genprogSetupZipObjectId)
      .append("relevantTests", uploadedObjects.relevantTestsObjectId)
    varexcCollection.insertOne(varexcAttempt)

    val genprogAttempt = new Document("_id", genprogObjectId)
    genprogAttempt
      .append("bug", projectName)
      .append("status", "CREATED")
      .append("date", new java.util.Date())
      .append("approach", "genprog")
      .append("genprogConfig", uploadedObjects.genprogConfigObjectId)
      .append("genprogSetup", uploadedObjects.genprogSetupZipObjectId)
      .append("relevantTests", uploadedObjects.relevantTestsObjectId)
    genprogCollection.insertOne(genprogAttempt)

    (varexcObjectId, genprogObjectId)
  }

  def zipSetup(folder: Path, outputPathString: String, approach: Approach): Unit = {
    try {
      val archive = new ZipArchiveOutputStream(new FileOutputStream(outputPathString))
      archive.setLevel(Deflater.BEST_COMPRESSION)
      Files
        .walk(folder)
        .forEach(p => {
          val f            = p.toFile
          val relativePath = folder.relativize(p)
          if (!f.isDirectory && !shouldFilter(relativePath)) {
            zipEntry(archive, f, relativePath)
          }
        })
      if (approach == GenProg) {
        zipEntry(archive, new File(mkPathString(genprogPath, "lib", "hamcrest-core-1.3.jar")), mkPath("hamcrest-core-1.3.jar"))
        zipEntry(archive, new File(mkPathString(genprogPath, "lib", "junit-4.12.jar")), mkPath("junit-4.12.jar"))
        zipEntry(archive, new File(mkPathString(genprogPath, "lib", "junittestrunner.jar")), mkPath("junittestrunner.jar"))
        zipEntry(archive, new File(mkPathString(genprogPath, "lib", "varexc.jar")), mkPath("varexc.jar"))
        zipEntry(archive, new File(mkPathString(genprogPath, "lib", "jacocoagent.jar")), mkPath("jacocoagent.jar"))
        zipEntry(archive, new File("coverage.path.neg"), mkPath("coverage.path.neg"))
        zipEntry(archive, new File("coverage.path.pos"), mkPath("coverage.path.pos"))
        zipEntry(archive, new File("FaultyStmtsAndWeights.txt"), mkPath("FaultyStmtsAndWeights.txt"))
        zipEntry(archive, new File("jacoco.exec"), mkPath("jacoco.exec"))
        zipEntry(archive, new File(mkPathString(genprogPath, "target", "uber-GenProg4Java-0.0.1-SNAPSHOT.jar")), mkPath("uber-GenProg4Java-0.0.1-SNAPSHOT.jar"))
      }
      archive.finish()
    } catch {
      case x: IOException =>
        x.printStackTrace()
        System.exit(2)
    }

    def shouldFilter(path: Path): Boolean = {
      path.startsWith(".git") // genprog needs the target folder as running maven in Docker has permission issues
    }

    def zipEntry(archive: ZipArchiveOutputStream, f: File, relativePath: Path): Unit = {
      println(s"Zipping file - ${relativePath.toFile.toString}")
      val entry = new ZipArchiveEntry(f, relativePath.toFile.toString)
      archive.putArchiveEntry(entry)
      val fis = new FileInputStream(f)
      IOUtils.copy(fis, archive)
      archive.closeArchiveEntry()
    }
  }

  def sqsSend(uri: String, collection: String, attemptObjectId: ObjectId): Unit = {
    val sqs = AmazonSQSClientBuilder.defaultClient()
    val send_msg_request = new SendMessageRequest()
      .withQueueUrl(uri)
      .withMessageBody(collection + " " + attemptObjectId.toString)
      .withMessageGroupId(collection)
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
  def run(): Unit = {
    val (collectionName, attemptObjectId) = sqsReceive()
    val (db, collection)                  = connectMongo(collectionName)
    val isGenProg = collectionName.endsWith("-genprog")
    updateStatusTo("RUNNING", collection, attemptObjectId)
    val projectName = downloadSetupFromMongo(db, collection, attemptObjectId)
    unzip(projectName)
    val destProject = mkPath("/tmp", projectName)
    Process(compileCMD, cwd = destProject.toFile).lazyLines.foreach(printlnAndLog)
    val startTime = new Date()
    if (isGenProg)
      launch(Array(collectionName, projectName))
    else
      launch(Array("/tmp", projectName))
    val endTime = new Date()

    val startTimeUpdate = Updates.set("executionStartTime", startTime)
    val endTimeUpdate   = Updates.set("executionEndTime", endTime)

    val logPathString =
      if (isGenProg) mkPathString("/tmp", projectName, "genprog.log")
      else mkPathString("/tmp", "varexc.log")
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].stop()
    val logObjectId = uploadFile(db, logPathString, s"$projectName-log.txt")
    val logUpdate       = Updates.set("log", logObjectId)

    val solutionsPathString = if (isGenProg) mkPathString("/tmp", projectName, "solutions.txt") else "solutions.txt"
    val canFix = if (isGenProg) new File(solutionsPathString).length() > 0 else VTestStat.hasOverallSolution
    val canFixUpdate    = Updates.set("canFix", canFix)
    val solutionsUpdate =
      if (canFix) {
        val solutionsObjectId = uploadFile(db, solutionsPathString, s"$projectName-solutions.txt")
        Updates.set("solutions", solutionsObjectId)
      }
      else Updates.set("solutions", "none")

    collection.updateOne(
      Filters.eq(attemptObjectId),
      Updates.combine(startTimeUpdate, endTimeUpdate, canFixUpdate, solutionsUpdate, logUpdate))
    updateStatusTo("EXECUTED", collection, attemptObjectId)
  }


  def uploadFile(db: MongoDatabase, filePathString: String, uploadFileName: String): ObjectId = {
    val gridFSBucket = GridFSBuckets.create(db)
    val toUpload     = new FileInputStream(new File(filePathString))
    println(s"Uploading ${filePathString} to MongoDB")
    gridFSBucket.uploadFromStream(uploadFileName, toUpload)
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
      printlnAndLog(s"Processing message: collection=${body(0)}, value=${body(1)}")
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
                             attemptObjectId: ObjectId
                            ): String = {
    val attempt               = collection.find(Filters.eq(attemptObjectId)).first()
    val projectName               = attempt.get("bug").asInstanceOf[String]
    val approach = attempt.get("approach").asInstanceOf[String]
    val zipObjectId           =
      if (approach == "genprog")
        attempt.get("genprogSetup").asInstanceOf[ObjectId]
      else
        attempt.get("varexcSetup").asInstanceOf[ObjectId]
    val relevantTestsObjectId = attempt.get("relevantTests").asInstanceOf[ObjectId]
    val gridFSBucket          = GridFSBuckets.create(db)
    val zipOutputStream       = new FileOutputStream(mkPathString("/tmp", projectName + ".zip"))
    val relevantTestsFolder   = new File("/tmp/RelevantTests")
    if (!relevantTestsFolder.exists()) relevantTestsFolder.mkdir()
    val relevantTestsOutputStream = new FileOutputStream(
      mkPathString("/tmp", "RelevantTests", projectName + ".txt"))
    gridFSBucket.downloadToStream(zipObjectId, zipOutputStream)
    gridFSBucket.downloadToStream(relevantTestsObjectId, relevantTestsOutputStream)
    zipOutputStream.close()
    relevantTestsOutputStream.close()
    projectName
  }

  def unzip(projectName: String): Unit = {
    try {
      val archive = new ZipArchiveInputStream(
        new BufferedInputStream(new FileInputStream(mkPathString("/tmp", projectName + ".zip"))))
      var entry = archive.getNextZipEntry
      while (entry != null) {
        val fp = mkPath("/tmp", projectName, entry.getName)
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
  override def relevantTestFilePathString =
    mkPathString(projects4VarexC, "RelevantTests", project + ".txt")
  override def mongoCollectionName: String = project.split("-")(0).toLowerCase()
  override def numMut = Three
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
       |edits=${edits()}
       |
       |""".stripMargin

  start()
}

object MathCloudPatchRunner extends App with CloudPatchRunner {
  override def launch(args: Array[String]): Unit = ApacheMathLauncher.main(args)
  override def compileCMD                        = Seq("ant", "compile.tests")

  run()
}
