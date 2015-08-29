package com.cluda.coinsignals.signals.postsignal

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.{PublishRequest, PublishResult}
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}
import com.typesafe.config.ConfigFactory

class NotifyActor(httpNotifierActor: ActorRef) extends Actor with ActorLogging {

  private val config = ConfigFactory.load()
  val streamServiceHost = config.getString("microservices.streams")

  //create a new SNS client and set endpoint
  private val credentials = new BasicAWSCredentials(config.getString("aws.accessKeyId"), config.getString("aws.secretAccessKey"))
  private val snsClient: AmazonSNSClient = new AmazonSNSClient(credentials)
  snsClient.setRegion(Region.getRegion(Regions.US_WEST_2))


  override def receive: Receive = {
    case (streamID: String, arn: String, signals: Seq[Signal]) =>
      log.info("NotifyActor: Received " + signals.length + "new signal(s).")
      import SignalJsonProtocol._
      import spray.json._

      val signalsString = signals.map(_.toJson).toJson.prettyPrint

      //publish to an SNS topic
      val publishRequest: PublishRequest = new PublishRequest(arn, signalsString)
      val publishResult: PublishResult = snsClient.publish(publishRequest)

      //print MessageId of message published to SNS topic
      log.info("NotifyActor: Published signals to SNS. MessageId: " + publishResult.getMessageId)

      httpNotifierActor ! (HttpNotification(streamServiceHost, "/streams/" + streamID + "/signals", signalsString), 0)

  }
}

object NotifyActor {
  def props(httpNotifierActor: ActorRef): Props = Props(new NotifyActor(httpNotifierActor))
}