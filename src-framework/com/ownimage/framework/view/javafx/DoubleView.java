package com.ownimage.framework.view.javafx;

import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;

public class DoubleView extends ViewBase<DoubleControl> {

	private final HBox mUI;
	private final Spinner<Double> mDoubleSpinner;

	private boolean mAllowUpdates = true;

	public DoubleView(final DoubleControl pDoubleControl) {
		super(pDoubleControl);

		double min = mControl.getMetaType().getMin();
		double max = mControl.getMetaType().getMax();
		double step = mControl.getMetaType().getStep();
		mDoubleSpinner = new Spinner<Double>(min, max, mControl.getValue(), step);
		mDoubleSpinner.setEditable(mControl.isEnabled());
		mDoubleSpinner.setDisable(!mControl.isEnabled());
		mDoubleSpinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		mDoubleSpinner.valueProperty().addListener((observable, oldValue, newValue) -> setControlValue(newValue));

		mUI = new HBox();
		mUI.setAlignment(Pos.CENTER);
		mUI.getChildren().addAll(mLabel, mDoubleSpinner);
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		if (pControl == mControl) {
			mDoubleSpinner.getEditor().setText(mControl.getString());
		}
	}

	@Override
	public Pane getUI() {
		return mUI;
	}

	private void setControlValue(final double pValue) {
		try {
			if (mAllowUpdates) {
				mControl.setValue(pValue, this, false);
				mAllowUpdates = false;
				mDoubleSpinner.getEditor().setText(mControl.getString());
			}
		} finally {
			mAllowUpdates = true;
		}
	}

}
