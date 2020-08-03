package com.ownimage.framework.math;

import lombok.val;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.LogManager;
import java.util.stream.IntStream;

public class KMathTest {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Test
    public void fromCenter_01() {
        // GIVEN
        val max = 10;
        val expected = "5, 4, 6, 3, 7, 2, 8, 1, 9, 0, 10, ";
        val sb = new StringBuilder();
        // WHEN
        IntStream.rangeClosed(0, max).map(i -> KMath.fromCenter(i, max)).forEach(i -> sb.append(i + ", "));
        // THEN
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void fromCenter_02() {
        // GIVEN
        val max = 0;
        val expected = "0, ";
        val sb = new StringBuilder();
        // WHEN
        IntStream.rangeClosed(0, max).map(i -> KMath.fromCenter(i, max)).forEach(i -> sb.append(i + ", "));
        // THEN
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void fromCenter_03() {
        // GIVEN
        val max = 1;
        val expected = "1, 0, ";
        val sb = new StringBuilder();
        // WHEN
        IntStream.rangeClosed(0, max).map(i -> KMath.fromCenter(i, max)).forEach(i -> sb.append(i + ", "));
        // THEN
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void fromCenter_04() {
        // GIVEN
        val max = 2;
        val expected = "1, 0, 2, ";
        val sb = new StringBuilder();
        // WHEN
        IntStream.rangeClosed(0, max).map(i -> KMath.fromCenter(i, max)).forEach(i -> sb.append(i + ", "));
        // THEN
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void fromCenter_5() {
        // GIVEN
        val max = 11;
        val expected = "6, 5, 7, 4, 8, 3, 9, 2, 10, 1, 11, 0, ";
        val sb = new StringBuilder();
        // WHEN
        IntStream.rangeClosed(0, max).map(i -> KMath.fromCenter(i, max)).forEach(i -> sb.append(i + ", "));
        // THEN
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void fromCenter_6() {
        // GIVEN
        val max = 11;
        val expected = "5, 4, 6, 3, 7, 2, 8, 1, 9, 0, 10, ";
        val sb = new StringBuilder();
        // WHEN
        IntStream.range(0, max).map(i -> KMath.fromCenter(i, 10)).forEach(i -> sb.append(i + ", "));
        // THEN
        Assert.assertEquals(expected, sb.toString());
    }

}
