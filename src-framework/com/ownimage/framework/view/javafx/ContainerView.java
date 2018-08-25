/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view.javafx;

import java.util.Iterator;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.layout.IViewable;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ContainerView extends ViewBase<IContainer> {

    private VBox mUI;

    public ContainerView(final IContainer pContainer) {
        super(pContainer);
        createView();
    }

    private void createView() {
        mUI = new VBox();

        Iterator<IViewable<?>> children = mControl.getViewableChildrenIterator();
        while (children.hasNext()) {
            IViewable<?> child = children.next();

            Node childView = ((FXView) (child.createView())).getUI();
            if (child instanceof IControl) {
                childView.setVisible(((IControl) child).isVisible()); // TODO these should align with ViewBase
                childView.setManaged(((IControl) child).isVisible());
            }

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.TOP_LEFT);
            hbox.getChildren().addAll(childView);

            mUI.getChildren().add(hbox);
        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
