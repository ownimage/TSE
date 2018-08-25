/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IMouseControl;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.perception.math.KMath;

public class ControlSelector implements IUIEventListener {


    private final static Logger mLogger = Framework.getLogger();

	private final BaseTransform mTransform;

	private final Vector<IMouseControl> mControlX = new Vector<IMouseControl>();
	private final Vector<IMouseControl> mControlY = new Vector<IMouseControl>();
	private int mIndex;

	private boolean mDragging = false;

	public ControlSelector(final BaseTransform pTransform) {
		mTransform = pTransform;
	}

	public void addXControl(final IMouseControl pControl) {
		mControlX.add(pControl);
		mControlY.add(null);
	}

	public void addXYControl(final IMouseControl pControl) {
		mControlX.add(pControl);
		mControlY.add(pControl);
	}

	public void addXYControlPair(final IMouseControl pControlX, final IMouseControl pControlY) {
		mControlX.add(pControlX);
		mControlY.add(pControlY);
	}

	public void addYControl(final IMouseControl pControl) {
		mControlX.add(null);
		mControlY.add(pControl);
	}

	private IMouseControl getXControl() {
		IMouseControl control = null;
		if (mControlX.size() != 0) {
			control = mControlX.get(mIndex);
		}
		return control;
	}

	private IMouseControl getYControl() {
		IMouseControl control = null;
		if (mControlY.size() != 0) {
			control = mControlY.get(mIndex);
		}
		return control;
	}

	public boolean isControlSelected(final IMouseControl pControl) {
		return getXControl() == pControl || getYControl() == pControl;
	}

	public boolean isDragging() {
		return mDragging;
	}

	public boolean isNotDragging() {
		return !mDragging;
	}

	@Override
	public void mouseClickEvent(final IUIEvent pEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDoubleClickEvent(final IUIEvent pEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragEndEvent(final IUIEvent pEvent) {
		Framework.logEntry(mLogger);
		mDragging = false;

		IMouseControl xControl = getXControl();
		IMouseControl yControl = getYControl();

		if (xControl == yControl) { // here we have an XY control so only need to call dragStart once
			xControl.dragEnd();

		} else {
			if (xControl != null) {
				xControl.dragEnd();
			}
			if (yControl != null) {
				yControl.dragEnd();
			}
		}

		mTransform.refreshOutputPreview();
		Framework.logExit(mLogger);
	}

	@Override
	public void mouseDragEvent(final IUIEvent pEvent) {
		Framework.logEntry(mLogger);
		mLogger.severe(pEvent.toString());

		setXYControlValue(pEvent.getNormalizedDeltaX(), pEvent.getNormalizedDeltaY());

		mTransform.setValues();
		mTransform.getPreviewImage().redrawGrafitti();
		Framework.logExit(mLogger);
	}

	@Override
	public void mouseDragStartEvent(final IUIEvent pEvent) {
		Framework.logEntry(mLogger);
		mDragging = true;

		IMouseControl xControl = getXControl();
		IMouseControl yControl = getYControl();

		if (xControl == yControl) { // here we have an XY control so only need to call dragStart once
			xControl.dragStart();

		} else {
			if (xControl != null) {
				xControl.dragStart();
			}
			if (yControl != null) {
				yControl.dragStart();
			}
		}

		Framework.logExit(mLogger);
	}

	@Override
	public void scrollEvent(final IUIEvent pEvent) {
		Framework.logEntry(mLogger);
		mIndex += pEvent.getScroll();
		mIndex = KMath.mod(mIndex, mControlX.size());
		mTransform.redrawGrafitti();
		Framework.logExit(mLogger);
	}

	private void setXControlValue(final double pValue) {
		Framework.logEntry(mLogger);

		IMouseControl xControl = getXControl();
		if (xControl != null) {
			xControl.drag(pValue);
		}

		Framework.logExit(mLogger);
	}

	private void setXYControlValue(final double pX, final double pY) {
		Framework.logEntry(mLogger);

		if (getXControl() != getYControl()) {
			setXControlValue(pX);
			setYControlValue(pY);
		} else {
			getXControl().drag(pX, pY);
		}

		Framework.logExit(mLogger);
	}

	private void setYControlValue(final double pValue) {
		Framework.logEntry(mLogger);

		IMouseControl yControl = getYControl();
		if (yControl != null) {
			yControl.drag(pValue);
		}

		Framework.logExit(mLogger);
	}
}
