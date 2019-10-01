/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import com.ownimage.framework.math.IntegerPoint;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Range2DTest {

    private Vector<IntegerPoint> expected = new Vector<>();
    private Vector<IntegerPoint> actual = new Vector<>();

    public void setupTest(int minX, int maxX, int stepX, int minY, int maxY, int stepY) {
        for (int x = minX; x < maxX; x = x + stepX)
            for (int y = minY; y < maxY; y = y + stepY)
                expected.add(new IntegerPoint(x, y));
    }

    @Test
    public void forEach_01() {
        // GIVEN
        int minX = 0;
        int maxX = 30;
        int stepX = 1;
        int minY = 0;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(maxX, maxY);
        // WHEN
        underTest.forEach((x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEach_02() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 1;
        int minY = 12;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, minY, maxY);
        // WHEN
        underTest.forEach((x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEach_03() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 2;
        int minY = 12;
        int maxY = 50;
        int stepY = 3;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        // WHEN
        underTest.forEach((x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallel_01() {
        // GIVEN
        int minX = 0;
        int maxX = 30;
        int stepX = 1;
        int minY = 0;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(maxX, maxY);
        // WHEN
        underTest.forEachParallel((x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallel_02() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 1;
        int minY = 12;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, minY, maxY);
        // WHEN
        underTest.forEachParallel((x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallel_03() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 2;
        int minY = 12;
        int maxY = 50;
        int stepY = 3;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        // WHEN
        underTest.forEachParallel((x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallelThread_xy__01() {
        // GIVEN
        int minX = 0;
        int maxX = 30;
        int stepX = 1;
        int minY = 0;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(maxX, maxY);
        // WHEN
        underTest.forEachParallelThread(8, (x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallelThread_xy__02() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 1;
        int minY = 12;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, minY, maxY);
        // WHEN
        underTest.forEachParallelThread(8, (x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallelThread_xy_03() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 2;
        int minY = 12;
        int maxY = 50;
        int stepY = 3;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        // WHEN
        underTest.forEachParallelThread(8, (x, y) -> actual.add(new IntegerPoint(x, y)));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallelThread_ip_01() {
        // GIVEN
        int minX = 0;
        int maxX = 30;
        int stepX = 1;
        int minY = 0;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(maxX, maxY);
        // WHEN
        underTest.forEachParallelThread(8, ip -> actual.add(ip));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallelThread_ip_02() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 1;
        int minY = 12;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, minY, maxY);
        // WHEN
        underTest.forEachParallelThread(8, ip -> actual.add(ip));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void forEachParallelThread_ip_03() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 2;
        int minY = 12;
        int maxY = 50;
        int stepY = 3;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        // WHEN
        underTest.forEachParallelThread(8, ip -> actual.add(ip));
        // THEN
        actual.sort((o1, o2) -> o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX());
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void stream_01() {
        // GIVEN
        int minX = 0;
        int maxX = 30;
        int stepX = 1;
        int minY = 0;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(maxX, maxY);
        // WHEN
        underTest.stream().forEach(p -> actual.add(p));
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void stream_02() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 1;
        int minY = 12;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, minY, maxY);
        // WHEN
        underTest.stream().forEach(p -> actual.add(p));
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void stream_03() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 2;
        int minY = 12;
        int maxY = 50;
        int stepY = 3;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        // WHEN
        underTest.stream().forEach(p -> actual.add(p));
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void iterator_01() {
        // GIVEN
        int minX = 0;
        int maxX = 30;
        int stepX = 1;
        int minY = 0;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(maxX, maxY);
        // WHEN
        for (IntegerPoint p : underTest) actual.add(p);
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void iterator_02() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 1;
        int minY = 12;
        int maxY = 50;
        int stepY = 1;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, minY, maxY);
        // WHEN
        for (IntegerPoint p : underTest) actual.add(p);
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void iterator_03() {
        // GIVEN
        int minX = 10;
        int maxX = 30;
        int stepX = 2;
        int minY = 12;
        int maxY = 50;
        int stepY = 3;
        setupTest(minX, maxX, stepX, minY, maxY, stepY);
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        // WHEN
        for (IntegerPoint p : underTest) actual.add(p);
        // THEN
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void performance_01() {
        // GIVEN
        int minX = 0;
        int maxX = 10000;
        int stepX = 3;
        int minY = 0;
        int maxY = 10000;
        int stepY = 2;
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        int iterations = 100000;
        Consumer<IntegerPoint> NOOP = ip -> {
        };
        // GIVEN WARM-UP
        underTest.stream().forEach(NOOP);
        underTest.stream().parallel().forEach(NOOP);
        underTest.forEach(NOOP);
        // WHEN
        val streamParallel = timeInMillis(() -> underTest.stream().parallel().forEach(NOOP));
        val stream = timeInMillis(() -> underTest.stream().forEach(NOOP));
        val forEach = timeInMillis(() -> underTest.forEach(NOOP));
        // THEN
        System.out.println(streamParallel + " " + stream + " " + forEach);
        Assert.assertTrue(stream <= forEach * 2); // there is a stream overhead
        Assert.assertTrue(streamParallel <= forEach * 2);
    }

    @Test
    public void performance_02() {
        // GIVEN
        int minX = 0;
        int maxX = 1000;
        int stepX = 3;
        int minY = 0;
        int maxY = 1000;
        int stepY = 2;
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        int iterations = 100000;
        Consumer<IntegerPoint> hardSum = ip -> hardSum(iterations);

        // GIVEN WARM-UP
        underTest.stream().forEach(hardSum);
        underTest.stream().parallel().forEach(hardSum);
        underTest.forEach(hardSum);
        // WHEN
        val streamParallel = timeInMillis(() -> underTest.stream().parallel().forEach(hardSum));
        val stream = timeInMillis(() -> underTest.stream().forEach(hardSum));
        val forEach = timeInMillis(() -> underTest.forEach(hardSum));
        // THEN
        System.out.println(streamParallel + " " + stream + " " + forEach);
        Assert.assertTrue(stream < forEach * 1.1);
        Assert.assertTrue(streamParallel < forEach);
    }

    @Test
    public void performance_03() {
        // GIVEN
        int minX = 0;
        int maxX = 1000;
        int stepX = 3;
        int minY = 0;
        int maxY = 1000;
        int stepY = 2;
        val underTest = new Range2D(minX, maxX, stepX, minY, maxY, stepY);
        int iterations = 100000;
        BiConsumer<Integer, Integer> hardSum = (x, y) -> hardSum(iterations);

        // GIVEN WARM-UP
        underTest.forEach(hardSum);
        underTest.forEachParallelThread(8, hardSum);
        // WHEN
        val streamParallel = timeInMillis(() -> underTest.forEachParallelThread(8, hardSum));
        val forEach = timeInMillis(() -> underTest.forEach(hardSum));
        // THEN
        System.out.println(streamParallel + " " + forEach);
        Assert.assertTrue(streamParallel < forEach);
    }

    private long timeInMillis(Runnable pRunnable) {
        val start = Instant.now();
        pRunnable.run();
        val end = Instant.now();
        val duration = Duration.between(start, end);
        return duration.toMillis();
    }

    private void hardSum(final int pCount) {
        int total = 0;
        for (int x = 0; x < pCount; x++) {
            total += x;
        }
    }

}
