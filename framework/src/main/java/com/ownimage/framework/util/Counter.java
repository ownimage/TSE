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

    private static final Logger logger = Framework.getLogger();

    private int count;
    private int max;

    private Counter() {
    }

    private Counter(final int max) {
        Framework.checkParameterGreaterThan(logger, max, 0, "max");
        this.max = max;
    }

    public static ICounter createCounter() {
        return new Counter();
    }

    public static IMaxCounter createMaxCounter(int max) {
        return new Counter(max != 0 ? max : 1);
    }

    @Override
    public synchronized Counter increase() {
        count++;
        return this;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public float getPercent() {
        if (max == 0) throw new RuntimeException("Cannot get percentage for Counter when max value has not been set");
        final float percent = 100.0f * count / max;
        return Math.min(percent, 100.0f);
    }

    @Override
    public int getPercentInt() {
        return (int) getPercent();
    }

}
