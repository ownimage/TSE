package com.ownimage.framework.view.javafx;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;

public class ApplicationEventQueue {

    public final static Logger mLogger = Framework.getLogger();

    private BlockingQueue<IAction> mUIEventQueue = new LinkedBlockingQueue<>();

    private static ApplicationEventQueue mInstance = new ApplicationEventQueue();

    public ApplicationEventQueue() {
        startEventQueue();
    }

    public static ApplicationEventQueue getInstance() {
        return mInstance;
    }

    public void queueEvent(IAction pAction) {
        mUIEventQueue.offer(pAction);
        mLogger.severe(() -> String.format("######## mUIEventQueue.size() = %s", mUIEventQueue.size()));
    }

    private void processEventQueue() {
        while (true) {
            try {
                IAction action = mUIEventQueue.poll(10, TimeUnit.SECONDS);
                if (action != null) {
                    action.performAction();
                } else {
                    mLogger.fine(() -> "is alive: " + Thread.currentThread());
                }
            } catch (Throwable e) {
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
