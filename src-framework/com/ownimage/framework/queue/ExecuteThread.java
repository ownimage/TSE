package com.ownimage.framework.queue;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.queue.IJob.Status;
import com.ownimage.framework.util.Framework;

public class ExecuteThread extends Thread {


    private final static Logger mLogger = Framework.getLogger();

	/** The job. */
	private final IJob mJob;

	/**
	 * Instantiates a new execute thread. When the thread is started the job is run on the background thread. When the job completes
	 * the this class calls complete() on the job. If there is an exception thrown during the processing of the doJob method then
	 * the exception is passed to the error method of the job.
	 * 
	 * @param pJob
	 *            the job
	 */
	public ExecuteThread(final IJob pJob) {
		super(pJob.getName());
		mJob = pJob;
	}

	/**
	 * Gets the job.
	 *
	 * @return the job
	 */
	public IJob getJob() {
		return mJob;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Framework.logEntry(mLogger);

		try {
			mJob.doJob();
			mLogger.finest("mJob.doJob() - DONE");

			if (mJob.getStatus() == Status.RUNNING) {
				mJob.complete();
				mLogger.finest("mJob.complete() - DONE");
			}

			if (mJob.getStatus() == Status.TERMINATED) {
				mLogger.finest("TERMINATED");
			}

		} catch (Throwable pThrowable) {
			mJob.error(pThrowable);
			mLogger.finest("mJob.error() - DONE");
			Framework.logThrowable(mLogger, Level.SEVERE, pThrowable);
		} finally {
			ExecuteQueue.getInstance().runNext();
			mLogger.finest("ExecuteQueue.getInstance().runNext() - DONE");

			Framework.logExit(mLogger);
		}
	}

}