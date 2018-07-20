/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.logging;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.layout.NamedTabs;
import com.ownimage.framework.control.type.ObjectStringMetaType;
import com.ownimage.framework.control.type.ObjectType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IAppControlView.DialogOptions;

public class FrameworkLogger implements IControlChangeListener {// implements IControlChangeListener {

	private static final FrameworkLogger mFrameworkLogger = new FrameworkLogger();

	public final Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final String mClassname = FrameworkLogger.class.getName();
	public final Logger mLogger = Logger.getLogger(mClassname);

	private final Vector<Logger> mKnownLoggers = new Vector<>();

	public final Level[] LOGLEVELS = { //
			Level.OFF, //
			Level.SEVERE, //
			Level.WARNING, //
			Level.INFO, //
			Level.FINE, //
			Level.FINER, //
			Level.FINEST, //
			Level.ALL //
	};

	private final Container mFileHandlerPropertiesContainer = new Container("File Properties", "fileProperties", NullContainer);
	private final IntegerControl mFileCount = new IntegerControl("File Count", "fileCount", mFileHandlerPropertiesContainer, 1, 1, 10, 1);
	private final IntegerControl mFileSize = new IntegerControl("File Size MB", "fileSize", mFileHandlerPropertiesContainer, 1, 1, 100, 1);
	private final BooleanControl mFileAppend = new BooleanControl("Append", "fileAppend", mFileHandlerPropertiesContainer, false);

	private StringBufferHandler mDialogHandler;
	private ConsoleHandler mConsoleHandler;
	private FileHandler mLogFileHandler;

	private final ObjectControl<Level> mConsoleLevel = //
			new ObjectControl<>("Console Level", "dialogLevel", mFileHandlerPropertiesContainer, LOGLEVELS[3], Arrays.asList(LOGLEVELS));
	private final ObjectControl<Level> mDialogLevel = //
			new ObjectControl<>("Dialog Level", "dialogLevel", mFileHandlerPropertiesContainer, LOGLEVELS[3], Arrays.asList(LOGLEVELS));
	private final ObjectControl<Level> mLogFileLevel = //
			new ObjectControl<>("Log File Level", "logFileLevel", mFileHandlerPropertiesContainer, LOGLEVELS[3], Arrays.asList(LOGLEVELS));

	private FrameworkLogger() {
	}

	public static FrameworkLogger getInstance() {
		return mFrameworkLogger;
	}

	/**
	 * Creates a string with the Throwable Message and Stacktrace.
	 * 
	 * @param pThrowable
	 *            the throwable
	 * @return the string
	 */
	public static String throwableToString(final Throwable pThrowable) {
		final Writer sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		pThrowable.printStackTrace(pw);
		return pThrowable.getMessage() + "\n" + sw.toString();
	}

	public synchronized void clearLog() {
		mDialogHandler.clearLog();
	}

	@Override
	public void controlChangeEvent(final Object pControl, final boolean pIsMutating) {
		if (pControl == mConsoleLevel) {
			mConsoleHandler.setLevel(mConsoleLevel.getValue());

		} else if (pControl == mLogFileLevel) {
			mLogFileHandler.setLevel(mLogFileLevel.getValue());

		} else if (pControl == mDialogLevel) {
			mDialogHandler.setLevel(mDialogLevel.getValue());

		}
	}

	public synchronized String getLog() {
		return mDialogHandler.getLog();
	}

	public synchronized void init(final String pPropertiesFilename, final String pLogFilename) {
		try {
			if (mDialogHandler == null) {
				mDialogHandler = new StringBufferHandler();
				mDialogHandler.setFormatter(new PerceptionFormatter());

				mConsoleHandler = new ConsoleHandler();
				mConsoleHandler.setFormatter(new PerceptionFormatter());

				new File(pLogFilename).createNewFile();
				mLogFileHandler = new FileHandler(pLogFilename);
				mLogFileHandler.setFormatter(new PerceptionFormatter());

				mDialogLevel.addControlChangeListener(mFrameworkLogger);
				mLogFileLevel.addControlChangeListener(mFrameworkLogger);
				mConsoleLevel.addControlChangeListener(mFrameworkLogger);

				final Logger logger = Logger.getLogger("");

				final Handler[] handlers = logger.getHandlers();
				for (final Handler handler : handlers) {
					logger.removeHandler(handler);
				}

				logger.addHandler(mDialogHandler);
				logger.addHandler(mConsoleHandler);
				logger.addHandler(mLogFileHandler);

				setLevel(Level.INFO);

				java.util.Properties logProperties = new java.util.Properties();
				new File(pPropertiesFilename).createNewFile();
				logProperties.load(new FileInputStream(pPropertiesFilename));
				read(logProperties, "");

			}
		} catch (Throwable pEx) {
			System.err.println(pEx.getMessage() + " at FrameworkLogger.init(String, String).");
		}
	}

