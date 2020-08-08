/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util.immutable;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImmutableMap2DTest {

    @Test
    public void constructor() {
        ImmutableMap2D<Integer> underTest = new ImmutableMap2D<>(10, 20, 0);
    }

    @Test
    public void getDefaultValue() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        assertEquals("0", underTest.get(5, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void xNegative() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest.get(-1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void yNegative() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest.get(5, -1);
    }

    @Test
    public void xMax() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(20, 10, "0");
        assertEquals("0", underTest.get(19, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void xTooLarge() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(20, 10, "0");
        underTest.get(20, 5);
    }

    @Test
    public void yMax() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        assertEquals("0", underTest.get(5, 19));
    }

    @Test(expected = IllegalArgumentException.class)
    public void yTooLarge() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest.get(5, 20);
    }

    @Test
    public void setValueAndSwitchBetweenversions() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest = underTest.set(5, 5, "1");
        ImmutableMap2D<String> other = underTest;
        other = other.set(5, 5, "2");
        assertEquals("1", underTest.get(5, 5));
        assertEquals("2", other.get(5, 5));
        assertEquals("1", underTest.get(5, 5));
    }

    @Test
    public void setValueAndSwitchBetweenversionsStressTest() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest = underTest.set(5, 5, "1");
        ImmutableMap2D<String> other = underTest;
        other = other.set(5, 5, "2");

        for (int i = 0; i < 50000; i++) {
            other = other.set(6, 6, Integer.toString(i));
        }

        assertEquals("1", underTest.get(5, 5));
        assertEquals("2", other.get(5, 5));
        assertEquals("1", underTest.get(5, 5));
    }

    @Test
    public void setDefaultValueAndSwitchBetweenversions() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest = underTest.set(5, 5, "1");
        ImmutableMap2D<String> other = underTest;
        other = other.set(5, 5, "2");
        underTest = underTest.set(5, 5, "0");
        assertEquals("0", underTest.get(5, 5));
        assertEquals("2", other.get(5, 5));
        assertEquals("0", underTest.get(5, 5));
    }

    @Test
    public void clear() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest = underTest.set(5, 5, "1");
        ImmutableMap2D<String> other = underTest.clear();
        other = other.set(6, 6, "2");
        underTest = underTest.set(6, 6, "3");
        assertEquals("1", underTest.get(5, 5));
        assertEquals("2", other.get(6, 6));
        assertEquals("3", underTest.get(6, 6));
    }

    @Test
    public void abc() {
        ImmutableMap2D<String> a = new ImmutableMap2D<>(10, 10, "0");
        a = a.set(1, 1, "a11");
        ImmutableMap2D<String> b = a.set(2, 1, "b21");
        ImmutableMap2D<String> c = b.set(3, 1, "c31");
        a = a.set(1, 2, "a12");
        b = b.set(2, 2, "b22");
        c = c.set(3, 2, "c32");

        assertEquals("0", a.get(5, 5));
        assertEquals("a11", a.get(1, 1));
        assertEquals("a12", a.get(1, 2));
        assertEquals("0", a.get(2, 1));
        assertEquals("0", a.get(2, 2));
        assertEquals("0", a.get(3, 1));
        assertEquals("0", a.get(3, 2));

        assertEquals("0", b.get(5, 5));
        assertEquals("a11", b.get(1, 1));
        assertEquals("0", b.get(1, 2));
        assertEquals("b21", b.get(2, 1));
        assertEquals("b22", b.get(2, 2));
        assertEquals("0", b.get(3, 1));
        assertEquals("0", b.get(3, 2));

        assertEquals("0", c.get(5, 5));
        assertEquals("a11", c.get(1, 1));
        assertEquals("0", c.get(1, 2));
        assertEquals("b21", c.get(2, 1));
        assertEquals("0", c.get(2, 2));
        assertEquals("c31", c.get(3, 1));
        assertEquals("c32", c.get(3, 2));
    }

    @Test
    public void getSize_01() {
        // GIVEN
        val underTest = new ImmutableMap2D<>(100, 100, "0");
        // THEN
        assertEquals(0, underTest.size());
    }

    @Test
    public void getSize_02() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(100, 100, "0");
        // WHEN
        underTest = underTest.set(1, 1, "1");
        // THEN
        assertEquals(1, underTest.size());
    }

    @Test
    public void getSize_03() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(100, 100, "0");
        // WHEN
        underTest = underTest.set(1, 1, "1");
        underTest = underTest.set(1, 1, "2");
        // THEN
        assertEquals(1, underTest.size());
    }

    @Test
    public void getSize_04() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(100, 100, "0");
        // WHEN
        underTest = underTest.set(1, 1, "1");
        underTest = underTest.set(2, 2, "2");
        // THEN
        assertEquals(2, underTest.size());
    }

    @Test
    public void forAll_01() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(100, 100, "0");
        // WHEN
        var result = underTest.forEach(v -> "1");
        // THEN
        assertEquals(0, underTest.size());
        assertEquals(result.get(1, 1), "1");
    }

    @Test
    public void forAll_02() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(100, 100, "0");
        underTest = underTest.set(1, 1, "1");
        // WHEN
        var result = underTest.forEach(v -> "2");
        // THEN
        assertEquals(1, underTest.size());
        assertEquals(result.get(2, 2), "2");
        assertEquals(result.get(1, 1), "2");
    }

    @Test
    public void width_height_01_02() {
        // GIVEN
        int width = 101;
        int height = 333;
        var underTest = new ImmutableMap2D<>(width, height, "0");
        // WHEN
        underTest = underTest.set(1, 1, "1");
        // THEN
        assertEquals(width, underTest.width());
        assertEquals(height, underTest.height());
    }

    @Test
    public void entrySet_00() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(10, 10, "0");
        // WHEN
        var actual = underTest.entrySet();
        // THEN
        assertEquals(0, actual.size());
    }

    @Test
    public void entrySet_01() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(10, 10, "0");
        underTest = underTest.set(1, 1, "X");
        // WHEN
        var actual = underTest.entrySet();
        // THEN
        assertEquals(1, actual.size());
    }

    @Test
    public void entrySet_02() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(10, 10, "0");
        underTest = underTest.set(1, 1, "X");
        underTest = underTest.set(1, 1, "Y");
        // WHEN
        var actual = underTest.entrySet();
        // THEN
        assertEquals(1, actual.size());
    }

    @Test
    public void entrySet_03() {
        // GIVEN
        var underTest = new ImmutableMap2D<>(10, 10, "0");
        underTest = underTest.set(1, 1, "X");
        underTest = underTest.set(2, 2, "X");
        // WHEN
        var actual = underTest.entrySet();
        // THEN
        assertEquals(2, actual.size());
    }
}