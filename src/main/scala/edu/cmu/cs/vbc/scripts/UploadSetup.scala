package edu.cmu.cs.vbc.scripts

import java.io.{File, FileInputStream}

import com.mongodb.client.MongoClients
import com.mongodb.client.gridfs.GridFSBuckets

object UploadSetup extends App with CloudPatchGenerator {
  override def numMut = ???
  override def relevantTestFilePathString = ???
  override def mongoCollectionName = ???
  override def genprogPath = ???
  override def projects4GenProg = ???
  override def template(project: String, seed: Long) = ???

  override def projects4VarexC = args(0)
  override def project = args(1)

  val zipPathString = mkPathString(System.getProperty("java.io.tmpdir"), project + ".zip")
  zipSetup(mkPath(projects4VarexC, project), zipPathString, VarexC)

  println("Uploading VarexC setup to MongoDB")
  val mongoClient = MongoClients.create(mongoURI)
  val varexpDB    = mongoClient.getDatabase("varexpatch")
  val gridFSBucket = GridFSBuckets.create(varexpDB)

  val varexcSetupZip = new FileInputStream(new File(zipPathString))
  val varexcSetupObjectId =
    gridFSBucket.uploadFromStream(s"$project-varexc.zip", varexcSetupZip)

  println(varexcSetupObjectId)
}
