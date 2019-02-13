package org.tmt.setools

import org.junit.Test
import org.scalatest.{FunSuite, Matchers}

@StoryId(Array("DEOPSCSW-002: fake story 2",
               "DEOPSCSW-006: fake story 6"))
class AnnotatedScalaTest extends FunSuite with Matchers {

  @StoryId(Array("DEOPSCSW-003: fake story 3", "DEOPSCSW-007: fake story 7"))
  @Test def should_do_it_first() {
    println("scala")
    val i=3
    i shouldBe 3
  }

  @StoryId(Array("DEOPSCSW-004: fake story 4"))
  @Test def should_do_it_second() {


    @StoryId(Array("DEOPSCSW-005: fake story 5"))
    val n=5
    n shouldBe 5
  }

  @Test def should_do_it_third(): Unit = {
  }

  def method_that_is_not_a_test(): Unit = {
  }

}
