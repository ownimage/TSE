/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.control.type.IntegerType;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class ProgressControl extends ControlBase<ProgressControl, IntegerType, IntegerMetaType, Integer, IView> implements IProgressObserver {

    public final static Logger mLogger = Framework.getLogger();

    private String mProgressString;
    private IAction mCompleteAction;
    private boolean mStarted;
    private boolean mFinished;

    public ProgressControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer) {
        super(pDisplayName, pPropertyName, pContainer, new IntegerType(0, new IntegerMetaType(0, 100, 1)));
        setTransient();
        setUndoEnabled(false);
        mCompleteAction = null;
    }

    public ProgressControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, IAction pAction) {
        this(pDisplayName, pPropertyName, pContainer);
        mCompleteAction = pAction;
    }

    @Override
    public ProgressControl clone(final IContainer pContainer) {
        if (pContainer == null) {
            throw new IllegalArgumentException("pContainer MUST not be null.");
        }

        ProgressControl pc = new ProgressControl(getDisplayName(), getPropertyName(), pContainer);
        pc.setValue(getValue());
        return pc;
    }

    @Override
    public IView createView() {
        IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    @Override
    public double getNormalizedValue() {
        return mValue.getNormalizedValue();
    }

    @Override
    public boolean setNormalizedValue(final double pNormalizedValue) {
        IntegerMetaType metaType = getMetaType();

        if (metaType == null) {
            throw new IllegalStateException("Cannot setNormalizedValue for an IntegerControl that does not have an IntegerMetaType.");
        }

        Integer value = metaType.getValueForNormalizedValue(pNormalizedValue);
        setValue(value);
        return true;
    }

    @Override
    public void setProgress(String pProgressString, int pPercent) {
        Framework.logEntry(mLogger);
        Framework.logParams(Framework.mLogger, "pProgressString,pPercent", pProgressString, pPercent);
        mStarted = true;
        int percent = pPercent;
        if (percent < 0) {
            mLogger.severe(() -> "pPercent = " + pPercent + ", must be >= 0");
            // the exeption is only used to get the stacktrace
            mLogger.fine(FrameworkLogger.throwableToString(new RuntimeException("stacktrace")));
            percent = 0;
        }
        if (percent > 100) {
            mLogger.severe(() -> "pPercent = " + pPercent + ", must be <= 100");
            // the exeption is only used to get the stacktrace
            mLogger.fine(FrameworkLogger.throwableToString(new RuntimeException("stacktrace")));
            percent = 100;
        }
        if (percent >= getValue()) {
            mLogger.fine("updating");
            setValue(pPercent);
            mProgressString = pProgressString;
        }
        Framework.logExit(Framework.mLogger);
    }

    @Override
    public void started() {
        mStarted = true;
        setVisible(true);
    }

    @Override
    public void finished() {
        mStarted = true;
        mFinished = true;
        setProgress("Finished", 100);
        if (mCompleteAction != null) mCompleteAction.performAction();
    }

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isFinished() {
        return mFinished;
    }

    public String getProgressString() {
        return mProgressString;
    }
}
