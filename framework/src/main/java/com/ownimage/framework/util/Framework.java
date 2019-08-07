/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import com.ownimage.framework.logging.FrameworkLogger;
import lombok.NonNull;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Framework {

    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public static void checkParameterGreaterThan(
            @NonNull final Logger pLogger,
            final int pA,
            final int pB,
            @NonNull final String pName
    ) {
        if (!(pA > pB)) {
            checkParameterMessage(pLogger, "Parameter ERROR: %s is %s BUT needs to be greater than %s.", pName, pA, pB);
        }
    }

    public static void checkParameterGreaterThanEqual(
            @NonNull final Logger pLogger,
            final int pA,
            final int pB,
            @NonNull final String pName
    ) {
        if (!(pA >= pB)) {
            checkParameterMessage(pLogger, "Parameter ERROR: %s is %s BUT needs to be greater than or equal to  %s.", pName, pA, pB);
        }
    }

    public static void checkParameterLessThan(
            @NonNull final Logger pLogger,
            final int pA,
            final int pB,
            @NonNull final String pName
    ) {
        if (!(pA < pB)) {
            checkParameterMessage(pLogger, "Parameter ERROR: %s is %s BUT needs to be less than %s.", pName, pA, pB);
        }
    }

    public static void checkParameterLessThanEqual(
            @NonNull final Logger pLogger,
            final int pA,
            final int pB,
            @NonNull final String pName
    ) {
        if (!(pA <= pB)) {
            checkParameterMessage(pLogger, "Parameter ERROR: %s is %s BUT needs to be less than or equal to  %s.", pName, pA, pB);
        }
    }

    private static void checkParameterMessage(
            @NonNull final Logger pLogger,
            @NonNull final String pMessage,
            @NonNull final String pName,
            final int pA,
            final int pB
    ) {
        final String msg = String.format(pMessage, pName, pA, pB);
        pLogger.log(Level.SEVERE, msg);
        throw new IllegalArgumentException(msg);
    }

    public static void checkParameterNotNullOrEmpty(
            @NonNull final Logger pLogger,
            @NonNull final String pString,
            @NonNull final String pName
    ) {
        if ("".equals(pString)) {
            final String message = pName + " must not be an empty string.";
            pLogger.warning(message);
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkStateNotNull(
            @NonNull final Logger pLogger,
            @NonNull final Object pObject,
            @NonNull final String pName
    ) {
        if (pObject == null) {
            final String message = pName + " must not be null";
            pLogger.warning(message);
            throw new IllegalStateException(message);
        }
    }

    public static void checkStateNoChangeOnceSet(
            final Logger pLogger,
            final Object pObject,
            final String pName
    ) {
        if (pObject != null) {
            final String message = pName + " cannot be changed once set";
            pLogger.warning(message);
            throw new IllegalStateException(message);
        }
    }

    public static Logger getLogger() {
        try {
            final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            final StackTraceElement stackTraceElement = stackTraceElements[2];
            final String fdClassName = stackTraceElement.getClassName();
            return Logger.getLogger(fdClassName);
        } catch (final Throwable pT) {
            mLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
        }
        return Logger.getLogger("");
    }

    public static void log(
            @NonNull final Logger pLogger,
            @NonNull final Level pLevel,
            @NonNull final Supplier<String> pMessageSupplier
    ) {
        if (pLogger.isLoggable(pLevel)) {
            pLogger.log(pLevel, pMessageSupplier.get());
        }
    }

    public static void logEntry(@NonNull final Logger pLogger) {
        try {
            if (pLogger.isLoggable(Level.FINEST)) {
                final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                final StackTraceElement stackTraceElement = stackTraceElements[2];
                final String methodName = stackTraceElement.getMethodName();
                final String fdClassName = stackTraceElement.getClassName();
                final String[] names = fdClassName.split("\\.");
                final String className = names[names.length - 1];
                pLogger.log(Level.FINEST, className + ":" + methodName + " entered.");
            }
        } catch (final Throwable pT) {
            pLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
        }
    }

    public static void logExit(@NonNull final Logger pLogger) {
        try {
            if (pLogger.isLoggable(Level.FINEST)) {
                final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                final StackTraceElement stackTraceElement = stackTraceElements[2];
                final String methodName = stackTraceElement.getMethodName();
                final String fdClassName = stackTraceElement.getClassName();
                final String[] names = fdClassName.split("\\.");
                final String className = names[names.length - 1];
                pLogger.log(Level.FINEST, className + ":" + methodName + " exited.");
            }
        } catch (final Throwable pT) {
            pLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
        }
    }

    public static void logExit(@NonNull final Logger pLogger, final Object pValue) {
        try {
            if (pLogger.isLoggable(Level.FINE)) {
                final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                final StackTraceElement stackTraceElement = stackTraceElements[2];
                final String methodName = stackTraceElement.getMethodName();
                final String fdClassName = stackTraceElement.getClassName();
                final String[] names = fdClassName.split("\\.");
                final String className = names[names.length - 1];
                pLogger.log(Level.FINE, className + ":" + methodName + " exited with " + pValue + ".");
            }
        } catch (final Throwable pT) {
            pLogger.log(Level.SEVERE, "UNEXPECTED ERROR", pT);
        }
    }

    public static void logParams(
            @NonNull final Logger pLogger,
            @NonNull final String pParamNames,
            final Object... pParams
    ) {
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

    public static void logThrowable(
            @NonNull final Logger pLogger,
            @NonNull final Level pLevel,
            @NonNull final Throwable pThrowable
    ) {
        pLogger.log(pLevel, FrameworkLogger.throwableToString(pThrowable));
    }

    public static void logValue(
            @NonNull final Logger pLogger,
            @NonNull final String pName,
            @NonNull final Object pValue
    ) {
        log(pLogger, Level.FINE, () -> pName + ": " + pValue);
    }
}
