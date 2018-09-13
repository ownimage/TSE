/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.math;

import com.ownimage.framework.util.Framework;
import org.junit.*;

import java.util.logging.Logger;

import static org.junit.Assert.*;

public class IntegerPointTEST {


    public final static Logger mLogger = Framework.getLogger();

    static IntegerPoint mIntegerPoint = new IntegerPoint(60, 50);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void add_TEST01() {
        final IntegerPoint add = new IntegerPoint(5, 6);
        final IntegerPoint result = mIntegerPoint.add(add);
        test(result, 65, 56);
    }

    @Test
    public void add_TEST02() {
        final IntegerPoint result = mIntegerPoint.add(5, 6);
        test(result, 65, 56);
    }

    @Test
    public void compareTo_TEST() {
        final IntegerPoint ip1 = new IntegerPoint(60, 50);
        assertTrue(mIntegerPoint.compareTo(ip1) == 0);

        final IntegerPoint ip2 = new IntegerPoint(60, 51);
        assertTrue(mIntegerPoint.compareTo(ip2) < 0);

        final IntegerPoint ip3 = new IntegerPoint(60, 49);
        assertTrue(mIntegerPoint.compareTo(ip3) > 0);

        final IntegerPoint ip4 = new IntegerPoint(61, 50);
        assertTrue(mIntegerPoint.compareTo(ip4) < 0);

        final IntegerPoint ip5 = new IntegerPoint(59, 50);
        assertTrue(mIntegerPoint.compareTo(ip5) > 0);

        final IntegerPoint ip6 = new IntegerPoint(59, 49);
        assertTrue(mIntegerPoint.compareTo(ip6) > 0);

        final IntegerPoint ip7 = new IntegerPoint(61, 51);
        assertTrue(mIntegerPoint.compareTo(ip7) < 0);

        final IntegerPoint ip8 = new IntegerPoint(59, 51);
        assertTrue(mIntegerPoint.compareTo(ip8) > 0);

        final IntegerPoint ip9 = new IntegerPoint(61, 49);
        assertTrue(mIntegerPoint.compareTo(ip9) < 0);
    }

    @Test
    public void ctor_TEST01() {
        final IntegerPoint ip1 = new IntegerPoint(33, 25);
        test(ip1, 33, 25);

        final IntegerPoint ip2 = new IntegerPoint(87, 14);
        test(ip2, 87, 14);
    }

    @Test
    public void equals_TEST01() {
        final IntegerPoint ip = new IntegerPoint(60, 50);
        assertTrue(ip.equals(mIntegerPoint));
        assertTrue(mIntegerPoint.equals(ip));
    }

    @Test
    public void equals_TEST02() {
        final IntegerPoint ip = new IntegerPoint(61, 50);
        assertFalse(ip.equals(mIntegerPoint));
        assertFalse(mIntegerPoint.equals(ip));
    }

    @Test
    public void equals_TEST03() {
        final IntegerPoint ip = new IntegerPoint(60, 51);
        assertFalse(ip.equals(mIntegerPoint));
        assertFalse(mIntegerPoint.equals(ip));
    }

    @Test
    public void equals_TEST04() {
        final IntegerPoint ip = new IntegerPoint(61, 51);
        assertFalse(ip.equals(mIntegerPoint));
        assertFalse(mIntegerPoint.equals(ip));
    }

    @Test
    public void getEast_TEST() {
        test(mIntegerPoint.getEast(), 61, 50);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getNorth_TEST() {
        test(mIntegerPoint.getNorth(), 60, 51);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getNorthEast_TEST() {
        test(mIntegerPoint.getNorthEast(), 61, 51);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getNorthWest_TEST() {
        test(mIntegerPoint.getNorthWest(), 59, 51);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getSouth_TEST() {
        test(mIntegerPoint.getSouth(), 60, 49);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getSouthEast_TEST() {
        test(mIntegerPoint.getSouthEast(), 61, 49);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getSouthWest_TEST() {
        test(mIntegerPoint.getSouthWest(), 59, 49);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getWest_TEST() {
        test(mIntegerPoint.getWest(), 59, 50);
        test(mIntegerPoint, 60, 50);
    }

    @Test
    public void getX_TEST() {
        assertEquals(60, mIntegerPoint.getX());
    }

    @Test
    public void getY_TEST() {
        assertEquals(50, mIntegerPoint.getY());
    }

    @Test
    public void minus_TEST01() {
        final IntegerPoint sub = new IntegerPoint(5, 6);
        final IntegerPoint result = mIntegerPoint.minus(sub);
        test(result, 55, 44);
    }

    @Test
    public void minus_TEST02() {
        final IntegerPoint result = mIntegerPoint.minus(5, 6);
        test(result, 55, 44);
    }

    @After
    public void setUpAfter() throws Exception {
    }

    @Before
    public void setUpBefore() throws Exception {
    }

    private void test(final IntegerPoint pIntegerPoint, final int pX, final int pY) {
        if (pIntegerPoint.getX() != pX) {
            fail();
        }
        if (pIntegerPoint.getY() != pY) {
            fail();
        }
    }

    @Test
    public void toString_TEST() {
        final IntegerPoint ip1 = new IntegerPoint(-5, 9);
        assertEquals("IntegerPoint[mX=-5, mY=9]", ip1.toString());

        final IntegerPoint ip2 = new IntegerPoint(2, -21);
        assertEquals("IntegerPoint[mX=2, mY=-21]", ip2.toString());
    }
}
