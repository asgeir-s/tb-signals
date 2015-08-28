package com.cluda.coinsignals.signals.messaging.postsignal

import com.cluda.coinsignals.signals.messaging.MessagingTest

/**
 * Created by sogasg on 28/08/15.
 */
class HttpNotifierActorTest extends MessagingTest {

  "When getting a 'HttpNotification' it" should
    "send the http request and wait for respondse with statuscode 'Accepted' and id of the signal in the body" in {

  }

  it should "if no accepted message is returned resend the signal (10 times with 5 seconds between)" in {

  }

  it should "if 10 retries fail send a error message" in {

  }

}
