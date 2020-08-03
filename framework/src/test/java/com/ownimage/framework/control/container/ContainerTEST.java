/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.container;

import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.undo.IUndoRedoProviderASSISTANT;
import com.ownimage.framework.undo.UndoRedoBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContainerTEST {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    public class Property extends Container {

        private final DoubleControl mDouble = new DoubleControl("double", "double", this, 0.5);
        private final BooleanControl mBoolean = new BooleanControl("boolean", "boolean", this, true);
        private final IntegerControl mInteger = new IntegerControl("integer", "integer", this, 50);

        public Property(final String pDisplayName, final String pPropertyName) {
            super(pDisplayName, pPropertyName, mUndoRedoBufferSource);
        }

        public boolean getBoolean() {
            return mBoolean.getValue();
        }

        public double getDouble() {
            return mDouble.getValue();
        }

        public int getInt() {
            return mInteger.getValue();
        }

        public void setBoolean(final boolean pB) {
            mBoolean.setValue(pB);
        }

        public void setDouble(final double pD) {
            mDouble.setValue(pD);
        }

        public void setInt(final int pI) {
            mInteger.setValue(pI);
        }

    }

    IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();

    UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // this is to simulate how controls/containers might be used to create the properties dialog editor
    @Test
    public void Container_PropertyTest_00() {
        final Property p = new Property("x", "x");

        p.setBoolean(true);
        assertTrue("setBoolean 1", p.getBoolean());

        p.setBoolean(false);
        assertTrue("setBoolean 2", !p.getBoolean());

        p.setDouble(0.6d);
        assertTrue("getDouble 1", p.getDouble() == 0.6d);

        p.setDouble(0.7d);
        assertTrue("getDouble 2", p.getDouble() == 0.7d);

        p.setInt(20);
        assertTrue("getInt 1", p.getInt() == 20);

        p.setInt(30);
        assertTrue("getInt 1", p.getInt() == 30);

    }

    // Test 01 Constructors
    @Test
    public void Container_Test01a_ctor() {

        final IContainer c = new Container("x", "x", mUndoRedoBufferSource);
    }

    // @Test
    // public void Container_Test02a_addBoolean() {
    // final IContainer container = new Container("x", "x");
    // final BooleanControl control = new BooleanControl("display1", "property1", container, true);
    // control.setValue(false);
    //
    // final BooleanControl dc1 = (BooleanControl) container.getControl("property1");
    // assertEquals(false, dc1.getValue());
    // control.setValue(true);
    // assertEquals(true, dc1.getValue());
    //
    // control.setValue(false);
    //
    // final BooleanControl dc2 = container.getBooleanControl("property1");
    // assertEquals(false, dc1.getValue());
    // control.setValue(true);
    // assertEquals(true, dc1.getValue());
    //
    // try {
    // container.getDoubleControl("property1");
    // assertEquals(true, false);
    // } catch (final Exception e) {
    // assertEquals(e.getClass(), ClassCastException.class);
    // }
    // }
    //
    // @Test
    // public void Container_Test02a_addDouble() {
    // final IContainer container = new Container("x", "x");
    // final DoubleControl doubleControl = new DoubleControl("display1", "property1", container, 0.5d);
    // doubleControl.setValue(0.4d);
    //
    // final DoubleControl dc1 = (DoubleControl) container.getControl("property1");
    // assertEquals(0.4d, dc1.getValue(), 0.0d);
    //
    // container.getDoubleControl("property1");
    // final DoubleControl dc2 = container.getDoubleControl("property1");
    // assertEquals(0.4d, dc1.getValue(), 0.0d);
    //
    // try {
    // container.getBooleanControl("property1");
    // assertEquals(true, false);
    // } catch (final Exception e) {
    // assertEquals(e.getClass(), ClassCastException.class);
    // }
    // }
    //
    // @Test
    // public void Container_Test02a_addInteger() {
    // final IContainer container = new Container("x", "x");
    // final IntegerControl control = new IntegerControl("display1", "property1", container, 10);
    // control.setValue(20);
    //
    // final IntegerControl dc1 = (IntegerControl) container.getControl("property1");
    // assertEquals(20, dc1.getValue(), 0);
    // control.setValue(30);
    // assertEquals(30, dc1.getValue(), 0);
    //
    // control.setValue(10);
    //
    // final IntegerControl dc2 = container.getIntegerControl("property1");
    // assertEquals(10, dc1.getValue(), 0);
    // control.setValue(20);
    // assertEquals(20, dc1.getValue(), 0);
    //
    // try {
    // container.getDoubleControl("property1");
    // assertEquals(true, false);
    // } catch (final Exception e) {
    // assertEquals(e.getClass(), ClassCastException.class);
    // }
    // }

    // @Test
    // public void Container_Test02b_addBoolean() {
    // final IContainer container = new Container("x", "x");
    // final BooleanControl control = new BooleanControl("display1", "property1", container, true);
    // control.setValue(false);
    //
    // final BooleanControl dc1 = (BooleanControl) container.getControl("property1");
    // assertEquals(false, dc1.getValue());
    // control.setValue(true);
    // assertEquals(true, dc1.getValue());
    //
    // control.setValue(false);
    //
    // final BooleanControl dc2 = container.getBooleanControl("property1");
    // assertEquals(false, dc1.getValue());
    // control.setValue(true);
    // assertEquals(true, dc1.getValue());
    //
    // try {
    // container.getDoubleControl("property1");
    // assertEquals(true, false);
    // } catch (final Exception e) {
    // assertEquals(e.getClass(), ClassCastException.class);
    // }
    // }
    //
    // @Test
    // public void Container_Test02b_addDouble() {
    // final IContainer container = new Container("x", "x");
    // final DoubleControl doubleControl = new DoubleControl("display1", "property1", container, 0.5d);
    // doubleControl.setValue(0.4d);
    //
    // final DoubleControl dc1 = (DoubleControl) container.getControl("property1");
    // assertEquals(0.4d, dc1.getValue(), 0.0d);
    //
    // container.getDoubleControl("property1");
    // final DoubleControl dc2 = container.getDoubleControl("property1");
    // assertEquals(0.4d, dc1.getValue(), 0.0d);
    //
    // try {
    // container.getBooleanControl("property1");
    // assertEquals(true, false);
    // } catch (final Exception e) {
    // assertEquals(e.getClass(), ClassCastException.class);
    // }
    // }
    //
    // @Test
    // public void Container_Test02b_addInteger() {
    // final IContainer container = new Container("x", "x");
    // final IntegerControl control = new IntegerControl("display1", "property1", container, 10);
    // control.setValue(20);
    //
    // final IntegerControl dc1 = (IntegerControl) container.getControl("property1");
    // assertEquals(20, dc1.getValue(), 0);
    // control.setValue(30);
    // assertEquals(30, dc1.getValue(), 0);
    //
    // control.setValue(40);
    //
    // final IntegerControl dc2 = container.getIntegerControl("property1");
    // assertEquals(40, dc1.getValue(), 0);
    // control.setValue(50);
    // assertEquals(50, dc1.getValue(), 0);
    //
    // try {
    // container.getDoubleControl("property1");
    // assertEquals(true, false);
    // } catch (final Exception e) {
    // assertEquals(e.getClass(), ClassCastException.class);
    // }
    // }

    // Test 01 Constructors
    @Test
    public void Container_Test01b_ctor() {
        final String displayName = "displayName";
        final String propertyName = "propertyName";

        final IContainer c = new Container(displayName, propertyName, mUndoRedoBufferSource);

        assertEquals(displayName, c.getDisplayName());
        assertEquals(propertyName, c.getPropertyName());
    }

    // Test 01 Constructors
    @Test
    public void Container_Test01c_ctor() {
        final IContainer parent = new Container("x", "x", mUndoRedoBufferSource);
        final String displayName = "displayName";
        final String propertyName = "propertyName";

        final IContainer c = new Container(displayName, propertyName, mUndoRedoBufferSource, parent);

        assertEquals(displayName, c.getDisplayName());
        assertEquals(propertyName, c.getPropertyName());
        assertEquals(parent, c.getParent());
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }
}
