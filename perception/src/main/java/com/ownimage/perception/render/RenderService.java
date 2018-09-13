/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.queue.ExecuteQueue;
import com.ownimage.framework.queue.IJob.Priority;
import com.ownimage.framework.queue.Job;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;

public class RenderService {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private TransformResultBatch mBatch;

    private final IBatchEngine mBaseBatchEngine;
    private final JTPBatchEngine mJTPBatchEngine;
    private final IBatchEngine mOpenCLBatchEngine;

    public class RenderJob {
        private String mReason;
        private PictureType mPictureType;
        private PictureControl mPictureControl;
        private IBatchTransform mTransform;
        private Object mControlObject;
        private IAction mCompleteAction;
        private IProgressObserver mObserver;
        private int mOverSample = 1;
        private boolean mAllowTerminate = true;

        private RenderJob() {
        }

        private RenderJob(final RenderJob pFrom) {
            mReason = pFrom.mReason;
            mPictureType = pFrom.mPictureType;
            mPictureControl = pFrom.mPictureControl;
            mTransform = pFrom.mTransform;
            mControlObject = pFrom.mControlObject;
            mCompleteAction = pFrom.mCompleteAction;
            mObserver = pFrom.mObserver;
            mOverSample = pFrom.mOverSample;
            mAllowTerminate = pFrom.mAllowTerminate;
        }

        public void run() {
            Framework.logEntry(mLogger);
            // note slightly forced use of checkParmeter below
            Framework.checkParameterNotNullOrEmpty(Framework.mLogger, mReason, "mReason");
            Framework.checkParameterGreaterThanEqual(mLogger, mOverSample, 1, "pOverSample");
            Framework.checkParameterLessThanEqual(mLogger, mOverSample, 4, "pOverSample");
            if (mPictureControl == null && mPictureType == null)
                throw new IllegalStateException("mPictureType and mPictureControl must not both be null");
            if (mPictureControl != null && mPictureType != null)
                throw new IllegalStateException("mPictureType and mPictureControl must not both be non null");

            mLogger.info(() -> String.format("RenderService::transform pReason=\"%s\", pTransform=%s", mReason, mTransform.getDisplayName()));

            final ExecuteQueue eq = ExecuteQueue.getInstance();

            class TransformJob extends Job {
                private boolean mTerminated = false;

                private TransformJob(final String pName, final Priority pPriority, final Object pControlObject) {
                    super(pName, pPriority, pControlObject);
                }

                @Override
                public void doJob() {
                    super.doJob();

                    if (mObserver != null) mObserver.started();
                    mJTPBatchEngine.setThreadPoolSize(
                            getProperties().getRenderThreadPoolSize(), getProperties().getRenderJTPBatchSize());

                    final IBatchEngine preferredBatchEngine = mTransform.getPreferredBatchEngine();
                    final IBatchEngine actualEngine = getActualBatchEngine(preferredBatchEngine);

                    final int maxBatchSize = getProperties().getRenderBatchSize();
                    final TransformResultBatch batch = getBatch(mTransform.getDisplayName());
                    final PictureType pictureType = mPictureType != null ? mPictureType : mPictureControl.getValue().createCompatible();
                    batch.initialize(pictureType, actualEngine, maxBatchSize);

                    while (batch.hasNext() && !mTerminated) {
                        if (mObserver != null) mObserver.setProgress("Transforming", batch.getPercentComplete());
                        batch.next(mOverSample);
                        transform(batch, mTransform);
                        batch.render(pictureType, mOverSample);
                    }

                    if (mPictureType == null) mPictureControl.setValue(pictureType);
                    if (mCompleteAction != null && !mTerminated) mCompleteAction.performAction();
                    if (mObserver != null) mObserver.finished();
                }

                @Override
                public void terminate() {
                    if (mAllowTerminate) mTerminated = true;
                    super.terminate();
                }
            }

            final TransformJob job = new TransformJob(mReason, Priority.NORMAL, mControlObject);
            job.submit();

            Framework.logExit(mLogger);
        }
    }

    public class RenderJobBuilder {

        private final RenderJob mRenderJob = new RenderJob();

        public RenderJobBuilder(final String pReason, final PictureControl pPictureControl, final IBatchTransform pTransform) {
            mRenderJob.mReason = pReason;
            mRenderJob.mPictureControl = pPictureControl;
            mRenderJob.mTransform = pTransform;
        }

