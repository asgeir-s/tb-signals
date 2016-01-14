package com.cluda.tradersbit.signals.postsignal

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.{PublishRequest, PublishResult}
import com.cluda.tradersbit.signals.model.SignalJsonProtocol
import com.cluda.tradersbit.signals.model.{SignalJsonProtocol, Signal}
import com.typesafe.config.ConfigFactory

class NotifyActor(httpNotifierActor: ActorRef) extends Actor with ActorLogging {

  private val config = ConfigFactory.load()
  val streamServiceHost = config.getString("microservices.streams")
  private val streamsApiKey = config.getString("microservices.streamsApiKey")
  private val https = config.getBoolean("microservices.https")

  private val awsAccessKeyId = config.getString("aws.accessKeyId")
  private val awsSecretAccessKey = config.getString("aws.secretAccessKey")

  private val snsClient: AmazonSNSClient =
    if(awsAccessKeyId == "none" || awsSecretAccessKey == "none"){
      new AmazonSNSClient()
    }
    else {
      new AmazonSNSClient(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey))
    }

  snsClient.setRegion(Region.getRegion(Regions.fromName(config.getString("aws.sns.region"))))

  override def receive: Receive = {
    case (globalRequestID: String, streamID: String, arn: String,  streamName: String, signals: Seq[Signal]) =>
      log.info(s"[$globalRequestID]: Received " + signals.length + "new signal(s).")
      import SignalJsonProtocol._
      import spray.json._

      val signalsString = signals.map(_.toJson).toJson.prettyPrint
      val notificationJson = Map("streamName"-> streamName.toJson, "streamId" -> streamID.toJson,"signals" -> signals.map(_.toJson).toJson).toJson.prettyPrint

      println("signalsString: " + signalsString)
      println("notificationJson: " + notificationJson)
      //publish to an SNS topic
      val publishRequest: PublishRequest = new PublishRequest(arn, notificationJson)
      val publishResult: PublishResult = snsClient.publish(publishRequest)

      //print MessageId of message published to SNS topic
      log.info(s"[$globalRequestID]: Published signals to SNS. MessageId: " + publishResult.getMessageId)

      httpNotifierActor ! (globalRequestID, HttpNotification(streamServiceHost, "/streams/" + streamID + "/signals", signalsString, streamsApiKey, https), 3)
  }
}

object NotifyActor {
  def props(httpNotifierActor: ActorRef): Props = Props(new NotifyActor(httpNotifierActor))
}