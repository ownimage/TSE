package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class TestDoubleView extends FXViewBase {

	private final DoubleControl mDoubleControl;
	private final HBox mUI;;
	private final Spinner<Double> mDoubleSpinner;

	private boolean mAllowUpdates = true;

	public TestDoubleView(final DoubleControl pDoubleControl) {
		mDoubleControl = pDoubleControl;
		mDoubleControl.addControlChangeListener(this);

		Label label = new Label(mDoubleControl.getDisplayName());
		label.setMaxWidth(200); // TODO need to move this to a parameter
		label.setMinWidth(200);
		label.setPrefWidth(200);

		// mDoubleControl.getMetaType().getMin(), mDoubleControl.getMetaType().getMax(), mDoubleControl.getMetaType().getStep()
		double min = 0.0;
		double max = 1.0;
		double step = 0.1;
		mDoubleSpinner = new Spinner<Double>(min, max, mDoubleControl.getValue(), step);
		mDoubleSpinner.setEditable(true);
		mDoubleSpinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // TODO need to fix this
		mDoubleSpinner.valueProperty().addListener((observable, oldValue, newValue) -> setControlValue(newValue));

		mUI = new HBox();
		mUI.getChildren().addAll(label, mDoubleSpinner);
	}

	@Override
	public void controlChangeEvent(IControl<?, ?, ?> pControl, final boolean pIsMutating) {
		mDoubleSpinner.getEditor().setText(pControl.getString());
	}

	public Region getUI() {
		return mUI;
	}

	private void setControlValue(final double pValue) {
		try {
			if (mAllowUpdates) {
				mDoubleControl.setValue(pValue, this);
				mAllowUpdates = false;
				mDoubleSpinner.getEditor().setText(mDoubleControl.getString());
			}
		} finally {
			mAllowUpdates = true;
		}
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		// TODO
	}

	@Override
	public void setVisible(final boolean pVisible) {
		mUI.setManaged(pVisible);
	}

}
