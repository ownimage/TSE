package com.ownimage.framework.control.type;

import org.junit.*;

import static org.junit.Assert.assertEquals;

public class BooleanTypeTEST {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void BooleanType_duplicate_00() {
        final BooleanType t = new BooleanType(false);
        final BooleanType d = t.clone();
        t.setValue(true);
        final BooleanType d2 = t.clone();

        assertEquals(false, d.getValue());
        assertEquals(true, d2.getValue());
    }

    @Test
    public void BooleanType_getString_00() {
        final BooleanType t = new BooleanType(true);
        assertEquals("true", t.getString());

        t.setValue(false);
        assertEquals("false", t.getString());
    }

    @Test
    public void BooleanType_setString_00() {
        final BooleanType t = new BooleanType(false);
        t.setString("true");
        assertEquals(true, t.getValue());
        t.setString("false");
        assertEquals(false, t.getValue());
    }

    @Test
    public void BooleanType_toString_00() {
        final String expected = "BooleanType(value=false)";
        final BooleanType t = new BooleanType(false);
        assertEquals(expected, t.toString());
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}