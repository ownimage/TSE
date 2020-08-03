/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.persist.PersistDBImpl;
import com.ownimage.framework.undo.IUndoRedoProviderASSISTANT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ColorControlTEST {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    private ColorControl mColorControl;
    private Container mContainer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void ColorControl_clone_0_00() {
        final Container container = new Container("x", "x", new IUndoRedoProviderASSISTANT());
        final ColorControl control = mColorControl.clone(container);
        mColorControl.setValue(Color.PINK);

        assertEquals("diplayName", "Control Display Name", control.getDisplayName());
        assertEquals("propertyName", "controlPropertyName", control.getPropertyName());
        assertEquals("container", container, control.getContainer());
        assertEquals("value", Color.MAGENTA, control.getValue());
    }

    @Test
    public void ColorControl_getDisplayName_0_00() {
        assertEquals("diplayName", "Control Display Name", mColorControl.getDisplayName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void ColorControl_getNormalizedvalue_0_00() {
        mColorControl.getNormalizedValue();
    }

    @Test
    public void ColorControl_getPropertyName_0_00() {
        assertEquals("diplayName", "controlPropertyName", mColorControl.getPropertyName());
    }

    @Test
    public void ColorControl_isDirty_00() {
        assertFalse("isDirty", mColorControl.isDirty());

        mColorControl.setValue(Color.BLACK);
        assertTrue("isDirty", mColorControl.isDirty());

        mColorControl.clean();
        assertFalse("isDirty", mColorControl.isDirty());
    }

    @Test
    public void ColorControl_isPersistent_0_00() {
        assertTrue("isTrasnient", mColorControl.isPersistent());
        mColorControl.setTransient();
        assertFalse("isTrasnient", mColorControl.isPersistent());
        mColorControl.setTransient();
        assertFalse("isTrasnient", mColorControl.isPersistent());
    }

    @Test
    public void ColorControl_isVisible_0_00() {
        assertTrue("isVisible", mColorControl.isVisible());
        mColorControl.setVisible(false);
        assertFalse("isVisible", mColorControl.isVisible());
        mColorControl.setVisible(true);
        assertTrue("isVisible", mColorControl.isVisible());
    }

    @Test
    public void ColorControl_read_0_00() throws IOException {
        final IPersistDB db = new PersistDBImpl();
        mContainer.write(db, "container");
        mColorControl.setValue(Color.RED);

        assertEquals("red", Color.RED, mColorControl.getValue());

        mContainer.read(db, "container");
        assertEquals("red", Color.MAGENTA, mColorControl.getValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void ColorControl_setNormalizedvalue_0_00() {
        mColorControl.setNormalizedValue(0.5d);
    }

    @Before
    public void setUp() throws Exception {
        mContainer = new Container("Container Display Name", "containerPropertyName", new IUndoRedoProviderASSISTANT());
        mColorControl = new ColorControl("Control Display Name", "controlPropertyName", mContainer, Color.MAGENTA);
    }

    @After
    public void tearDown() throws Exception {
    }

}
