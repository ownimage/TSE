/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.queue;

import java.util.PriorityQueue;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

/**
 * The Class ExecuteQueue.
 * 
 */
public class ExecuteQueue {

	public final static Version mVersion = new Version(5, 0, 0, "2015/053/19 08:02");
	private final static Logger mLogger = Logger.getLogger(ExecuteQueue.class.getName());

	private static ExecuteQueue mExecuteQueue = new ExecuteQueue();

	/** The job queue. */
	private final PriorityQueue<IJob> mQueue;

	/** The running ExecuteThread. */
	private ExecuteThread mRunningThread;

	/**
	 * Instantiates a new execute queue.
	 */
	private ExecuteQueue() {
		mQueue = new PriorityQueue<IJob>((pJob1, pJob2) -> {
			// Note: this comparator imposes orderings that are inconsistent with equals.
			int priorityOrder = pJob1.getPriority().ordinal() - pJob2.getPriority().ordinal();
			if (priorityOrder != 0) { return priorityOrder; }

			if (pJob1.getCreateDate().equals(pJob2.getCreateDate())) { return 0; }

			int dateOrder = pJob1.getCreateDate().before(pJob2.getCreateDate()) ? 1 : -1;
			return dateOrder;
		});
	};

	public static ExecuteQueue getInstance() {
		return mExecuteQueue;
	}

	/**
	 * Gets the depth.
	 * 
	 * @return the depth
	 */
	public int getDepth() {
		return mQueue.size();
	}

	/**
	 * Run next.
	 */
	void runNext() {
		synchronized (mQueue) {
			// if something is already running then we are done
			ExecuteThread runningThread = mRunningThread;

			if (runningThread != Thread.currentThread()) {
				if (runningThread != null && runningThread.isAlive()) { return; }
			}

			// get the head of the queue
			final IJob job = mQueue.size() != 0 ? mQueue.remove() : null;

			// if the queue is empty we are done
			if (job == null) { return; }

			// kick off new thread
			mRunningThread = new ExecuteThread(job);
			mRunningThread.start();
		}
	}

	/**
	 * Submit.
	 * 
	 * @param pJob
	 *            the job
	 */
	public void submit(final IJob pJob) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pJob, "pJob");

		synchronized (mQueue) {
			ExecuteThread runningThread = mRunningThread;
			if (runningThread != null && runningThread.getJob().getControlObject() != null && runningThread.getJob().getControlObject() == pJob.getControlObject()) {
				runningThread.getJob().terminate();
			}

			final Vector<IJob> remove = new Vector<IJob>();

			for (final IJob job : mQueue) {
				if (job.getControlObject() == pJob.getControlObject()) {
					job.cancel();
					remove.add(job);
				}
			}

			mQueue.removeAll(remove);
			mQueue.add(pJob);
			runNext();
		}

		Framework.logExit(mLogger);
	}

}
