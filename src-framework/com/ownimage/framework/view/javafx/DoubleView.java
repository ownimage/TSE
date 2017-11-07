package com.ownimage.framework.view.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.type.DoubleMetaType;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class DoubleView extends ViewBase<DoubleControl> {

	private static Image mSpinnerImage;
	private static Image mSliderImage;

	static {
		mSpinnerImage = getImage("/icon/spinner.png");
		mSliderImage = getImage("/icon/slider.png");
	};

	private final HBox mUI;
	private final HBox mControlPanel;
	private final Spinner<Double> mSpinner;
	private final Slider mSlider;
	private final Label mDisplayedValue;
	private final Button mDisplayOption;
	private boolean mAllowUpdates = true;
	private DoubleMetaType.DisplayType mDisplayState = DoubleMetaType.DisplayType.SPINNER;

	public DoubleView(final DoubleControl pDoubleControl) {
		super(pDoubleControl);

		double min = mControl.getMetaType().getMin();
		double max = mControl.getMetaType().getMax();
		double value = mControl.getValue();
		double step = mControl.getMetaType().getStep();

		mSpinner = new Spinner<Double>(min, max, value, step);
		mSpinner.setEditable(mControl.isEnabled());
		mSpinner.setDisable(!mControl.isEnabled());
		mSpinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		mSpinner.valueProperty().addListener((observable, oldValue, newValue) -> setControlValueFromSpinner(newValue));
		bindWidth(mSpinner, getFactory().controlWidthProperty);

		mSlider = new Slider(min, max, value);
		mSlider.valueProperty().addListener((observable, oldValue, newValue) -> setControlValueFromSlider());
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
		mDisplayOption.setOnAction((e) -> changeDisplay());
		bindWidth(mDisplayOption, getFactory().smallButtonWidthProperty);

		mControlPanel = new HBox();
		mControlPanel.setAlignment(Pos.CENTER);
		mControlPanel.getChildren().addAll(mSpinner);

		mUI = new HBox();
		mUI.setAlignment(Pos.CENTER);
		mUI.getChildren().addAll(mLabel, mControlPanel, mDisplayOption);
	}

	private static Image getImage(final String pName) {
		try {
			URL url = pName.getClass().getResource(pName);
			InputStream stream = url.openStream();
			Image image = new Image(stream);
			;
			return image;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	private void bindWidth(final Region pRegion, final SimpleIntegerProperty pWidthProperty) {
		pRegion.prefWidthProperty().bind(pWidthProperty);
		pRegion.maxWidthProperty().bind(pWidthProperty);
		pRegion.minWidthProperty().bind(pWidthProperty);
	}

	private void changeDisplay() {
		if (mDisplayState == DoubleMetaType.DisplayType.SPINNER) {
			mDisplayState = DoubleMetaType.DisplayType.SLIDER;
			mDisplayOption.setGraphic(new ImageView(mSliderImage));
			mControlPanel.getChildren().clear();
			mControlPanel.getChildren().addAll(mSlider, mDisplayedValue);
		} else {
			mDisplayState = DoubleMetaType.DisplayType.SPINNER;
			mDisplayOption.setGraphic(new ImageView(mSpinnerImage));
			mControlPanel.getChildren().clear();
			mControlPanel.getChildren().add(mSpinner);
		}
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

	private void setControlValueFromSlider() {
		System.out.println("setControlValueFromSlider");
		try {
			if (mAllowUpdates) {
				mAllowUpdates = false;
				mControl.setValue(mSlider.getValue(), this, false);
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

	private void updateDisplayedValue() {
		mDisplayedValue.setText(String.format(getFactory().doubleFormat.get(), mControl.getValue()));
	}

}
