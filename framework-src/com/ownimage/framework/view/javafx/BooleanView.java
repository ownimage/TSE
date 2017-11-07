package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.IControl;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class BooleanView extends ViewBase<BooleanControl> {

	private final HBox mUI;
	private final CheckBox mCheckbox;

	private boolean mAllowUpdates = true;

	public BooleanView(final BooleanControl pBooleanControl) {
		super(pBooleanControl);

		mCheckbox = new CheckBox();
		mCheckbox.setSelected(mControl.getValue());
		mCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> setControlValue(newValue));
		mCheckbox.prefWidthProperty().bind(FXViewFactory.getInstance().labelWidthProperty);

		mUI = new HBox();
		mUI.setAlignment(Pos.CENTER);
		mUI.getChildren().addAll(mLabel, mCheckbox);
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		if (pControl == mControl) {
			mCheckbox.setSelected(mControl.getValue());
		}
	}

	@Override
	public Pane getUI() {
		return mUI;
	}

	private void setControlValue(final boolean pValue) {
		try {
			if (mAllowUpdates && mControl.getValue() != pValue) { // note the value change check was becasue this seemed to be being
																	// called more than once.
				mAllowUpdates = false;
				mControl.setValue(pValue, this, false);
			}
		} finally {
			mAllowUpdates = true;
		}
	}

}
