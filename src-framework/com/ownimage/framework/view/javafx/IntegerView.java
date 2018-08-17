package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IntegerControl;

import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class IntegerView extends ViewBase<IntegerControl> {

    private final HBox mUI;
	private final Spinner<Integer> mIntegerSpinner;

	private boolean mAllowUpdates = true;

	public IntegerView(final IntegerControl pIntegerControl) {
		super(pIntegerControl);

		int min = mControl.getMetaType().getMin();
		int max = mControl.getMetaType().getMax();
		int step = mControl.getMetaType().getStep();

		// lost focus commiting value from
		// https://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, pIntegerControl.getValue(), step);
        TextFormatter<Integer> formatter = new TextFormatter<>(factory.getConverter(), factory.getValue());
        mIntegerSpinner = new Spinner<>(min, max, mControl.getValue(), step);
		mIntegerSpinner.setValueFactory(factory);
		mIntegerSpinner.setEditable(true);
		mIntegerSpinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // TODO need to fix this
		mIntegerSpinner.valueProperty().addListener((observable, oldValue, newValue) -> setControlValue(newValue));
		mIntegerSpinner.prefWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);
		mIntegerSpinner.minWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);
		mIntegerSpinner.maxWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);
		mIntegerSpinner.getEditor().setTextFormatter(formatter);
		factory.valueProperty().bindBidirectional(formatter.valueProperty());

		mUI = new HBox();
		mUI.setAlignment(Pos.CENTER);
		mUI.getChildren().addAll(mLabel, mIntegerSpinner);

		setEnabled(mControl.isEnabled());
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		if (pControl == mControl) {
            if (!mIntegerSpinner.getValue().equals(mControl.getValue())) {
                mIntegerSpinner.getEditor().setText(mControl.getString());
            }
		}
	}

	@Override
	public Pane getUI() {
		return mUI;
	}

	private void setControlValue(final int pValue) {
		try {
            if (mAllowUpdates && !mIntegerSpinner.getValue().equals(mControl.getValue())) {
				mControl.setValue(pValue, this, false);
				mAllowUpdates = false;
				mIntegerSpinner.getEditor().setText(mControl.getString());
			}
		} finally {
			mAllowUpdates = true;
		}
	}

}
