/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.app.menu.MenuAction;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.view.IView;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import lombok.NonNull;

public class MenuItemView implements IView {

    private final String mDisplayName;
    private final IAction mAction;
    private MenuItem mUI;

    public MenuItemView(@NonNull final ActionControl pActionControl) {
        mDisplayName = pActionControl.getDisplayName();
        mAction = pActionControl.getAction();
        createView();
    }

    public MenuItemView(@NonNull final MenuAction pMenuAction) {
        mDisplayName = pMenuAction.getDisplayName();
        mAction = pMenuAction.getAction();
        createView();
    }

    @Override
    public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
        // TODO Auto-generated method stub

    }

    private void createView() {
        mUI = new MenuItem(mDisplayName);
        mUI.setOnAction(e -> new Thread(new ThreadGroup("UI lambda"), mAction::performAction).start());
    }

    public MenuItem getUI() {
        return mUI;
    }

    @Override
    public void redraw() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEnabled(final boolean pEnabled) {
        Platform.runLater(() -> mUI.setDisable(!pEnabled));
    }

    @Override
    public void setVisible(final boolean pVisible) {
        Platform.runLater(() -> mUI.setVisible(pVisible));
    }

}
