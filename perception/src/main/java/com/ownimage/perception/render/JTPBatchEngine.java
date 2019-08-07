/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import com.ownimage.framework.util.Framework;
import lombok.NonNull;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JTPBatchEngine extends BaseBatchEngine {


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    private int mThreadPoolSize = -1;

    private int mThreadBatchSize;
    private ForkJoinPool mForkJoinPool;

    public JTPBatchEngine(final int pThreadPoolSize, final int pThreadBatchSize) {
        mThreadPoolSize = pThreadPoolSize;
        mThreadBatchSize = pThreadBatchSize;

    }

    private void checkThreadPool() {
        Framework.logEntry(mLogger);

        if (mForkJoinPool == null || mForkJoinPool.getParallelism() != mThreadPoolSize) {

            if (mForkJoinPool != null) {
                mLogger.fine("Shutting down pool");
                mForkJoinPool.shutdown();
                boolean terminated = false;
                while (!terminated) {
                    try {
                        terminated = mForkJoinPool.awaitTermination(10, TimeUnit.MILLISECONDS);
                        mLogger.severe("Still waiting");
                    } catch (final InterruptedException e) {
                        mLogger.log(Level.SEVERE, "Waiting", e);
                    }
                }
            }
            mLogger.fine("Creating new pool");
            mForkJoinPool = new ForkJoinPool(mThreadPoolSize);
        }

        Framework.logExit(mLogger);
    }

    @Override
    public synchronized void next(final TransformResultBatch pBatch, final int pOverSample) {
        super.next(pBatch, pOverSample);
    }

    public void setThreadPoolSize(final int pThreadPoolSize, final int pThreadBatchSize) {
        mThreadPoolSize = pThreadPoolSize;
        mThreadBatchSize = pThreadBatchSize;
    }

    @Override
    public synchronized void transform(
            @NonNull final TransformResultBatch pBatch,
            @NonNull final IBatchTransform pTransform
    ) {
        Framework.logEntry(mLogger);
        Framework.logValue(mLogger, "mThreadPoolSize", mThreadPoolSize);

        checkThreadPool();
        mForkJoinPool.invoke(new JTPTransformAction(pTransform, pBatch, mThreadBatchSize));

        Framework.logExit(mLogger);
    }

}
