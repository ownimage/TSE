/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.app;

import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.StringControl;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.NamedTabs;
import com.ownimage.framework.control.type.StringType;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import lombok.NonNull;

import java.awt.*;
import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

public abstract class AppControlBase implements IAppControl {

    public final static Logger mLogger = Framework.getLogger();


    private UndoRedoBuffer mUndoRedoBuffer;

    private int mX;
    private int mY;
    private int mWidth;

    private int mHeight;
    private String mTitle;
    protected IAppControlView mAppControlView;

    private MenuControl mMenu;

    private IView mContent;

    public AppControlBase(final String pTitle) {
        setTitle(pTitle);
        setSize();

    }

    protected IView createContentView() {
        final Container container = new Container("x", "x", this::getUndoRedoBuffer);
        final ColorControl colorControl = new ColorControl("x", "x", container, Color.ORANGE);

        final NamedTabs namedTabs = new NamedTabs("Test", "test");
        namedTabs.addTab("one", container);
        namedTabs.addTab("two", container);
        namedTabs.setSelectedIndex(1);

        final HSplitLayout hsplit = new HSplitLayout(namedTabs, namedTabs);

        return hsplit.createView();
    }

    protected MenuControl createMenuView() {
        final Container menuContainer = new Container("AppControlBase", "AppControlBase", this::getUndoRedoBuffer);

        final MenuControl menu = new MenuControl.Builder()
                .addMenu(
                        new MenuControl.Builder().setDisplayName("File")
                                .addMenu(
                                        new MenuControl.Builder().setDisplayName("File Sub")
                                                .addAction(new ActionControl("Exit", "exit", menuContainer, this::exit))
                                                .build()
                                )
                                .build()
                )
                .build();

        return menu;
    }

    protected void menuRegenerate() {
        mMenu = null;
        getAppControlView().ifPresent(IAppControlView::redraw);
    }

    private Optional<IAppControlView> getAppControlView() {
        return Optional.ofNullable(mAppControlView);
    }

    protected void dialogOKCancel(
            @NonNull final String pTitle,
            @NonNull final String pMessage,
            @NonNull final IAction pOKAction,
            @NonNull final IAction pCancelAction
    ) {
        Framework.logEntry(mLogger);

        final Container dialogContainer = new Container(pTitle, "title", this::getUndoRedoBuffer);

        final StringControl message = new StringControl("Message", "message", dialogContainer, new StringType(pMessage, StringType.LABEL));
        final ActionControl ok = ActionControl.create("OK", NullContainer, pOKAction);
        final ActionControl cancel = ActionControl.create("Cancel", NullContainer, pCancelAction);

        showDialog(dialogContainer, DialogOptions.NONE, cancel, ok);

        Framework.logExit(mLogger);
    }

    private void exit() {
        System.exit(0);
    }

    protected void fileExistsCheck(
            @NonNull final File pFile,
            @NonNull final String pTitle,
            @NonNull final IAction pOKAction,
            @NonNull final IAction pCancelAction
    ) {
        Framework.logEntry(mLogger);

        if (pFile.exists()) {
            final String message = "File " + pFile.getAbsolutePath() + " already exists.  Do you want to overwrite?";
            dialogOKCancel(pTitle, message, pOKAction, pCancelAction);
        } else {
            pOKAction.performAction();
        }

        Framework.logExit(mLogger);

    }

    @Override
    public IView getContent() {
        // note the lazy instantiation is because some UIs fail if content created too early
        if (mContent == null) {

            mContent = createContentView();
            if (mContent == null) {
                throw new IllegalStateException("createContentView() must not return null.");
            }
        }
        return mContent;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public MenuControl getMenu() {
        if (mMenu == null) {
            mMenu = createMenuView();
            if (mMenu == null) {
                throw new IllegalStateException("createMenuView() must not return null.");
            }
            if (!mMenu.isMenuBar()) {
                throw new IllegalStateException("createMenuView() needs to return a menuBar.");
            }
        }
        return mMenu;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    private synchronized UndoRedoBuffer getUndoRedoBuffer() {
        Framework.logEntry(mLogger);

        if (mUndoRedoBuffer == null) {
            mLogger.fine("Creating UndoRedoBuffer");
            mUndoRedoBuffer = new UndoRedoBuffer(100);
        }

        Framework.logExit(mLogger);
        return mUndoRedoBuffer;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getX() {
        return mX;
    }

    @Override
    public int getY() {
        return mY;
    }

    @Override
    public void setHeight(final int pHeight) {
        mHeight = pHeight;
    }

    protected void setSize() {
        mX = 300;
        mY = 300;
        mHeight = 300;
        mWidth = 300;
    }

    private void setTitle(final String pTitle) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNullOrEmpty(mLogger, pTitle, "pTitle");

        mTitle = pTitle;
        Framework.logExit(mLogger);
    }

    @Override
    public void setView(@NonNull final IAppControlView pAppControlView) {
        Framework.checkStateNoChangeOnceSet(mLogger, mAppControlView, "mAppControlView");
        mAppControlView = pAppControlView;
    }

    @Override
    public void setWidth(final int pWidth) {
        mWidth = pWidth;
    }

    @Override
    public void setX(final int pX) {
        mX = pX;
    }

    @Override
    public void setY(final int pY) {
        mY = pY;
    }

    public void showDialog(final IViewable pViewable, final DialogOptions pOptions, final ActionControl... pButtons) {
        mAppControlView.showDialog(pViewable, pOptions, pButtons);
    }

    public void showDialog(final IViewable pViewable, final DialogOptions pOptions, final UndoRedoBuffer pUndoRedoBuffer, final ActionControl... pButtons) {
        mAppControlView.showDialog(pViewable, pOptions, pUndoRedoBuffer, pButtons);
    }

}
