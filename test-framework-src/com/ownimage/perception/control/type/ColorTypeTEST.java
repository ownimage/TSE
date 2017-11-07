package com.ownimage.perception.control.type;

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.type.ColorType;

public class ColorTypeTEST {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void ColorType_00() {
		ColorType type = new ColorType(Color.RED);
		assertEquals("Red", Color.RED, type.getValue());

		type = new ColorType(Color.GREEN);
		assertEquals("Green", Color.GREEN, type.getValue());
	}

	@Test
	public void ColorType_clone_00() {
		ColorType type = new ColorType(Color.RED);
		ColorType clone = type.clone();

		assertEquals("Red", Color.RED, clone.getValue());

		type = new ColorType(Color.GREEN);
		clone = type.clone();
		assertEquals("Green", Color.GREEN, clone.getValue());
	}

	@Test
	public void ColorType_getString_00() {
		Color color;
		ColorType type;

		color = Color.RED;
		type = new ColorType(color);
		assertEquals("RED", String.valueOf(color.getRGB()), type.getString());

		color = Color.GREEN;
		type = new ColorType(color);
		assertEquals("GREEN", String.valueOf(color.getRGB()), type.getString());

		color = Color.BLUE;
		type = new ColorType(color);
		assertEquals("BLUE", String.valueOf(color.getRGB()), type.getString());

		color = Color.ORANGE;
		type = new ColorType(color);
		assertEquals("ORANGE", String.valueOf(color.getRGB()), type.getString());
	}

	@Test
	public void ColorType_setString_00() {
		Color color;
		ColorType type = new ColorType(Color.BLACK);

		color = Color.RED;
		type.setString(String.valueOf(color.getRGB()));
		assertEquals("RED", color, type.getValue());

		color = Color.GREEN;
		type.setString(String.valueOf(color.getRGB()));
		assertEquals("GREEN", color, type.getValue());

		color = Color.BLUE;
		type.setString(String.valueOf(color.getRGB()));
		assertEquals("BLUE", color, type.getValue());

		color = Color.BLACK;
		type.setString(String.valueOf(color.getRGB()));
		assertEquals("BLACK", color, type.getValue());

		color = Color.ORANGE;
		type.setString(String.valueOf(color.getRGB()));
		assertEquals("ORANGE", color, type.getValue());
	}

	@Test
	public void ColorType_toString_00() {
		ColorType type;
		String s;

		type = new ColorType(Color.ORANGE);
		s = "ColorType(value=[255,200,0])";
		assertEquals("ORANGE", s, type.toString());

		type = new ColorType(Color.BLACK);
		s = "ColorType(value=[0,0,0])";
		assertEquals("BLACK", s, type.toString());

		type = new ColorType(Color.WHITE);
		s = "ColorType(value=[255,255,255])";
		assertEquals("WHITE", s, type.toString());

		type = new ColorType(Color.RED);
		s = "ColorType(value=[255,0,0])";
		assertEquals("RED", s, type.toString());

		type = new ColorType(Color.GREEN);
		s = "ColorType(value=[0,255,0])";
		assertEquals("GREEN", s, type.toString());

		type = new ColorType(Color.BLUE);
		s = "ColorType(value=[0,0,255])";
		assertEquals("BLUE", s, type.toString());

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}