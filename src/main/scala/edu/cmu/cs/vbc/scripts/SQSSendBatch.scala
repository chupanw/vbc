package edu.cmu.cs.vbc.scripts

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.SendMessageRequest
import org.bson.types.ObjectId

object SQSSendBatch extends App {

  val uri = "https://sqs.us-east-2.amazonaws.com/631830735476/varexp.fifo"

  def sqsSend(uri: String, collection: String, attemptObjectId: ObjectId): Unit = {
    val sqs = AmazonSQSClientBuilder.defaultClient()
    val send_msg_request = new SendMessageRequest()
      .withQueueUrl(uri)
      .withMessageBody(collection + " " + attemptObjectId.toString)
      .withMessageGroupId(collection)
      .withMessageDeduplicationId(System.currentTimeMillis().toString)
    sqs.sendMessage(send_msg_request)
  }

  val lines = io.Source.fromFile("running.csv").getLines().toList.filterNot(_.startsWith("//"))
  for (l <- lines) {
    val split = l.split(",").map(_.trim)
    sqsSend(uri, split(0), new ObjectId(split(1)))
  }
}
