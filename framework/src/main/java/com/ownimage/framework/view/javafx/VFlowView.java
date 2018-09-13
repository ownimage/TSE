/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.VFlowLayout;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Iterator;

public class VFlowView extends ViewBase<VFlowLayout> {

    private VBox mUI;

    public VFlowView(final VFlowLayout pVFlow) {
        super(pVFlow);
        createView();
    }

    private void createView() {
        mUI = new VBox();
        mUI.setPadding(new Insets(10, 0, 0, 0));

        final Iterator<IViewable<?>> children = mControl.getViewableChildrenIterator();
        while (children.hasNext()) {
            final IViewable child = children.next();

            final HBox hbox = new HBox();
            hbox.setAlignment(Pos.TOP_LEFT);
            hbox.getChildren().add(((FXView) (child.createView())).getUI());

            mUI.getChildren().add(hbox);
        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
