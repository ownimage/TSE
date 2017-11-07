package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.layout.IViewable;

import javafx.scene.Node;

public class ViewBase<C extends IViewable> implements FXView {

	protected C mControl;
	protected Label mLabel;

	public ViewBase(final C pControl) {
		mControl = pControl;

		if (pControl instanceof IControl) {
			((IControl) pControl).addControlChangeListener(this);
			mLabel = new Label(((IControl) pControl).getDisplayName());
			mLabel.prefWidthProperty().bind(FXViewFactory.getInstance().labelWidthProperty);
			mLabel.minWidthProperty().bind(FXViewFactory.getInstance().labelWidthProperty);
			mLabel.maxWidthProperty().bind(FXViewFactory.getInstance().labelWidthProperty);
		}
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
	}

	public FXViewFactory getFactory() {
		return FXViewFactory.getInstance();
	}

	@Override
	public Node getUI() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void redraw() {
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		getUI().setDisable(!pEnabled);
	}

	@Override
	public void setVisible(final boolean pVisible) {
		getUI().setManaged(pVisible);
		getUI().setVisible(pVisible);
	}

}
