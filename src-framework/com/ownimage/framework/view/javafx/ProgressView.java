/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.ProgressControl;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class ProgressView extends ViewBase<ProgressControl> {

    private final HBox mUI;
    private final ProgressBar mProgressBar;
    private final Label mProgressText;

    public ProgressView(final ProgressControl pProgressControl) {
        super(pProgressControl);

        int min = mControl.getMetaType().getMin();
        int max = mControl.getMetaType().getMax();
        int step = mControl.getMetaType().getStep();

        final ObservableValue<? extends Number> controlWidthProperty = FXViewFactory.getInstance().controlWidthProperty;
        final ObservableValue<? extends Number> labelWidthProperty = FXViewFactory.getInstance().labelWidthProperty;
        final ObservableValue<? extends Number> progressBarHeight = FXViewFactory.getInstance().progressBarHeight;

        mUI = new HBox();
        mUI.setAlignment(Pos.CENTER);

        mProgressBar = new ProgressBar(mControl.getNormalizedValue());
        mProgressBar.prefHeightProperty().bind(progressBarHeight);
        mProgressBar.minHeightProperty().bind(progressBarHeight);
        mProgressBar.maxHeightProperty().bind(progressBarHeight);
        if (mControl.getShowLabel()) {
            mProgressBar.prefWidthProperty().bind(controlWidthProperty);
            mProgressBar.minWidthProperty().bind(controlWidthProperty);
            mProgressBar.maxWidthProperty().bind(controlWidthProperty);
        }

        mProgressText = new Label(mControl.getProgressString());
        mProgressText.setAlignment(Pos.CENTER);
        mProgressText.prefWidthProperty().bind(controlWidthProperty);
        mProgressText.minWidthProperty().bind(controlWidthProperty);
        mProgressText.maxWidthProperty().bind(controlWidthProperty);

        if (!mControl.getShowLabel()) {
            ChangeListener<Number> widthListener = (obs, oldValue, newValue) -> {
                double width = labelWidthProperty.getValue().doubleValue() + controlWidthProperty.getValue().doubleValue();
                mProgressBar.prefWidthProperty().setValue(width);
                mUI.prefWidthProperty().setValue(width);
            };
            labelWidthProperty.addListener(widthListener);
            controlWidthProperty.addListener(widthListener);
            widthListener.changed(null, null, null);
        }

        StackPane stack = new StackPane(mProgressBar, mProgressText);

        if (mControl.getShowLabel()) mUI.getChildren().add(mLabel);
        mUI.getChildren().add(stack);
        mUI.setUserData(this);

        setEnabled(mControl.isEnabled());
        mControl.addControlChangeListener(this);
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mControl) {
            Platform.runLater(() -> {
                mProgressBar.setProgress(mControl.getNormalizedValue());
                mProgressText.setText(mControl.getProgressString());
                mProgressBar.setStyle("-fx-accent: " + getColorString());
            });

        }
    }

    private String getColorString() {
        return mControl.isFinished()
                ? FXViewFactory.getInstance().getProgressCompleteColorHex()
                : FXViewFactory.getInstance().getProgressNormalColorHex();
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
