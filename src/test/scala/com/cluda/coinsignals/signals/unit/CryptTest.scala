package com.cluda.coinsignals.signals.unit

import java.util.UUID

import com.cluda.coinsignals.signals.util.Crypt
import com.typesafe.config.ConfigFactory

import scala.util.Random

/**
 * Created by sogasg on 19/07/15.
 */
class CryptTest extends UnitTest {

  // This tests dont run on CircleCI because JCE Unlimited Strength is not available there
  /*
  val config = ConfigFactory.load()
  val salt = config.getString("crypt.salt").getBytes
  val password = config.getString("crypt.password").toCharArray
  val hKey = config.getString("crypt.hmac")

  "when something is encryted and decrypted withe the same key it" should
    "result in the data back (short)" in {
    val secret: String = "0123456689012345"
    val originalData: String  = "asgeir er kul"

    val encrypted = Crypt.encrypt(originalData.getBytes(), salt, password)

    val decrypted: String = Crypt.decrypt(encrypted._1.getBytes, encrypted._2, salt, password)

    assert(originalData == decrypted)
    assert(originalData != encrypted)

  }


  "when something is encryted and decrypted withe the same key it" should
    "result in the same data back (long)" in {
    val secret: String = "0123456789012345"
    val originalData: String  = """[
                          |  '{{repeat(5, 7)}}',
                          |  {
                          |    _id: '{{objectId()}}',
                          |    index: '{{index()}}',
                          |    guid: '{{guid()}}',
                          |    isActive: '{{bool()}}',
                          |    balance: '{{floating(1000, 4000, 2, "$0,0.00")}}',
                          |    picture: 'http://placehold.it/32x32',
                          |    age: '{{integer(20, 40)}}',
                          |    eyeColor: '{{random("blue", "brown", "green")}}',
                          |    name: '{{firstName()}} {{surname()}}',
                          |    gender: '{{gender()}}',
                          |    company: '{{company().toUpperCase()}}',
                          |    email: '{{email()}}',
                          |    phone: '+1 {{phone()}}',
                          |    address: '{{integer(100, 999)}} {{street()}}, {{city()}}, {{state()}}, {{integer(100, 10000)}}',
                          |    about: '{{lorem(1, "paragraphs")}}',
                          |    registered: '{{date(new Date(2014, 0, 1), new Date(), "YYYY-MM-ddThh:mm:ss Z")}}',
                          |    latitude: '{{floating(-90.000001, 90)}}',
                          |    longitude: '{{floating(-180.000001, 180)}}',
                          |    tags: [
                          |      '{{repeat(7)}}',
                          |      '{{lorem(1, "words")}}'
                          |    ],
                          |    friends: [
                          |      '{{repeat(3)}}',
                          |      {
                          |        id: '{{index()}}',
                          |        name: '{{firstName()}} {{surname()}}'
                          |      }
                          |    ],
                          |    greeting: function (tags) {
                          |      return 'Hello, ' + this.name + '! You have ' + tags.integer(1, 10) + ' unread messages.';
                          |    },
                          |    favoriteFruit: function (tags) {
                          |      var fruits = ['apple', 'banana', 'strawberry'];
                          |      return fruits[tags.integer(0, fruits.length - 1)];
                          |    }
                          |  }
                          |]"""

    val encrypted = Crypt.encrypt(originalData.getBytes(), salt, password)
    val decrypted: String = Crypt.decrypt(encrypted._1.getBytes, encrypted._2, salt, password)

    assert(originalData == decrypted)
    assert(originalData != encrypted)

  }


  "hmac of the same message with thge same key" should
    "result in the same signature" in {
    val sign1 = Crypt.hmacEncode("key2", "The quick brown fox jumps over the lazy dog")
    val sign2 = Crypt.hmacEncode("key2", "The quick brown fox jumps over the lazy dog")
    val sign3 = Crypt.hmacEncode("key", "The quick brown fox jumps over the lazy dog2")

    assert(sign1 == sign2)
    assert(sign1 != sign3)

  }

  "generateSecureMessage and receiveSecureMessage" should
    "encrypt and decrypt the message" in {
    val randomMassege = UUID.randomUUID().toString

    val encryptedMessage = Crypt.generateSecureMessage(randomMassege)
    assert(encryptedMessage.contains("hash"))
    assert(encryptedMessage.contains("iv"))
    assert(encryptedMessage.contains("data"))

    val decryptedMessageOption = Crypt.receiveSecureMessage(encryptedMessage)
    assert(randomMassege == decryptedMessageOption.get)
  }

  "hacked messages" should
    "not be excepted" in {
    val encMessage1 = """{ "hash": "5dfbf1dc398d6794e49f642e87e5abb1fffc21fa2c400fb7a161b5e249b8dd5", "iv": "BuEZWZv53LTSDsQUszGOwA==", "data": "4uf2FkV9lMzKPHWQCtc+3GPcOzmdCwiiNkkV0UIjzrD2Uv9ShiSwxRPjJ3e6D8J1"}
                       |"""
    assert(Crypt.receiveSecureMessage(encMessage1).isEmpty)

    val encMessage2 = """{ "hash": "15dfbf1dc398d6794e49f642e87e5abb1fffc21fa2c400fb7a161b5e249b8dd5", "iv": "BuEZWZv53LTSDsQUszGOwA==", "data": "4uf2FkV9lMzKPHWQCtc3GPcOzmdCwiiNkkV0UIjzrD2Uv9ShiSwxRPjJ3e6D8J1"}
                       |"""
    assert(Crypt.receiveSecureMessage(encMessage2).isEmpty)

    val encMessage3 = """{ "hash": "15dfbf1dc398d6794e49f642e87e5abb1fffc21fa2c400fb7a161b5e249b8dd5", "iv": "BEZWZv53LTSDsQUszGOwA==", "data": "4uf2FkV9lMzKPHWQCtc+3GPcOzmdCwiiNkkV0UIjzrD2Uv9ShiSwxRPjJ3e6D8J1"}
                       |"""
    assert(Crypt.receiveSecureMessage(encMessage3).isEmpty)
  }
  */

}
