/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.type;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DoubleTypeTEST {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // Test 01 Constructors
    @Test
    public void DoubleType_00() {
        DoubleType d = new DoubleType(0.5);

        // Case 01 check default meta model
        assertSame("Check expected MetaModel", DoubleType.ZeroToOne, d.getMetaModel());

        // Case 02 check default meta model limits initial value too large
        assertEquals("Expected 0.5", 0.5d, d.getValue(), 0.0d);

        // Case 03 check default meta model limits setting value too large
        d.setValue(5.0);
        assertEquals("Expected restricted to 1.0", 1.0d, d.getValue(), 0.0d);

        // Case 04 check default meta model allows setting valid value
        d.setValue(0.1);
        assertEquals(0.1d, d.getValue(), 0.0d);

        // Case 05 check default meta model limits setting value too small
        d.setValue(-1.0);
        assertEquals("Expected restricted to 0.0", 0.0d, d.getValue(), 0.0d);

        // Case 06 check default meta model allows creation of valid value in constructor
        d = new DoubleType(0.1);
        assertEquals(0.1d, d.getValue(), 0.0d);

        // Case 07 check that default meta model throws exception
        try {
            d = new DoubleType(-0.1);
            assertTrue(false);
        } catch (final IllegalArgumentException pIAE) {
            assertTrue(true);
        }
    }

    // Test 02 using a null meta model in the constuctor
    @Test
    public void DoubleType_01() {
        DoubleType d = new DoubleType(1.0, null);

        // Case 01 check that the default model of ZeroToOne is used.
        assertSame("Check expected MetaModel", DoubleType.ZeroToOne, d.getMetaModel());

        // Case 02 check default meta model limits initial value too large
        assertEquals("Expected restricted to 1.0", 1.0d, d.getValue(), 0.0d);

        // Case 03 check default meta model limits setting value too large
        d.setValue(5.0);
        assertEquals("Expected restricted to 1.0", 1.0d, d.getValue(), 0.0d);

        // Case 04 check default meta model allows setting valid value
        d.setValue(0.1);
        assertEquals(0.1d, d.getValue(), 0.0d);

        // Case 05 check default meta model limits setting value too small
        d.setValue(-1.0);
        assertEquals("Expected restricted to 0.0", 0.0d, d.getValue(), 0.0d);

        // Case 06 check default meta model allows creation of valid value in constructor
        d = new DoubleType(0.1, null);
        assertEquals(0.1d, d.getValue(), 0.0d);

        // Case 07 check that default meta model throws exception
        try {
            d = new DoubleType(-0.1, null);
            assertTrue(false);
        } catch (final IllegalArgumentException pIAE) {
            assertTrue(true);
        }

        // Case 08 set expected string value
        d.setString("0.5d");
        assertEquals("Expected restricted to 0.5", 0.5d, d.getValue(), 0.0d);

        // Case 09 getStringValue
        d.setValue(0.6d);
        final String s = d.getString();
        assertEquals("0.6", s);
    }

    @Test
    public void DoubleType_03() {
        DoubleType d = new DoubleType(0.0, DoubleType.MinusHalfToHalf);

        assertSame("Check expected MetaModel", DoubleType.MinusHalfToHalf, d.getMetaModel());

        assertEquals("Expected 0.0", 0.0d, d.getValue(), 0.0d);
        d.setValue(5.0);
        assertEquals("Expected restricted to 0.5", 0.5d, d.getValue(), 0.0d);

        d.setValue(0.1);
        assertEquals(0.1d, d.getValue(), 0.0d);

        d.setValue(-1.0);
        assertEquals("Expected restricted to -0.5", -0.5d, d.getValue(), 0.0d);

        d = new DoubleType(0.1);
        assertEquals(0.1d, d.getValue(), 0.0d);

    }

    @Test
    public void DoubleType_duplicate_00() {
        final DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
        final DoubleType d = new DoubleType(0.5, m);

        assertSame("Check expected MetaModel", m, d.getMetaModel());

        d.setValue(5.0);
        assertEquals("Expected restricted to 0.7", 0.7d, d.getValue(), 0.0d);

        d.setValue(0.2);
        assertEquals(0.2d, d.getValue(), 0.0d);

        d.setValue(-1.0);
        assertEquals("Expected restricted to 0.1", 0.1d, d.getValue(), 0.0d);

        d.setValue(0.5d);
        final DoubleType d2 = d.clone();
        assertSame("Check expected MetaModel", m, d2.getMetaModel());
        assertEquals(0.5d, d2.getValue(), 0.0d);

    }

    @Test
    public void DoubleType_getMin_00() {
        final DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);

        assertEquals("Get Min", 0.1d, m.getMin(), 0.0d);
        assertEquals("Get Max", 0.7d, m.getMax(), 0.0d);
    }

    @Test
    public void DoubleType_getNormalizedValue_00() {
        final DoubleType d = new DoubleType(0.5);
        d.getNormalizedValue();
        assert (true);
    }

    @Test
    public void DoubleType_getNormalizedValue_01() {
        final DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
        final DoubleType d = new DoubleType(0.5d, m);

        d.setValue(0.1d);
        assertEquals(0.0d, d.getNormalizedValue(), 0.0d);

        d.setValue(0.7d);
        assertEquals(1.0d, d.getNormalizedValue(), 0.0d);

        d.setValue(0.4d);
        assertEquals((0.4d - 0.1d) / (0.7d - 0.1d), d.getNormalizedValue(), 0.0d);

        d.setValue(0.16d);
        assertEquals(0.1d, d.getNormalizedValue(), 0.0d);

    }

    @Test
    public void DoubleType_getStep_00() {
        DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
        assertEquals(DoubleMetaType.DisplayType.SLIDER, m.getDisplayType());
        assertEquals((0.7d - 0.1d) / 100.0d, m.getStep(), 0.0d);

        m = new DoubleMetaType(0.1d, 0.7d, 0.1, DoubleMetaType.DisplayType.SLIDER);
        assertEquals(DoubleMetaType.DisplayType.SLIDER, m.getDisplayType());
        assertEquals(0.1d, m.getStep(), 0.0d);

        m = new DoubleMetaType(0.1d, 0.7d, 0.1, DoubleMetaType.DisplayType.SPINNER);
        assertEquals(DoubleMetaType.DisplayType.SPINNER, m.getDisplayType());

        m = new DoubleMetaType(0.1d, 0.7d, 0.1, DoubleMetaType.DisplayType.BOTH);
        assertEquals(DoubleMetaType.DisplayType.BOTH, m.getDisplayType());
    }

    @Test
    public void DoubleType_getString_00() {
        final DoubleType d = new DoubleType(0.5);
        d.getString();
        assert (true);
    }

    @Test
    public void DoubleType_getValue_00() {
        final DoubleType d = new DoubleType(0.5);
        d.getValue();
        assert (true);
    }

    @Test
    public void DoubleType_getValue_01() {
        final DoubleType d = new DoubleType(0.5);
        d.getValue();
        assert (true);
    }

    @Test
    public void DoubleType_isValid_00() {
        final DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);

        assertEquals(true, m.isValid(0.1d));
        assertEquals(true, m.isValid(0.5d));
        assertEquals(true, m.isValid(0.7d));

        assertEquals(false, m.isValid(0.0d));
        assertEquals(false, m.isValid(1.0d));
    }

    @Test
    public void DoubleType_setNormalizedValue_00() {
        final DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
        final DoubleType d = new DoubleType(0.5d, m);

        d.setNormalizedValue(0.0d);
        assertEquals(0.1d, d.getValue(), 0.0d);

        d.setNormalizedValue(1.0d);
        assertEquals(0.7d, d.getValue(), 0.0d);

        d.setNormalizedValue(0.5d);
        assertEquals(0.4d, d.getValue(), 0.0d);

        d.setNormalizedValue(0.1d);
        assertEquals(0.16d, d.getValue(), 0.0d);

        d.setNormalizedValue(1.1d);
        assertEquals(0.7d, d.getValue(), 0.0d);

        d.setNormalizedValue(-0.1d);
        assertEquals(0.1d, d.getValue(), 0.0d);
    }

    @Test
    public void DoubleType_toString_00() {
        final DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
        final DoubleType d = new DoubleType(0.3, m);
        final String expected = "DoubleType:(value=0.1, min=0.1, max=0.7, step=0.006)";

        d.setValue(0.1d);
        assertEquals(d.toString(), expected);
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     *
     * SLIDER, SPINNER, BOTH SeroToHald SeroToOne MinusPOiToPo SeroTo2Po SetStringValue Duplicate getDefaultMetaModel getValueValue
     * getNormalizedValue setNormalisecValue getMetaModel toString
     *
     *
     *
     *
     * DoubleMetaModel 2 constructors min max step type
     **/
}
