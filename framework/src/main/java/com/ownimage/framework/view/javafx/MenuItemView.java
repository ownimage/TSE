/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.view.IView;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;

public class MenuItemView implements IView {

    private final ActionControl mActionControl;
    private MenuItem mUI;

    public MenuItemView(final ActionControl pActionControl) {
        if (pActionControl == null) {
            throw new IllegalArgumentException("pActionControl must not be null.");
        }

        mActionControl = pActionControl;

        createView();
    }

    @Override
    public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
        // TODO Auto-generated method stub

    }

    private void createView() {
        mUI = new MenuItem(mActionControl.getDisplayName());
        mUI.setOnAction(e -> new Thread(new ThreadGroup("UI lambda"), mActionControl::performAction).start());
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
