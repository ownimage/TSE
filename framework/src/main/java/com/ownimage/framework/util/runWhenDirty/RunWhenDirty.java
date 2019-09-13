package com.ownimage.framework.util.runWhenDirty;

public class RunWhenDirty implements IRunWhenDirty {

    private final Runnable mClean;
    private final boolean mUseThread;
    private boolean mDirty;
    private Thread mRunningThread;

    public RunWhenDirty(final Runnable pClean, final boolean pUseThread) {
        mClean = pClean;
        mUseThread = pUseThread;
    }

    @Override
    public void markDirty() {
        if (mUseThread) markDirtyNewThread();
        else markDirtyThisThread();
    }

    @Override
    public boolean isClean() {
        return !isDirty();
    }

    @Override
    public boolean isDirty() {
        return mDirty || mRunningThread != null;
    }

    public void markDirtyThisThread() {
        if (shouldStart()) {
            do {
                mClean.run(); // should stay on same thread
            } while (shouldContinue());
        } else {
        }
    }

    public synchronized void markDirtyNewThread() {
        if (mRunningThread == null) {
            Thread thread = new Thread(this::markDirtyThisThread);
            mRunningThread = thread;
            thread.start();
        } else {
            mDirty = true;
        }
    }

    private synchronized boolean shouldStart() {
        if (mRunningThread == null || mRunningThread == Thread.currentThread()) { // this thread needs to run a clean loom
            mRunningThread = Thread.currentThread();
            return true;
        }
        if (mRunningThread != Thread.currentThread()) {
            mDirty = true; // another thread keeping this clean
            return false;
        }
        if (mDirty) {
            return false;
        }
        return true;
    }

    private synchronized boolean shouldStart2() {
        if (mDirty) {
            return false;
        }
        if (mRunningThread == null) { // this thread needs to run a clean loom
            mRunningThread = Thread.currentThread();
            return true;
        }
        if (mRunningThread != Thread.currentThread()) {
            System.out.println("shouldStart mRunningThread != Thread.currentThread() - false");
            mDirty = true; // another thread keeping this clean
            return false;
        }
        return true;
    }

    private synchronized boolean shouldContinue() {
        if (mDirty) {
            mDirty = false;
            return true;
        }
        mRunningThread = null;
        return false;
    }

}
