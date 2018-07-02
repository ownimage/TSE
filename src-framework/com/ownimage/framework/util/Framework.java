package com.ownimage.framework.util;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.logging.FrameworkLogger;

public class Framework {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = Framework.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	public static void checkGreaterThan(final Logger pLogger, final int pA, final int pB, final String pMessage) {
		Framework.checkNotNull(pLogger, pMessage, "pMessage");
		if (!(pA > pB)) {
			checkMessage(pLogger, pMessage, pA, pB);
		}
	}

	public static void checkGreaterThanEqual(final Logger pLogger, final int pA, final int pB, final String pMessage) {
		Framework.checkNotNull(pLogger, pMessage, "pMessage");
		if (!(pA >= pB)) {
			checkMessage(pLogger, pMessage, pA, pB);
		}
	}

	public static void checkLessThan(final Logger pLogger, final int pA, final int pB, final String pMessage) {
		Framework.checkNotNull(pLogger, pMessage, "pMessage");
		if (!(pA < pB)) {
			checkMessage(pLogger, pMessage, pA, pB);
		}
	}

	public static void checkLessThanEqual(final Logger pLogger, final int pA, final int pB, final String pMessage) {
		Framework.checkNotNull(pLogger, pMessage, "pMessage");
		if (!(pA <= pB)) {
			checkMessage(pLogger, pMessage, pA, pB);
		}
	}

	private static void checkMessage(final Logger pLogger, final String pMessage, final int pA, final int pB) {
		Framework.logValue(pLogger, "pA", pA);
		Framework.logValue(pLogger, "pB", pB);
		String msg = String.format(pMessage, pA, pB);
		pLogger.log(Level.SEVERE, msg);
		throw new IllegalArgumentException(msg);
	}

	public static void checkNotNull(final Logger pLogger, final Object pObject, final String pName) {
		if (pObject == null) {
			String message = pName + " must not be null";
			mLogger.warning(message);
			throw new IllegalArgumentException(message);
		}
	}

	public static void checkNotNullOrEmpty(final Logger pLogger, final String pString, final String pName) {
		checkNotNull(pLogger, pString, pName);
		if ("".equals(pString)) {
			String message = pName + " must not be an empty string.";
			mLogger.warning(message);
			throw new IllegalArgumentException(message);
		}
	}

	public static void checkNull(final Logger pLogger, final Object pObject, final String pName) {
		if (pObject != null) {
			String message = pName + " must be null";
			mLogger.warning(message);
			throw new IllegalArgumentException(message);
		}
	}

	public static void checkNoChangeOnceSet(final Logger pLogger, final Object pObject, final String pName) {
		if (pObject != null) {
			String message = pName + " cannot be changed once set";
			mLogger.warning(message);
			throw new IllegalArgumentException(message);
		}
	}
	public static Logger getLogger() {
		try {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			StackTraceElement stackTraceElement = stackTraceElements[2];
			String fdClassName = stackTraceElement.getClassName();
			return Logger.getLogger(fdClassName);
		} catch (Throwable pT) {
			mLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
		}
		return Logger.getLogger("");
	}

	public static void log(final Logger pLogger, final Level pLevel, final Supplier<String> pMessageSupplier) {
		if (pLogger.isLoggable(pLevel)) {
			pLogger.log(pLevel, pMessageSupplier.get());
		}
	}

	public static void logEntry(final Logger pLogger) {
		try {
			if (pLogger.isLoggable(Level.FINE)) {
				StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
				StackTraceElement stackTraceElement = stackTraceElements[2];
				String methodName = stackTraceElement.getMethodName();
				String fdClassName = stackTraceElement.getClassName();
				String[] names = fdClassName.split("\\.");
				String className = names[names.length - 1];
				pLogger.log(Level.FINE, className + ":" + methodName + " entered.");
			}
		} catch (Throwable pT) {
			mLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
		}
	}

	public static void logExit(final Logger pLogger) {
		try {
			if (pLogger.isLoggable(Level.FINE)) {
				StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
				StackTraceElement stackTraceElement = stackTraceElements[2];
				String methodName = stackTraceElement.getMethodName();
				String fdClassName = stackTraceElement.getClassName();
				String[] names = fdClassName.split("\\.");
				String className = names[names.length - 1];
				pLogger.log(Level.FINE, className + ":" + methodName + " exited.");
			}
		} catch (Throwable pT) {
			mLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
		}
	}

	public static void logExit(final Logger pLogger, final Object pValue) {
		try {
			if (pLogger.isLoggable(Level.FINE)) {
				StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
				StackTraceElement stackTraceElement = stackTraceElements[2];
				String methodName = stackTraceElement.getMethodName();
				String fdClassName = stackTraceElement.getClassName();
				String[] names = fdClassName.split("\\.");
				String className = names[names.length - 1];
				pLogger.log(Level.FINE, className + ":" + methodName + " exited with " + pValue + ".");
			}
		} catch (Throwable pT) {
			mLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
		}
	}

	public static void logParams(final Logger pLogger, final String pParamNames, final Object... pParams) {
		if (pLogger.isLoggable(Level.FINEST)) {
			final String[] paramNames = pParamNames.split(",");
			if (paramNames.length != pParams.length) {
				pLogger.severe("Mismatch. " + paramNames.length + " parameter names have been provided, but " + pParams.length + " values are given.");
			}
			for (int index = 0; index < pParams.length; index++) {
				pLogger.finest(paramNames[index].trim() + ": " + pParams[index]);
			}
		}

	}

	public static void logThrowable(final Logger pLogger, final Level pLevel, final Throwable pThrowable) {
		mLogger.log(pLevel, FrameworkLogger.throwableToString(pThrowable));
	}

	public static void logValue(final Logger pLogger, final String pName, final Object pValue) {
		log(pLogger, Level.FINE, () -> pName + ": " + pValue);
	}
}
