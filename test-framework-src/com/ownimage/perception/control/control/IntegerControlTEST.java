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
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.type.IntegerType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IView;
import com.ownimage.perception.undo.IUndoRedoProviderASSISTANT;

public class IntegerControlTEST {

	IContainer globalContainer = new IContainer() {

		private final UndoRedoBuffer mUndoRedoBuffer = new UndoRedoBuffer(100);

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
			return mUndoRedoBuffer;
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
	public void IntegerControl_Test01_ctor() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "y", mContainer, v);

		assertEquals(50, (int) d.getValue());

		v.setValue(40);
		assertEquals(40, (int) v.getValue());

		assertEquals(50, (int) d.getValue());
	}

	@Test
	public void IntegerControl_Test01a_ctor() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "y", mContainer, v);

		assertEquals(50, (int) d.getValue());
		assertSame(IntegerType.ZeroToOneHundredStepFive, d.getMetaType());
		assertEquals("y", d.getPropertyName());
		assertEquals("x", d.getDisplayName());
		assertEquals(mContainer, d.getContainer());
	}

	@Test
	public void IntegerControl_Test01d_ctor() {
		final String propertyName = "propertyName";
		final String displayName = "displayName";
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl(displayName, propertyName, mContainer, v);

		assertEquals(50, (int) d.getValue());
		assertSame(IntegerType.ZeroToOneHundredStepFive, d.getMetaType());
		assertEquals(propertyName, d.getPropertyName());
		assertEquals(displayName, d.getDisplayName());
		assertEquals(mContainer, d.getContainer());
	}

	@Test
	public void IntegerControl_Test01e_ctor() {
		final String propertyName = "propertyName";
		final String displayName = "displayName";
		final IntegerControl d = new IntegerControl(displayName, propertyName, mContainer, 50);

		assertEquals(50, (int) d.getValue());
		assertSame(IntegerType.ZeroToOneHundredStepFive, d.getMetaType());
		assertEquals(propertyName, d.getPropertyName());
		assertEquals(displayName, d.getDisplayName());
		assertEquals(mContainer, d.getContainer());
	}

	@Test
	public void IntegerControl_Test01g_ctor() {
		final String propertyName = "propertyName";
		final String displayName = "displayName";
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl(displayName, propertyName, globalContainer, v);

		assertEquals(50, (int) d.getValue());
		assertSame(IntegerType.ZeroToOneHundredStepFive, d.getMetaType());
		assertEquals(propertyName, d.getPropertyName());
		assertEquals(displayName, d.getDisplayName());
		assertEquals(globalContainer, d.getContainer());
	}

	@Test
	public void IntegerControl_Test01h_ctor() {
		final String propertyName = "propertyName";
		final String displayName = "displayName";
		final IntegerControl d = new IntegerControl(displayName, propertyName, globalContainer, 50);

		assertEquals(50, (int) d.getValue());
		assertSame(IntegerType.ZeroToOneHundredStepFive, d.getMetaType());
		assertEquals(propertyName, d.getPropertyName());
		assertEquals(displayName, d.getDisplayName());
		assertEquals(globalContainer, d.getContainer());
	}

	@Test
	public void IntegerControl_Test01i_ctor() {
		final String propertyName = "propertyName";
		final String displayName = "displayName";
		final IntegerControl d = new IntegerControl(displayName, propertyName, globalContainer, 50, IntegerType.ZeroToOneHundredStepFive);

		assertEquals(50, (int) d.getValue());
		assertSame(IntegerType.ZeroToOneHundredStepFive, d.getMetaType());
		assertEquals(propertyName, d.getPropertyName());
		assertEquals(displayName, d.getDisplayName());
		assertEquals(globalContainer, d.getContainer());
	}

	@Test
	// test that a copy is made of the Type so that it is icontained in the Control
	public void IntegerControl_Test01j_ctor() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "x", mContainer, v);

		assertEquals(50, (int) d.getValue());

		v.setValue(40);
		assertEquals(40, (int) v.getValue());

		assertEquals(50, (int) d.getValue());
	}

	@Test
	// test that a copy is made of the Type so that it is icontained in the Control
	public void IntegerControl_Test01k_ctor() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "x", globalContainer, v);

		assertEquals(50, (int) d.getValue());

		v.setValue(40);
		assertEquals(40, (int) v.getValue());

		assertEquals(50, (int) d.getValue());
	}

	@Test
	public void IntegerControl_Test2e_getValueValue() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "y", mContainer, v);

		d.getValue();
		assert (true);
	}

	@Test
	public void IntegerControl_Test2f_getNormalizedValue() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "y", mContainer, v);

		d.getNormalizedValue();
		assert (true);
	}

	@Test
	public void IntegerControl_Test2f_setNormalizedValue() {
		final IntegerControl d = new IntegerControl("x", "y", mContainer, 5);

		d.setNormalizedValue(0.5d);
		assert (true);
	}

	@Test
	public void IntegerControl_Test2i_getStringValue() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "y", mContainer, v);

		d.getString();
		assert (true);
	}

	@Test
	public void IntegerControl_Test2j_getValue() {
		final IntegerType v = new IntegerType(50);
		final IntegerControl d = new IntegerControl("x", "y", mContainer, v);

		d.getValue();
		assert (true);
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

	// @Test
	// public void IntegerControl_Test05_setNormalizedValue() {
	// IntegerType v = new IntegerType(5.0);
	// IntegerControl d = new IntegerControl(v);
	//
	// IntegerControl d = new IntegerControl(5.0, m);
	//
	// d.setNormalizedValue(0.0d);
	// assertEquals(0.1d, d.getDoubleValue());
	//
	// d.setNormalizedValue(1.0d);
	// assertEquals(0.7d, d.getDoubleValue());
	//
	// d.setNormalizedValue(50d);
	// assertEquals(40, d.getDoubleValue());
	//
	// d.setNormalizedValue(0.1d);
	// assertEquals(0.16d, d.getDoubleValue());
	//
	// d.setNormalizedValue(1.1d);
	// assertEquals(0.7d, d.getDoubleValue());
	//
	// d.setNormalizedValue(-0.1d);
	// assertEquals(0.1d, d.getDoubleValue());
	// }
	//
	// @Test
	// public void IntegerControl_Test06_getNormalizedValue() {
	// IntegerControl.DoubleMetaType m = new IntegerControl.DoubleMetaType(0.1d, 0.7d);
	// IntegerControl d = new IntegerControl(5.0, m);
	//
	// d.setValue(0.1d);
	// assertEquals(0.0d, d.getNormalizedValue());
	//
	// d.setValue(0.7d);
	// assertEquals(1.0d, d.getNormalizedValue());
	//
	// d.setValue(40);
	// assertEquals((40 - 0.1d) / (0.7d - 0.1d), d.getNormalizedValue());
	//
	// d.setValue(0.16d);
	// assertEquals(0.1d, d.getNormalizedValue());
	//
	// }
	//
	// @Test
	// public void IntegerControl_Test08_toString() {
	// IntegerControl.DoubleMetaType m = new IntegerControl.DoubleMetaType(0.1d, 0.7d);
	// IntegerControl d = new IntegerControl(0.3, m);
	// String expected = "IntegerControl:(value=0.1, min=0.1, max=0.7)";
	//
	// d.setValue(0.1d);
	// assertEquals(d.toString(), expected);
	// }
	//
	// @Test
	// public void IntegerControl_Test09_MetaModel_isValid() {
	// IntegerControl.DoubleMetaType m = new IntegerControl.DoubleMetaType(0.1d, 0.7d);
	//
	// assertEquals(true, m.isValid(0.1d));
	// assertEquals(true, m.isValid(50d));
	// assertEquals(true, m.isValid(0.7d));
	//
	// assertEquals(false, m.isValid(0.0d));
	// assertEquals(false, m.isValid(1.0d));
	// }

	// @Test
	// public void IntegerControl_Test04_duplicate() throws CloneNotSupportedException {
	// IntegerControl.DoubleMetaType m = new IntegerControl.DoubleMetaType(0.1d, 0.7d);
	// IntegerControl d = new IntegerControl(5.0, m);
	//
	// assertSame("Check expected MetaModel", m, d.getMetaModel());
	//
	// assertEquals("Expected restricted to 0.7", 0.7d, d.getDoubleValue());
	// d.setValue(5.0);
	// assertEquals("Expected restricted to 0.7", 0.7d, d.getDoubleValue());
	//
	// d.setValue(0.2);
	// assertEquals(0.2d, d.getDoubleValue());
	//
	// d.setValue(-1.0);
	// assertEquals("Expected restricted to 0.1", 0.1d, d.getDoubleValue());
	//
	// d = new IntegerControl(0.2);
	// assertEquals(0.2d, d.getDoubleValue());
	//
	// d = new IntegerControl(-0.6d, m);
	// assertEquals("Expected restricted to 0.1", 0.1d, d.getDoubleValue());
	//
	// d.setValue(50d);
	// IntegerControl d2 = d.duplicate();
	// assertSame("Check expected MetaModel", m, d2.getMetaModel());
	// assertEquals(50d, d2.getDoubleValue());
	//
	// IntegerControl d3 = d.clone();
	// assertSame("Check expected MetaModel", m, d3.getMetaModel());
	// assertEquals(50d, d3.getDoubleValue());
	// }

	// public boolean setNormalizedValue(final double pValue) {
	// Needs to be tested in subclass
	// TODO

	// public boolean setNormalizedValue(final double pValue, final IView pSource, final boolean pIsMutating) {
	// Needs to be tested in subclass
	// TODO

	// public R getValidateValue() {
	// TODO

	// public R getValue()
	// TODO

}
