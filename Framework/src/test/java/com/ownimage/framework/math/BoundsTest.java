package com.ownimage.framework.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BoundsTest {
    @Test
    public void constructor_valid_0() {
        // GIVEN WHEN THEN
        final Bounds underTest = new Bounds(0, 0, 0, 0);
    }

    @Test
    public void constructor_valid_1() {
        // GIVEN WHEN THEN
        final Bounds underTest = new Bounds(-1, -1, -1, -1);
    }

    @Test
    public void constructor_valid_2() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(1, 2, 3, 4);
        // THEN
        assertEquals(1, underTest.getXMin());
        assertEquals(2, underTest.getYMin());
        assertEquals(3, underTest.getXMax());
        assertEquals(4, underTest.getYMax());
    }

    @Test
    public void constructor_valid_3() {
        // GIVEN
        IntegerPoint lowerLeft = new IntegerPoint(1, 2);
        IntegerPoint upperRight = new IntegerPoint(3, 4);
        // WHEN
        final Bounds underTest = new Bounds(lowerLeft, upperRight);
        // THEN
        assertEquals(1, underTest.getXMin());
        assertEquals(2, underTest.getYMin());
        assertEquals(3, underTest.getXMax());
        assertEquals(4, underTest.getYMax());
        assertSame(lowerLeft, underTest.getLowerLeft());
        assertSame(upperRight, underTest.getUpperRight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalid_x() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, -6, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_invalid_y() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, -4, -6);
    }

    @Test
    public void contains_true() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, 5, 5);
        final IntegerPoint point = new IntegerPoint(4, 4);
        // THEN
        assertTrue(underTest.contains(point));
    }

    // Note that this will fail if run in IntelliJ as it injects the runtime handling of the @NotNull
    @Test(expected = NullPointerException.class)
    public void contains_null() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, 5, 5);
        // THEN
        assertTrue(underTest.contains(null));
    }

    @Test
    public void contains_true_boundryLeft() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -3, 5, 5);
        final IntegerPoint point = new IntegerPoint(-5, 0);
        // THEN
        assertTrue(underTest.contains(point));
    }

    @Test
    public void contains_false_boundryLeft() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -3, 5, 5);
        final IntegerPoint point = new IntegerPoint(-6, 0);
        // THEN
        assertFalse(underTest.contains(point));
    }

    @Test
    public void contains_true_boundryRight() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, 5, 3);
        final IntegerPoint point = new IntegerPoint(4, 0);
        // THEN
        assertTrue(underTest.contains(point));
    }

    @Test
    public void contains_false_boundryRight() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, 5, 3);
        final IntegerPoint point = new IntegerPoint(5, 0);
        // THEN
        assertFalse(underTest.contains(point));
    }

    @Test
    public void contains_true_boundryBottom() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-1, -5, 5, 5);
        final IntegerPoint point = new IntegerPoint(0, -5);
        // THEN
        assertTrue(underTest.contains(point));
    }

    @Test
    public void contains_false_boundryBottom() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-1, -5, 5, 5);
        final IntegerPoint point = new IntegerPoint(0, -6);
        // THEN
        assertFalse(underTest.contains(point));
    }

    @Test
    public void contains_false_EmptyBounds() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds();
        final IntegerPoint point = new IntegerPoint(0, -6);
        // THEN
        assertFalse(underTest.contains(point));
    }

    @Test
    public void contains_true_boundryTop() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, 1, 5);
        final IntegerPoint point = new IntegerPoint(0, 4);
        // THEN
        assertTrue(underTest.contains(point));
    }

    @Test
    public void contains_false_boundryTop() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -5, 1, 5);
        final IntegerPoint point = new IntegerPoint(0, 5);
        // THEN
        assertFalse(underTest.contains(point));
    }

    @Test
    public void contains_true_self() {
        // GIVEN WHEN
        final IntegerPoint point = new IntegerPoint(-5, 5);
        final Bounds underTest = new Bounds(point);
        // THEN
        assertTrue(underTest.contains(point));
    }

    @Test
    public void equality_1() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -4, 1, 3);
        final Bounds bounds = new Bounds(-5, -4, 1, 3);
        // THEN
        assertTrue(underTest.equals(bounds));
        assertTrue(bounds.equals(underTest));
    }

    @Test
    public void equality_2() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds();
        final Bounds bounds = new Bounds();
        // THEN
        assertTrue(underTest.equals(bounds));
        assertTrue(bounds.equals(underTest));
    }

    @Test
    public void equality_fail_1() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -4, 1, 3);
        final Bounds bounds = new Bounds(-6, -4, 1, 3);
        // THEN
        assertFalse(underTest.equals(bounds));
        assertFalse(bounds.equals(underTest));
    }

    @Test
    public void equality_fail_2() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -4, 1, 3);
        final Bounds bounds = new Bounds(-5, -3, 1, 3);
        // THEN
        assertFalse(underTest.equals(bounds));
        assertFalse(bounds.equals(underTest));
    }

    @Test
    public void equality_fail_3() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -4, 1, 3);
        final Bounds bounds = new Bounds(-5, -4, 2, 3);
        // THEN
        assertFalse(underTest.equals(bounds));
        assertFalse(bounds.equals(underTest));
    }

    @Test
    public void equality_fail_4() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -4, 1, 3);
        final Bounds bounds = new Bounds(-5, -4, 1, 4);
        // THEN
        assertFalse(underTest.equals(bounds));
        assertFalse(bounds.equals(underTest));
    }

    @Test
    public void equality_fail_5() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-5, -4, 1, 3);
        final Bounds bounds = new Bounds();
        // THEN
        assertFalse(underTest.equals(bounds));
        assertFalse(bounds.equals(underTest));
    }

    @Test
    public void getBounds() {
        // GIVEN
        final Bounds underTest = new Bounds(-5, -5, 1, 4);
        final IntegerPoint point = new IntegerPoint(-10, 5);
        final Bounds expected = new Bounds(-10, -5, 1, 6);
        // WHEN
        Bounds actual = underTest.getBounds(point);
        // THEN
        assertTrue(expected.equals(actual));
        assertTrue(actual.contains(point));
    }

    // Note that this will fail if run in IntelliJ as it injects the runtime handling of the @NotNull
    @Test(expected = NullPointerException.class)
    public void getBounds_null() {
        // GIVEN
        final Bounds underTest = new Bounds(-5, -5, 1, 4);
        // WHEN THEN
        underTest.getBounds(null);
    }

    @Test
    public void getBounds_shortCircuit() {
        // GIVEN
        final Bounds underTest = new Bounds(-5, -5, 1, 4);
        final IntegerPoint point = new IntegerPoint(0, 0);
        // WHEN
        Bounds actual = underTest.getBounds(point);
        // THEN
        assertTrue(underTest == actual);
    }

    @Test
    public void getWidth_1() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-8, -5, 2, 4);
        // THEN
        assertEquals(10, underTest.getWidth());
    }

    @Test(expected = IllegalStateException.class)
    public void getWidth_2() {
        // GIVEN
        final Bounds underTest = new Bounds();
        // WHEN THEN
        underTest.getWidth();
    }

    @Test
    public void getHeight_1() {
        // GIVEN WHEN
        final Bounds underTest = new Bounds(-8, -5, 2, 4);
        // THEN
        assertEquals(9, underTest.getHeight());
    }

    @Test(expected = IllegalStateException.class)
    public void getHeight_2() {
        // GIVEN
        final Bounds underTest = new Bounds();
        // WHEN THEN
        underTest.getHeight();
    }
}
