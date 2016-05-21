package com.cluda.tradersbit.signals.postsignal

import akka.actor._
import akka.stream.ActorMaterializer
import com.amazonaws.regions.RegionUtils
import com.cluda.tradersbit.signals.model.Meta
import com.cluda.tradersbit.signals.protocoll.SignalProcessingException
import com.cluda.tradersbit.signals.util.MetaUtil
import com.typesafe.config.ConfigFactory
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec

import scala.concurrent.{Future, Promise}


/**
 * Used to receive a streams exchange (thereby also checking existence)
 *
 */
class Step1_StreamInfoActor(getPriceActor: ActorRef) extends Actor with ActorLogging {

  implicit val executor = context.system.dispatcher
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  val streamInfoHost = config.getString("microservices.streams")
  val streamInfoPort = config.getInt("microservices.streamsPort")

  val client: AmazonDynamoDBClient = new AmazonDynamoDBClient()
  client.setRegion(RegionUtils.getRegion(config.getString("aws.dynamo.region")))
  val dynamoDB: DynamoDB = new DynamoDB(client)
  val table: Table = dynamoDB.getTable(config.getString("aws.dynamo.streamsTable"))

  /**
    * get stream with dynamoDB
    * @param globalRequestID
    * @param streamID
    * @return
    */
  def getExchangeAndArnANdName(globalRequestID: String, streamID: String): Future[(String, String, String)] = {
    val promise = Promise[(String, String, String)]()
    val theFuture = promise.future

    val spec: GetItemSpec = new GetItemSpec()
      .withPrimaryKey("id", streamID)
      .withAttributesToGet("exchange", "topicArn", "name")

    try {
      val outcome: Item = table.getItem(spec)
      promise.success((outcome.getString("exchange"), outcome.getString("topicArn"), outcome.getString("name")))
    } catch {
      case e: Throwable => {
        promise.failure(new Exception(s"[$globalRequestID]: NO stream with that ID"))
        println("Unable to get stream with id: " + streamID)
        println(e.getMessage)
      }
    }
    theFuture
  }

  override def receive: Receive = {
    case (globalRequestID: String, meta: Meta) =>
      log.info(s"[$globalRequestID]: Got meta: " + meta)

      getExchangeAndArnANdName(globalRequestID, meta.streamID).map {
        case (exchangeName: String, awsARN: String, streamName: String) =>
          getPriceActor ! (globalRequestID, MetaUtil.setExchangeAndARN(meta, exchangeName, awsARN).copy(streamName = Some(streamName)))
      }.recover {
        case _ =>
          log.error(s"[$globalRequestID]: Cold not get the exchange and aws-sns-arn for the streamID: " + meta.streamID)
          meta.respondsActor.get ! SignalProcessingException(
            s"[$globalRequestID]: cold not get the exchange and aws-sns-arn for the streamID: " + meta.streamID)
      }
  }
}

object Step1_StreamInfoActor {
  def props(getPriceActor: ActorRef): Props = Props(new Step1_StreamInfoActor(getPriceActor))
}