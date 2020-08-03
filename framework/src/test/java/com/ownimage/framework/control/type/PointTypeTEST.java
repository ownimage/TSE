package com.ownimage.framework.control.type;

import com.ownimage.framework.math.Point;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;

public class PointTypeTEST {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void PointType_duplicate_00() {
        final PointType t = new PointType(Point.Point00);
        final PointType d = t.clone();
        t.setValue(Point.Point11);
        final PointType d2 = t.clone();

        assertEquals(Point.Point00, d.getValue());
        assertEquals(Point.Point11, d2.getValue());
    }

    @Test
    public void PointType_getString_00() {
        final PointType t = new PointType(new Point(0.1, 0.2));
        assertEquals("0.1,0.2", t.getString());

        t.setValue(new Point(0.3, 0.4));
        assertEquals("0.3,0.4", t.getString());
    }

    @Test
    public void PointType_setString_00() {
        final PointType t = new PointType(Point.Point00);
        t.setString("0.1,0.2");
        assertEquals(new Point(0.1, 0.2), t.getValue());
        t.setString("0.3,0.4");
        assertEquals(new Point(0.3, 0.4), t.getValue());
    }

    @Test
    public void PointType_toString_00() {
        final String expected = "PointType(x=0.1,y=0.2)";
        final PointType t = new PointType(new Point(0.1, 0.2));
        assertEquals(expected, t.toString());
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}