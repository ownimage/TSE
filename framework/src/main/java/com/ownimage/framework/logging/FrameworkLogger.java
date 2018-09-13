/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.logging;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.layout.NamedTabs;
import com.ownimage.framework.control.type.ObjectStringMetaType;
import com.ownimage.framework.control.type.ObjectType;
import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.javafx.DialogView;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

public class FrameworkLogger implements IControlChangeListener {// implements IControlChangeListener {

    private static final FrameworkLogger mFrameworkLogger = new FrameworkLogger();


    public final Logger mLogger = Framework.getLogger();

    private final Vector<Logger> mKnownLoggers = new Vector<>();

    private final List<Level> mLogLevels = Arrays.asList( //
            Level.OFF, //
            Level.SEVERE, //
            Level.WARNING, //
            Level.INFO, //
            Level.FINE, //
            Level.FINER, //
            Level.FINEST, //
            Level.ALL //
    );

    private final Container mFileHandlerPropertiesContainer = new Container("File Properties", "fileProperties", NullContainer);
    private final IntegerControl mFileCount = new IntegerControl("File Count", "fileCount", mFileHandlerPropertiesContainer, 1, 1, 10, 1);
    private final IntegerControl mFileSize = new IntegerControl("File Size MB", "fileSize", mFileHandlerPropertiesContainer, 1, 1, 100, 1);
    private final BooleanControl mFileAppend = new BooleanControl("Append", "fileAppend", mFileHandlerPropertiesContainer, false);

    private StringBufferHandler mDialogHandler;
    private ConsoleHandler mConsoleHandler;
    private FileHandler mLogFileHandler;

    private final ObjectControl<Level> mConsoleLevel = //
            new ObjectControl<>("Console Level", "dialogLevel", mFileHandlerPropertiesContainer, Level.INFO, getLogLevels());
    private final ObjectControl<Level> mDialogLevel = //
            new ObjectControl<>("Dialog Level", "dialogLevel", mFileHandlerPropertiesContainer, Level.INFO, getLogLevels());
    private final ObjectControl<Level> mLogFileLevel = //
            new ObjectControl<>("Log File Level", "logFileLevel", mFileHandlerPropertiesContainer, Level.INFO, getLogLevels());

    private FrameworkLogger() {
    }

    public static FrameworkLogger getInstance() {
        return mFrameworkLogger;
    }

    /**
     * Creates a string with the Throwable Message and Stacktrace.
     *
     * @param pThrowable the throwable
     * @return the string
     */
    public static String throwableToString(final Throwable pThrowable) {
        final Writer sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pThrowable.printStackTrace(pw);
        return pThrowable.getMessage() + "\n" + sw.toString();
    }

