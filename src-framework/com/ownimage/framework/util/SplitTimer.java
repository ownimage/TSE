package com.ownimage.framework.util;

import java.util.Date;

public class SplitTimer {

	private static Date mPrevious = new Date();

	public static void split(final String pMessage) {
		Date now = new Date();
		float miliseconds = now.getTime() - mPrevious.getTime();
		System.out.println(miliseconds + " " + now + " " + pMessage);
		mPrevious = now;
	}

}
