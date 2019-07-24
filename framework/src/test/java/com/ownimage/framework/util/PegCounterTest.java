/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PegCounterTest {

    @Test
    public void test_get_01() {
        // GIVEN
        var underTest = new PegCounter();
        var peg = new Object();
        // WHEN
        var expected = underTest.get(peg);
        // THEN
        assertEquals(0, expected);
    }

    @Test
    public void test_increase_01() {
        // GIVEN
        var underTest = new PegCounter();
        var peg0 = new Object();
        var peg1 = new Object();
        var peg2 = new Object();
        // WHEN
        underTest.increase(peg1);
        underTest.increase(peg2);
        underTest.increase(peg2);
        // THEN
        assertEquals(1, underTest.get(peg1));
        assertEquals(2, underTest.get(peg2));
        assertEquals(0, underTest.get(peg0));
    }

    @Test
    public void test_remove_01() {
        // GIVEN
        var underTest = new PegCounter();
        var peg1 = new Object();
        var peg2 = new Object();
        underTest.increase(peg1);
        underTest.increase(peg1);
        underTest.increase(peg2);
        assertEquals(2, underTest.get(peg1));
        assertEquals(1, underTest.get(peg2));
        // WHEN
        underTest.clear(peg1);
        // THEN
        assertEquals(0, underTest.get(peg1));
        assertEquals(1, underTest.get(peg2));
    }

    @Test
    public void test_clear_01() {
        // GIVEN
        var underTest = new PegCounter();
        var peg1 = new Object();
        var peg2 = new Object();
        underTest.increase(peg1);
        underTest.increase(peg1);
        underTest.increase(peg2);
        assertEquals(2, underTest.get(peg1));
        assertEquals(1, underTest.get(peg2));
        // WHEN
        underTest.clear(peg1, peg2);
        // THEN
        assertEquals(0, underTest.get(peg1));
        assertEquals(0, underTest.get(peg2));
    }

    @Test
    public void test_getString_01() {
        // GIVEN
        var underTest = new PegCounter();
        var peg1 = "Peg1";
        var peg2 = "Peg2";
        underTest.increase(peg1);
        underTest.increase(peg1);
        underTest.increase(peg2);
        assertEquals(2, underTest.get(peg1));
        assertEquals(1, underTest.get(peg2));
        var expected = "Peg1: 2";
        // WHEN
        String actual = underTest.getString(peg1);
        // THEN
        assertEquals(expected, actual);
    }

    @Test
    public void test_getString_02() {
        // GIVEN
        var underTest = new PegCounter();
        var peg1 = "Peg1";
        var peg2 = "Peg2";
        underTest.increase(peg1);
        underTest.increase(peg1);
        underTest.increase(peg2);
        assertEquals(2, underTest.get(peg1));
        assertEquals(1, underTest.get(peg2));
        var expected = "Peg1: 2\nPeg2: 1";
        // WHEN
        String actual = underTest.getString(peg1, peg2);
        // THEN
        assertEquals(expected, actual);
    }
}
