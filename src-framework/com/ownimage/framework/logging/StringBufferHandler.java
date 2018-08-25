/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class StringBufferHandler extends Handler {


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