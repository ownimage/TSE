/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.util.logging.Logger;

/**
 * This is a counter that can be used by forEach style methods to track progress.
 */
public class Counter {

    private static Logger mLogger = Framework.getLogger();

    private int mCount;
    private int mMax;

    public Counter() {
    }

    public Counter(int pMax) {
        Framework.checkParameterGreaterThan(mLogger, pMax, 0, "pMax");
        mMax = pMax;
    }

    public synchronized Counter increase() {
        mCount++;
        return this;
    }

    public int getCount() {
        return mCount;
    }

    public float getPercent() {
        if (mMax == 0) throw new RuntimeException("Cannot get percentage for Counter when max value has not been set");
        float percent = 100.0f * mCount / mMax;
        return percent > 100.0f ? 100.0f : percent;
    }

    public int getPercentInt() {
        return (int) getPercent();
    }

}
