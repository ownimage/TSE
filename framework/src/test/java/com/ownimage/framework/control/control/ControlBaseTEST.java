package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.event.ContolValidatorASSISTANT;
import com.ownimage.framework.control.event.ControlChangeListenerASSISTANT;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.control.type.IntegerType;
import com.ownimage.framework.control.view.ViewASSISTANT;
import com.ownimage.framework.factory.ViewFactoryASSISTANT;
import com.ownimage.framework.factory.ViewFactoryDELEGATOR;
import com.ownimage.framework.undo.IUndoRedoProviderASSISTANT;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IView;
import org.junit.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ControlBaseTEST {

    private ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView> mControlBase;

    private Container mContainer;

    private ViewFactoryASSISTANT mViewFactory;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue) {
    // pDisplayName must not be null
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_0_00() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>(null, "propertyName", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue) {
    // pDisplayName must not be 0 length
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_0_01() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("", "propertyName", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue) {
    // pDisplayName must allow spaces
    @Test
    public void ControlBase_0_02() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("display name", "propertyName", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must not be null
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_00() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", null, mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must not be 0 length
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_01() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_02() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a.b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_03() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a!b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_04() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a<b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_05() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a>b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_06() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a,b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_07() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a?b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_08() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a=b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_09() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_10() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a\tb", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_11() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a\nb", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pPropertyName must only contain characters a-zA-Z
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_12() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "a\\b", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pDisplayName must allow spaces
    @Test
    public void ControlBase_1_13() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("display Name", "a", mContainer, type);
    }

    // public ControlBase(final String pDisplayName, final String pPropertyName, final T pValue, final IContainer pContainer) {
    // pDisplayName must not allow special characters
    @Test(expected = IllegalArgumentException.class)
    public void ControlBase_1_14() {
        final IntegerType type = new IntegerType(1);
        new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("display.Name", "a", mContainer, type);
    }

    // public void addControlChangeListener(final IControlChangeListener pListener) {
    // tested by the fireControlChangeEvent methods

    // public void addControlValidator(final IControlValidator pValidator) {
    //
    @Test
    public void ControlBase_addControlValidator_0_00() {
        final DoubleControl dc = new DoubleControl("x", "x", mContainer, 0.5);
        final ContolValidatorASSISTANT<DoubleControl> v1 = new ContolValidatorASSISTANT<DoubleControl>();
        final ContolValidatorASSISTANT<DoubleControl> v2 = new ContolValidatorASSISTANT<DoubleControl>();
        final ContolValidatorASSISTANT<DoubleControl> v3 = new ContolValidatorASSISTANT<DoubleControl>(); // control test

        dc.addControlValidator(v1);
        dc.addControlValidator(v2);

        dc.setValue(0.7);

        assertTrue("value set", dc.getValue() == 0.7);

        assertTrue("v1 has fired", v1.hasFired());
        assertTrue("v1 fired for dc", v1.getLastObject() == dc);

        assertTrue("v2 has fired", v2.hasFired());
        assertTrue("v2 fired for dc", v2.getLastObject() == dc);

        assertTrue("v3 has not fired", !v3.hasFired());
        assertTrue("v3 fired for null", v3.getLastObject() == null);
    }

    // public void addControlValidator(final IControlValidator pValidator) {
    //
    @Test
    public void ControlBase_addControlValidator_0_01() {
        final DoubleControl dc = new DoubleControl("x", "x", mContainer, 0.5);
        final ContolValidatorASSISTANT<DoubleControl> v1 = new ContolValidatorASSISTANT<DoubleControl>();
        final ContolValidatorASSISTANT<DoubleControl> v2 = new ContolValidatorASSISTANT<DoubleControl>();

        v1.setReturnValue(false);

        dc.addControlValidator(v1);
        dc.addControlValidator(v2);

        dc.setValue(0.7);

        assertTrue("value not set", dc.getValue() == 0.5);
        assertTrue("dc not dirty", !dc.isDirty());

        assertTrue("v1 has fired", v1.hasFired());
        assertTrue("v1 fired for dc", v1.getLastObject() == dc);

        assertTrue("v2 has not fired", !v2.hasFired());
        assertTrue("v2 fired for dc", v2.getLastObject() == null);
    }

    // public void addControlValidator(final IControlValidator pValidator) {
    //
    @Test
    public void ControlBase_addControlValidator_0_02() {
        final IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
        final UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

        final Container container = new Container("x", "x", mUndoRedoBufferSource);

        final DoubleControl dc = new DoubleControl("x", "x", container, 0.5);
        final ContolValidatorASSISTANT<DoubleControl> v1 = new ContolValidatorASSISTANT<DoubleControl>();
        final ContolValidatorASSISTANT<DoubleControl> v2 = new ContolValidatorASSISTANT<DoubleControl>();

        container.addControlValidator(v1);
        container.addControlValidator(v2);

        assertTrue("dc not dirty", !dc.isDirty());

        dc.setValue(0.7);

        assertTrue("value set", dc.getValue() == 0.7);
        assertTrue("dc not dirty", dc.isDirty());

        assertTrue("v1 has fired", v1.hasFired());
        assertTrue("v1 fired for dc", v1.getLastObject() == dc);

        assertTrue("v2 has fired", v2.hasFired());
        assertTrue("v2 fired for dc", v2.getLastObject() == dc);
    }

    // public void addControlValidator(final IControlValidator pValidator) {
    //
    @Test
    public void ControlBase_addControlValidator_0_03() {
        final IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
        final UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

        final Container container = new Container("x", "x", mUndoRedoBufferSource);

        final DoubleControl dc = new DoubleControl("x", "x", container, 0.5);
        final ContolValidatorASSISTANT<DoubleControl> v1 = new ContolValidatorASSISTANT<DoubleControl>();
        final ContolValidatorASSISTANT<DoubleControl> v2 = new ContolValidatorASSISTANT<DoubleControl>();

        v1.setReturnValue(false);

        container.addControlValidator(v1);
        container.addControlValidator(v2);

        dc.setValue(0.7);

        assertTrue("value set", dc.getValue() == 0.5);

        assertTrue("v1 has fired", v1.hasFired());
        assertTrue("v1 fired for dc", v1.getLastObject() == dc);

        assertTrue("v2 has not fired", !v2.hasFired());
        assertTrue("v2 fired for dc", v2.getLastObject() == null);
    }

    // protected void addView(final IView pView) {
    // tested by set enabled

    // public void clean() {
    // should be created clean
    @Test
    public void ControlBase_clean_0_00() {
        assertFalse("control should be created clean", mControlBase.isDirty());
    }

    // public void clean() {
    // should be marked dirty by set operation
    @Test
    public void ControlBase_clean_0_01() {
        mControlBase.setValue(4);
        assertTrue("control should now be dirty", mControlBase.isDirty());
        // TODO should realy test other set operations set the dirty flag
    }

    // public void clean() {
    // should be made clean by clean
    @Test
    public void ControlBase_clean_0_02() {
        mControlBase.setValue(4);
        mControlBase.clean();
        assertFalse("control should now be clean", mControlBase.isDirty());
    }

    // public C clone() {
    // should return a new object
    @Test
    public void ControlBase_clone_0_00() {
        final IntegerControl original = new IntegerControl("displayName", "displayValue", mContainer, 1);
        final IntegerControl clone = original.clone(mContainer);
        assertFalse("should return a new object", clone == mControlBase);
    }

    // public C clone() {
    // should return correct class
    @Test
    public void ControlBase_clone_0_01() {
        final IntegerControl original = new IntegerControl("displayName", "displayValue", mContainer, 1);
        final IntegerControl clone = original.clone(mContainer);
        assertTrue("should return correct class", clone.getClass() == original.getClass());
    }

    // public C clone() {
    // should use new container
    @Test
    public void ControlBase_clone_0_02() {
        final IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
        final UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

        final Container cloneContainer = new Container("x", "x", mUndoRedoBufferSource);
        final IntegerControl original = new IntegerControl("displayName", "displayValue", mContainer, 1);
        final IntegerControl clone = original.clone(cloneContainer);
        assertTrue("original should have container", mControlBase.getContainer() == mContainer);
        assertTrue("copy should not have container", clone.getContainer() == cloneContainer);
    }

    // public void fireControlChangeEvent(final C pControl, final IView pView, final boolean pIsMutating) {
    // TODO

    // public boolean fireControlValidate(final C pControl) {
    // TODO

    // public C clone() {
    // should return same value
    @Test
    public void ControlBase_clone_0_03() {
        final IntegerControl original = new IntegerControl("displayName", "displayValue", mContainer, 1);
        final IntegerControl clone = original.clone(mContainer);
        assertTrue("should same value", clone.getValue() == original.getValue());
    }

    // public C clone() {
    // should return copy of type - value independence
    @Test
    public void ControlBase_clone_0_04() {
        final IntegerControl original = new IntegerControl("displayName", "displayValue", mContainer, 1);
        final IntegerControl clone = original.clone(mContainer);

        assertTrue("original should have value 1", original.getValue() == 1);
        original.setValue(2);
        assertTrue("original should have value 2", original.getValue() == 2);
        assertTrue("clone should have value 1", clone.getValue() == 1);
    }

    // public C clone() {
    // should return a empty change set
    public void ControlBase_clone_0_05() {
        final ControlChangeListenerASSISTANT origListener = new ControlChangeListenerASSISTANT();
        final ControlChangeListenerASSISTANT cloneListener = new ControlChangeListenerASSISTANT();

        final IntegerControl original = new IntegerControl("displayName", "displayValue", mContainer, 1);
        final IntegerControl clone = original.clone(mContainer);

        original.addControlChangeListener(origListener);
        clone.addControlChangeListener(cloneListener);

        original.setValue(2);
        assertTrue("original listener should have fired", origListener.getLastControl() == original);
        assertTrue("clone should not have fired", cloneListener.getHasFired() == false);
    }

    // public M getMetaType() {
    // TODO

    // public double getNormalizedValue() {
    // TODO

    // public IView createView() {
    //
    @Test(expected = UnsupportedOperationException.class)
    public void ControlBase_createView_0_00() {
        final IView view = mControlBase.createView();
    }

    // public R getValidateValue() {
    // TODO

    // public R getValue() {
    // TODO

    // public void fireControlChangeEvent(final C pControl) {
    //
    @Test
    public void ControlBase_fireControlChangeEvent_0_01() {
        final ControlChangeListenerASSISTANT listener = new ControlChangeListenerASSISTANT();
        mControlBase.addControlChangeListener(listener);
        assertFalse("should not have fired", listener.getHasFired());

        mControlBase.fireControlChangeEvent();
        assertTrue("should have fired", listener.getHasFired());
    }

    // public IContainer getContainer() {
    //
    @Test
    public void ControlBase_getContainer_0_00() {
        assertTrue("Container", mControlBase.getContainer() == mContainer);
    }

    // public IContainer getContainer() {
    //
    @Test
    public void ControlBase_getContainer_0_01() {
        final IntegerType type = new IntegerType(1);
        final ControlBase controlBase = new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "propertyName", mContainer, type);
        assertTrue("Container", controlBase.getContainer() == mContainer);
    }

    // public String getDisplayName() {
    //
    @Test
    public void ControlBase_getDisplayName_0_01() {
        assertTrue("displayName", "displayName".equals(mControlBase.getDisplayName()));
    }

    // public String getPropertyName() {
    //
    @Test
    public void ControlBase_getPropertyName_0_01() {
        assertTrue("getPropertyName", "propertyName".equals(mControlBase.getPropertyName()));
    }

    // public boolean isDirty() {
    // tested in the context of clean()

    // public boolean isPersistant() {
    //
    @Test
    public void ControlBase_isPersistent_0_0() {
        assertTrue("should be persistant when created", mControlBase.isPersistent());

        mControlBase.setTransient();
        assertFalse("should nol longer be persistant", mControlBase.isPersistent());
    }

    // public boolean isVisible() {
    // tested by isVisible

    // public void read(final IPersistDB pDB, final String pId) {
    // tested by persist calls

    // public void removeControlChangeListener(final IControlChangeListener pLIstener) {
    // TODO

    // public void removeControlValidator(final IControlValidator pValidator) {
    // TODO

    // private void resetValidateValue() {
    // TODO

    // private void setDirty(final boolean pIsDirty) {
    // TODO

    // public void setEnabled(final boolean pVisible) {
    //
    @Test
    public void ControlBase_setEnabled_0_00() {
        // TODO should be replicated into each of the controls, e.g. BooleanControl
        final DoubleControl control = new DoubleControl("x", "x", mContainer, 1);
        final ViewASSISTANT view = (ViewASSISTANT) control.createView();

        assertTrue("should be created true", control.isEnabled());
        assertTrue("should be created true", view.isEnabled());

        control.setEnabled(false);
        assertFalse("should be false", control.isEnabled());
        assertFalse("should be false", view.isEnabled());

        control.setEnabled(true);
        assertTrue("should be true", control.isEnabled());
        assertTrue("should be true", view.isEnabled());
    }

    // public boolean setValue(final R pValue) {
    // needs to be tested by subclass

    // public boolean setValue(final R pValue, final IView pView, final boolean pIsMutating) {
    // needs to be tested by subclasss

    // public void setTransient() {
    //
    @Test
    public void ControlBase_setTransient_0_00() {
        // TODO should be replicated into each of the controls, e.g. BooleanControl
        final DoubleControl control = new DoubleControl("x", "x", mContainer, 1);

        assertTrue("should be created true", control.isPersistent());

        control.setTransient();
        assertFalse("should be created true", control.isPersistent());
    }

    // public void setVisible(final boolean pVisible) {
    //
    @Test
    public void ControlBase_setVisible_0_00() {
        // TODO should be replicated into each of the controls, e.g. BooleanControl
        final DoubleControl control = new DoubleControl("x", "x", mContainer, 1);
        final ViewASSISTANT view = (ViewASSISTANT) control.createView();

        assertTrue("should be created true", control.isVisible());
        assertTrue("should be created true", view.isVisible());

        control.setVisible(false);
        assertFalse("should be false", control.isVisible());
        assertFalse("should be false", view.isVisible());

        control.setVisible(true);
        assertTrue("should be true", control.isVisible());
        assertTrue("should be true", view.isVisible());
    }

    // public String toString() {
    // TODO

    // public void write(final IPersistDB pDB, final String pId) {
    // covered in persist tests

    @Before
    public void setUp() throws Exception {
        try {
            final IUndoRedoProviderASSISTANT mUndoRedoBufferSource = new IUndoRedoProviderASSISTANT();
            final UndoRedoBuffer undoRedoBuffer = mUndoRedoBufferSource.getUndoRedoBuffer();

            mContainer = new Container("x", "x", mUndoRedoBufferSource);

            mViewFactory = new ViewFactoryASSISTANT();
            ViewFactoryDELEGATOR.setDelegate(mViewFactory);
            final IntegerType type = new IntegerType(1);

            mControlBase = new ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView>("displayName", "propertyName", mContainer, type);
        } catch (final Throwable pT) {
            System.out.println(pT.getMessage());
            throw pT;
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}
