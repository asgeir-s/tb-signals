package com.cluda.tradersbit.signals.messaging.postsignal

import java.util.UUID

import akka.actor.Props
import akka.testkit.{TestProbe, TestActorRef}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.{CreateTopicRequest, CreateTopicResult, DeleteTopicRequest}
import com.cluda.tradersbit.signals.postsignal.HttpNotification
import com.cluda.tradersbit.signals.TestData
import com.cluda.tradersbit.signals.messaging.MessagingTest
import com.cluda.tradersbit.signals.postsignal.{NotifyActor, HttpNotification}
import com.sun.org.glassfish.external.probe.provider.annotations.Probe
import com.typesafe.config.ConfigFactory

class NotifyActorTest extends MessagingTest {

  val httpNotifierProbe = TestProbe()
  val credentials = new BasicAWSCredentials(config.getString("aws.accessKeyId"), config.getString("aws.secretAccessKey"))

  //create a new SNS client and set endpoint
  val snsClient: AmazonSNSClient = new AmazonSNSClient(credentials)
  snsClient.setRegion(Region.getRegion(Regions.US_WEST_2))

  //create a new SNS topic
  val createTopicRequest: CreateTopicRequest = new CreateTopicRequest("notifyActorTest")
  val createTopicResult: CreateTopicResult = snsClient.createTopic(createTopicRequest)
  val topicArn = createTopicResult.getTopicArn
  //print TopicArn
  //println(createTopicResult)
  //get request id for CreateTopicRequest from SNS metadata
  //println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest))


  override def afterTest(): Unit = {
    //delete an SNS topic
    val deleteTopicRequest: DeleteTopicRequest = new DeleteTopicRequest(topicArn)
    snsClient.deleteTopic(deleteTopicRequest)
    //get request id for DeleteTopicRequest from SNS metadata
    //println("DeleteTopicRequest - " + snsClient.getCachedResponseMetadata(deleteTopicRequest))
  }

  def globalRequestID = UUID.randomUUID().toString

  "when receiving a signal it" should
    "should send a notification to the SNS topic and send HTTP-notifications with the signal" in {
    val streamID = "test-stream-id"
    val actor = TestActorRef(NotifyActor.props(httpNotifierProbe.ref), "notifyActor1")
    actor ! (globalRequestID, streamID, topicArn, Seq(TestData.signal1))
    val messageToHttpNotifier1 = httpNotifierProbe.expectMsgType[(String, HttpNotification, Int)]
    //println(messageToHttpNotifier1._1.uri + " - " + "test1.com" + streamID +"signal")
    //assert(messageToHttpNotifier1.uri == "test1.com/" + streamID +"/signal")
    //assert(messageToHttpNotifier2.uri == "test2.com/" + streamID +"/signal")
    assert(messageToHttpNotifier1._2.body.contains("234.453"))

  }

  "when receiving multiple signals it" should
    "should send a notification to the SNS topic with the signals" in {
   // val actor = TestActorRef(NotifyActor.props(httpNotifierProbe.ref), "notifyActor2")
   // actor ! (globalRequestID, Seq(TestData.signalSeq(11), TestData.signalSeq(12)))
  }

}
