/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import com.ownimage.framework.util.Framework;
import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

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
        Framework.log(mLogger, Level.INFO, () -> "Running batch, size=" + pBatch.getBatchSize());
//        transformNoThreads(pBatch, pTransform);
        transformNew(pBatch, pTransform);
//        transformParallelStream(pBatch, pTransform);
//       transformOriginal(pBatch, pTransform);
    }

    public synchronized void transformOriginal(
            @NonNull final TransformResultBatch pBatch,
            @NonNull final IBatchTransform pTransform
    ) {
        Framework.logEntry(mLogger);
        Framework.logValue(mLogger, "mThreadPoolSize", mThreadPoolSize);

        checkThreadPool();
        mForkJoinPool.invoke(new JTPTransformAction(pTransform, pBatch, mThreadBatchSize));

        Framework.logExit(mLogger);
    }

    public synchronized void transformNew(
            @NonNull final TransformResultBatch pBatch,
            @NonNull final IBatchTransform pTransform
    ) {
        class Transformer extends Thread {
            private int mOffset;
            private int mIncrement;

            Transformer(int pOffset, int pIncrement) {
                mOffset = pOffset;
                mIncrement = pIncrement;
            }

            @Override
            public void run() {
                for (int i = mOffset; i < pBatch.getBatchSize(); i += mIncrement) {
                    val result = pBatch.getTransformResult(i);
                    pTransform.transform(result);
                }
            }
        }

        val threads = new ArrayList<Thread>();
        for (int t = 0; t < mThreadPoolSize; t++) {
            val transformer = new Transformer(t, mThreadPoolSize);
            threads.add(transformer);
            transformer.start();
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException pE) {
                Framework.logThrowable(mLogger, Level.SEVERE, pE);
            }
        });
    }

    public synchronized void transformNoThreads(
            @NonNull final TransformResultBatch pBatch,
            @NonNull final IBatchTransform pTransform
    ) {
        for (int i = 0; i < pBatch.getBatchSize(); i++) {
            val result = pBatch.getTransformResult(i);
            pTransform.transform(result);
        }
    }

    public synchronized void transformParallelStream(
            @NonNull final TransformResultBatch pBatch,
            @NonNull final IBatchTransform pTransform
    ) {
        IntStream.range(0, pBatch.getBatchSize()).forEach(i -> {
            val result = pBatch.getTransformResult(i);
            pTransform.transform(result);
        });
    }
}
