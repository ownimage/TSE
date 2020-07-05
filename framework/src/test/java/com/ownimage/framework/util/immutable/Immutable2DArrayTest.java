/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util.immutable;

import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Immutable2DArrayTest {

    @Test
    public void constructor() {
        Immutable2DArray<Integer> underTest = new Immutable2DArray<>(10, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void xNegative() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest.get(-1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void yNegative() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest.get(5, -1);
    }

    @Test
    public void xMax_01() {
        val underTest = new Immutable2DArray<String>(20, 10).set(19, 5, "0");
        assertEquals("0", underTest.get(19, 5));
    }

    @Test
    public void xMax_02() {
        val underTest = new Immutable2DArray<String>(200, 100).set(190, 50, "0");
        assertEquals("0", underTest.get(190, 50));
    }

    @Test(expected = IllegalArgumentException.class)
    public void xTooLarge() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(20, 10);
        underTest.get(20, 5);
    }

    @Test
    public void yMax_01() {
        val underTest = new Immutable2DArray<String>(10, 20).set(5, 19, "0");
        assertEquals("0", underTest.get(5, 19));
    }

    @Test
    public void yMax_02() {
        val underTest = new Immutable2DArray<String>(100, 200).set(50, 190, "0");
        assertEquals("0", underTest.get(50, 190));
    }

    @Test
    public void noChange_01() {
        val underTest = new Immutable2DArray<String>(100, 200).set(50, 190, "0");
        assertEquals(underTest, underTest.set(50, 190, "0"));
    }

    @Test
    public void noChange_02() {
        val underTest = new Immutable2DArray<String>(100, 200).set(50, 190, null);
        assertEquals(underTest, underTest.set(50, 190, null));
    }

    @Test
    public void noChange_03() {
        val underTest = new Immutable2DArray<String>(100, 200).set(0, 0, null);
        assertEquals(underTest, underTest.set(0, 0, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void yTooLarge() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest.get(5, 20);
    }

    @Test
    public void setValueAndSwitchBetweenversions() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest = underTest.set(5, 5, "1");
        Immutable2DArray<String> other = underTest;
        other = other.set(5, 5, "2");
        assertEquals("1", underTest.get(5, 5));
        assertEquals("2", other.get(5, 5));
        assertEquals("1", underTest.get(5, 5));
    }

    @Test
    public void testThatVersionsAreDecoupled() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest = underTest.set(5, 5, "1");
        Immutable2DArray<String> other = underTest;
        other = other.set(5, 5, "2");
        assertEquals("1", underTest.get(5, 5));
        assertEquals("2", other.get(5, 5));
        assertEquals("1", underTest.get(5, 5));
    }

    @Test
    public void testThatVersionsAreDecoupled2() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest = underTest.set(5, 5, "1");
        Immutable2DArray<String> other = underTest;
        other = other.set(5, 4, "2");

        assertEquals("1", underTest.get(5, 5));
        assertNull(underTest.get(5, 4));
        assertEquals("2", other.get(5, 4));
        assertEquals("1", other.get(5, 5));
        // roll back and forward
        assertEquals("1", underTest.get(5, 5));
        assertNull(underTest.get(5, 4));
        assertEquals("2", other.get(5, 4));
        assertEquals("1", other.get(5, 5));
    }

    @Test
    public void testClear_1() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest = underTest.set(5, 5, "1");
        Immutable2DArray<String> other = underTest;
        other = other.clear();

        assertEquals("1", underTest.get(5, 5));
        assertNull(underTest.get(5, 4));
        assertNull(other.get(5, 4));
        assertNull(other.get(5, 5));
        // roll back and forward
        assertEquals("1", underTest.get(5, 5));
        assertNull(underTest.get(5, 4));
        assertNull(other.get(5, 4));
        assertNull(other.get(5, 5));
    }

    @Test
    public void testClear_2() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest = underTest.set(5, 5, "1");
        Immutable2DArray<String> other = underTest;
        other = other.set(5, 4, "2");
        underTest = underTest.clear();

        assertNull(underTest.get(5, 5));
        assertNull(underTest.get(5, 4));
        assertEquals("2", other.get(5, 4));
        assertEquals("1", other.get(5, 5));
        // roll back and forward
        assertNull(underTest.get(5, 5));
        assertNull(underTest.get(5, 4));
        assertEquals("2", other.get(5, 4));
        assertEquals("1", other.get(5, 5));
    }

    @Test
    public void setDefaultValueAndSwitchBetweenversions() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest = underTest.set(5, 5, "1");
        Immutable2DArray<String> other = underTest;
        other = other.set(5, 5, "2");
        underTest = underTest.set(5, 5, "0");
        assertEquals("0", underTest.get(5, 5));
        assertEquals("2", other.get(5, 5));
        assertEquals("0", underTest.get(5, 5));
    }

    @Test
    public void clear() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        underTest = underTest.set(5, 5, "1");
        Immutable2DArray<String> other = underTest.clear();
        other = other.set(6, 6, "2");
        underTest = underTest.set(6, 6, "3");
        assertEquals("1", underTest.get(5, 5));
        assertEquals("2", other.get(6, 6));
        assertEquals("3", underTest.get(6, 6));
    }

    @Test
    public void abc() {
        Immutable2DArray<String> a = new Immutable2DArray<>(10, 10);
        a = a.set(1, 1, "a11");
        Immutable2DArray<String> b = a.set(2, 1, "b21");
        Immutable2DArray<String> c = b.set(3, 1, "c31");
        a = a.set(1, 2, "a12");
        b = b.set(2, 2, "b22");
        c = c.set(3, 2, "c32");

        assertEquals(null, a.get(5, 5));
        assertEquals("a11", a.get(1, 1));
        assertEquals("a12", a.get(1, 2));
        assertEquals(null, a.get(2, 1));
        assertEquals(null, a.get(2, 2));
        assertEquals(null, a.get(3, 1));
        assertEquals(null, a.get(3, 2));

        assertEquals(null, b.get(5, 5));
        assertEquals("a11", b.get(1, 1));
        assertEquals(null, b.get(1, 2));
        assertEquals("b21", b.get(2, 1));
        assertEquals("b22", b.get(2, 2));
        assertEquals(null, b.get(3, 1));
        assertEquals(null, b.get(3, 2));

        assertEquals(null, c.get(5, 5));
        assertEquals("a11", c.get(1, 1));
        assertEquals(null, c.get(1, 2));
        assertEquals("b21", c.get(2, 1));
        assertEquals(null, c.get(2, 2));
        assertEquals("c31", c.get(3, 1));
        assertEquals("c32", c.get(3, 2));
    }


    private int hash(int x, int y) {
        return 2000 * x + y;
    }

    @Test
    public void fill_01() {
        // GIVEN
        val array = new Immutable2DArray<Integer>(1500, 2000);
        val underTest = new StrongReference<Immutable2DArray<Integer>>(array);
        val range = new Range2D(1500, 2000);
        // WHEN
        range.forEach((x, y) -> underTest.set(underTest.get().set(x, y, hash(x, y))));
        // THEN

        range.forEach((x, y) -> {
            int acutal = underTest.get().get(x, y);
            int expected = hash(x, y);
            if (acutal != expected) {
                System.out.println(x + " " + y);
            }
            assertEquals(expected, acutal);
        });

    }

    @Test
    public void xNegative_2() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        assertTrue(underTest.getOptional(-1, 5).isEmpty());
    }

    @Test
    public void yNegative_2() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(10, 20);
        assertTrue(underTest.getOptional(-1, 5).isEmpty());
    }

    @Test
    public void xMax_01_2() {
        val underTest = new Immutable2DArray<String>(20, 10).set(19, 5, "0");
        assertEquals("0", underTest.getOptional(19, 5).orElseThrow());
    }

    @Test
    public void xMax_02_2() {
        val underTest = new Immutable2DArray<String>(200, 100).set(190, 50, "0");
        assertEquals("0", underTest.getOptional(190, 50).orElseThrow());
    }

    @Test
    public void xTooLarge_2() {
        Immutable2DArray<String> underTest = new Immutable2DArray<>(20, 10);
        assertTrue(underTest.getOptional(-1, 5).isEmpty());
    }

    @Test
    public void yMax_01_2() {
        val underTest = new Immutable2DArray<String>(10, 20).set(5, 19, "0");
        assertEquals("0", underTest.getOptional(5, 19).orElseThrow());
    }

    @Test
    public void yMax_02_2() {
        val underTest = new Immutable2DArray<String>(100, 200).set(50, 190, "0");
        assertEquals("0", underTest.getOptional(50, 190).orElseThrow());
    }

    @Test
    public void optionalGet_misses() {
        val underTest = new Immutable2DArray<String>(100, 200).set(50, 190, "0");
        assertTrue(underTest.getOptional(50, 191).isEmpty());
    }

}