/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class ImmutableVectorMap2DTest {

    @Test
    public void constructor() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorX() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(-10, 10, () -> new Byte((byte) 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorY() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, -10, () -> new Byte((byte) 0));
    }

    @Test
    public void get() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 20, () -> new Byte((byte) 0));
        Assert.assertEquals(new Byte((byte) 0), underTest.get(0, 0).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(0, 19).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(9, 0).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(9, 19).get());
    }

    @Test
    public void getPoorX1() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(-1, 0).isPresent());
    }

    @Test
    public void getPoorX2() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(10, 0).isPresent());
    }

    @Test
    public void getPoorY1() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(0, 10).isPresent());
    }

    @Test
    public void getPoorY2() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(0, -1).isPresent());
    }

    @Test
    public void set() {
        ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Optional<ImmutableVectorMap2D<Byte>> actual = underTest.set(5, 6, new Byte((byte) 7));
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(new Byte((byte) 7), actual.get().get(5, 6).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(5, 6).get());
    }
}
