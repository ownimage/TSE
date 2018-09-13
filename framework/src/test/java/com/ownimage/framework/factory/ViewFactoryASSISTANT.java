package com.ownimage.framework.factory;

import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.layout.*;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.view.ViewASSISTANT;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.view.*;
import com.ownimage.framework.view.factory.IViewFactory;

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
    public IDoubleView createView(final DoubleControl pDoubleControl) {
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
