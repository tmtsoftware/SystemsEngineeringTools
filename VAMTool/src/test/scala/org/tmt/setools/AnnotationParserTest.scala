package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}

class AnnotationParserTest extends FunSuite with Matchers {

  private val expectedMap = Map(
    "should_do_it_first"  -> Set("DEOPSCSW-002: fake story 2", "DEOPSCSW-006: fake story 6", "DEOPSCSW-003: fake story 3", "DEOPSCSW-007: fake story 7"),
    "should_do_it_second" -> Set("DEOPSCSW-002: fake story 2", "DEOPSCSW-006: fake story 6", "DEOPSCSW-004: fake story 4"),
    "should_do_it_third"  -> Set("DEOPSCSW-002: fake story 2", "DEOPSCSW-006: fake story 6")
  )

  test("should get annotations form java test") {
    AnnotationParser.getTestToStoryMap(classOf[AnnotatedJavaTest]) shouldBe expectedMap
  }

  test("should get annotations form scala test") {
    AnnotationParser.getTestToStoryMap(classOf[AnnotatedScalaTest]) shouldBe expectedMap
  }

}
