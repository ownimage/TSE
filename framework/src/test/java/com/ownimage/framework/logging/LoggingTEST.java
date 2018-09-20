/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.logging;

import com.ownimage.framework.util.Framework;
import org.junit.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LoggingTEST {


    private final static Logger mLogger = Framework.getLogger();

    private final String testMessage = "this is a message";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FrameworkLogger.getInstance().init("logging.proerties", "junit.log");
        FrameworkLogger.getInstance().clearLog();
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    @Test
    public void exception_TEST01() {
        final String message = "UI message";
        try {
            throw new FrameworkException(this, Level.WARNING, message, null);
        } catch (final Throwable pT) {
            testLog(message);
        }

    }

    @Test
    public void exception_TEST02() {
        final String UIMessage = "UI message";
        final String causedByMessage = "something went wrong";
        try {
            throw new RuntimeException(causedByMessage);
        } catch (final Throwable pT) {
            new FrameworkException(this, Level.WARNING, UIMessage, pT);
            final String log = FrameworkLogger.getInstance().getLog();
            testLog(UIMessage);
            testLog(causedByMessage);
            testLog("com.ownimage.framework.logging.LoggingTEST");
        }

    }

    // if a problem is identified where there does not need to be a raw exception thrown
    @Test
    public void exception_TEST03() {
        final String UIMessage = "UI message";
        try {
            throw new FrameworkException(this, Level.WARNING, UIMessage);
        } catch (final Throwable pT) {
            final String log = FrameworkLogger.getInstance().getLog();
            testLog(UIMessage);
            testLog("com.ownimage.framework.logging.LoggingTEST");
            testLog(" at ");
        }
    }

    private void log(final Level pLevel, final String pMessage) {
        mLogger.log(pLevel, pMessage);
    }

    // check logging levels
    @Test
    public void logging_checkLevelFINE_TEST() {

        FrameworkLogger.getInstance().setLevel(Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINER);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.INFO);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINE, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.WARNING);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINE, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.SEVERE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINE, testMessage);
        testNoLog(testMessage);

    }

    // check logging levels
    @Test
    public void logging_checkLevelFINER_TEST() {

        FrameworkLogger.getInstance().setLevel(Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINER, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINER);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINER, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINER, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.INFO);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINER, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.WARNING);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINER, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.SEVERE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINER, testMessage);
        testNoLog(testMessage);

    }

    // check logging levels
    @Test
    public void logging_checkLevelFINEST_TEST() {

        FrameworkLogger.getInstance().setLevel(Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINEST, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINER);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINEST, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINEST, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.INFO);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINEST, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.WARNING);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINEST, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.SEVERE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.FINEST, testMessage);
        testNoLog(testMessage);

    }

    // check logging levels
    @Test
    public void logging_checkLevelINFO_TEST() {

        FrameworkLogger.getInstance().setLevel(Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
        log(Level.INFO, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINER);
        FrameworkLogger.getInstance().clearLog();
        log(Level.INFO, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.INFO, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.INFO);
        FrameworkLogger.getInstance().clearLog();
        log(Level.INFO, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.WARNING);
        FrameworkLogger.getInstance().clearLog();
        log(Level.INFO, testMessage);
        testNoLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.SEVERE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.INFO, testMessage);
        testNoLog(testMessage);

    }

    // check logging levels
    @Test
    public void logging_checkLevelSEVERE_TEST() {

        FrameworkLogger.getInstance().setLevel(Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
        log(Level.SEVERE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINER);
        FrameworkLogger.getInstance().clearLog();
        log(Level.SEVERE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.SEVERE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.INFO);
        FrameworkLogger.getInstance().clearLog();
        log(Level.SEVERE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.WARNING);
        FrameworkLogger.getInstance().clearLog();
        log(Level.SEVERE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.SEVERE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.SEVERE, testMessage);
        testLog(testMessage);

    }

    // check logging levels
    @Test
    public void logging_checkLevelWARNING_TEST() {

        FrameworkLogger.getInstance().setLevel(Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
        log(Level.WARNING, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINER);
        FrameworkLogger.getInstance().clearLog();
        log(Level.WARNING, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.FINE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.WARNING, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.INFO);
        FrameworkLogger.getInstance().clearLog();
        log(Level.WARNING, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.WARNING);
        FrameworkLogger.getInstance().clearLog();
        log(Level.WARNING, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().setLevel(Level.SEVERE);
        FrameworkLogger.getInstance().clearLog();
        log(Level.WARNING, testMessage);
        testNoLog(testMessage);
    }

    // log a message and collect it from the logging framework
    @Test
    public void logging_TEST01() {
        log(Level.SEVERE, testMessage);
        testLog(testMessage);
    }

    // check that clearLog works
    @Test
    public void logging_TEST02() {
        log(Level.SEVERE, testMessage);
        testLog(testMessage);

        FrameworkLogger.getInstance().clearLog();
        final String log = FrameworkLogger.getInstance().getLog();
        assertTrue(log == null || log.length() == 0);
    }

    @After
    public void setUpAfter() {
        FrameworkLogger.getInstance().clearLog();
    }

    @Before
    public void setUpBefore() throws Exception {
        FrameworkLogger.getInstance().clearLog();
        FrameworkLogger.getInstance().setLevel(Level.WARNING);
    }

    private void testLog(final String pMessage) {
        final String log = FrameworkLogger.getInstance().getLog();
        if (log == null || log.length() == 0 || !log.contains(pMessage)) {
            fail();
        }
    }

    private void testNoLog(final String pMessage) {
        final String log = FrameworkLogger.getInstance().getLog();
        if (log == null || log.length() == 0 || !log.contains(pMessage)) {
            ;
        } else {
            fail();
        }
    }

    // 03 logParams
    // 03 enter
    // 03 exit
    //
    // 05 throwableToString
    // 05 stacktraceToString
    // 07 getLoggerNames
    //
    // 10 read
    // 10 write

}
