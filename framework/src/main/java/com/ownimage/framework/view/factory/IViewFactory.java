/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.factory;

import com.ownimage.framework.app.menu.MenuAction;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.*;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.*;

public interface IViewFactory {

    public IView createMenuItemView(MenuAction pMenuAction);

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

    public IView createView(ProgressControl pProgressControl);

    public UndoRedoBuffer getPropertiesUndoRedoBuffer();

    public IContainer getViewFactoryPropertiesViewable();

    public void showDialog(FileControl pFileControl);

    public IDialogView createDialog(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons);


}
