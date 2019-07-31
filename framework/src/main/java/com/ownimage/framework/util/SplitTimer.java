/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import lombok.val;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

public class SplitTimer {

    public final static Logger mLogger = Framework.getLogger();
    private static final SplitTimer mInstance = new SplitTimer();

    private Instant mPrevious = Instant.now();

    public static void split(final String pMessage) {
        val milliseconds = mInstance.split();
        mLogger.info(() -> String.format("SplitTimer: %s %s", milliseconds, pMessage));
    }

    public long split() {
        val now = Instant.now();
        val duration = Duration.between(mPrevious, now);
        mPrevious = now;
        return duration.toMillis();
    }

}


