/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class Label extends javafx.scene.control.Label implements ChangeListener<Number> {

    public Label() {
        this("");
    }

    public Label(final String pLabel) {
        super(pLabel);

        prefWidthProperty().bind(getFactory().labelWidthProperty);
        maxWidthProperty().bind(getFactory().labelWidthProperty);
        minWidthProperty().bind(getFactory().labelWidthProperty);

        int rightPadding = getFactory().labelRightPaddingProperty.intValue();
        changed(getFactory().labelRightPaddingProperty, 0, rightPadding);
        getFactory().labelRightPaddingProperty.addListener(this);

        setAlignment(Pos.CENTER_RIGHT);
    }

    @Override
    public void changed(final ObservableValue<? extends Number> pObservable, final Number pOldValue, final Number pNewValue) {
        setPadding(new Insets(0, pNewValue.intValue(), 0, 0));
    }

    public FXViewFactory getFactory() {
        return FXViewFactory.getInstance();
    }
}
