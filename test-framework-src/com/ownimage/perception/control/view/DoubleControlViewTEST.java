/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.control.view;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.app.IAppControl;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.control.StringControl;
import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.IContainerList;
import com.ownimage.framework.control.layout.INamedTabs;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.ScrollLayout;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IBorderView;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.IDoubleView;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.IViewFactory;
import com.ownimage.perception.undo.IUndoRedoProviderASSISTANT;
import com.ownimage.perception.view.factory.ViewFactoryDELEGATOR;

public class DoubleControlViewTEST {

	private static class TestDoubleView implements IDoubleView {

		private String mName;
		private final DoubleControl mDoubleControl;
		private double mValue;

		public TestDoubleView(final DoubleControl pDoubleControl) {
			mDoubleControl = pDoubleControl;
			mDoubleControl.addControlChangeListener(this);

			mValue = pDoubleControl.getValue();
		}

		@Override // TODO this needs to be strongly types
		public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
			System.out.println("controlValueChange " + mName);
			mValue = ((DoubleControl) pControl).getValue();

		}

		public double getValue() {
			return mValue;
		}

		@Override
		public void redraw() {
			// TODO Auto-generated method stub

		}

		@Override
		public void setEnabled(final boolean pEnabled) {
		}

		//

		public void setName(final String pName) {
			mName = pName;
		}

		/**
		 * Sets the value. Boomerang means that this will NOT pass itself as the view so the change event will be boomeranged back
		 * to this object..
		 *
		 * @param pValue
		 *            the new value boomerang
		 */
		public void setValueBoomerang(final double pValue) {
			mDoubleControl.setValue(pValue, null, false);
		}

		/**
		 * Sets the value. NoBoomerang means that this will pass itself as the view so the change event will be boomeranged back to
		 * this object..
		 *
		 * @param pValue
		 *            the new value boomerang
		 */
		public void setValueNoBoomerang(final double pValue) {
			mDoubleControl.setValue(pValue, this, false);
		}

		@Override
		public void setVisible(final boolean pVisible) {
		}

		@Override
		public void setDisplayType(final DoubleMetaType.DisplayType pDisplayType) {

		}
	}

	private static class TestViewFactory implements IViewFactory {

		@Override
		public IView createMenuItemView(final ActionControl pActionControl) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createMenuView(final MenuControl pMenuControl) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final ActionControl pActionControl) {
			return null;
		}

		@Override
		public IView createView(final BooleanControl pBooleanControl) {
			return null;
		}

		@Override
		public IBorderView createView(final BorderLayout pBorder) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final ColorControl pColorControl) {
			return null;
		}

		@Override
		public IDoubleView createView(final DoubleControl pDoubleControl) {
			return new TestDoubleView(pDoubleControl);
		}

		@Override
		public IView createView(final FileControl pFileControl) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final HFlowLayout pHFlow) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final HSplitLayout pHSplit) {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public IView createView(final IContainer pContainer) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ISingleSelectView createView(final IContainerList pContainerList) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ISingleSelectView createView(final INamedTabs pNamedTabs) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final IntegerControl pIntegerControl) {
			return null;
		}

		@Override
		public IView createView(final MenuControl pMenu) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final ObjectControl<?> pObjectControl) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IPictureView createView(final PictureControl pPictureControl) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final ScrollLayout pScrollLayout) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final StringControl pStringControl) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IView createView(final VFlowLayout pVFlow) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UndoRedoBuffer getPropertiesUndoRedoBuffer() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IContainer getViewFactoryPropertiesViewable() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void showDialog(final FileControl pFileControl) {
			// TODO Auto-generated method stub

		}

		@Override
		public IDialogView createDialog(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
			return null;
		}

	}

	private Container mContainer;

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

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void DoubleControlView_Test01_Boomerang() {
		final DoubleControl d = new DoubleControl("x", "y", mContainer, 0.5d);
		final TestDoubleView view1 = (TestDoubleView) d.createView();
		final TestDoubleView view2 = (TestDoubleView) d.createView();

		view1.setName("View 1");
		view2.setName("View 2");

		view1.setValueBoomerang(0.6d);

		assertEquals(0.6d, view1.getValue(), 0.0d);
		assertEquals(0.6d, view2.getValue(), 0.0d);
		assertEquals(0.6d, d.getValue(), 0.0d);
	}

	@Test
	public void DoubleControlView_Test01_NoBoomerang() {
		final DoubleControl d = new DoubleControl("x", "y", mContainer, 0.5d);
		final TestDoubleView view1 = (TestDoubleView) d.createView();
		final TestDoubleView view2 = (TestDoubleView) d.createView();

		view1.setName("View 1");
		view2.setName("View 2");

		view1.setValueNoBoomerang(0.6d);

		assertEquals(0.5d, view1.getValue(), 0.0d);
		assertEquals(0.6d, view2.getValue(), 0.0d);
		assertEquals(0.6d, d.getValue(), 0.0d);
	}

	@Before
	public void setUp() throws Exception {
		ViewFactoryDELEGATOR.setDelegate(new TestViewFactory());
		mContainer = new Container("x", "x", new IUndoRedoProviderASSISTANT());
	}

	@After
	public void tearDown() throws Exception {
	}
}
