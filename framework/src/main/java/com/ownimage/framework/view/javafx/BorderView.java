/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.view.IBorderView;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class BorderView extends ViewBase<BorderLayout> implements IBorderView {

    private BorderPane mUI;

    public BorderView(final BorderLayout pBorder) {
        super(pBorder);
        createView();
    }

    private void createView() {
        mUI = new BorderPane();
        mUI.setUserData(this);
        redrawTop();
        redrawBottom();
        redrawLeft();
        redrawRight();
        redrawCenter();
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

    @Override
    public void redrawBottom() {
        runOnFXApplicationThread(() -> {
            IViewable<?> viewable = mControl.getBottom();
            Node content = null;

            if (viewable != null) {
                FXView view = (FXView) viewable.createView();
                content = view.getUI();
            }

            mUI.setBottom(content);
        });
    }

    @Override
    public void redrawCenter() {
        runOnFXApplicationThread(() -> {
            IViewable<?> viewable = mControl.getCenter();
            Node content = null;

            if (viewable != null) {
                FXView view = (FXView) viewable.createView();
                content = view.getUI();
            }

            mUI.setCenter(content);
        });
    }

    @Override
    public void redrawLeft() {
        runOnFXApplicationThread(() -> {
            IViewable<?> viewable = mControl.getLeft();
            Node content = null;

            if (viewable != null) {
                FXView view = (FXView) viewable.createView();
                content = view.getUI();
            }

            mUI.setLeft(content);
        });
    }

    @Override
    public void redrawRight() {
        runOnFXApplicationThread(() -> {
            IViewable<?> viewable = mControl.getRight();
            Node content = null;

            if (viewable != null) {
                FXView view = (FXView) viewable.createView();
                content = view.getUI();
            }

            mUI.setRight(content);
        });
    }

    @Override
    public void redrawTop() {
        runOnFXApplicationThread(() -> {
            IViewable<?> viewable = mControl.getTop();
            Node content = null;

            if (viewable != null) {
                FXView view = (FXView) viewable.createView();
                content = view.getUI();
            }

            mUI.setTop(content);
        });
    }

}
