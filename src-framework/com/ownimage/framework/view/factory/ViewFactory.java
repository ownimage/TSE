/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
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

public class ViewFactory implements IViewFactory {

    private static IViewFactory mViewFactory;

    public static IViewFactory getInstance() {
        stateCheck();
        return mViewFactory;
    }

    public static void setViewFactory(final IViewFactory pViewFactory) {
        if (mViewFactory != null) { throw new IllegalStateException("viewFactory delegate has already been set to: " + mViewFactory); }

        mViewFactory = pViewFactory;
    }

    public static void stateCheck() {
        if (mViewFactory == null) { throw new IllegalStateException("viewFactory delegate has not been set"); }
    }

    @Override
    public IView createMenuItemView(final ActionControl pActionControl) {
        stateCheck();
        return mViewFactory.createMenuItemView(pActionControl);
    }

    @Override
    public IView createMenuView(final MenuControl pMenuControl) {
        stateCheck();
        return mViewFactory.createMenuView(pMenuControl);
    }

    @Override
    public IView createView(final ActionControl pActionControl) {
        stateCheck();
        return mViewFactory.createView(pActionControl);
    }

    @Override
    public IView createView(final BooleanControl pBooleanControl) {
        stateCheck();
        return mViewFactory.createView(pBooleanControl);
    }

    @Override
    public IBorderView createView(final BorderLayout pBorder) {
        stateCheck();
        return mViewFactory.createView(pBorder);
    }

    @Override
    public IView createView(final ColorControl pColorControl) {
        stateCheck();
        return mViewFactory.createView(pColorControl);
    }

    @Override
    public IDoubleView createView(final DoubleControl pDoubleControl) {
        stateCheck();
        return mViewFactory.createView(pDoubleControl);
    }

    @Override
    public IView createView(final FileControl pFileControl) {
        stateCheck();
        return mViewFactory.createView(pFileControl);
    }

    @Override
    public IView createView(final HFlowLayout pHFlow) {
        stateCheck();
        return mViewFactory.createView(pHFlow);
    }

    @Override
    public IView createView(final HSplitLayout pHSplit) {
        stateCheck();
        return mViewFactory.createView(pHSplit);
    }

    @Override
    public IView createView(final IContainer pContainer) {
        stateCheck();
        return mViewFactory.createView(pContainer);
    }

    @Override
    public ISingleSelectView createView(final IContainerList pContainerList) {
        stateCheck();
        return mViewFactory.createView(pContainerList);
    }

    @Override
    public ISingleSelectView createView(final INamedTabs pNamedTabs) {
        stateCheck();
        return mViewFactory.createView(pNamedTabs);
    }

    @Override
    public IView createView(final IntegerControl pIntegerControl) {
        stateCheck();
        return mViewFactory.createView(pIntegerControl);
    }

    @Override
    public IView createView(final MenuControl pMenu) {
        stateCheck();
        return mViewFactory.createView(pMenu);
    }

    @Override
    public IView createView(final ObjectControl<?> pObjectControl) {
        stateCheck();
        return mViewFactory.createView(pObjectControl);
    }

    @Override
    public IPictureView createView(final PictureControl pPictureControl) {
        stateCheck();
        return mViewFactory.createView(pPictureControl);
    }

    @Override
    public IView createView(final ScrollLayout pScrollLayout) {
        stateCheck();
        return mViewFactory.createView(pScrollLayout);
    }

    @Override
    public IView createView(final StringControl pStringControl) {
        stateCheck();
        return mViewFactory.createView(pStringControl);
    }

    @Override
    public IView createView(final VFlowLayout pVFlow) {
        stateCheck();
        return mViewFactory.createView(pVFlow);
    }

    @Override
    public IView createView(final ProgressControl pProgressControl) {
        stateCheck();
        return mViewFactory.createView(pProgressControl);
    }

    @Override
    public UndoRedoBuffer getPropertiesUndoRedoBuffer() {
        stateCheck();
        return mViewFactory.getPropertiesUndoRedoBuffer();
    }

    @Override
    public IContainer getViewFactoryPropertiesViewable() {
        stateCheck();
        return mViewFactory.getViewFactoryPropertiesViewable();
    }

    @Override
    public void showDialog(final FileControl pFileControl) {
        stateCheck();
        mViewFactory.showDialog(pFileControl);
    }

    @Override
    public IDialogView createDialog(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        stateCheck();
        return mViewFactory.createDialog(pViewable,pDialogOptions,pUndoRedo,pButtons);
    }

}
