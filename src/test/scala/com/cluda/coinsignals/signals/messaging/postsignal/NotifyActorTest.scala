package com.cluda.coinsignals.signals.messaging.postsignal

import akka.actor.Props
import akka.testkit.TestActorRef
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.{CreateTopicRequest, CreateTopicResult, DeleteTopicRequest}
import com.cluda.coinsignals.signals.TestData
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.postsignal.NotifyActor
import com.typesafe.config.ConfigFactory

class NotifyActorTest extends MessagingTest {

  val credentials = new BasicAWSCredentials(config.getString("aws.accessKeyId"), config.getString("aws.secretAccessKey"))

  //create a new SNS client and set endpoint
  val snsClient: AmazonSNSClient = new AmazonSNSClient(credentials)
  snsClient.setRegion(Region.getRegion(Regions.US_WEST_2))

  //create a new SNS topic
  val createTopicRequest: CreateTopicRequest = new CreateTopicRequest("notifyActorTest")
  val createTopicResult: CreateTopicResult = snsClient.createTopic(createTopicRequest)
  val topicArn = createTopicResult.getTopicArn
  //print TopicArn
  System.out.println(createTopicResult)
  //get request id for CreateTopicRequest from SNS metadata
  System.out.println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest))


  override def afterTest(): Unit = {
    //delete an SNS topic
    val deleteTopicRequest: DeleteTopicRequest = new DeleteTopicRequest(topicArn)
    snsClient.deleteTopic(deleteTopicRequest)
    //get request id for DeleteTopicRequest from SNS metadata
    System.out.println("DeleteTopicRequest - " + snsClient.getCachedResponseMetadata(deleteTopicRequest))
  }

  "when receiving a signal it" should
    "should send a notification to the SNS topic with the signal" in {
    val actor = TestActorRef(Props[NotifyActor], "notifyActor1")
    actor ! Seq(TestData.signal1)
  }

  "when receiving multiple signals it" should
    "should send a notification to the SNS topic with the signals" in {
    val actor = TestActorRef(Props[NotifyActor], "notifyActor2")
    actor ! Seq(TestData.signalSeq(11), TestData.signalSeq(12))
  }

}
