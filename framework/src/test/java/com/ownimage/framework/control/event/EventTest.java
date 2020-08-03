/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.event;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.undo.IUndoRedoProviderASSISTANT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;

public class EventTest {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    class Listener implements IControlChangeListener<DoubleControl>, IControlValidator<DoubleControl> {

        public boolean mControlValueChangeEvent_called = false;
        public boolean mValidateControlValue_called = false;

        @Override
        public void controlChangeEvent(final DoubleControl pControl, final boolean pIsMutating) {
            mControlValueChangeEvent_called = true;
        }

        @Override
        public boolean validateControl(final DoubleControl pControl) {
            mValidateControlValue_called = true;
            return true;
        }

    }

    private final IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();

    private Container mContainer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void event_Test01_listenToControl() {

        final Listener listener = new Listener();

        final DoubleControl control = new DoubleControl("x", "y", mContainer, 0.6);
        control.addControlChangeListener(listener);
        control.setValue(0.1);

        assertEquals(true, listener.mControlValueChangeEvent_called);
    }

    @Test
    public void event_Test01_listenToParentOfControl() {
        final Listener listener = new Listener();

        final Container container = new Container("x", "x", mUndoRedoBufferSource);
        final DoubleControl control = new DoubleControl("x", "x", container, 0.6);
        container.addControlChangeListener(listener);
        control.setValue(0.1);

        assertEquals(true, listener.mControlValueChangeEvent_called);
    }

    @Test
    public void event_Test01_listenToParentOfControl2() {
        final Listener listener = new Listener();

        final Container container = new Container("x", "x", mUndoRedoBufferSource);
        final DoubleControl control = new DoubleControl("x", "x", container, 0.6);
        container.addControlChangeListener(listener);
        container.removeControlChangeListener(listener);
        control.setValue(0.1);

        assertEquals(false, listener.mControlValueChangeEvent_called);
    }

    @Test
    public void event_Test01_listenToParentOfParentOfControl() {
        final Listener listener = new Listener();

        final Container parent = new Container("x", "x", mUndoRedoBufferSource);
        final Container container = new Container("x", "x", mUndoRedoBufferSource, parent);
        final DoubleControl control = new DoubleControl("x", "x", container, 0.6);
        parent.addControlChangeListener(listener);

        control.setValue(0.1);
        assertEquals(false, listener.mControlValueChangeEvent_called);
    }

    @Before
    public void setUp() throws Exception {
        mContainer = new Container("x", "x", mUndoRedoBufferSource);
    }

    @After
    public void tearDown() throws Exception {
    }

}
