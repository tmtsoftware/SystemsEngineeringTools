package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}

class AnnotationParserTest extends FunSuite with Matchers {

  test("should get annotations form java test") {
    println(AnnotationParser.getTestToStoryMap(classOf[AnnotatedJavaTest]))
  }

  test("should get annotations form scala test") {
    println(AnnotationParser.getTestToStoryMap(classOf[AnnotatedScalaTest]))
  }

}
