package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.event.ControlChangeListenerASSISTANT;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.control.type.IntegerType;
import com.ownimage.framework.undo.IUndoRedoProviderASSISTANT;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IView;
import org.junit.*;

import static org.junit.Assert.*;

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
        final UndoRedoBuffer undobuffer = mUndoRedoBuffer;
        undobuffer.resetAndDestroyAllBuffers();

        mContainer = new Container("displayName", "propertyName", mUndoRedoBufferSource);
        final IntegerMetaType imt = new IntegerMetaType(0, 100, 10);
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

        assertEquals("i2 reset", Integer.valueOf(2), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(3), mInteger1.getValue());

        assertFalse("listerner 1", mListener1.getHasFired());
        assertTrue("listerner 2", mListener2.getHasFired());
        assertTrue("container", mContainerListener.getHasFired());
        assertSame("container", mInteger2, mContainerListener.getLastControl());
        assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
        assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

        /// UNDO
        resetListeners();
        assertTrue("null", mUndoRedoBuffer.undo());

        assertEquals("i2 reset", Integer.valueOf(2), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(1), mInteger1.getValue());

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

        assertEquals("i2 reset", Integer.valueOf(2), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(3), mInteger1.getValue());

        assertTrue("listerner 1", mListener1.getHasFired());
        assertFalse("listerner 2", mListener2.getHasFired());
        assertTrue("container", mContainerListener.getHasFired());
        assertSame("container", mInteger1, mContainerListener.getLastControl());
        assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
        assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

        /// REDO
        resetListeners();
        assertTrue("null", mUndoRedoBuffer.redo());

        assertEquals("i2 reset", Integer.valueOf(4), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(3), mInteger1.getValue());

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

        assertEquals("i2 reset", Integer.valueOf(2), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(3), mInteger1.getValue());

        assertFalse("listerner 1", mListener1.getHasFired());
        assertTrue("listerner 2", mListener2.getHasFired());
        assertTrue("container", mContainerListener.getHasFired());
        assertSame("container", mInteger2, mContainerListener.getLastControl());
        assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
        assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

        /// UNDO
        resetListeners();
        assertTrue("null", mUndoRedoBuffer.undo());

        assertEquals("i2 reset", Integer.valueOf(2), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(1), mInteger1.getValue());

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

        assertEquals("i2 reset", Integer.valueOf(2), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(3), mInteger1.getValue());

        assertTrue("listerner 1", mListener1.getHasFired());
        assertFalse("listerner 2", mListener2.getHasFired());
        assertTrue("container", mContainerListener.getHasFired());
        assertSame("container", mInteger1, mContainerListener.getLastControl());
        assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
        assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

        /// REDO
        resetListeners();
        assertTrue("null", mUndoRedoBuffer.redo());

        assertEquals("i2 reset", Integer.valueOf(4), mInteger2.getValue());
        assertEquals("i1 newval", Integer.valueOf(3), mInteger1.getValue());

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

        assertEquals("i2 reset", Double.valueOf(2), mDouble2.getValue());
        assertEquals("i1 newval", Double.valueOf(3), mDouble1.getValue());

        assertFalse("listerner 1", mListener1.getHasFired());
        assertTrue("listerner 2", mListener2.getHasFired());
        assertTrue("container", mContainerListener.getHasFired());
        assertSame("container", mDouble2, mContainerListener.getLastControl());
        assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
        assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

        /// UNDO
        resetListeners();
        assertTrue("null", mUndoRedoBuffer.undo());

        assertEquals("i2 reset", Double.valueOf(2), mDouble2.getValue());
        assertEquals("i1 newval", Double.valueOf(1), mDouble1.getValue());

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

        assertEquals("i2 reset", Double.valueOf(2), mDouble2.getValue());
        assertEquals("i1 newval", Double.valueOf(3), mDouble1.getValue());

        assertTrue("listerner 1", mListener1.getHasFired());
        assertFalse("listerner 2", mListener2.getHasFired());
        assertTrue("container", mContainerListener.getHasFired());
        assertSame("container", mDouble1, mContainerListener.getLastControl());
        assertEquals("listerner 1 cnt", 1, mListener1.getFiredCount());
        assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

        /// REDO
        resetListeners();
        assertTrue("null", mUndoRedoBuffer.redo());

        assertEquals("i2 reset", Double.valueOf(4), mDouble2.getValue());
        assertEquals("i1 newval", Double.valueOf(3), mDouble1.getValue());

        assertFalse("listerner 1", mListener1.getHasFired());
        assertTrue("listerner 2", mListener2.getHasFired());
        assertTrue("container", mContainerListener.getHasFired());
        assertSame("container", mDouble2, mContainerListener.getLastControl());
        assertEquals("listerner 2 cnt", 1, mListener2.getFiredCount());
        assertEquals("countainer cnt", 1, mContainerListener.getFiredCount());

        assertFalse("null", mUndoRedoBuffer.redo());
    }
}
