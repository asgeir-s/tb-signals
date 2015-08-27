package com.cluda.coinsignals.signals.postsignal

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{StatusCodes, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.cluda.coinsignals.signals.model.Meta
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException
import com.cluda.coinsignals.signals.util.MetaUtil
import com.typesafe.config.ConfigFactory

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



  def doGet(host: String, path: String, port: Int = streamInfoPort): Future[HttpResponse] = {
    val conn = Http().outgoingConnection(host, port)
    val request = HttpRequest(GET, uri = path)
    Source.single(request).via(conn).runWith(Sink.head[HttpResponse])
  }

  def getExchangeAndArn(streamID: String): Future[(String, String)] = {
    import spray.json._
    // if you don't supply your own Protocol (see below)

    val promise = Promise[(String, String)]()
    val theFuture = promise.future
    doGet(streamInfoHost, "/streams/" + streamID + "?private=true").map { x =>
      if (x.status == StatusCodes.NotFound) {
        log.warning("Step1_StreamInfoActor: Got responds from stream-info that their is no stream with id: " + streamID)
        promise.failure(new Exception("NO stream with that ID"))
      }
      Unmarshal(x.entity).to[String].map { string =>
          println("STRING BACK: " + string)
          val exchange = string.parseJson.asJsObject.getFields("exchange").head.toString()
          val arn = string.parseJson.asJsObject.fields.get("streamPrivate").get.asJsObject
            .getFields("topicArn").head.toString()
          log.info("Step1_StreamInfoActor: Got responds from stream-info: that exchange: " + exchange + ", topicArn: " + arn)
          promise.success((exchange, arn))
        }
    }
    theFuture
  }

  override def receive: Receive = {
    case meta: Meta =>
      log.info("Step1_GetExchangeActor got meta: " + meta)

      getExchangeAndArn(meta.streamID).map {
        case (exchangeName: String, awsARN: String) =>
          getPriceActor ! MetaUtil.setExchangeAndARN(meta, exchangeName, awsARN)
      }.recover {
        case _ =>
          log.error("cold not get the exchange and aws-sns-arn for the streamID: " + meta.streamID)
          meta.respondsActor.get ! SignalProcessingException(
            "cold not get the exchange and aws-sns-arn for the streamID: " + meta.streamID)
      }
  }
}

object Step1_StreamInfoActor {
  def props(getPriceActor: ActorRef): Props = Props(new Step1_StreamInfoActor(getPriceActor))
}