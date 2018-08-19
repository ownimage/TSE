package com.ownimage.perception.view.factory;

import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.control.ProgressControl;
import com.ownimage.framework.control.control.StringControl;
import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.IContainerList;
import com.ownimage.framework.control.layout.INamedTabs;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.ScrollLayout;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IBorderView;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.IDoubleView;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.IViewFactory;
import com.ownimage.framework.view.factory.ViewFactory;

public class ViewFactoryDELEGATOR implements IViewFactory {

	private static ViewFactoryDELEGATOR mViewFactorySingleton = new ViewFactoryDELEGATOR();
	private IViewFactory mCurrentDelegatee;

	public static void setDelegate(final IViewFactory pDelegatee) {
		try {
			mViewFactorySingleton.mCurrentDelegatee = pDelegatee;
			ViewFactory.setViewFactory(mViewFactorySingleton);
		} catch (Throwable pT) {
			if (ViewFactory.getInstance() != mViewFactorySingleton) { throw new IllegalStateException("Have not been able to set the ViewFactory delegate"); }
		}
	}

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
		return mCurrentDelegatee.createView(pActionControl);
	}

	@Override
	public IView createView(final BooleanControl pBooleanControl) {
		return mCurrentDelegatee.createView(pBooleanControl);
	}

	@Override
	public IBorderView createView(final BorderLayout pBorder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IView createView(final ColorControl pColorControl) {
		return mCurrentDelegatee.createView(pColorControl);
	}

	@Override
	public IDoubleView createView(final DoubleControl pDoubleControl) {
		return mCurrentDelegatee.createView(pDoubleControl);
	}

	@Override
	public IView createView(final FileControl pFileControl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IView createView(final HFlowLayout pHFlow) {
		return mCurrentDelegatee.createView(pHFlow);
	}

	@Override
	public IView createView(final HSplitLayout pHSplit) {
		return mCurrentDelegatee.createView(pHSplit);
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
		return mCurrentDelegatee.createView(pNamedTabs);
	}

	@Override
	public IView createView(final IntegerControl pIntegerControl) {
		return mCurrentDelegatee.createView(pIntegerControl);
	}

	@Override
	public IView createView(final MenuControl pMenu) {
		return mCurrentDelegatee.createView(pMenu);
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
	public IView createView(final ProgressControl pProgressControl) {
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