        public RenderJobBuilder(final String pReason, final PictureType pPictureType, final IBatchTransform pTransform) {
            mRenderJob.mReason = pReason;
            mRenderJob.mPictureType = pPictureType;
            mRenderJob.mTransform = pTransform;
        }

        public RenderJobBuilder withControlObject(final Object pControlObject) {
            mRenderJob.mControlObject = pControlObject;
            return this;
        }

        public RenderJobBuilder withCompleteAction(final IAction pCompleteAction) {
            mRenderJob.mCompleteAction = pCompleteAction;
            return this;
        }

        public RenderJobBuilder withProgressObserver(final IProgressObserver pObserver) {
            mRenderJob.mObserver = pObserver;
            return this;
        }

        public RenderJobBuilder withOverSample(final int pOverSample) {
            mRenderJob.mOverSample = pOverSample;
            return this;
        }

        public RenderJobBuilder withAllowTerminate(final boolean pAllowTerminate) {
            mRenderJob.mAllowTerminate = pAllowTerminate;
            return this;
        }

        public RenderJob build() {
            return new RenderJob(mRenderJob);
        }
    }

    public RenderService() {
        Framework.logEntry(mLogger);

        mBaseBatchEngine = new BaseBatchEngine();
        mJTPBatchEngine = new JTPBatchEngine(getProperties().getRenderThreadPoolSize(), getProperties().getRenderJTPBatchSize());
        mOpenCLBatchEngine = new OpenCLBatchEngine();

        Framework.logExit(mLogger);
    }

    public RenderJobBuilder getRenderJobBuilder(final String pReason, final PictureControl pPictureControl, final IBatchTransform pTransform) {
        return new RenderJobBuilder(pReason, pPictureControl, pTransform).withControlObject(pPictureControl);
    }

    public RenderJobBuilder getRenderJobBuilder(final String pReason, final PictureType pPictureType, final IBatchTransform pTransform) {
        return new RenderJobBuilder(pReason, pPictureType, pTransform).withControlObject(pPictureType);
    }

    private IBatchEngine getActualBatchEngine(final IBatchEngine pPreferredBatchEngine) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pPreferredBatchEngine, "pPreferredBatchEngine");

        return (getProperties().useJTP()) ? mJTPBatchEngine : mBaseBatchEngine;
    }

    public IBatchEngine getBaseBatchEngine() {
        return mBaseBatchEngine;
    }

    private synchronized TransformResultBatch getBatch(final String pName) {
        Framework.logEntry(mLogger);

        if (mBatch == null) {
            final int maxBatchSize = getProperties().getRenderBatchSize();
            mLogger.fine(String.format("Creating new TransformResultBatch with maxBatchSize = %d.", maxBatchSize));
            mBatch = new TransformResultBatch(this, maxBatchSize);
        }
        mBatch.setName(pName);

        Framework.logExit(mLogger);
        return mBatch;
    }

    public IBatchEngine getJTPBatchEngine() {
        return mJTPBatchEngine;
    }

    public IBatchEngine getOpenCLBatchEngine() {
        return mOpenCLBatchEngine;
    }

    private Properties getProperties() {
        return Services.getServices().getProperties();
    }

    private synchronized void transform(final TransformResultBatch pBatch, final IBatchTransform pTransform) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pBatch, "pBatch");
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");
        mLogger.fine("transform pBatch " + pTransform.getClass().getSimpleName());

        final IBatchEngine preferredBatchEngine = pTransform.getPreferredBatchEngine();
        final IBatchEngine actualEngine = getActualBatchEngine(preferredBatchEngine);

        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        final long start = cal.getTimeInMillis();

        if (pTransform.getUseTransform()) {
            actualEngine.transform(pBatch, pTransform);
        }

        cal.setTime(new Date());
        final long end = cal.getTimeInMillis();

        mLogger.fine(() -> String.format("Transform=%s, batchSise=%d, millisecs=%d", pTransform.getDisplayName(), pBatch.getBatchSize(), (end - start)));

        final IBatchTransform previousTransform = pTransform.getPreviousTransform();
        if (previousTransform != null) {
            transform(pBatch, previousTransform);
        }

        Framework.logExit(mLogger);
    }

}
