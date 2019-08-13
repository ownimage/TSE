package com.ownimage.framework.math;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;
import java.util.stream.IntStream;

public class LineTest {

    @Test
    public void fromCenter_01() {
        // GIVEN
        val underTest = new Line(0.0d, 1.0d);
        val max = 10;
        val sb = new StringBuilder();
        val expected = "Point{ x : 0.5, y: 1.0 }, Point{ x : 0.4, y: 1.0 }, Point{ x : 0.6, y: 1.0 }, Point{ x : 0.3, y: 1.0 }, Point{ x : 0.7, y: 1.0 }, Point{ x : 0.2, y: 1.0 }, Point{ x : 0.8, y: 1.0 }, Point{ x : 0.1, y: 1.0 }, Point{ x : 0.9, y: 1.0 }, Point{ x : 0.0, y: 1.0 }, Point{ x : 1.0, y: 1.0 }, ";
        // WHEN
        underTest.streamFromCenter(10).forEach(i -> sb.append(i + ", "));
        //THEN
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void test() {
        IntStream.rangeClosed(0, 10)
                .mapToObj(i -> new Integer(i))
                .flatMap(i -> IntStream.rangeClosed(0, 10)
                        .mapToObj(j -> new Point(i, j)))
                .forEach(i -> System.out.println(i));
    }

    @Test
    public void test2() {
        IntStream.range(0, 10)
                .mapToObj(y -> IntStream.range(0, 10)
                        .mapToObj(x -> new Point(x, y)))
                .flatMap(Function.identity())
                .forEach(i -> System.out.println(i));
    }

}
