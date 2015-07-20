package com.cluda.coinsignals.protocol

import com.cluda.coinsignals.protocol.JwtUtil
import com.cluda.coinsignals.signals.unit.UnitTest

/**
 * Created by sogasg on 20/07/15.
 */
class JwtUtilTest extends UnitTest {

  "when calling write() with some json, a new encrypted and signed JWT (icluding the json)" should
    "be returned" in {

    val message = """{"something":"this is the message"}"""
    val jwt = JwtUtil.create(message)
    val dataOption = JwtUtil.validateAndRetrieve(jwt)
    val data = dataOption.get

    assert(message == data)
  }

}
