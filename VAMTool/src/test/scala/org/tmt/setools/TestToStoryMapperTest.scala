package org.tmt.setools

import java.io.File

import org.scalatest.{FunSuite, Matchers}
import org.tmt.setools.Utilities.{TestFile, TestReference, UserStoryReference}

import scala.collection.immutable.{ListMap, Set}

class TestToStoryMapperTest extends FunSuite with Matchers {
  val HOME: String = System.getProperty("user.home")
  val extraTestData = Array(
    "csw-event/csw-event-client/src/test/scala/csw/event/client/EventPublisherTest.scala\t176\tcsw.event.client.EventPublisherTest\tshould_be_able_to_maintain_ordering_while_publish(Redis)\tDEOPSCSW-595",
    "csw-event/csw-event-client/src/test/scala/csw/event/client/EventPublisherTest.scala\t176\tcsw.event.client.EventPublisherTest\tshould_be_able_to_maintain_ordering_while_publish(Kafka)\tDEOPSCSW-595",
    "csw-event/csw-event-client/src/test/scala/csw/event/client/EventPublisherTest.scala\t146\tcsw.event.client.EventPublisherTest\tshould_be_able_to_publish_an_event_with_block_generating_future_of_event(Redis)\tDEOPSCSW-516",
    "csw-event/csw-event-client/src/test/scala/csw/event/client/EventPublisherTest.scala\t146\tcsw.event.client.EventPublisherTest\tshould_be_able_to_publish_an_event_with_block_generating_future_of_event(Kafka)\tDEOPSCSW-516",
    "csw-event/csw-event-client/src/test/scala/csw/event/client/EventPublisherTest.scala\t95\tcsw.event.client.EventPublisherTest\tshould_be_able_to_publish_an_event_with_duration(Redis)\tDEOPSCSW-345, DEOPSCSW-516",
    "csw-event/csw-event-client/src/test/scala/csw/event/client/EventPublisherTest.scala\t95\tcsw.event.client.EventPublisherTest\tshould_be_able_to_publish_an_event_with_duration(Kafka)\tDEOPSCSW-345, DEOPSCSW-516"
  )


  test("should parse scala file") {
    val mapper = new TestToStoryMapper("", ".")
    val f      = new File(".")
    println(f.getAbsolutePath)
    val testMapIn = ListMap[TestReference, Set[UserStoryReference]]()
    val testMap =
      mapper.processScalaFile(testMapIn,
                              TestFile("./src/test/scala/org/tmt/setools", "src/test/scala/org/tmt/setools/TestFile01.scala"))
    TestToStoryMapper.printMap(testMap)
  }

  test("should print tests") {
    val mapper  = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")
    val testMap = mapper.updateMapFromSheets()
    testMap.foreach { kv =>
      val t = kv._1
      println(t.packageName + "." + t.className + "," + t.testName + ",0.000,PASSED")
    }
  }

  test("should parse extra test data") {
    val mapper = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")
    val testMap = mapper.updateMapWithExtraLinkageFromList(ListMap[TestReference, Set[UserStoryReference]](), extraTestData.toIterator)
    testMap.foreach { kv =>
      val t = kv._1
      println(s"${t.packageName}.${t.className},${t.testName} -> ${kv._2}")
    }

  }
}
