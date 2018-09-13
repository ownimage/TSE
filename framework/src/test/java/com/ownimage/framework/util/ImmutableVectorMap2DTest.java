/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class ImmutableVectorMap2DTest {

    @Test
    public void constructor() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorX() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(-10, 10, () -> new Byte((byte) 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorY() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, -10, () -> new Byte((byte) 0));
    }

    @Test
    public void get() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 20, () -> new Byte((byte) 0));
        Assert.assertEquals(new Byte((byte) 0), underTest.get(0, 0).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(0, 19).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(9, 0).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(9, 19).get());
    }

    @Test
    public void getPoorX1() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(-1, 0).isPresent());
    }

    @Test
    public void getPoorX2() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(10, 0).isPresent());
    }

    @Test
    public void getPoorY1() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(0, 10).isPresent());
    }

    @Test
    public void getPoorY2() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        Assert.assertFalse(underTest.get(0, -1).isPresent());
    }

    @Test
    public void set() {
        final ImmutableVectorMap2D<Byte> underTest = new ImmutableVectorMap2D<>(10, 10, () -> new Byte((byte) 0));
        final Optional<ImmutableVectorMap2D<Byte>> actual = underTest.set(5, 6, new Byte((byte) 7));
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(new Byte((byte) 7), actual.get().get(5, 6).get());
        Assert.assertEquals(new Byte((byte) 0), underTest.get(5, 6).get());
    }
}
