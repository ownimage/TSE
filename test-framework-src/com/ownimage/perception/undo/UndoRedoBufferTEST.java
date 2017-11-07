package com.ownimage.perception.undo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Id;

public class UndoRedoBufferTEST {

	private UndoRedoBuffer mUndoRedoBuffer = new UndoRedoBuffer(100);
	private UndoRedoActionASSISTANT[] mUndoRedoActions;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private void reset() {
		for (int i = 0; i < 5; i++) {
			mUndoRedoActions[i].reset();
		}
	}

	// savepoint
	// redo
	// should fail
	@Test(expected = IllegalStateException.class)
	public void savepoint_0_00() {
		UndoRedoBuffer undoRedoBuffer = new UndoRedoBuffer(100);
		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");
		undoRedoBuffer.redo();
	}

	// savepoint
	// undo
	// should fail
	@Test(expected = IllegalStateException.class)
	public void savepoint_0_01() {
		UndoRedoBuffer undoRedoBuffer = new UndoRedoBuffer(100);
		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");
		undoRedoBuffer.redo();
	}

	// update of object 1
	// savepoint
	// update of object 2
	// update of object 3
	// endSavepoint
	// rollback
	// rollforward
	// update of object 5
	// savepoint
	// update of object 6
	// update of object 7
	// endSavepoint
	// rollback
	// rollforward
	@Test
	public void savepoint_0_02() {
		IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
		UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

		Container container = new Container("x", "x", mUndoRedoBufferSource);
		IntegerControl integer1 = new IntegerControl("x", "x", container, 1);
		IntegerControl integer2 = new IntegerControl("x", "x", container, 2);
		IntegerControl integer3 = new IntegerControl("x", "x", container, 3);
		IntegerControl integer4 = new IntegerControl("x", "x", container, 4);
		IntegerControl integer5 = new IntegerControl("x", "x", container, 5);
		IntegerControl integer6 = new IntegerControl("x", "x", container, 6);
		IntegerControl integer7 = new IntegerControl("x", "x", container, 7);

		integer1.setValue(11);

		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");

		integer2.setValue(12);
		integer3.setValue(13);

		undoRedoBuffer.endSavepoint(savepoint1);

		assertEquals("before", new Integer(12), integer2.getValue());
		assertEquals("before", new Integer(13), integer3.getValue());

		undoRedoBuffer.undo();

		assertEquals("before", new Integer(11), integer1.getValue());
		assertEquals("before", new Integer(2), integer2.getValue());
		assertEquals("before", new Integer(3), integer3.getValue());

		undoRedoBuffer.undo();

		assertEquals("before", new Integer(1), integer1.getValue());
		assertEquals("before", new Integer(2), integer2.getValue());
		assertEquals("before", new Integer(3), integer3.getValue());

		undoRedoBuffer.redo();

		assertEquals("before", new Integer(11), integer1.getValue());
		assertEquals("before", new Integer(2), integer2.getValue());
		assertEquals("before", new Integer(3), integer3.getValue());

		integer5.setValue(15);

		Id savepoint2 = undoRedoBuffer.startSavepoint("savepoint 2");

		integer6.setValue(17);
		integer7.setValue(16);

		undoRedoBuffer.endSavepoint(savepoint2);
		undoRedoBuffer.undo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(2), integer2.getValue());
		assertEquals("after", new Integer(3), integer3.getValue());
		assertEquals("after", new Integer(6), integer6.getValue());
		assertEquals("after", new Integer(7), integer7.getValue());

		undoRedoBuffer.undo();

