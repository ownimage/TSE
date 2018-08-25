/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.ProgressControl;

import javafx.application.Platform;
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
        final ObservableValue<? extends Number> progressBarHeight = FXViewFactory.getInstance().progressBarHeight;

        mProgressBar = new ProgressBar(mControl.getNormalizedValue());
        mProgressBar.prefWidthProperty().bind(controlWidthProperty);
        mProgressBar.minWidthProperty().bind(controlWidthProperty);
        mProgressBar.maxWidthProperty().bind(controlWidthProperty);
        mProgressBar.prefHeightProperty().bind(progressBarHeight);
        mProgressBar.minHeightProperty().bind(progressBarHeight);
        mProgressBar.maxHeightProperty().bind(progressBarHeight);
        mProgressBar.setStyle("-fx-accent: " + FXViewFactory.getInstance().getProgressNormalColorHex());

        mProgressText = new Label(mControl.getProgressString());
        mProgressText.setAlignment(Pos.CENTER);
        mProgressText.prefWidthProperty().bind(controlWidthProperty);
        mProgressText.minWidthProperty().bind(controlWidthProperty);
        mProgressText.maxWidthProperty().bind(controlWidthProperty);

        StackPane vbox = new StackPane(mProgressBar, mProgressText);

        mUI = new HBox();
        mUI.setAlignment(Pos.CENTER);
        mUI.getChildren().addAll(mLabel, vbox);
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
                if (mControl.isFinished())
                    mProgressBar.setStyle("-fx-accent: " + FXViewFactory.getInstance().getProgressCompleteColorHex());
            });

        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
