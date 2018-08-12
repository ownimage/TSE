/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class StringBufferHandler extends Handler {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	@SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

	StringBuffer mStringBuffer = new StringBuffer();
	Formatter mFormatter = new PerceptionFormatter();

	@Override
	public void publish(LogRecord pRecord) {
		mStringBuffer.append(mFormatter.format(pRecord));
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

	public String getLog() {
		return mStringBuffer.toString();
	}

	public void clearLog() {
		mStringBuffer = new StringBuffer();
	}

}