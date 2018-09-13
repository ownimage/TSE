/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import org.junit.*;

import static org.junit.Assert.assertFalse;

public class LockTEST {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // Test 01 Constructors
    // Test allows space in name
    @Test
    public void Lock_0_00() {
        final Lock lock1 = new Lock();
        final Lock lock2 = new Lock();

        assertFalse("not same", lock1 == lock2);
        assertFalse("not equal", lock1.equals(lock2));
        assertFalse("not equal", lock2.equals(lock1));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
