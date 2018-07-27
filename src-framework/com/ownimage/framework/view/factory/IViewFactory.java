package com.ownimage.framework.view.factory;

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
import com.ownimage.framework.view.IBorderView;
import com.ownimage.framework.view.IDoubleView;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.IView;

public interface IViewFactory {

	public IView createMenuItemView(ActionControl pActionControl);

	public IView createMenuView(MenuControl pMenuControl);

	public IView createView(ActionControl pActionControl);

	public IView createView(BooleanControl pBooleanControl);

	public IBorderView createView(BorderLayout pBorder);

	public IView createView(ColorControl pColorControl);

	public IDoubleView createView(DoubleControl pDoubleControl);

	public IView createView(FileControl pFileControl);

	public IView createView(HFlowLayout pHFlow);

	public IView createView(HSplitLayout pHSplit);

	public IView createView(IContainer pContainer);

	public ISingleSelectView createView(IContainerList pContainerList);

	public ISingleSelectView createView(INamedTabs pNamedTabs);

	public IView createView(IntegerControl pIntegerControl);

	public IView createView(MenuControl pMenu);

	public IView createView(ObjectControl<?> pObjectControl);

	public IPictureView createView(PictureControl pPictureControl);

	public IView createView(ScrollLayout pScrollLayout);

	public IView createView(StringControl pStringControl);

	public IView createView(VFlowLayout pVFlow);

	public UndoRedoBuffer getPropertiesUndoRedoBuffer();

	public IContainer getViewFactoryPropertiesViewable();

	public void showDialog(FileControl pFileControl);

}
