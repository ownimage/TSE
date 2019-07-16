package com.ownimage.framework.control.event;

public class ControlChangeListenerASSISTANT<C> implements IControlChangeListener<C> {

    private int mFired = 0;
    private Object mLastControl = null;
    private boolean mLastIsMutating = false;

    @Override
    public void controlChangeEvent(final C pControl, final boolean pIsMutating) {
        mFired++;
        mLastControl = pControl;
        mLastIsMutating = pIsMutating;
    }

    public int getFiredCount() {
        return mFired;
    }

    public boolean getHasFired() {
        return mFired != 0;
    }

    public Object getLastControl() {
        return mLastControl;
    }

    public boolean getLastIsMutating() {
        return mLastIsMutating;
    }

    public void reset() {
        mFired = 0;
        mLastControl = null;
        mLastIsMutating = false;
    }
}
