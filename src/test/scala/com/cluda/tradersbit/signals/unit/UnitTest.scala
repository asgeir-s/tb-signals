package com.cluda.tradersbit.signals.unit

import org.scalatest._

/**
 * All unit tests should extend this.
 */
abstract class
UnitTest extends FlatSpecLike with Matchers with
OptionValues with Inside with Inspectors with BeforeAndAfterAll {

}