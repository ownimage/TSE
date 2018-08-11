package com.ownimage.perception.render;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.queue.ExecuteQueue;
import com.ownimage.framework.queue.IJob.Priority;
import com.ownimage.framework.queue.Job;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Properties;

public class RenderService {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = RenderService.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final Perception mPerception;

	private TransformResultBatch mBatch;

	private final IBatchEngine mBaseBatchEngine;
	private final JTPBatchEngine mJTPBatchEngine;
	private final IBatchEngine mOpenCLBatchEngine;

	public RenderService(final Perception pPerception) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pPerception, "pPerception");

		mPerception = pPerception;

		mBaseBatchEngine = new BaseBatchEngine();
		mJTPBatchEngine = new JTPBatchEngine(getProperties().getRenderThreadPoolSize(), getProperties().getRenderJTPBatchSize());
		mOpenCLBatchEngine = new OpenCLBatchEngine();

		Framework.logExit(mLogger);
	}

	private IBatchEngine getActualBatchEngine(final IBatchEngine pPreferredBatchEngine) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pPreferredBatchEngine, "pPreferredBatchEngine");

		return (getProperties().useJTP()) ? mJTPBatchEngine : mBaseBatchEngine;
	}

	public IBatchEngine getBaseBatchEngine() {
		return mBaseBatchEngine;
	}

    private synchronized TransformResultBatch getBatch(String pName) {
		Framework.logEntry(mLogger);

		if (mBatch == null) {
			int maxBatchSize = getProperties().getRenderBatchSize();
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
		return mPerception.getProperties();
	}

	/**
	 * This is the public entry point into the RenderService.
	 *
	 * @param pPictureControl
	 *            the picture
	 * @param pTransform
	 *            the transform
	 */
	public void transform(final PictureControl pPictureControl, final IBatchTransform pTransform) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pPictureControl, "pPictureControl");
		Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");

		transform(pPictureControl, pTransform, null);

		Framework.logExit(mLogger);
	}

    public void transform(final PictureControl pPictureControl, final IBatchTransform pTransform, final IAction pCompleteAction) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pPictureControl, "pPictureControl");
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");

        PictureType picture = pPictureControl.getValue().createCompatible();
        transform(picture, pTransform, () -> {
            pPictureControl.setValue(picture);
            if (pCompleteAction != null) {
                pCompleteAction.performAction();
            }
        }, 1);

        Framework.logExit(mLogger);
    }

    public void transform(final PictureType pPictureType, final IBatchTransform pTransform, final IAction pCompleteAction, int pOverSample) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pPictureType, "pPictureControl");
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");
        Framework.checkParameterGreaterThanEqual(mLogger, pOverSample, 1, "pOverSample should be between 1 and 4");
        Framework.checkParameterLessThanEqual(mLogger, pOverSample, 4, "pOverSample should be between 1 and 4");
        mLogger.severe(() -> "RenderService.transform for:" + pTransform.getDisplayName());

        ExecuteQueue eq = ExecuteQueue.getInstance();
        String name = pTransform.getClass().getSimpleName();

        class TransformJob extends Job {

            public TransformJob(final String pName, final Priority pPriority, final Object pControlObject) {
                super(pName, pPriority, pControlObject);
            }

            @Override
            public void doJob() {
                super.doJob();

                mJTPBatchEngine.setThreadPoolSize(
                        getProperties().getRenderThreadPoolSize(), getProperties().getRenderJTPBatchSize());

                IBatchEngine preferredBatchEngine = pTransform.getPreferredBatchEngine();
                IBatchEngine actualEngine = getActualBatchEngine(preferredBatchEngine);

                int maxBatchSize = getProperties().getRenderBatchSize();
                TransformResultBatch batch = getBatch(pTransform.getDisplayName());
                batch.initialize(pPictureType, actualEngine, maxBatchSize);

                while (batch.hasNext()) {
                    batch.next(pOverSample);
                    transform(batch, pTransform);
                    batch.render(pPictureType, pOverSample);
                }

                if (pCompleteAction != null) {
                    pCompleteAction.performAction();
                }
            }

            @Override
            public void terminate() {
                // currently dont support terminate
            }

        }

        TransformJob job = new TransformJob(name, Priority.NORMAL, pPictureType);
        job.submit();

        Framework.logExit(mLogger);
    }

	synchronized void transform(final TransformResultBatch pBatch, final IBatchTransform pTransform) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pBatch, "pBatch");
		Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");

		mLogger.info("transform pBatch " + pTransform.getClass().getSimpleName());

		IBatchEngine preferredBatchEngine = pTransform.getPreferredBatchEngine();
		IBatchEngine actualEngine = getActualBatchEngine(preferredBatchEngine);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		long start = cal.getTimeInMillis();

		if (pTransform.getUseTransform()) {
			actualEngine.transform(pBatch, pTransform);
		}

		cal.setTime(new Date());
		long end = cal.getTimeInMillis();

		// System.out.println(String.format("Transform=%s, batchSise=%d, millisecs=%d", pTransform.getDisplayName(),
		// pBatch.getBatchSize(), (end - start)));

		IBatchTransform previousTransform = pTransform.getPreviousTransform();
		if (previousTransform != null) {
			transform(pBatch, previousTransform);
		}

		Framework.logExit(mLogger);
	}

}
