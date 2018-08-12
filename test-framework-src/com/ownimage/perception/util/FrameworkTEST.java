/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.util;

import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.util.Framework;

public class FrameworkTEST {


    public final static Logger mLogger = Framework.getLogger();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void getLogger_0() {
		assertEquals(FrameworkTEST.class.getName(), mLogger.getName());
	}

//	@Test
//	public void GT_0() {
//		Framework.checkGreaterThan(mLogger, 10, 5, "(A) %d should be greater than (B) %d");
//		assertTrue("not same", true);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void GT_1() {
//		Framework.checkGreaterThan(mLogger, 10, 10, "(A) %d should be greater than (B) %d");
//		assertTrue("not same", true);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void GT_2() {
//		Framework.checkGreaterThan(mLogger, 10, 15, "(A) %d should be greater than (B) %d");
//		assertTrue("not same", false);
//	}
//
//	@Test
//	public void GTE_0() {
//		Framework.checkGreaterThanEqual(mLogger, 10, 5, "(A) %d should be greater than (B) %d");
//		assertTrue("not same", true);
//	}
//
//	@Test
//	public void GTE_1() {
//		Framework.checkGreaterThanEqual(mLogger, 10, 10, "(A) %d should be greater than (B) %d");
//		assertTrue("not same", true);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void GTE_2() {
//		Framework.checkGreaterThanEqual(mLogger, 10, 15, "(A) %d should be greater than (B) %d");
//		assertTrue("not same", false);
//	}
//
//	@Test
//	public void LT_0() {
//		Framework.checkLessThan(mLogger, 10, 15, "(A) %d should be less than (B) %d");
//		assertTrue("not same", true);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void LT_1() {
//		Framework.checkLessThan(mLogger, 10, 10, "(A) %d should be less than (B) %d");
//		assertTrue("not same", false);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void LT_2() {
//		Framework.checkLessThan(mLogger, 10, 5, "(A) %d should be less than (B) %d");
//		assertTrue("not same", false);
//	}
//
//	@Test
//	public void LTE_0() {
//		Framework.checkLessThanEqual(mLogger, 10, 15, "(A) %d should be less than (B) %d");
//		assertTrue("not same", true);
//	}
//
//	@Test
//	public void LTE_1() {
//		Framework.checkLessThanEqual(mLogger, 10, 10, "(A) %d should be less than (B) %d");
//		assertTrue("not same", true);
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void LTE_2() {
//		Framework.checkLessThanEqual(mLogger, 10, 5, "(A) %d should be less than (B) %d");
//		assertTrue("not same", false);
//	}
//
//	@Test
//	public void message_0() {
//		try {
//			Framework.checkLessThan(mLogger, 10, 1, "(A) %d should be less than (B) %d");
//		} catch (Throwable pT) {
//			assertEquals("(A) 10 should be less than (B) 1", pT.getMessage());
//		}
//		assertTrue("true", true);
//	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
