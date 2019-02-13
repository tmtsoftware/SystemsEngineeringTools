package org.tmt.setools;

import org.junit.Assert;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

@StoryId({"DEOPSCSW-002: fake story 2",
          "DEOPSCSW-006: fake story 6"})
public class AnnotatedJavaTest extends JUnitSuite {


    @StoryId({"DEOPSCSW-003: fake story 3",
              "DEOPSCSW-007: fake story 7"})
    @Test
    public void should_do_it_first() {
        System.out.println("java");
        int i = 3;
        Assert.assertEquals(i, 3);
    }

    @StoryId({"DEOPSCSW-004: fake story 4"})
    @Test
    public void should_do_it_second() {

        @StoryId({"DEOPSCSW-005: fake story 5"})
        int n=5;
        Assert.assertEquals(n, 5);
    }

    @Test
    public void should_do_it_third() {
    }

    public void method_that_is_not_a_test() {

    }
}

