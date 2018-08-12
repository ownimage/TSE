package com.ownimage.framework.util;

import java.util.Date;
import java.util.logging.Logger;

public class SplitTimer {

	public final static Logger mLogger = Framework.getLogger();

	private static Date mPrevious = new Date();

	public static void split(final String pMessage) {
		Date now = new Date();
		float miliseconds = now.getTime() - mPrevious.getTime();
		mLogger.info(() -> miliseconds + " " + now + " " + pMessage);
		mPrevious = now;
	}

}
