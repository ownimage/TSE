/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import com.ownimage.framework.util.Framework;

import java.util.logging.Logger;

public class IntegerMetaType implements IMetaType<Integer> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final int mMin;
    private final int mMax;
    private final int mStep;

    public IntegerMetaType(final int pMin, final int pMax, final int pStep) {
        mMin = pMin;
        mMax = pMax;
        mStep = pStep;
    }

    public int getMax() {
        return mMax;
    }

    public int getMin() {
        return mMin;
    }

    public int getStep() {
        return mStep;
    }

    /**
     * Gets the normalized value.
     *
     * @param pNormalizedValue the value does NOT need to be constrained between 0.0d and 1.0d
     * @return the normalized value
     */
    public Integer getValueForNormalizedValue(final double pNormalizedValue) {
        final Integer value = getMin() + (int) ((getMax() - getMin()) * pNormalizedValue);
        return value;
    }

    @Override
    public boolean isValid(final Integer pValue) {
        return getMin() <= pValue && pValue <= getMax();
    }
}