	public void read(final java.util.Properties pProperties, final String pString) {

		String fileCountString = pProperties.getProperty(mFileCount.getPropertyName(), mFileCount.getString());
		int fileCountNumber = Integer.parseInt(fileCountString);
		mFileCount.setValue(fileCountNumber);

		String fileSizeString = pProperties.getProperty(mFileSize.getPropertyName(), mFileSize.getString());
		int fileSizeNumber = Integer.parseInt(fileSizeString);
		mFileCount.setValue(fileSizeNumber);

		String fileAppendString = pProperties.getProperty(mFileAppend.getPropertyName(), mFileAppend.getString());
		boolean fileAppend = Boolean.parseBoolean(fileAppendString);
		mFileAppend.setValue(fileAppend);

		String consoleLevelString = pProperties.getProperty(mConsoleLevel.getPropertyName(), mConsoleLevel.getString());
		Level consoleLevel = Level.parse(consoleLevelString);
		mConsoleLevel.setValue(consoleLevel);
		mConsoleHandler.setLevel(consoleLevel);

		String dialogLevelString = pProperties.getProperty(mDialogLevel.getPropertyName(), mDialogLevel.getString());
		Level dialogLevel = Level.parse(dialogLevelString);
		mDialogLevel.setValue(dialogLevel);
		mDialogHandler.setLevel(dialogLevel);

		String logFileLevelString = pProperties.getProperty(mLogFileLevel.getPropertyName(), mLogFileLevel.getString());
		Level logFileLevel = Level.parse(logFileLevelString);
		mLogFileLevel.setValue(logFileLevel);
		mLogFileHandler.setLevel(logFileLevel);

		for (final Object property : pProperties.keySet()) {
			final String loggerName = (String) property;
			final String level = pProperties.getProperty(loggerName);
			setLevel(loggerName, level);
		}
	}

	public void setLevel(final Level pLevel) {
		final Logger logger = Logger.getLogger("com.ownimage");
		logger.setLevel(pLevel);
	}

	public void setLevel(final String pLevel) {
		final Level level = Level.parse(pLevel);
		setLevel(level);
	}

	public void setLevel(final String pName, final Level pLevel) {
		System.out.println("FrameworkLogger.setLevel " + pName + " " + pLevel.getName());
		final Logger logger = Logger.getLogger(pName);
		mKnownLoggers.add(logger);
		logger.setLevel(pLevel);
		mDialogHandler.setLevel(Level.FINEST);
		mConsoleHandler.setLevel(Level.FINEST);
		mConsoleHandler.setFormatter(new PerceptionFormatter());
		logger.log(pLevel, "Logger: " + pName + " level set to: " + pLevel);
	}

	public void setLevel(final String pName, final String pLevel) {
		final Level level = Level.parse(pLevel);
		setLevel(pName, level);
	}

	public void showEditDialog(final IAppControlView mAppView, final String mPrefix) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, mAppView, "mAppView");
		Framework.checkParameterNotNull(mLogger, mPrefix, "mPrefix");

		Vector<String> loggerNames = new Vector<String>();
		Enumeration<String> names = LogManager.getLogManager().getLoggerNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name != null && name.startsWith(mPrefix)) {
				loggerNames.add(name);
			}
		}
		Collections.sort(loggerNames);

		Container levels = new Container("Levels", "levels", NullContainer);

		ObjectStringMetaType loggerNameMetaModel = new ObjectStringMetaType(loggerNames, true, true);
		ObjectType<String> loggerNameType = new ObjectType<>("", loggerNameMetaModel);
		ObjectControl<String> loggerName = new ObjectControl<>("Loggers", "loggers", levels, loggerNameType);

		ObjectControl<Level> logLevel = new ObjectControl<>("Level", "level", levels, LOGLEVELS[3], Arrays.asList(LOGLEVELS));

		loggerName.addControlChangeListener((o, m) -> {
			String name = ((ObjectControl<String>) o).getString();
			Logger logger = Logger.getLogger(name);
			Level level = logger.getLevel();
			if (level != null) {
				logLevel.setValue(level);
			}
		});

		NamedTabs tabs = new NamedTabs("Edit Logging", "editLogging");
		tabs.addTab(mFileHandlerPropertiesContainer);
		tabs.addTab(levels);

		new ActionControl("Set", "set", levels, () -> setLevel(loggerName.getValue(), logLevel.getValue()));
		new ActionControl("Reset All", "resetAll", levels, () -> {
			for (String name : loggerNames) {
				setLevel(name, logLevel.getValue());
			}
		});

		ActionControl ok = ActionControl.create("OK", NullContainer, () -> mLogger.info("FrameworkLogger.showEditDialog() OK pressed"));

		mAppView.showDialog(tabs, DialogOptions.NONE, ok);

		Framework.logExit(mLogger);
	}

	public void warning(final Object pFrom, final String pUIMessage, final Exception pEx) {
		warning(pFrom.getClass().getName(), pUIMessage, pEx);
	}

	// used when called from a context
	public void warning(final String pLoggerName, final String pUIMessage, final Exception pEx) {
		final Logger logger = Logger.getLogger(pLoggerName);
		logger.warning(pUIMessage);
		if (logger.isLoggable(Level.WARNING)) {
			logger.warning(FrameworkLogger.throwableToString(pEx));
		}
	}

	public void write(final java.util.Properties pProperties, final String pPrefix) {
		pProperties.setProperty(pPrefix + mFileCount.getPropertyName(), mFileCount.getString());
		pProperties.setProperty(pPrefix + mFileSize.getPropertyName(), mFileSize.getString());
		pProperties.setProperty(pPrefix + mFileAppend.getPropertyName(), mFileAppend.getString());
		pProperties.setProperty(pPrefix + mConsoleLevel.getPropertyName(), mConsoleLevel.getString());
		pProperties.setProperty(pPrefix + mDialogLevel.getPropertyName(), mDialogLevel.getString());
		pProperties.setProperty(pPrefix + mLogFileLevel.getPropertyName(), mLogFileLevel.getString());

		final Enumeration<String> names = LogManager.getLogManager().getLoggerNames();
		while (names.hasMoreElements()) {
			final String name = names.nextElement();
			if (name != null && !name.equals("") && name.startsWith("com")) {
				Level level = Logger.getLogger(name).getLevel();
				level = level != null ? level : Level.INFO;
				pProperties.setProperty(pPrefix + name, level.toString());
			}
		}
	}
}
