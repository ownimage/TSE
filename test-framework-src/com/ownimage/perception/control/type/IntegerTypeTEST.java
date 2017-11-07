/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.control.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.control.type.IntegerType;

public class IntegerTypeTEST {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	// Test 01 Constructors
	@Test
	public void IntegerType_Test00_ctor() {
		IntegerType d;

		try {
			d = new IntegerType(-1);
			assertTrue(false);
		} catch (final IllegalArgumentException pIAE) {
			assertTrue(true);
		}

		d = new IntegerType(0);

		// Case 01 check default meta model
		assertSame("Check expected MetaModel", IntegerType.ZeroToOneHundredStepFive, d.getMetaModel());

		// Case 02 check default meta model limits initial value too large
		assertEquals("Expected restricted to 0", 0, (int) d.getValue());

		// Case 03 check default meta model limits setting value too large
		d.setValue(105);
		assertEquals("Expected restricted to 100", 100, (int) d.getValue());

		// Case 04 check default meta model allows setting valid value
		d.setValue(10);
		assertEquals(10, (int) d.getValue());

		// Case 05 check default meta model limits setting value too small
		d.setValue(-11);
		assertEquals("Expected restricted to 0", 0, (int) d.getValue());

		// Case 06 check default meta model allows creation of valid value in constructor
		d = new IntegerType(13);
		assertEquals(13, (int) d.getValue());
	}

	// Test 02 using a null meta model in the constuctor
	@Test
	public void IntegerType_Test01_ctor_null() {
		IntegerType d = new IntegerType(0, null);

		// Case 01 check default meta model
		assertSame("Check expected MetaModel", IntegerType.ZeroToOneHundredStepFive, d.getMetaModel());

		// Case 02 check default meta model limits initial value too large
		assertEquals("Expected restricted to 0", 0, (int) d.getValue());

		// Case 03 check default meta model limits setting value too large
		d.setValue(105);
		assertEquals("Expected restricted to 100", 100, (int) d.getValue());

		// Case 04 check default meta model allows setting valid value
		d.setValue(10);
		assertEquals(10, (int) d.getValue());

		// Case 05 check default meta model limits setting value too small
		d.setValue(-11);
		assertEquals("Expected restricted to 0", 0, (int) d.getValue());

		// Case 06 check default meta model allows creation of valid value in constructor
		d = new IntegerType(13);
		assertEquals(13, (int) d.getValue());

		d = new IntegerType(0);

		// Case 08 set expected string value
		d.setString("105");
		assertEquals("Expected restricted to 100", 100, (int) d.getValue());

		// Case 09 getStringValue
		d.setValue(17);
		final String s = d.getString();
		assertEquals("17", s);
	}

	@Test
	public void IntegerType_Test04_duplicate() throws CloneNotSupportedException {
		final IntegerMetaType m = new IntegerMetaType(-100, 100, 10);
		final IntegerType d = new IntegerType(10, m);

		assertSame("Check expected MetaModel", m, d.getMetaModel());

		final IntegerType d2 = d.clone();
		d.setValue(5);
		assertSame("Check expected MetaModel", m, d2.getMetaModel());
		assertEquals(10, (int) d2.getValue());

		assertEquals(5, (int) d.getValue());

	}

	@Test
	public void IntegerType_Test05_setNormalizedValue() {
		final IntegerMetaType m = new IntegerMetaType(10, 90, 7);
		final IntegerType d = new IntegerType(55, m);

		d.setNormalizedValue(0.0d);
		assertEquals(10, (int) d.getValue());

		d.setNormalizedValue(1.0d);
		assertEquals(90, (int) d.getValue());

		d.setNormalizedValue(0.5d);
		assertEquals(50, (int) d.getValue());

		d.setNormalizedValue(0.1d);
		assertEquals(18, (int) d.getValue());

		d.setNormalizedValue(1.1d);
		assertEquals(90, (int) d.getValue());

		d.setNormalizedValue(-0.1d);
		assertEquals(10, (int) d.getValue());
	}

	@Test
	public void IntegerType_Test06_getNormalizedValue() {
		final IntegerMetaType m = new IntegerMetaType(10, 90, 7);
		final IntegerType d = new IntegerType(55, m);

		d.setValue(10);
		assertEquals(0.0d, d.getNormalizedValue(), 0.0d);

		d.setValue(90);
		assertEquals(1.0d, d.getNormalizedValue(), 0.0d);

		d.setValue(18);
		assertEquals(0.1, d.getNormalizedValue(), 0.0d);

		d.setValue(50);
		assertEquals(0.5d, d.getNormalizedValue(), 0.0d);

	}

	@Test
	public void IntegerType_Test08_toString() {
		final IntegerMetaType m = new IntegerMetaType(10, 90, 7);
		final IntegerType d = new IntegerType(15, m);
		final String expected = "IntegerType:(value=15, min=10, max=90, step=7)";
		final String result = d.toString();
		assertEquals(result, expected);
	}

	@Test
	public void IntegerType_Test09_MetaModel_isValid() {
		final IntegerMetaType m = new IntegerMetaType(10, 90, 7);

		assertEquals(true, m.isValid(10));
		assertEquals(true, m.isValid(50));
		assertEquals(true, m.isValid(90));

		assertEquals(false, m.isValid(0));
		assertEquals(false, m.isValid(100));
	}

	@Test
	public void IntegerType_Test11_MetaModel_min_max() {
		final IntegerMetaType m = new IntegerMetaType(10, 90, 7);

		assertEquals("Get Min", 10, m.getMin());
		assertEquals("Get Max", 90, m.getMax());
	}

	@Test
	public void IntegerType_Test12b_getValueValue() {
		final IntegerType d = new IntegerType(5);
		d.getValue();
		assert (true);
	}

	@Test
	public void IntegerType_Test12e_getValueValue() {
		final IntegerType d = new IntegerType(5);
		d.getValue();
	}

	@Test
	public void IntegerType_Test12f_getNormalizedValue() {
		final IntegerType d = new IntegerType(5);
		d.getNormalizedValue();
		assert (true);
	}

	@Test
	public void IntegerType_Test12i_getStringValue() {
		final IntegerType d = new IntegerType(5);
		d.getString();
		assert (true);
	}

	@Test
	public void IntegerType_Test12j_getValue() {
		final IntegerType d = new IntegerType(5);
		d.getValue();
		assert (true);
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
	 * integerMetaModel 2 constructors min max step type
	 **/
}
