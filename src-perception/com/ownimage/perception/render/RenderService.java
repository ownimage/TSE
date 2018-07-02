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
		Framework.checkNotNull(mLogger, pPerception, "pPerception");

		mPerception = pPerception;

		mBaseBatchEngine = new BaseBatchEngine();
		mJTPBatchEngine = new JTPBatchEngine(getProperties().getRenderThreadPoolSize(), getProperties().getRenderJTPBatchSize());
		mOpenCLBatchEngine = new OpenCLBatchEngine();

		Framework.logExit(mLogger);
	}

	private IBatchEngine getActualBatchEngine(final IBatchEngine pPreferredBatchEngine) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pPreferredBatchEngine, "pPreferredBatchEngine");

		return (getProperties().useJTP()) ? mJTPBatchEngine : mBaseBatchEngine;
	}

	public IBatchEngine getBaseBatchEngine() {
		return mBaseBatchEngine;
	}

	private synchronized TransformResultBatch getBatch() {
		Framework.logEntry(mLogger);

		if (mBatch == null) {
			int maxBatchSize = getProperties().getRenderBatchSize();
			mLogger.fine(String.format("Creating new TransformResultBatch with maxBatchSize = %d.", maxBatchSize));
			mBatch = new TransformResultBatch(this, maxBatchSize);
		}

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
		Framework.checkNotNull(mLogger, pPictureControl, "pPictureControl");
		Framework.checkNotNull(mLogger, pTransform, "pTransform");

		transform(pPictureControl, pTransform, null);

		Framework.logExit(mLogger);
	}

	public void transform(final PictureControl pPictureControl, final IBatchTransform pTransform, final IAction pCompleteAction) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pPictureControl, "pPictureControl");
		Framework.checkNotNull(mLogger, pTransform, "pTransform");

		ExecuteQueue eq = ExecuteQueue.getInstance();
		String name = pTransform.getClass().getSimpleName();
		mLogger.info("transform " + pPictureControl + " " + name);

		class TransformJob extends Job {

			public TransformJob(final String pName, final Priority pPriority, final Object pControlObject) {
				super(pName, pPriority, pControlObject);
			}

			@Override
			public void doJob() {
				super.doJob();

				PictureType picture = pPictureControl.getValue().createCompatible();

				mJTPBatchEngine.setThreadPoolSize(
						getProperties().getRenderThreadPoolSize(), getProperties().getRenderJTPBatchSize());

				IBatchEngine preferredBatchEngine = pTransform.getPreferredBatchEngine();
				IBatchEngine actualEngine = getActualBatchEngine(preferredBatchEngine);

				int maxBatchSize = getProperties().getRenderBatchSize();
				TransformResultBatch batch = getBatch();
				batch.initialize(picture, actualEngine, maxBatchSize);

				while (batch.hasNext()) {
					batch.next();
					transform(batch, pTransform);
					batch.render(picture);
				}

				pPictureControl.setValue(picture);

				if (pCompleteAction != null) {
					pCompleteAction.performAction();
				}
			}

			@Override
			public void terminate() {
				// currently dont support terminate
			}

		}

		TransformJob job = new TransformJob(name, Priority.NORMAL, pPictureControl);
		job.submit();

		Framework.logExit(mLogger);
	}

	synchronized void transform(final TransformResultBatch pBatch, final IBatchTransform pTransform) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pBatch, "pBatch");
		Framework.checkNotNull(mLogger, pTransform, "pTransform");

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
