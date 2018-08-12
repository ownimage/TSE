/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class FrameworkException extends RuntimeException {


    private final Level mLevel;

	public FrameworkException(final Object pSource, final Level pLevel, final String pUIText) {
		this(pSource, pLevel, pUIText, null);
	}

	public FrameworkException(final Object pSource, final Level pLevel, final String pUIText, final Throwable pCausedBy) {
		super(pUIText, pCausedBy);

		mLevel = pLevel;

		final String loggerName = pSource.getClass().getName(); // TODO need to fill the null case here
		final Logger logger = Logger.getLogger(loggerName);
		final LogRecord record = new LogRecord(pLevel, pUIText);
		record.setLoggerName(loggerName);
		record.setThrown(pCausedBy != null ? pCausedBy : this);
		logger.log(record);
	}

	public Level getLevel() {
		return mLevel;
	}

}
