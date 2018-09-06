/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.util;

import org.junit.*;

import java.awt.*;

public class KColorTEST {

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }


    private void runTest(final Color color1, final Color color2, final double amount, final Color expected) {
        Color actual = KColor.fade(color1, color2, amount);
        assertEquals(expected, actual);
    }

    private void assertEquals(Color expected, Color actual) {
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getAlpha(), actual.getAlpha());
    }

    @Test
    public void fade_ccd_n() {
        runTest(new Color(0.0f, 0.0f, 0.0f, 1.0f), new Color(1.0f, 0.0f, 0.0f, 1.0f), 0.5, new Color(0.0f, 0.0f, 0.0f, 1.0f));
        runTest(new Color(0.0f, 0.0f, 0.0f, 0.5f), new Color(1.0f, 0.8f, 0.4f, 1.0f), 1.0, new Color(0.5f, 0.4f, 0.2f, 1.0f));
        runTest(new Color(0.0f, 0.0f, 0.0f, 0.5f), new Color(1.0f, 0.8f, 0.4f, 0.5f), 1.0, new Color(0.25f, 0.2f, 0.1f, 0.75f));
        runTest(new Color(0.0f, 0.0f, 0.0f, 0.5f), new Color(1.0f, 0.8f, 0.4f, 1.0f), 0.5, new Color(0.25f, 0.2f, 0.1f, 0.75f));
        runTest(new Color(0.0f, 0.0f, 0.0f, 0.5f), new Color(1.0f, 0.8f, 0.4f, 0.5f), 0.5, new Color(0.125f, 0.1f, 0.05f, 0.625f));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
