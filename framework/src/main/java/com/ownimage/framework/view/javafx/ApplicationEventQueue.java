/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ApplicationEventQueue {

    public final static Logger mLogger = Framework.getLogger();

    private final BlockingQueue<IAction> mUIEventQueue = new LinkedBlockingQueue<>();

    private static final ApplicationEventQueue mInstance = new ApplicationEventQueue();

    public ApplicationEventQueue() {
        startEventQueue();
    }

    public static ApplicationEventQueue getInstance() {
        return mInstance;
    }

    public void queueEvent(final IAction pAction) {
        mUIEventQueue.offer(pAction);
        mLogger.fine(() -> String.format("#adding mUIEventQueue.size() = %s", mUIEventQueue.size()));
    }

    private void processEventQueue() {
        while (true) {
            try {
                final IAction action = mUIEventQueue.poll(10, TimeUnit.SECONDS);
                if (action != null) {
                    mLogger.fine(() -> String.format("removing mUIEventQueue.size() = %s", mUIEventQueue.size()));
                    action.performAction();
                } else {
                    mLogger.fine(() -> "is alive: " + Thread.currentThread());
                }
            } catch (final Throwable e) {
                mLogger.info("Queue generatedAction");
                mLogger.fine(() -> FrameworkLogger.throwableToString(e));
            }
        }
    }

    private void startEventQueue() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != null) threadGroup = threadGroup.getParent();
        threadGroup = new ThreadGroup(threadGroup, "ApplicationEventQueue");
        new Thread(threadGroup, this::processEventQueue, "ApplicationEventQueue thread").start();
    }

    public int getQueueSize() {
        return mUIEventQueue.size();
    }
}
