/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import java.util.Iterator;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.StringControl;
import com.ownimage.framework.control.layout.IViewable;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
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

        if (mControl.hasTitle()) {
            StringControl title = StringControl.createTitle(mControl.getDisplayName());
            mUI.getChildren().add(((FXView) title.createView()).getUI());
        }

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

        if (mControl.hasBottomPadding()) {
            FXViewFactory.getInstance().containerBottomPaddingProperty.addListener(this::bottomPaddingChange);
            bottomPaddingChange(null, null, null);
        }

    }

    private void bottomPaddingChange(final ObservableValue<? extends Number> observableValue, final Number object, final Number object1) {
        mUI.setPadding(new Insets(0, 0, FXViewFactory.getInstance().containerBottomPaddingProperty.doubleValue(), 0));
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
