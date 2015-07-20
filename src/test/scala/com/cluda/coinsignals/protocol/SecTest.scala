package com.cluda.coinsignals.protocol

import com.cluda.coinsignals.protocol.{CryptUtil, JwtUtil, Sec}
import com.cluda.coinsignals.signals.unit.UnitTest

/**
 * Created by sogasg on 20/07/15.
 */
class SecTest extends UnitTest {

  val originalMessage =
    """{
      |    "timestamp": 1432122282747,
      |    "price": 200.453,
      |    "change": 0,
      |    "id": 1,
      |    "value": 100,
      |    "signal": 1
      |}""".stripMargin

  "message send and receive" should
    "work" in {

    val messageToSend = Sec.secureMessage(originalMessage)
    val receivedMessage = Sec.validateAndDecryptMessage(messageToSend)

    assert(originalMessage == receivedMessage.get)

    }


  "when a message is encrypted and placed in a jwt it" should
    "be possible to retreive the message back" in {

    val encryptedMessage = CryptUtil.generateSecureMessage(originalMessage)
    println("encryptedMessage: " + encryptedMessage)

    val jwt = JwtUtil.create(encryptedMessage)
    println("jwt: " + jwt)

    val encryptedMessageBack = JwtUtil.validateAndRetrieve(jwt).get
    println("encryptedMessageBack: " + encryptedMessageBack)
    assert(encryptedMessage == encryptedMessageBack)

    val messageBack = CryptUtil.receiveSecureMessage(encryptedMessageBack).get
    println("messageBack: " + messageBack)
    assert(originalMessage == messageBack)

  }



}
