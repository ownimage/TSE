package com.ownimage.framework.view.javafx;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IRawUIEventListener;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.event.IUIEvent;

public class UIEventQueue {

    public final static Logger mLogger = Framework.getLogger();

    private class EventItem {
        private IUIEvent mEvent;
        private IRawUIEventListener mListener;

        public EventItem(final IUIEvent mEvent, final IRawUIEventListener mListener) {
            this.mEvent = mEvent;
            this.mListener = mListener;
        }

        public IUIEvent getEvent() {
            return mEvent;
        }

        public IRawUIEventListener getListener() {
            return mListener;
        }
    }

    private BlockingQueue<EventItem> mUIEventQueue = new LinkedBlockingQueue();

    private static UIEventQueue mInstance = new UIEventQueue();

    public UIEventQueue() {
        startEventQueue();
    }

    public static UIEventQueue getInstance() {
        return mInstance;
    }

    public void queueEvent(final IUIEvent pEvent, IRawUIEventListener pListener) {
        mUIEventQueue.offer(new EventItem(pEvent, pListener));
    }

    private void processMouseEventQueue() {
        while (true) {
            try {
                LocalTime start = LocalTime.now();
                EventItem eventItem = mUIEventQueue.poll(10, TimeUnit.SECONDS);
                if (eventItem != null) {
                    eventItem.getListener().uiEvent(eventItem.getEvent());
                    LocalTime end = LocalTime.now();
                    long elapsed = ChronoUnit.MICROS.between(start, end);
                    System.out.println( String.format("%s took %s ms", eventItem.getEvent().getEventType(), elapsed));
                } else {
                    mLogger.fine(() -> "is alive: " + Thread.currentThread());
                }
            } catch (Throwable e) {
                // do nothing handled by loop retry
            }
        }
    }

    private void startEventQueue() {
        new Thread(this::processMouseEventQueue, "UIEventQueue processor").start();
    }

    public int getQueueSize() {
        return mUIEventQueue.size();
    }
}
