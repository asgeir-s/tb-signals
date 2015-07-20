package com.cluda.coinsignals.signals.postsignal

import akka.actor.{Actor, ActorLogging, Props}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.{PublishRequest, PublishResult}
import com.cluda.coinsignals.protocol.Sec
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}
import com.typesafe.config.ConfigFactory

class NotifyActor extends Actor with ActorLogging {

  private val config = ConfigFactory.load()

  //create a new SNS client and set endpoint
  private val credentials = new BasicAWSCredentials(config.getString("aws.accessKeyId"), config.getString("aws.secretAccessKey"))
  private val snsClient: AmazonSNSClient = new AmazonSNSClient(credentials)
  snsClient.setRegion(Region.getRegion(Regions.US_WEST_2))


  override def receive: Receive = {
    case (arn: String, signals: Seq[Signal]) =>
      import SignalJsonProtocol._
      import spray.json._

      //publish to an SNS topic
      val publishRequest: PublishRequest = new PublishRequest(arn, Sec.secureMessage(signals.map(_.toJson).toJson.prettyPrint))
      val publishResult: PublishResult = snsClient.publish(publishRequest)

      //print MessageId of message published to SNS topic
      log.info("NotifyActor: Received " + signals.length + "new signal(s). Publishing to SNS. MessageId: " + publishResult.getMessageId)
  }
}