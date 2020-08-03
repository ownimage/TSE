package com.ownimage.framework.math;

import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class LineTest {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

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
        assertEquals(expected, sb.toString());
    }

    @Test
    public void test() {
        var expected = "Point{ x : 0.0, y: 0.0 } Point{ x : 0.0, y: 1.0 } Point{ x : 0.0, y: 2.0 } Point{ x : 0.0, y: 3.0 } Point{ x : 0.0, y: 4.0 } Point{ x : 0.0, y: 5.0 } Point{ x : 0.0, y: 6.0 } Point{ x : 0.0, y: 7.0 } Point{ x : 0.0, y: 8.0 } Point{ x : 0.0, y: 9.0 } Point{ x : 0.0, y: 10.0 } Point{ x : 1.0, y: 0.0 } Point{ x : 1.0, y: 1.0 } Point{ x : 1.0, y: 2.0 } Point{ x : 1.0, y: 3.0 } Point{ x : 1.0, y: 4.0 } Point{ x : 1.0, y: 5.0 } Point{ x : 1.0, y: 6.0 } Point{ x : 1.0, y: 7.0 } Point{ x : 1.0, y: 8.0 } Point{ x : 1.0, y: 9.0 } Point{ x : 1.0, y: 10.0 } Point{ x : 2.0, y: 0.0 } Point{ x : 2.0, y: 1.0 } Point{ x : 2.0, y: 2.0 } Point{ x : 2.0, y: 3.0 } Point{ x : 2.0, y: 4.0 } Point{ x : 2.0, y: 5.0 } Point{ x : 2.0, y: 6.0 } Point{ x : 2.0, y: 7.0 } Point{ x : 2.0, y: 8.0 } Point{ x : 2.0, y: 9.0 } Point{ x : 2.0, y: 10.0 } Point{ x : 3.0, y: 0.0 } Point{ x : 3.0, y: 1.0 } Point{ x : 3.0, y: 2.0 } Point{ x : 3.0, y: 3.0 } Point{ x : 3.0, y: 4.0 } Point{ x : 3.0, y: 5.0 } Point{ x : 3.0, y: 6.0 } Point{ x : 3.0, y: 7.0 } Point{ x : 3.0, y: 8.0 } Point{ x : 3.0, y: 9.0 } Point{ x : 3.0, y: 10.0 } Point{ x : 4.0, y: 0.0 } Point{ x : 4.0, y: 1.0 } Point{ x : 4.0, y: 2.0 } Point{ x : 4.0, y: 3.0 } Point{ x : 4.0, y: 4.0 } Point{ x : 4.0, y: 5.0 } Point{ x : 4.0, y: 6.0 } Point{ x : 4.0, y: 7.0 } Point{ x : 4.0, y: 8.0 } Point{ x : 4.0, y: 9.0 } Point{ x : 4.0, y: 10.0 } Point{ x : 5.0, y: 0.0 } Point{ x : 5.0, y: 1.0 } Point{ x : 5.0, y: 2.0 } Point{ x : 5.0, y: 3.0 } Point{ x : 5.0, y: 4.0 } Point{ x : 5.0, y: 5.0 } Point{ x : 5.0, y: 6.0 } Point{ x : 5.0, y: 7.0 } Point{ x : 5.0, y: 8.0 } Point{ x : 5.0, y: 9.0 } Point{ x : 5.0, y: 10.0 } Point{ x : 6.0, y: 0.0 } Point{ x : 6.0, y: 1.0 } Point{ x : 6.0, y: 2.0 } Point{ x : 6.0, y: 3.0 } Point{ x : 6.0, y: 4.0 } Point{ x : 6.0, y: 5.0 } Point{ x : 6.0, y: 6.0 } Point{ x : 6.0, y: 7.0 } Point{ x : 6.0, y: 8.0 } Point{ x : 6.0, y: 9.0 } Point{ x : 6.0, y: 10.0 } Point{ x : 7.0, y: 0.0 } Point{ x : 7.0, y: 1.0 } Point{ x : 7.0, y: 2.0 } Point{ x : 7.0, y: 3.0 } Point{ x : 7.0, y: 4.0 } Point{ x : 7.0, y: 5.0 } Point{ x : 7.0, y: 6.0 } Point{ x : 7.0, y: 7.0 } Point{ x : 7.0, y: 8.0 } Point{ x : 7.0, y: 9.0 } Point{ x : 7.0, y: 10.0 } Point{ x : 8.0, y: 0.0 } Point{ x : 8.0, y: 1.0 } Point{ x : 8.0, y: 2.0 } Point{ x : 8.0, y: 3.0 } Point{ x : 8.0, y: 4.0 } Point{ x : 8.0, y: 5.0 } Point{ x : 8.0, y: 6.0 } Point{ x : 8.0, y: 7.0 } Point{ x : 8.0, y: 8.0 } Point{ x : 8.0, y: 9.0 } Point{ x : 8.0, y: 10.0 } Point{ x : 9.0, y: 0.0 } Point{ x : 9.0, y: 1.0 } Point{ x : 9.0, y: 2.0 } Point{ x : 9.0, y: 3.0 } Point{ x : 9.0, y: 4.0 } Point{ x : 9.0, y: 5.0 } Point{ x : 9.0, y: 6.0 } Point{ x : 9.0, y: 7.0 } Point{ x : 9.0, y: 8.0 } Point{ x : 9.0, y: 9.0 } Point{ x : 9.0, y: 10.0 } Point{ x : 10.0, y: 0.0 } Point{ x : 10.0, y: 1.0 } Point{ x : 10.0, y: 2.0 } Point{ x : 10.0, y: 3.0 } Point{ x : 10.0, y: 4.0 } Point{ x : 10.0, y: 5.0 } Point{ x : 10.0, y: 6.0 } Point{ x : 10.0, y: 7.0 } Point{ x : 10.0, y: 8.0 } Point{ x : 10.0, y: 9.0 } Point{ x : 10.0, y: 10.0 } ";
        var actual = new StringBuilder();
        IntStream.rangeClosed(0, 10)
                .boxed()
                .flatMap(i -> IntStream.rangeClosed(0, 10)
                        .mapToObj(j -> new Point(i, j)))
                .forEach(i -> actual.append(i + " "));
        assertEquals(expected, actual.toString());
    }

    @Test
    public void test2() {
        var expected = "Point{ x : 0.0, y: 0.0 } Point{ x : 1.0, y: 0.0 } Point{ x : 2.0, y: 0.0 } Point{ x : 3.0, y: 0.0 } Point{ x : 4.0, y: 0.0 } Point{ x : 5.0, y: 0.0 } Point{ x : 6.0, y: 0.0 } Point{ x : 7.0, y: 0.0 } Point{ x : 8.0, y: 0.0 } Point{ x : 9.0, y: 0.0 } Point{ x : 0.0, y: 1.0 } Point{ x : 1.0, y: 1.0 } Point{ x : 2.0, y: 1.0 } Point{ x : 3.0, y: 1.0 } Point{ x : 4.0, y: 1.0 } Point{ x : 5.0, y: 1.0 } Point{ x : 6.0, y: 1.0 } Point{ x : 7.0, y: 1.0 } Point{ x : 8.0, y: 1.0 } Point{ x : 9.0, y: 1.0 } Point{ x : 0.0, y: 2.0 } Point{ x : 1.0, y: 2.0 } Point{ x : 2.0, y: 2.0 } Point{ x : 3.0, y: 2.0 } Point{ x : 4.0, y: 2.0 } Point{ x : 5.0, y: 2.0 } Point{ x : 6.0, y: 2.0 } Point{ x : 7.0, y: 2.0 } Point{ x : 8.0, y: 2.0 } Point{ x : 9.0, y: 2.0 } Point{ x : 0.0, y: 3.0 } Point{ x : 1.0, y: 3.0 } Point{ x : 2.0, y: 3.0 } Point{ x : 3.0, y: 3.0 } Point{ x : 4.0, y: 3.0 } Point{ x : 5.0, y: 3.0 } Point{ x : 6.0, y: 3.0 } Point{ x : 7.0, y: 3.0 } Point{ x : 8.0, y: 3.0 } Point{ x : 9.0, y: 3.0 } Point{ x : 0.0, y: 4.0 } Point{ x : 1.0, y: 4.0 } Point{ x : 2.0, y: 4.0 } Point{ x : 3.0, y: 4.0 } Point{ x : 4.0, y: 4.0 } Point{ x : 5.0, y: 4.0 } Point{ x : 6.0, y: 4.0 } Point{ x : 7.0, y: 4.0 } Point{ x : 8.0, y: 4.0 } Point{ x : 9.0, y: 4.0 } Point{ x : 0.0, y: 5.0 } Point{ x : 1.0, y: 5.0 } Point{ x : 2.0, y: 5.0 } Point{ x : 3.0, y: 5.0 } Point{ x : 4.0, y: 5.0 } Point{ x : 5.0, y: 5.0 } Point{ x : 6.0, y: 5.0 } Point{ x : 7.0, y: 5.0 } Point{ x : 8.0, y: 5.0 } Point{ x : 9.0, y: 5.0 } Point{ x : 0.0, y: 6.0 } Point{ x : 1.0, y: 6.0 } Point{ x : 2.0, y: 6.0 } Point{ x : 3.0, y: 6.0 } Point{ x : 4.0, y: 6.0 } Point{ x : 5.0, y: 6.0 } Point{ x : 6.0, y: 6.0 } Point{ x : 7.0, y: 6.0 } Point{ x : 8.0, y: 6.0 } Point{ x : 9.0, y: 6.0 } Point{ x : 0.0, y: 7.0 } Point{ x : 1.0, y: 7.0 } Point{ x : 2.0, y: 7.0 } Point{ x : 3.0, y: 7.0 } Point{ x : 4.0, y: 7.0 } Point{ x : 5.0, y: 7.0 } Point{ x : 6.0, y: 7.0 } Point{ x : 7.0, y: 7.0 } Point{ x : 8.0, y: 7.0 } Point{ x : 9.0, y: 7.0 } Point{ x : 0.0, y: 8.0 } Point{ x : 1.0, y: 8.0 } Point{ x : 2.0, y: 8.0 } Point{ x : 3.0, y: 8.0 } Point{ x : 4.0, y: 8.0 } Point{ x : 5.0, y: 8.0 } Point{ x : 6.0, y: 8.0 } Point{ x : 7.0, y: 8.0 } Point{ x : 8.0, y: 8.0 } Point{ x : 9.0, y: 8.0 } Point{ x : 0.0, y: 9.0 } Point{ x : 1.0, y: 9.0 } Point{ x : 2.0, y: 9.0 } Point{ x : 3.0, y: 9.0 } Point{ x : 4.0, y: 9.0 } Point{ x : 5.0, y: 9.0 } Point{ x : 6.0, y: 9.0 } Point{ x : 7.0, y: 9.0 } Point{ x : 8.0, y: 9.0 } Point{ x : 9.0, y: 9.0 } ";
        var actual = new StringBuilder();
        IntStream.range(0, 10)
                .mapToObj(y -> IntStream.range(0, 10)
                        .mapToObj(x -> new Point(x, y)))
                .flatMap(Function.identity())
                .forEach(i -> actual.append(i + " "));
        assertEquals(expected, actual.toString());
    }

}
