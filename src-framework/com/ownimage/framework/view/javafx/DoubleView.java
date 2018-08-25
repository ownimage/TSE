/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view.javafx;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IDoubleView;

import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class DoubleView extends ViewBase<DoubleControl> implements IDoubleView {


    private final static Logger mLogger = Framework.getLogger();

    private static Image mSpinnerImage = getImage("/icon/spinner.png");
    private static Image mSliderImage = getImage("/icon/slider.png");

    private final HBox mUI;
    private final HBox mControlPanel;
    private final Spinner<Double> mSpinner;
    private final Slider mSlider;
    private final Label mDisplayedValue;
    private final Button mDisplayOption;
    private boolean mAllowUpdates = true;
    private DoubleMetaType.DisplayType mDisplayType;

    public DoubleView(final DoubleControl pDoubleControl) {
        super(pDoubleControl);
        setMutating(true);

        double min = mControl.getMetaType().getMin();
        double max = mControl.getMetaType().getMax();
        double value = mControl.getValue();
        double step = mControl.getMetaType().getStep();

        mSpinner = new Spinner<>(min, max, value, step);
        mSpinner.setEditable(mControl.isEnabled());
        mSpinner.setDisable(!mControl.isEnabled());
        mSpinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mSpinner.valueProperty().addListener((observable, oldValue, newValue) -> setControlValueFromSpinner(newValue));
        bindWidth(mSpinner, getFactory().controlWidthProperty);

        mSlider = new Slider(min, max, value);
        mSlider.setDisable(!mControl.isEnabled());
        mSlider.valueProperty().addListener((observable, oldValue, newValue) -> setControlValueFromSlider(true));
        mSlider.onMouseReleasedProperty().setValue(event -> setControlValueFromSlider(false));
        InvalidationListener sliderSizeListener = o -> {
            int width = getFactory().controlWidthProperty.get() - getFactory().sliderValueWidthProperty.get();
            mSlider.setMaxWidth(width);
            mSlider.setMinWidth(width);
            mSlider.setPrefWidth(width);
        };
        sliderSizeListener.invalidated(null);
        getFactory().controlWidthProperty.addListener(sliderSizeListener);
        getFactory().sliderValueWidthProperty.addListener(sliderSizeListener);

        mDisplayedValue = new Label(String.format(getFactory().doubleFormat.get(), mControl.getValue()));
        bindWidth(mDisplayedValue, getFactory().sliderValueWidthProperty);
        getFactory().doubleFormat.addListener((observable, oldValue, newValue) -> updateDisplayedValue());

        mDisplayOption = new Button();
        mDisplayOption.setGraphic(new ImageView(mSliderImage));
        mDisplayOption.setOnAction((e) -> toggleDisplayType());
        bindWidth(mDisplayOption, getFactory().smallButtonWidthProperty);

        mControlPanel = new HBox();
        mControlPanel.setAlignment(Pos.CENTER);
        mControlPanel.getChildren().addAll(mSpinner);

        mUI = new HBox();
        mUI.setAlignment(Pos.CENTER);
        mUI.getChildren().addAll(mLabel, mControlPanel, mDisplayOption);
        setDisplayType(mControl.getDisplayType());
        setMutating(false);
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mControl) {
            mSpinner.getEditor().setText(mControl.getString());
            mSlider.setValue(mControl.getValue());
            updateDisplayedValue();
        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

    private void setControlValueFromSlider(boolean pIsMutating) {
        mLogger.log(Level.FINEST, "setControlValueFromSlider");
        try {
            if (mAllowUpdates) {
                mAllowUpdates = false;
                queueApplicationEvent(() -> mControl.setValue(mSlider.getValue(), this, pIsMutating));
                controlChangeEvent(mControl, false);
            }
        } finally {
            mAllowUpdates = true;
        }
    }

    private void setControlValueFromSpinner(final double pValue) {
        try {
            if (mAllowUpdates) {
                mAllowUpdates = false;
                mControl.setValue(pValue, this, false);
                controlChangeEvent(mControl, false);
            }
        } finally {
            mAllowUpdates = true;
        }
    }

    private void toggleDisplayType() {
        if (mDisplayType == DoubleMetaType.DisplayType.SPINNER) {
            setDisplayType(DoubleMetaType.DisplayType.SLIDER);
        } else {
            setDisplayType(DoubleMetaType.DisplayType.SPINNER);
        }
    }

    public void setDisplayType(DoubleMetaType.DisplayType pDisplayType) {
        runOnFXApplicationThread(() -> {
            if (pDisplayType == DoubleMetaType.DisplayType.SLIDER) {
                mDisplayType = DoubleMetaType.DisplayType.SLIDER;
                mDisplayOption.setGraphic(new ImageView(mSliderImage));
                mControlPanel.getChildren().clear();
                mControlPanel.getChildren().addAll(mSlider, mDisplayedValue);
            } else {
                mDisplayType = DoubleMetaType.DisplayType.SPINNER;
                mDisplayOption.setGraphic(new ImageView(mSpinnerImage));
                mControlPanel.getChildren().clear();
                mControlPanel.getChildren().add(mSpinner);
            }

            if (isNotMutating()) {
                mControl.setDisplayType(pDisplayType, this);
            }
        });
    }

    private void updateDisplayedValue() {
        mDisplayedValue.setText(String.format(getFactory().doubleFormat.get(), mControl.getValue()));
    }

}
