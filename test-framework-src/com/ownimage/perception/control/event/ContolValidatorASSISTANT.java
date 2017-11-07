package com.ownimage.perception.control.event;

import com.ownimage.framework.control.event.IControlValidator;

public class ContolValidatorASSISTANT<T> implements IControlValidator<T> {

	private boolean mReturnValue = true;
	private boolean mHasFired;
	private T mLastControl;

	public T getLastObject() {
		return mLastControl;
	}

	public boolean hasFired() {
		return mHasFired;
	}

	public void reset() {
		mHasFired = false;
		mLastControl = null;
	}

	public void setReturnValue(final boolean pReturnValue) {
		mReturnValue = pReturnValue;
	}

	@Override
	public boolean validateControl(final T pControl) {
		mHasFired = true;
		mLastControl = pControl;
		return mReturnValue;
	}

}
