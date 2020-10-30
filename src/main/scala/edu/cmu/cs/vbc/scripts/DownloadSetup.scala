package edu.cmu.cs.vbc.scripts

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream, IOException}
import java.nio.file.{FileSystems, Files, Path}

import com.mongodb.client.{MongoClients, MongoCollection, MongoDatabase}
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.model.Filters
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.utils.IOUtils
import org.bson.Document
import org.bson.types.ObjectId

import scala.sys.process._

object DownloadSetup extends App {
  val mongoURI = System.getProperty("mongoURI")
  assert(mongoURI != null, "please specify -DmongoURI")
  val collectionName = args(0)
  val attemptObjectId = new ObjectId(args(1))

  val (db, collection) = connectMongo(collectionName)
  val projectName = downloadSetupFromMongoToLocal(db, collection, attemptObjectId)
  unzip(projectName)
  Process(Seq("ant", "clean", "compile-tests"), cwd = Some(mkPath("Closure", projectName).toFile)).!

  def connectMongo(collectionName: String): (MongoDatabase, MongoCollection[Document]) = {
    val mongoClient = MongoClients.create(mongoURI)
    val varexpDB    = mongoClient.getDatabase("varexpatch")
    (varexpDB, varexpDB.getCollection(collectionName))
  }

  def downloadSetupFromMongoToLocal(db: MongoDatabase,
                             collection: MongoCollection[Document],
                             attemptObjectId: ObjectId): String = {
    val attempt     = collection.find(Filters.eq(attemptObjectId)).first()
    val projectName = attempt.get("bug").asInstanceOf[String]
    val approach    = attempt.get("approach").asInstanceOf[String]
    val zipObjectId =
      if (approach == "genprog")
        attempt.get("genprogSetup").asInstanceOf[ObjectId]
      else
        attempt.get("varexcSetup").asInstanceOf[ObjectId]
    val relevantTestsObjectId = attempt.get("relevantTests").asInstanceOf[ObjectId]
    val gridFSBucket          = GridFSBuckets.create(db)
    val zipOutputStream       = new FileOutputStream(mkPathString("Closure", projectName + ".zip"))
    val relevantTestsFolder   = new File("/Closure/RelevantTests")
    if (!relevantTestsFolder.exists()) relevantTestsFolder.mkdir()
    val relevantTestsOutputStream = new FileOutputStream(
      mkPathString("Closure", "RelevantTests", projectName + ".txt"))
    gridFSBucket.downloadToStream(zipObjectId, zipOutputStream)
    gridFSBucket.downloadToStream(relevantTestsObjectId, relevantTestsOutputStream)
    zipOutputStream.close()
    relevantTestsOutputStream.close()
    projectName
  }

  def unzip(projectName: String): Unit = {
    try {
      val archive = new ZipArchiveInputStream(
        new BufferedInputStream(new FileInputStream(mkPathString("Closure", projectName + ".zip"))))
      var entry = archive.getNextZipEntry
      while (entry != null) {
        val fp = mkPath("Closure", projectName, entry.getName)
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
    mkPath("Closure", projectName + ".zip").toFile.delete()
  }

  def mkPath(elems: String*): Path         = FileSystems.getDefault.getPath(elems.head, elems.tail: _*)
  def mkPathString(elems: String*): String = mkPath(elems: _*).toFile.getAbsolutePath
}
