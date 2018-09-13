/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IdTEST {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // Test 01 Constructors
    // Test allows space in name
    @Test
    public void Id_0_00() {
        Id id1 = new Id("test");
        Id id2 = new Id("test");
        Id id3 = new Id("test2");

        assertFalse("not same", id1 == id2);
        assertFalse("not equal", id1.equals(id2));
        assertFalse("not equal", id2.equals(id1));

        assertEquals("description", "test", id1.getDescription());
        assertEquals("description", "test2", id3.getDescription());
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
