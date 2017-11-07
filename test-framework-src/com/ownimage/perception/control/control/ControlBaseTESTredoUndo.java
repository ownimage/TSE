package com.ownimage.perception.control.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.control.type.IntegerType;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IView;
import com.ownimage.perception.control.event.ControlChangeListenerASSISTANT;
import com.ownimage.perception.undo.IUndoRedoProviderASSISTANT;

public class ControlBaseTESTredoUndo {

	private ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView> mControlBase;

	IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
	UndoRedoBuffer mUndoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

	private Container mContainer = new Container("x", "x", mUndoRedoBufferSource);

	private IntegerControl mInteger1;
	private IntegerControl mInteger2;
	private DoubleControl mDouble1;
	private DoubleControl mDouble2;
	private ControlChangeListenerASSISTANT mContainerListener;
	private ControlChangeListenerASSISTANT mListener1;
	private ControlChangeListenerASSISTANT mListener2;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private void resetListeners() {
		mContainerListener.reset();
		mListener1.reset();
		mListener2.reset();
	}

	@Before
	public void setUp() throws Exception {
		UndoRedoBuffer undobuffer = mUndoRedoBuffer;
		undobuffer.resetAndDestroyAllBuffers();

		mContainer = new Container("displayName", "propertyName", mUndoRedoBufferSource);
		IntegerMetaType imt = new IntegerMetaType(0, 100, 10);
		mInteger1 = new IntegerControl("x", "x", mContainer, 1, imt);
		mInteger2 = new IntegerControl("x", "x", mContainer, 2, imt);

		mDouble1 = new DoubleControl("x", "x", mContainer, 1d, new DoubleMetaType(0d, 100d));
		mDouble2 = new DoubleControl("x", "x", mContainer, 2d, new DoubleMetaType(0d, 100d));

		mContainerListener = new ControlChangeListenerASSISTANT();
		mListener1 = new ControlChangeListenerASSISTANT();
		mListener2 = new ControlChangeListenerASSISTANT();

		mContainer.addControlChangeListener(mContainerListener);
		mInteger1.addControlChangeListener(mContainer);
		mInteger1.addControlChangeListener(mListener1);
		mInteger2.addControlChangeListener(mListener2);
		mDouble1.addControlChangeListener(mListener1);
		mDouble2.addControlChangeListener(mListener2);

		while (mUndoRedoBuffer.undo()) {
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void undoRedo_1() {
		mInteger1.setValue(3);
		mInteger2.setValue(4);

		/// UNDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.undo());

		assertEquals("i2 reset", new Integer(2), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(3), mInteger1.getValue());

		assertFalse("listerner 1", mListener1.getHasFired());
		assertTrue("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger2, mContainerListener.getLastControl());
		assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// UNDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.undo());

		assertEquals("i2 reset", new Integer(2), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(1), mInteger1.getValue());

		assertTrue("listerner 1", mListener1.getHasFired());
		assertFalse("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger1, mContainerListener.getLastControl());
		assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// UNDO
		assertFalse("null", mUndoRedoBuffer.undo());

		/// REDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.redo());

		assertEquals("i2 reset", new Integer(2), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(3), mInteger1.getValue());

		assertTrue("listerner 1", mListener1.getHasFired());
		assertFalse("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger1, mContainerListener.getLastControl());
		assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// REDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.redo());

		assertEquals("i2 reset", new Integer(4), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(3), mInteger1.getValue());

		assertFalse("listerner 1", mListener1.getHasFired());
		assertTrue("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger2, mContainerListener.getLastControl());
		assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		assertFalse("null", mUndoRedoBuffer.redo());
	}

	@Test
	public void undoRedo_2() {
		mInteger1.setNormalizedValue(0.03d);
		mInteger2.setNormalizedValue(0.04d);

		/// UNDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.undo());

		assertEquals("i2 reset", new Integer(2), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(3), mInteger1.getValue());

		assertFalse("listerner 1", mListener1.getHasFired());
		assertTrue("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger2, mContainerListener.getLastControl());
		assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// UNDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.undo());

		assertEquals("i2 reset", new Integer(2), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(1), mInteger1.getValue());

		assertTrue("listerner 1", mListener1.getHasFired());
		assertFalse("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger1, mContainerListener.getLastControl());
		assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// UNDO
		assertFalse("null", mUndoRedoBuffer.undo());

		/// REDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.redo());

		assertEquals("i2 reset", new Integer(2), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(3), mInteger1.getValue());

		assertTrue("listerner 1", mListener1.getHasFired());
		assertFalse("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger1, mContainerListener.getLastControl());
		assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// REDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.redo());

		assertEquals("i2 reset", new Integer(4), mInteger2.getValue());
		assertEquals("i1 newval", new Integer(3), mInteger1.getValue());

		assertFalse("listerner 1", mListener1.getHasFired());
		assertTrue("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mInteger2, mContainerListener.getLastControl());
		assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		assertFalse("null", mUndoRedoBuffer.redo());
	}

	@Test
	public void undoRedo_3() {
		mDouble1.setNormalizedValue(0.03d);
		mDouble2.setNormalizedValue(0.04d);

		/// UNDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.undo());

		assertEquals("i2 reset", new Double(2), mDouble2.getValue());
		assertEquals("i1 newval", new Double(3), mDouble1.getValue());

		assertFalse("listerner 1", mListener1.getHasFired());
		assertTrue("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mDouble2, mContainerListener.getLastControl());
		assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// UNDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.undo());

		assertEquals("i2 reset", new Double(2), mDouble2.getValue());
		assertEquals("i1 newval", new Double(1), mDouble1.getValue());

		assertTrue("listerner 1", mListener1.getHasFired());
		assertFalse("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mDouble1, mContainerListener.getLastControl());
		assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// UNDO
		assertFalse("null", mUndoRedoBuffer.undo());

		/// REDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.redo());

		assertEquals("i2 reset", new Double(2), mDouble2.getValue());
		assertEquals("i1 newval", new Double(3), mDouble1.getValue());

		assertTrue("listerner 1", mListener1.getHasFired());
		assertFalse("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mDouble1, mContainerListener.getLastControl());
		assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		/// REDO
		resetListeners();
		assertTrue("null", mUndoRedoBuffer.redo());

		assertEquals("i2 reset", new Double(4), mDouble2.getValue());
		assertEquals("i1 newval", new Double(3), mDouble1.getValue());

		assertFalse("listerner 1", mListener1.getHasFired());
		assertTrue("listerner 2", mListener2.getHasFired());
		assertTrue("container", mContainerListener.getHasFired());
		assertSame("container", mDouble2, mContainerListener.getLastControl());
		assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
		assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

		assertFalse("null", mUndoRedoBuffer.redo());
	}
}
