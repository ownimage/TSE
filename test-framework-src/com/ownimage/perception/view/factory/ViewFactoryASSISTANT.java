package com.ownimage.perception.view.factory;

import com.ownimage.framework.app.IAppControl;
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
import com.ownimage.framework.control.control.StringControl;
import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.IContainerList;
import com.ownimage.framework.control.layout.INamedTabs;
import com.ownimage.framework.control.layout.ScrollLayout;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IBorderView;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.IViewFactory;
import com.ownimage.perception.control.view.ViewASSISTANT;

public class ViewFactoryASSISTANT implements IViewFactory {

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
		return new ViewASSISTANT(pActionControl);
	}

	@Override
	public IView createView(final BooleanControl pBooleanControl) {
		return new ViewASSISTANT(pBooleanControl);
	}

	@Override
	public IBorderView createView(final BorderLayout pBorder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IView createView(final ColorControl pColorControl) {
		return new ViewASSISTANT(pColorControl);
	}

	@Override
	public IView createView(final DoubleControl pDoubleControl) {
		return new ViewASSISTANT(pDoubleControl);
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
	public IAppControlView createView(final IAppControl pAppControl) {
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
		return null; // TODO new ViewASSISTANT(pNamedTabs);
	}

	@Override
	public IView createView(final IntegerControl pIntegerControl) {
		return new ViewASSISTANT(pIntegerControl);
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

}
