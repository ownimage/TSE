/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.cm, Keith Hart
 */
package com.ownimage.framework.control.event;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;

public class ControlEventDispatcher implements IControlEventDispatcher<IControl<?, ?, ?, ?>> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	@SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();
	private final EventDispatcher<IControlChangeListener> mControlChangeListeners = new EventDispatcher<IControlChangeListener>(this);
	private final EventDispatcher<IControlValidator> mControlValidators = new EventDispatcher<IControlValidator>(this);
	private final Object mOwner; // this is for debugging purposes so you can tell who owns a dispatcher

	public ControlEventDispatcher(final Object pOwner) {
		mOwner = pOwner;
	}

	@Override
	public void addControlChangeListener(final IControlChangeListener pListener) {
		mControlChangeListeners.addListener(pListener);
	}

	@Override
	public void addControlValidator(final IControlValidator pValidator) {
		mControlValidators.addListener(pValidator);
	}

	@Override
	public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl) {
		fireControlChangeEvent(pControl, null, false);

	}

	@Override
	public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl, final IView pView, final boolean pIsMutating) {
		mControlChangeListeners.invokeAllExcept(pView, (listener) -> listener.controlChangeEvent(pControl, pIsMutating));
	}

	@Override
	public boolean fireControlValidate(final IControl<?, ?, ?, ?> pControl) {
		synchronized (pControl) {
			pControl.setValid(true);

			mControlValidators.invokeAll((validator) -> {
				if (pControl.isValid()) {
					pControl.setValid(validator.validateControl(pControl));
				}
			});

			return pControl.isValid();
		}
	}

	@Override
	public void removeControlChangeListener(final IControlChangeListener pListener) {
		mControlChangeListeners.removeListener(pListener);
	}

	@Override
	public void removeControlValidator(final IControlValidator pValidator) {
		mControlValidators.removeListener(pValidator);
	}

}
