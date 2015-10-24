package com.cluda.tradersbit.signals.model

import akka.actor.ActorRef

/**
 * This is the object that will be populated during the "post-pipeline"
 * Created by sogasg on 18/05/15.
 */
case class Meta(respondsActor: Option[ActorRef], streamID: String, signal: Int, exchange: Option[String], price: Option[BigDecimal], timestamp: Option[Long], awsARN: Option[String])
