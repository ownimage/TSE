/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view.javafx;

import java.util.Iterator;

import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.VFlowLayout;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class VFlowView extends ViewBase<VFlowLayout> {

    private VBox mUI;

    public VFlowView(final VFlowLayout pVFlow) {
        super(pVFlow);
        createView();
    }

    private void createView() {
        mUI = new VBox();

        Iterator<IViewable<?>> children = mControl.getViewableChildrenIterator();
        while (children.hasNext()) {
            IViewable child = children.next();
            mUI.getChildren().add(((FXView) (child.createView())).getUI());
        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
