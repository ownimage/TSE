/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.control.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.DoubleType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IView;
import com.ownimage.perception.undo.IUndoRedoProviderASSISTANT;

public class DoubleControlTEST {

    IContainer globalContainer = new IContainer() {

        @Override
        public void addContainer(final IContainer pChild) {

        }

        @Override
        public void addContainer(final IContainer pChild, final boolean pListenForEvents) {

        }

        @Override
        public IContainer addControl(final IControl pControl) {
            return null;
        }

        @Override
        public void addControlChangeListener(final IControlChangeListener pListener) {
        }

        @Override
        public void addControlValidator(final IControlValidator pValidator) {
        }

        @Override
        public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
        }

        @Override
        public IView createView() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl) {
        }

        @Override
        public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl, final IView pView, final boolean pIsMutating) {
        }

        @Override
        public boolean fireControlValidate(final IControl<?, ?, ?, ?> pControl) {
            return false;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public IContainer getParent() {
            return null;
        }

        @Override
        public String getPropertyName() {
            return null;
        }

        @Override
        public boolean isPersistent() {
            return true;
        }

        @Override
        public boolean canRead(final IPersistDB pDB, final String pId) {
            return false;
        }

        @Override
        public UndoRedoBuffer getUndoRedoBuffer() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterator<IViewable<?>> getViewableChildrenIterator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void read(final IPersistDB pDB, final String pId) {
        }

        @Override
        public void removeControlChangeListener(final IControlChangeListener pLIstener) {
        }

        @Override
        public void removeControlValidator(final IControlValidator pValidator) {
        }

        @Override
        public void write(final IPersistDB pDB, final String pId) {
        }

    };
    private Container mContainer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // Test 01 Constructors
    @Test
    // test that a copy is made of the Type so that it is icontained in the Control
    public void DoubleControl_Test01_ctor() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "y", mContainer, v);

        assertEquals(0.5d, d.getValue(), 0.0d);

        v.setValue(0.4d);
        assertEquals(0.4d, v.getValue(), 0.0d);

        assertEquals(0.5d, d.getValue(), 0.0d);
    }

    @Test
    public void DoubleControl_Test01a_ctor() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "y", mContainer, v);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.ZeroToOne, d.getMetaType());
        assertEquals("y", d.getPropertyName());
        assertEquals("x", d.getDisplayName());
        assertEquals(mContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01b_ctor() {
        final DoubleControl d = new DoubleControl("x", "y", mContainer, 0.5);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.ZeroToOne, d.getMetaType());
        assertEquals("y", d.getPropertyName());
        assertEquals("x", d.getDisplayName());
        assertEquals(mContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01c_ctor() {
        final DoubleControl d = new DoubleControl("x", "y", mContainer, 0.5, DoubleType.MinusHalfToHalf);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.MinusHalfToHalf, d.getMetaType());
        assertEquals("y", d.getPropertyName());
        assertEquals("x", d.getDisplayName());
        assertEquals(mContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01d_ctor() {
        final String propertyName = "propertyName";
        final String displayName = "displayName";
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl(displayName, propertyName, mContainer, v);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.ZeroToOne, d.getMetaType());
        assertEquals(propertyName, d.getPropertyName());
        assertEquals(displayName, d.getDisplayName());
        assertEquals(mContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01e_ctor() {
        final String propertyName = "propertyName";
        final String displayName = "displayName";
        final DoubleControl d = new DoubleControl(displayName, propertyName, mContainer, 0.5);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.ZeroToOne, d.getMetaType());
        assertEquals(propertyName, d.getPropertyName());
        assertEquals(displayName, d.getDisplayName());
        assertEquals(mContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01f_ctor() {
        final String propertyName = "propertyName";
        final String displayName = "displayName";
        final DoubleControl d = new DoubleControl(displayName, propertyName, mContainer, 0.5, DoubleType.MinusHalfToHalf);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.MinusHalfToHalf, d.getMetaType());
        assertEquals(propertyName, d.getPropertyName());
        assertEquals(displayName, d.getDisplayName());
        assertEquals(mContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01g_ctor() {
        final String propertyName = "propertyName";
        final String displayName = "displayName";
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl(displayName, propertyName, globalContainer, v);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.ZeroToOne, d.getMetaType());
        assertEquals(propertyName, d.getPropertyName());
        assertEquals(displayName, d.getDisplayName());
        assertEquals(globalContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01h_ctor() {
        final String propertyName = "propertyName";
        final String displayName = "displayName";
        final DoubleControl d = new DoubleControl(displayName, propertyName, globalContainer, 0.5);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.ZeroToOne, d.getMetaType());
        assertEquals(propertyName, d.getPropertyName());
        assertEquals(displayName, d.getDisplayName());
        assertEquals(globalContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01i_ctor() {
        final String propertyName = "propertyName";
        final String displayName = "displayName";
        final DoubleControl d = new DoubleControl(displayName, propertyName, globalContainer, 0.5, DoubleType.MinusHalfToHalf);

        assertEquals(0.5d, d.getValue(), 0.0d);
        assertSame(DoubleType.MinusHalfToHalf, d.getMetaType());
        assertEquals(propertyName, d.getPropertyName());
        assertEquals(displayName, d.getDisplayName());
        assertEquals(globalContainer, d.getContainer());
    }

    @Test
    public void DoubleControl_Test01j_ctor() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "x", mContainer, v);

        assertEquals(0.5d, d.getValue(), 0.0d);

        v.setValue(0.4d);
        assertEquals(0.4d, v.getValue(), 0.0d);

        assertEquals(0.5d, d.getValue(), 0.0d);
    }

    @Test
    public void DoubleControl_Test01k_ctor() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "x", globalContainer, v);

        assertEquals(0.5d, d.getValue(), 0.0d);

        v.setValue(0.4d);
        assertEquals(0.4d, v.getValue(), 0.0d);

        assertEquals(0.5d, d.getValue(), 0.0d);
    }

    @Test
    public void DoubleControl_Test2b_getValue() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "y", mContainer, v);

        d.getValue();
        assert (true);
    }

    @Test
    public void DoubleControl_Test2f_getNormalizedValue() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "y", mContainer, v);

        d.getNormalizedValue();
        assert (true);
    }

    @Test
    public void DoubleControl_Test2i_getStringValue() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "y", mContainer, v);

        d.getString();
        assert (true);
    }

    @Test
    public void DoubleControl_Test2j_getValue() {
        final DoubleType v = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "y", mContainer, v);

        d.getValue();
        assert (true);
    }

    @Test
    public void DoubleControl_Test3a_setValue() {
        final Container c = new Container("x", "y", new UndoRedoBuffer(10));
        final DoubleType t = new DoubleType(0.5);
        final DoubleControl d = new DoubleControl("x", "y", c, t);

        double validateValue = 0.0d;
        class Validator implements IControlValidator {

            private double mValidateValue = 0.0d;

            public double getValidateValue() {
                return mValidateValue;
            }

            @Override
            public boolean validateControl(final Object pControl) {
                mValidateValue = ((DoubleControl) pControl).getValidateValue();
                return false;
            }
        }
        Validator v = new Validator();
        c.addControlValidator(v);
        d.setValue(0.6d);
        assertEquals("validate value should be set", 0.6d, v.getValidateValue(), 0.0d);
        assertEquals("value should not be set", 0.5d, d.getValue(), 0.0d);
    }

    @Test
    public void DoubleControl_Test4a_getValidateValue() {
        final DoubleType v = new DoubleType(0.65d);
        final DoubleControl d = new DoubleControl("x", "y", mContainer, v);

        assertEquals("validate value should be set", 0.6d, d.getValidateValue(), 0.65d);
    }

    @Before
    public void setUp() throws Exception {
        IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
        UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

        mContainer = new Container("x", "x", mUndoRedoBufferSource);
    }

    @After
    public void tearDown() throws Exception {
    }

     @Test
     public void DoubleControl_Test05_setNormalizedValue() {
     DoubleType v = new DoubleType(0.1, new DoubleMetaType(0.1d, 0.7d));
     DoubleControl d = new DoubleControl("DC", "dc", NullContainer.NullContainer, v);

     //DoubleControl d = new DoubleControl(5.0, m);

     d.setNormalizedValue(0.0d);
     assertEquals(0.1d, d.getValue(), 0.0d);

     d.setNormalizedValue(1.0d);
     assertEquals(0.7d, d.getValue(), 0.0d);

     d.setNormalizedValue(0.5d);
     assertEquals(0.4d, d.getValue(), 0.0d);

     d.setNormalizedValue(0.1d);
     assertEquals(0.16d, d.getValue(), 0.0d);

     d.setNormalizedValue(1.1d);
     assertEquals(0.7d, d.getValue(), 0.0d);

     d.setNormalizedValue(-0.1d);
     assertEquals(0.1d, d.getValue(), 0.0d);
     }

     @Test
     public void DoubleControl_Test06_getNormalizedValue() {
     DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
     DoubleControl d = new DoubleControl("DC", "dc", NullContainer.NullContainer, 0.1, m);

     d.setValue(0.1d);
     assertEquals(0.0d, d.getNormalizedValue(), 0.0d);

     d.setValue(0.7d);
     assertEquals(1.0d, d.getNormalizedValue(), 0.0d);

     d.setValue(0.4d);
     assertEquals((0.4d - 0.1d) / (0.7d - 0.1d), d.getNormalizedValue(), 0.0d);

     d.setValue(0.16d);
     assertEquals(0.1d, d.getNormalizedValue(), 0.0d);

     }

     @Test
     public void DoubleControl_Test08_toString() {
     DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
     DoubleControl d = new DoubleControl("DC", "dc", NullContainer.NullContainer,0.3, m);
     String expected = "DoubleControl:(value=0.1, min=0.1, max=0.7)";

     d.setValue(0.1d);
     assertEquals(expected, d.toString());
     }

     @Test
     public void DoubleControl_Test09_MetaModel_isValid() {
     DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);

     assertEquals(true, m.isValid(0.1d));
     assertEquals(true, m.isValid(0.5d));
     assertEquals(true, m.isValid(0.7d));

     assertEquals(false, m.isValid(0.0d));
     assertEquals(false, m.isValid(1.0d));
     }

     @Test
     public void DoubleControl_Test04_duplicate() throws CloneNotSupportedException {
     DoubleMetaType m = new DoubleMetaType(0.1d, 0.7d);
     DoubleControl d = new DoubleControl("DC", "dc", NullContainer.NullContainer,0.7, m);

     assertSame("Check expected MetaModel", m, d.getMetaType());

     assertEquals("Expected restricted to 0.7", 0.7d, d.getValue(), 0.0d);
     d.setValue(5.0);
     assertEquals("Expected restricted to 0.7", 0.7d, d.getValue(), 0.0d);

     d.setValue(0.2);
     assertEquals(0.2d, d.getValue(), 0.0d);

     d.setValue(-1.0);
     assertEquals("Expected restricted to 0.1", 0.1d, d.getValue(), 0.0d);

     d = new DoubleControl("DC", "dc", NullContainer.NullContainer, 0.2);
     assertEquals(0.2d, d.getValue(), 0.0d);

     d = new DoubleControl("DC", "dc", NullContainer.NullContainer, 0.1d, m);
     assertEquals("Expected restricted to 0.1", 0.1d, d.getValue(), 0.0d);

     DoubleControl d3 = d.clone(NullContainer.NullContainer);
     assertSame("Check expected MetaModel", m, d3.getMetaType());
     assertEquals(0.1d, d3.getValue(), 0.0d);
     }

}
