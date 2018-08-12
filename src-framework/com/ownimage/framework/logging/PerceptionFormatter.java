/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */

package com.ownimage.framework.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

class PerceptionFormatter extends SimpleFormatter {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
    public final static Logger mLogger = Framework.getLogger();

	public final static int CLASSNAME_LENGTH = 80;

	private final Date mDate = new Date();
	private final SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss.SSS");

	@Override
	public synchronized String format(final LogRecord record) {
		final Thread thread = Thread.currentThread();
		final String threadId = thread.getName() == null || thread.getName().length() == 0 ? String.valueOf(thread.getId()) : thread.getName();

		mDate.setTime(record.getMillis());

		final String message = formatMessage(record);

		String source = record.getLoggerName();

		if (source.length() > CLASSNAME_LENGTH) {
			source = "..." + source.substring(source.length() + 3 - CLASSNAME_LENGTH);
		}

		final String oneLine = String.format("%-20s %-8s %-100s %s", "[" + threadId + "] ", record.getLevel(), mDateFormatter.format(mDate) + " " + source, message) + "\n";
		if (record.getThrown() == null) {
			return oneLine;
		} else {
			final String thrownInfo = FrameworkLogger.throwableToString(record.getThrown());
			return oneLine + thrownInfo;
		}

	}
}