    public List<Level> getLogLevels() {
        return mLogLevels;
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

                if (pPropertiesFilename != null) {
                    new File(pPropertiesFilename).createNewFile();
                    read(pPropertiesFilename);
                }

            }
        } catch (final Throwable pEx) {
            System.err.println(pEx.getMessage() + " at FrameworkLogger.init(String, String).");
        }
    }

    public void read(final String pFilename) {
        final File file = new File(pFilename);
        read(file);
    }

    public void read(final File pFile) {
        System.out.println("Logging properties file:" + pFile.getAbsolutePath());
        try (final FileInputStream fstream = new FileInputStream(pFile)) {
            final java.util.Properties props = new java.util.Properties();
            props.load(fstream);
            read(props);
        } catch (final IOException pIOE) {
            System.out.println(FrameworkLogger.throwableToString(pIOE));
            throw new FrameworkException(this, Level.SEVERE, "Cannot Open Logging settings from: " + pFile.getAbsolutePath(), pIOE);
        }
    }

    private void read(final java.util.Properties pProperties) {

        final String fileCountString = pProperties.getProperty(mFileCount.getPropertyName(), mFileCount.getString());
        final int fileCountNumber = Integer.parseInt(fileCountString);
        mFileCount.setValue(fileCountNumber);

        final String fileSizeString = pProperties.getProperty(mFileSize.getPropertyName(), mFileSize.getString());
        final int fileSizeNumber = Integer.parseInt(fileSizeString);
        mFileCount.setValue(fileSizeNumber);

        final String fileAppendString = pProperties.getProperty(mFileAppend.getPropertyName(), mFileAppend.getString());
        final boolean fileAppend = Boolean.parseBoolean(fileAppendString);
        mFileAppend.setValue(fileAppend);

        final String consoleLevelString = pProperties.getProperty(mConsoleLevel.getPropertyName(), mConsoleLevel.getString());
        final Level consoleLevel = Level.parse(consoleLevelString);
        mConsoleLevel.setValue(consoleLevel);
        mConsoleHandler.setLevel(consoleLevel);

        final String dialogLevelString = pProperties.getProperty(mDialogLevel.getPropertyName(), mDialogLevel.getString());
        final Level dialogLevel = Level.parse(dialogLevelString);
        mDialogLevel.setValue(dialogLevel);
        mDialogHandler.setLevel(dialogLevel);

        final String logFileLevelString = pProperties.getProperty(mLogFileLevel.getPropertyName(), mLogFileLevel.getString());
        final Level logFileLevel = Level.parse(logFileLevelString);
        mLogFileLevel.setValue(logFileLevel);
        mLogFileHandler.setLevel(logFileLevel);

        final String frameworkLoggerName = mLogger.getName();
        final String frameworkLoggerLevel = pProperties.getProperty(frameworkLoggerName);
        if (isValidLevelString(frameworkLoggerLevel))
            setLevel(frameworkLoggerName, frameworkLoggerLevel);

        pProperties.entrySet().stream()
                .filter(e -> isValidLevelString(e.getValue().toString()))
                .filter(e -> ((String) e.getKey()).contains("."))
                .filter(e -> !frameworkLoggerName.equals(e.getKey()))
                .forEach(e -> setLevel(e.getKey().toString(), e.getValue().toString()));
    }

    private boolean isValidLevelString(final String pLevel) {
        return getLogLevels().stream().anyMatch(l -> l.toString().equals(pLevel));
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
        final Logger logger = Logger.getLogger(pName);
        mKnownLoggers.add(logger);
        logger.setLevel(pLevel);
        mDialogHandler.setLevel(Level.FINEST);
        mConsoleHandler.setLevel(Level.FINEST);
        mConsoleHandler.setFormatter(new PerceptionFormatter());
        mLogger.info(() -> "Logger: " + pName + " level set to: " + pLevel);
    }

    public void setLevel(final String pName, final String pLevel) {
        final Level level = Level.parse(pLevel);
        setLevel(pName, level);
    }

    public void showEditDialog(final IAppControlView mAppView, final String mPrefix, final ActionControl... pButtons) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, mAppView, "mAppView");
        Framework.checkParameterNotNull(mLogger, mPrefix, "mPrefix");

        final Vector<String> loggerNames = new Vector<>();
        final Enumeration<String> names = LogManager.getLogManager().getLoggerNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            if (name != null && name.startsWith(mPrefix)) {
                loggerNames.add(name);
            }
        }
        Collections.sort(loggerNames);

        final Container levels = new Container("Levels", "levels", NullContainer);

        final ObjectStringMetaType loggerNameMetaModel = new ObjectStringMetaType(loggerNames, true, true);
        final ObjectType<String> loggerNameType = new ObjectType<>("", loggerNameMetaModel);
        final ObjectControl<String> loggerName = new ObjectControl<>("Loggers", "loggers", levels, loggerNameType);

        final ObjectControl<Level> logLevel = new ObjectControl<>("Level", "level", levels, Level.INFO, getLogLevels());

        loggerName.addControlChangeListener((o, m) -> {
            final Logger logger = Logger.getLogger(loggerName.getValue());
            final Level level = logger.getLevel();
            if (level != null) {
                logLevel.setValue(level);
            }
        });

        logLevel.addControlChangeListener((c, m) -> {
            final Logger logger = Logger.getLogger(loggerName.getValue());
            final Level level = logger.getLevel();
            if (!level.equals(logLevel.getValue())) {
                setLevel(loggerName.getValue(), logLevel.getValue());
            }
        });

        final NamedTabs tabs = new NamedTabs("Edit Logging", "editLogging");
        tabs.addTab(mFileHandlerPropertiesContainer);
        tabs.addTab(levels);
        tabs.setSelectedIndex(1);

        new ActionControl("Reset All", "resetAll", levels, () -> {
            for (final String name : loggerNames) {
                setLevel(name, logLevel.getValue());
            }
        });

        final Vector<ActionControl> buttons = new Vector<>(Arrays.asList(pButtons));
        buttons.add(ActionControl.create("OK", NullContainer, () -> mLogger.info("FrameworkLogger.showEditDialog() OK pressed")));
        final ActionControl[] buttonsArray = buttons.toArray(new ActionControl[buttons.size()]);

        new DialogView(tabs, DialogOptions.builder().withCompleteFunction(() -> mLogger.info("Dialog closed.")).build(), null, buttonsArray).showModal();
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

    public void write(final File pFile, final String pComment) {
        try (final FileOutputStream fstream = new FileOutputStream(pFile)) {
            final PersistDB props = new PersistDB();
            write(props, "");
            props.store(fstream, pComment);
        } catch (final Exception pEx) {
            throw new FrameworkException(this, Level.SEVERE, "Cannot save Properties to: " + pFile.getAbsolutePath(), pEx);
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
