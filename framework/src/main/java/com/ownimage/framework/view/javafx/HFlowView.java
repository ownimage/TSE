/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.IViewable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.Iterator;

public class HFlowView extends ViewBase<HFlowLayout> {

    private HBox mUI;

    public HFlowView(final HFlowLayout pHFlow) {
        super(pHFlow);
        createView();
    }

    private void createView() {
        mUI = new HBox();

        final Iterator<IViewable<?>> children = mControl.getViewableChildrenIterator();
        while (children.hasNext()) {
            final IViewable child = children.next();
            mUI.getChildren().add(((FXView) (child.createView())).getUI());
        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
