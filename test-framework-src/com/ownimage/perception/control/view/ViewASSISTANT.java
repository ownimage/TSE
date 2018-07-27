package com.ownimage.perception.control.view;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.view.IDoubleView;
import com.ownimage.framework.view.IView;

public class ViewASSISTANT implements IDoubleView {

	private boolean mHasFired = false;
	private IControl mControl = null;
	private Object mLastControl = null;
	private boolean mLastIsMutating = false;

	private boolean mVisible = true;
	private boolean mEnabled = false;

	public ViewASSISTANT(final IControl pControl) {
		mControl = pControl;
		mVisible = pControl.isVisible();
		mEnabled = pControl.isEnabled();
	}

	@Override
	public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
		mHasFired = true;
		mLastControl = pControl;
		mLastIsMutating = pIsMutating;
		mVisible = pControl.isVisible();
		mEnabled = pControl.isEnabled();
	}

	public boolean getHasFired() {
		return mHasFired;
	}

	public Object getLastControl() {
		return mLastControl;
	}

	public boolean getLastIsMutating() {
		return mLastIsMutating;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public boolean isVisible() {
		return mVisible;
	}

	@Override
	public void redraw() {
		// TODO Auto-generated method stub

	}

	public void reset() {
		mVisible = ((IControl) mLastControl).isVisible();
		mEnabled = ((IControl) mLastControl).isEnabled();

		mHasFired = false;
		mLastControl = null;
		mLastIsMutating = false;
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		mEnabled = pEnabled;
	}

	@Override
	public void setVisible(final boolean pVisible) {
		mVisible = pVisible;
	}

	@Override
	public void setDisplayType(final DoubleMetaType.DisplayType pDisplayType) {

	}
}