		assertEquals("after", new Integer(11), integer1.getValue());
	}

	// update of object 1
	// savepoint
	// update of object 2
	// update of object 3
	// endSavepoint
	// savepoint
	// update of object 4
	// update of object 5
	// endSavepoint
	// rollback
	// rollforward
	@Test
	public void savepoint_0_03() {
		IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
		UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

		Container container = new Container("x", "x", mUndoRedoBufferSource);
		IntegerControl integer1 = new IntegerControl("x", "x", container, 1);
		IntegerControl integer2 = new IntegerControl("x", "x", container, 2);
		IntegerControl integer3 = new IntegerControl("x", "x", container, 3);
		IntegerControl integer4 = new IntegerControl("x", "x", container, 4);
		IntegerControl integer5 = new IntegerControl("x", "x", container, 5);
		IntegerControl integer6 = new IntegerControl("x", "x", container, 6);
		IntegerControl integer7 = new IntegerControl("x", "x", container, 7);

		integer1.setValue(11);

		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");

		integer2.setValue(12);
		integer3.setValue(13);

		undoRedoBuffer.endSavepoint(savepoint1);

		Id savepoint2 = undoRedoBuffer.startSavepoint("savepoint 2");

		integer6.setValue(16);
		integer7.setValue(17);

		undoRedoBuffer.endSavepoint(savepoint2);

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(12), integer2.getValue());
		assertEquals("after", new Integer(16), integer6.getValue());
		assertEquals("after", new Integer(17), integer7.getValue());

		undoRedoBuffer.undo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(12), integer2.getValue());
		assertEquals("after", new Integer(6), integer6.getValue());
		assertEquals("after", new Integer(7), integer7.getValue());

		undoRedoBuffer.undo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(2), integer2.getValue());
		assertEquals("after", new Integer(6), integer6.getValue());
		assertEquals("after", new Integer(7), integer7.getValue());

		undoRedoBuffer.redo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(12), integer2.getValue());
		assertEquals("after", new Integer(6), integer6.getValue());
		assertEquals("after", new Integer(7), integer7.getValue());

		undoRedoBuffer.redo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(12), integer2.getValue());
		assertEquals("after", new Integer(16), integer6.getValue());
		assertEquals("after", new Integer(17), integer7.getValue());

	}

	// update of object
	// savepoint 1
	// update of object 1
	// update of object 2
	// savepoint 2
	// end savepoint 1
	// mismatched savepoints
	@Test(expected = IllegalArgumentException.class)
	public void savepoint_0_04() {
		IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
		UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

		Container container = new Container("x", "x", mUndoRedoBufferSource);
		IntegerControl integer1 = new IntegerControl("x", "x", container, 1);
		IntegerControl integer2 = new IntegerControl("x", "x", container, 2);

		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");

		integer1.setValue(11);
		integer2.setValue(12);

		Id savepoint2 = undoRedoBuffer.startSavepoint("savepoint 2");

		undoRedoBuffer.endSavepoint(savepoint1);
	}

	// update of object
	// savepoint 1
	// end savepoint 1
	// savepoint 2
	// endsavepoint 1
	// savepoint not active
	@Test(expected = IllegalArgumentException.class)
	public void savepoint_0_05() {
		IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
		UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

		Container container = new Container("x", "x", mUndoRedoBufferSource);
		IntegerControl integer1 = new IntegerControl("x", "x", container, 1);
		IntegerControl integer2 = new IntegerControl("x", "x", container, 2);

		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");
		undoRedoBuffer.endSavepoint(savepoint1);

		integer1.setValue(11);
		integer2.setValue(12);

		Id savepoint2 = undoRedoBuffer.startSavepoint("savepoint 2");
		undoRedoBuffer.endSavepoint(savepoint1); // to throw error
	}

	// update of object
	// savepoint 1
	// savepoint 2
	// end savepoint 1
	// savepoints not matched
	@Test(expected = IllegalArgumentException.class)
	public void savepoint_0_06() {
		IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
		UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

		Container container = new Container("x", "x", mUndoRedoBufferSource);
		IntegerControl integer1 = new IntegerControl("x", "x", container, 1);
		IntegerControl integer2 = new IntegerControl("x", "x", container, 2);

		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");
		Id savepoint2 = undoRedoBuffer.startSavepoint("savepoint 2");
		undoRedoBuffer.endSavepoint(savepoint1);
	}

	// update of object 1
	// savepoint 1
	// update of object 2
	// update of object 3
	// savepoint 2
	// update of object 4
	// update of object 5
	// endSavepoint 2
	// end savepoint 1
	// rollback
	// rollforward
	@Test
	public void savepoint_0_07() {
		IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
		UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

		Container container = new Container("x", "x", mUndoRedoBufferSource);
		IntegerControl integer1 = new IntegerControl("x", "x", container, 1);
		IntegerControl integer2 = new IntegerControl("x", "x", container, 2);
		IntegerControl integer3 = new IntegerControl("x", "x", container, 3);
		IntegerControl integer4 = new IntegerControl("x", "x", container, 4);
		IntegerControl integer5 = new IntegerControl("x", "x", container, 5);

		// update of object 1
		integer1.setValue(11);

		// savepoint 1
		Id savepoint1 = undoRedoBuffer.startSavepoint("savepoint 1");

		// update of object 2
		integer2.setValue(12);

		// update of object 3
		integer3.setValue(13);

		// savepoint 2
		Id savepoint2 = undoRedoBuffer.startSavepoint("savepoint 2");

		// update of object 4
		integer4.setValue(14);

		// update of object 5
		integer5.setValue(15);

		// endSavepoint 2
		undoRedoBuffer.endSavepoint(savepoint2);

		// end savepoint 1
		undoRedoBuffer.endSavepoint(savepoint1);

		// rollback
		undoRedoBuffer.undo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(2), integer2.getValue());
		assertEquals("after", new Integer(3), integer3.getValue());
		assertEquals("after", new Integer(4), integer4.getValue());
		assertEquals("after", new Integer(5), integer5.getValue());

		// rollback
		undoRedoBuffer.undo();

		assertEquals("after", new Integer(1), integer1.getValue());
		assertEquals("after", new Integer(2), integer2.getValue());
		assertEquals("after", new Integer(3), integer3.getValue());
		assertEquals("after", new Integer(4), integer4.getValue());
		assertEquals("after", new Integer(5), integer5.getValue());

		// rollforward
		undoRedoBuffer.redo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(2), integer2.getValue());
		assertEquals("after", new Integer(3), integer3.getValue());
		assertEquals("after", new Integer(4), integer4.getValue());
		assertEquals("after", new Integer(5), integer5.getValue());

		// rollforward
		undoRedoBuffer.redo();

		assertEquals("after", new Integer(11), integer1.getValue());
		assertEquals("after", new Integer(12), integer2.getValue());
		assertEquals("after", new Integer(13), integer3.getValue());
		assertEquals("after", new Integer(14), integer4.getValue());
		assertEquals("after", new Integer(15), integer5.getValue());
	}

	@After
	public void setUpAfter() throws Exception {
	}

	@Before
	public void setUpBefore() throws Exception {
		mUndoRedoBuffer.resetAndDestroyAllBuffers();

		mUndoRedoBuffer = new UndoRedoBuffer(4);
		mUndoRedoActions = new UndoRedoActionASSISTANT[5];

		for (int i = 0; i < 5; i++) {
			UndoRedoActionASSISTANT action = new UndoRedoActionASSISTANT("UndoRedoAction " + i);
			mUndoRedoActions[i] = action;
			mUndoRedoBuffer.add(action);
		}
	}

	// normal
	@Test
	public void undo_0_00() {
		assertFalse("fired?", mUndoRedoActions[4].undoFired());
		assertFalse("fired?", mUndoRedoActions[3].undoFired());

		assertFalse("fired?", mUndoRedoActions[4].redoFired());
		assertFalse("fired?", mUndoRedoActions[3].redoFired());

		assertTrue("undo", mUndoRedoBuffer.undo());

		assertTrue("fired?", mUndoRedoActions[4].undoFired());
		assertFalse("fired?", mUndoRedoActions[3].undoFired());

		assertFalse("fired?", mUndoRedoActions[4].redoFired());
		assertFalse("fired?", mUndoRedoActions[3].redoFired());

		reset();
		assertTrue("undo", mUndoRedoBuffer.undo());

		assertFalse("fired?", mUndoRedoActions[4].undoFired());
		assertTrue("fired?", mUndoRedoActions[3].undoFired());

		assertFalse("fired?", mUndoRedoActions[4].redoFired());
		assertFalse("fired?", mUndoRedoActions[3].redoFired());

		reset();
		assertTrue("undo", mUndoRedoBuffer.undo());

		assertFalse("fired?", mUndoRedoActions[4].undoFired());
		assertFalse("fired?", mUndoRedoActions[3].undoFired());

		assertFalse("fired?", mUndoRedoActions[4].redoFired());
		assertFalse("fired?", mUndoRedoActions[3].redoFired());

		reset();
		assertTrue("redo", mUndoRedoBuffer.redo());

		assertFalse("fired?", mUndoRedoActions[4].undoFired());
		assertFalse("fired?", mUndoRedoActions[3].undoFired());

		assertFalse("fired?", mUndoRedoActions[4].redoFired());
		assertFalse("fired?", mUndoRedoActions[3].redoFired());

		reset();
		assertTrue("redo", mUndoRedoBuffer.redo());

		assertFalse("fired?", mUndoRedoActions[4].undoFired());
		assertFalse("fired?", mUndoRedoActions[3].undoFired());

		assertFalse("fired?", mUndoRedoActions[4].redoFired());
		assertTrue("fired?", mUndoRedoActions[3].redoFired());

		reset();
		assertTrue("redo", mUndoRedoBuffer.redo());

		assertFalse("fired?", mUndoRedoActions[4].undoFired());
		assertFalse("fired?", mUndoRedoActions[3].undoFired());

		assertTrue("fired?", mUndoRedoActions[4].redoFired());
		assertFalse("fired?", mUndoRedoActions[3].redoFired());

		assertFalse("redo", mUndoRedoBuffer.redo());
	}

	// overflow
	@Test
	public void undo_0_01() {
		assertTrue("undo", mUndoRedoBuffer.undo());
		assertTrue("undo", mUndoRedoBuffer.undo());
		assertTrue("undo", mUndoRedoBuffer.undo());
		reset();
		assertTrue("undo", mUndoRedoBuffer.undo());

		assertFalse("fired?", mUndoRedoActions[0].undoFired());
		assertTrue("fired?", mUndoRedoActions[1].undoFired());

		assertFalse("fired?", mUndoRedoActions[0].redoFired());
		assertFalse("fired?", mUndoRedoActions[1].redoFired());

		assertFalse("undo", mUndoRedoBuffer.undo());
	}

}
