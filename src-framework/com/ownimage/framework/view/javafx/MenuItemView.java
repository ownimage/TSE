package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.view.IView;

import javafx.scene.control.MenuItem;

public class MenuItemView implements IView {

	private final ActionControl mActionControl;
	private MenuItem mUI;

	public MenuItemView(final ActionControl pActionControl) {
		if (pActionControl == null) {
			throw new IllegalArgumentException("pActionControl must not be null.");
		}

		mActionControl = pActionControl;

		createView();
	}

	@Override
	public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
		// TODO Auto-generated method stub

	}

	private void createView() {
		mUI = new MenuItem(mActionControl.getDisplayName());
		mUI.setOnAction(e -> mActionControl.performAction());
	}

	public MenuItem getUI() {
		return mUI;
	}

	@Override
	public void redraw() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		mUI.setDisable(!pEnabled);
	}

	@Override
	public void setVisible(final boolean pVisible) {
		mUI.setVisible(pVisible);
	}

}
