package com.cluda.tradersbit.signals.postsignal

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, StatusCodes, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.cluda.tradersbit.signals.model.Meta
import com.cluda.tradersbit.signals.protocoll.SignalProcessingException
import com.cluda.tradersbit.signals.util.{HttpUtil, MetaUtil}
import com.typesafe.config.ConfigFactory
import spray.json._

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
  private val https = config.getBoolean("microservices.https")
  private val authorizationHeader = RawHeader("Authorization", "apikey " + config.getString("microservices.streamsApiKey"))

  def doGet(globalRequestID: String, host: String, path: String, port: Int = streamInfoPort): Future[HttpResponse] = {
    HttpUtil.request(
      HttpMethods.GET,
      https,
      host,
      path,
      headers = List(RawHeader("Global-Request-ID", globalRequestID), authorizationHeader)
    )
  }

  def getExchangeAndArnANdName(globalRequestID: String, streamID: String): Future[(String, String, String)] = {
    val promise = Promise[(String, String, String)]()
    val theFuture = promise.future
    doGet(globalRequestID, streamInfoHost, "/streams/" + streamID + "?private=true").map { x =>
      if (x.status == StatusCodes.NotFound) {
        log.warning(s"[$globalRequestID]: Got responds from stream-info that their is no stream with id: " + streamID)
        promise.failure(new Exception(s"[$globalRequestID]: NO stream with that ID"))
      }
      Unmarshal(x.entity).to[String].map { string =>
        val json = string.parseJson.asJsObject
          val exchange = json.getFields("exchange").head.toString()
          val arn = json.fields.get("streamPrivate").get.asJsObject
            .getFields("topicArn").head.toString()
          val streamName = json.getFields("name").head.toString()
          log.info(s"[$globalRequestID]: Got responds from stream-info: that exchange: " + exchange + ", topicArn: " + arn)
          promise.success((exchange, arn, streamName))
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