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
public class Counter implements IMaxCounter {

    private static final Logger mLogger = Framework.getLogger();

    private int mCount;
    private int mMax;

    private Counter() {
    }

    private Counter(final int pMax) {
        Framework.checkParameterGreaterThan(mLogger, pMax, 0, "pMax");
        mMax = pMax;
    }

    public static ICounter createCounter() {
        return new Counter();
    }

    public static IMaxCounter createMaxCounter(final int pMax) {
        return new Counter(pMax);
    }

    @Override
    public synchronized Counter increase() {
        mCount++;
        return this;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public float getPercent() {
        if (mMax == 0) throw new RuntimeException("Cannot get percentage for Counter when max value has not been set");
        final float percent = 100.0f * mCount / mMax;
        return percent > 100.0f ? 100.0f : percent;
    }

    @Override
    public int getPercentInt() {
        return (int) getPercent();
    }

}
