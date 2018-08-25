/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.util;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.util.Lock;

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
		Lock lock1 = new Lock();
		Lock lock2 = new Lock();

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
