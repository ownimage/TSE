package com.ownimage.perception.control.type;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.type.PointType;
import com.ownimage.perception.math.Point;

public class PointTypeTEST {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void PointType_duplicate_00() {
		PointType t = new PointType(Point.Point00);
		PointType d = t.clone();
		t.setValue(Point.Point11);
		PointType d2 = t.clone();

		assertEquals(Point.Point00, d.getValue());
		assertEquals(Point.Point11, d2.getValue());
	}

	@Test
	public void PointType_getString_00() {
		PointType t = new PointType(new Point(0.1, 0.2));
		assertEquals("0.1,0.2", t.getString());

		t.setValue(new Point(0.3, 0.4));
		assertEquals("0.3,0.4", t.getString());
	}

	@Test
	public void PointType_setString_00() {
		PointType t = new PointType(Point.Point00);
		t.setString("0.1,0.2");
		assertEquals(new Point(0.1, 0.2), t.getValue());
		t.setString("0.3,0.4");
		assertEquals(new Point(0.3, 0.4), t.getValue());
	}

	@Test
	public void PointType_toString_00() {
		String expected = "PointType(x=0.1,y=0.2)";
		PointType t = new PointType(new Point(0.1, 0.2));
		assertEquals(expected, t.toString());
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}