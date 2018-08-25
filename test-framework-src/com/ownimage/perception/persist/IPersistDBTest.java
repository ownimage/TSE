/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.perception.undo.IUndoRedoProviderASSISTANT;

public class IPersistDBTest {

	private Container mContainer;
	private final IUndoRedoProviderASSISTANT mIGetUndoRedoAssistant = new IUndoRedoProviderASSISTANT();

	/**
	 * Tests that reading and re reading a valid value works
	 */
	@Test
	public void BooleanControl_read_TEST2() {
		final PersistDBImpl db = new PersistDBImpl();
		db.write("test.property", "true");
		final BooleanControl control = new BooleanControl("display", "property", mContainer, false);

		control.read(db, "test");
		assertEquals(true, control.getValue());

		db.write("test.property", "false");
		control.read(db, "test");
		assertEquals(false, control.getValue());
	}

	/**
	 * Tests that a value can be written
	 */
	@Test
	public void BooleanControl_write_TEST2() {
		final PersistDBImpl db = new PersistDBImpl();
		final BooleanControl control = new BooleanControl("display", "property", mContainer, true);

		control.write(db, "test");
		assertEquals("true", db.read("test.property"));

		control.setValue(false);
		control.write(db, "test");
		assertEquals("false", db.read("test.property"));
	}

	@Test
	public void Container_read_TEST1() throws IOException {
		final double testValue = 0.811d;
		final PersistDBImpl db = new PersistDBImpl();
		final Container containerWrite = new Container("display", "container", mIGetUndoRedoAssistant);
		final DoubleControl controlWrite = new DoubleControl("display", "control", containerWrite, testValue);

		containerWrite.write(db, "");
		assertEquals(testValue, Double.parseDouble(db.read("container.control")), 0.0);

		final Container containerRead = new Container("display", "container", mIGetUndoRedoAssistant);
		final DoubleControl controlRead = new DoubleControl("display", "control", containerRead, 0.3d);

		containerRead.read(db, "");
		assertEquals(testValue, controlRead.getValue(), 0.0);
	}

	@Test
	public void Container_write_TEST1() throws IOException {
		final PersistDBImpl db = new PersistDBImpl();
		final Container container = new Container("display", "container", mIGetUndoRedoAssistant);
		final DoubleControl control = new DoubleControl("display", "control", container, 0.3d);

		container.write(db, "");
		assertEquals("0.3", db.read("container.control"));
	}

	/**
	 * Tests that reading an invalid value will throw an exception
	 */
	@Test
	public void DoubleControl_read_TEST1() {
		final PersistDBImpl db = new PersistDBImpl();
		db.write("test.x", "true");
		final DoubleControl control = new DoubleControl("x", "x", mContainer, 0);

		try {
			control.read(db, "test");
			fail();
		} catch (final RuntimeException e) {
			// OK
		} catch (final Throwable t) {
			fail();
		}
	}

	@Test
	public void DoubleControl_read_TEST2() {
		final PersistDBImpl db = new PersistDBImpl();
		db.write("test.property", "0.5");
		final DoubleControl control = new DoubleControl("display", "property", mContainer, 0.3d);

		control.read(db, "test");
		assertEquals(0.5d, control.getValue(), 0.0d);

		db.write("test.property", "0.7");
		control.read(db, "test");
		assertEquals(0.7d, control.getValue(), 0.0d);
	}

	/**
	 * Tests that a value can be written
	 */
	@Test
	public void DoubleControl_write_TEST2() {
		final PersistDBImpl db = new PersistDBImpl();
		final DoubleControl control = new DoubleControl("display", "property", mContainer, 0.5d);

		control.write(db, "test");
		assertEquals("0.5", db.read("test.property"));

		control.setValue(0.7d);
		control.write(db, "test");
		assertEquals("0.7", db.read("test.property"));
	}

	/**
	 * Tests that reading an invalid value will throw an exception
	 */
	// TODO this should also log an output
	@Test
	public void IntegerControl_read_TEST1() {
		final PersistDBImpl db = new PersistDBImpl();
		db.write("test.x", "true");
		final IntegerControl control = new IntegerControl("x", "x", mContainer, 0);

		try {
			control.read(db, "test");
			fail();
		} catch (final RuntimeException e) {
			// OK
		} catch (final Throwable t) {
			fail();
		}
	}

	/**
	 * Tests that reading and re reading a valid value works
	 */
	@Test
	public void IntegerControl_read_TEST2() {
		final PersistDBImpl db = new PersistDBImpl();
		db.write("test.property", "2");
		final IntegerControl control = new IntegerControl("display", "property", mContainer, 0);

		control.read(db, "test");
		assertEquals(new Integer(2), control.getValue());

		db.write("test.property", "3");
		control.read(db, "test");
		assertEquals(new Integer(3), control.getValue());
	}

	/**
	 * Tests that a value can be written
	 */
	@Test
	public void IntegerControl_write_TEST2() {
		final PersistDBImpl db = new PersistDBImpl();
		final IntegerControl control = new IntegerControl("display", "property", mContainer, 0);

		control.write(db, "test");
		assertEquals("0", db.read("test.property"));

		control.setValue(1);
		control.write(db, "test");
		assertEquals("1", db.read("test.property"));
	}

	@Test
	public void NestedContainer_read_TEST1() {
		final double testValue = 0.766;
		final PersistDBImpl db = new PersistDBImpl();
		db.write("parent.container.control", Double.toString(testValue));
		final Container parent = new Container("display", "parent", mIGetUndoRedoAssistant);
		final Container container = new Container("display", "container", mIGetUndoRedoAssistant, parent);
		final DoubleControl control = new DoubleControl("display", "control", container, 0.3d);

		parent.read(db, "");

		assertEquals(testValue, control.getValue(), 0.0);
	}

	@Test
	public void NestedContainer_write_TEST1() throws IOException {
		final PersistDBImpl db = new PersistDBImpl();
		final Container parent = new Container("display", "parent", mIGetUndoRedoAssistant);
		final Container container = new Container("display", "container", mIGetUndoRedoAssistant, parent);
		final DoubleControl control = new DoubleControl("display", "control", container, 0.3d);

		parent.write(db, "");
		assertEquals("0.3", db.read("parent.container.control"));
	}

	@Before
	public void setUp() throws Exception {
		mContainer = new Container("x", "x", mIGetUndoRedoAssistant);
	}

	@After
	public void tearDown() throws Exception {
	}

}
