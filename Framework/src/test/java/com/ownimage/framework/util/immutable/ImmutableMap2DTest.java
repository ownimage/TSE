/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util.immutable;

import org.junit.Assert;
import org.junit.Test;

public class ImmutableMap2DTest {

    @Test
    public void constructor() {
        final ImmutableMap2D<Integer> underTest = new ImmutableMap2D<>(10, 20, 0);
    }

    @Test
    public void getDefaultValue() {
        final ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        Assert.assertEquals("0", underTest.get(5, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void xNegative() {
        final ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest.get(-1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void yNegative() {
        final ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest.get(5, -1);
    }

    @Test
    public void xMax() {
        final ImmutableMap2D<String> underTest = new ImmutableMap2D<>(20, 10, "0");
        Assert.assertEquals("0", underTest.get(19, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void xTooLarge() {
        final ImmutableMap2D<String> underTest = new ImmutableMap2D<>(20, 10, "0");
        underTest.get(20, 5);
    }

    @Test
    public void yMax() {
        final ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        Assert.assertEquals("0", underTest.get(5, 19));
    }

    @Test(expected = IllegalArgumentException.class)
    public void yTooLarge() {
        final ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest.get(5, 20);
    }

    @Test
    public void setValueAndSwitchBetweenversions() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest = underTest.set(5, 5, "1");
        ImmutableMap2D<String> other = underTest;
        other = other.set(5, 5, "2");
        Assert.assertEquals("1", underTest.get(5, 5));
        Assert.assertEquals("2", other.get(5, 5));
        Assert.assertEquals("1", underTest.get(5, 5));
    }

    @Test
    public void setDefaultValueAndSwitchBetweenversions() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest = underTest.set(5, 5, "1");
        ImmutableMap2D<String> other = underTest;
        other = other.set(5, 5, "2");
        underTest = underTest.set(5, 5, "0");
        Assert.assertEquals("0", underTest.get(5, 5));
        Assert.assertEquals("2", other.get(5, 5));
        Assert.assertEquals("0", underTest.get(5, 5));
    }

    @Test
    public void clear() {
        ImmutableMap2D<String> underTest = new ImmutableMap2D<>(10, 20, "0");
        underTest = underTest.set(5, 5, "1");
        ImmutableMap2D<String> other = underTest.clear();
        other = other.set(6, 6, "2");
        underTest = underTest.set(6, 6, "3");
        Assert.assertEquals("1", underTest.get(5, 5));
        Assert.assertEquals("2", other.get(6, 6));
        Assert.assertEquals("3", underTest.get(6, 6));
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

        Assert.assertEquals("0", a.get(5, 5));
        Assert.assertEquals("a11", a.get(1, 1));
        Assert.assertEquals("a12", a.get(1, 2));
        Assert.assertEquals("0", a.get(2, 1));
        Assert.assertEquals("0", a.get(2, 2));
        Assert.assertEquals("0", a.get(3, 1));
        Assert.assertEquals("0", a.get(3, 2));

        Assert.assertEquals("0", b.get(5, 5));
        Assert.assertEquals("a11", b.get(1, 1));
        Assert.assertEquals("0", b.get(1, 2));
        Assert.assertEquals("b21", b.get(2, 1));
        Assert.assertEquals("b22", b.get(2, 2));
        Assert.assertEquals("0", b.get(3, 1));
        Assert.assertEquals("0", b.get(3, 2));

        Assert.assertEquals("0", c.get(5, 5));
        Assert.assertEquals("a11", c.get(1, 1));
        Assert.assertEquals("0", c.get(1, 2));
        Assert.assertEquals("b21", c.get(2, 1));
        Assert.assertEquals("0", c.get(2, 2));
        Assert.assertEquals("c31", c.get(3, 1));
        Assert.assertEquals("c32", c.get(3, 2));
    }
}
