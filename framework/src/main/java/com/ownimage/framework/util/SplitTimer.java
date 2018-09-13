/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.util.Date;
import java.util.logging.Logger;

public class SplitTimer {

    public final static Logger mLogger = Framework.getLogger();

    private static Date mPrevious = new Date();

    public static void split(final String pMessage) {
        final Date now = new Date();
        final float miliseconds = now.getTime() - mPrevious.getTime();
        mLogger.info(() -> miliseconds + " " + now + " " + pMessage);
        mPrevious = now;
    }

}
