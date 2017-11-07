/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013, 2015, 2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.queue;

import java.util.Date;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

/**
 * The abstract Class Job. Subclasses of this must implement the doJob and suspend methods of the IJob interface.
 */
public class Job implements IJob {

	public final static Version mVersion = new Version(5, 0, 0, "2015/053/19 08:02");
	private final static Logger mLogger = Framework.getLogger();

	/** The Name. */
	private final String mName;

	/** The Control object. */
	private final Object mControlObject;

	/** The Created date field records the time that the job was created. */
	private final Date mCreatedDate;

	/**
	 * The Priority of the job. This is set when the job is submitted, i.e. runBackground(...), this is because if the job is
	 * runImmediate the priority is irrelevant.
	 */
	private final Priority mPriority;

	/** The Status of the Job. */
	private Status mStatus;

	/**
	 * Instantiates a new job.
	 * 
	 * @param pName
	 *            the name
	 * 
	 */
	public Job(final String pName, final Priority pPriority) {
		this(pName, pPriority, null);
	}

	/**
	 * Instantiates a new job with a control object.
	 * 
	 * @param pName
	 *            the name
	 * @param pControlObject
	 *            the control object
	 */
	public Job(final String pName, final Priority pPriority, final Object pControlObject) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pPriority, "pPriority");
		Framework.checkNotNullOrEmpty(mLogger, pName, "pName");

		mName = pName;
		mPriority = pPriority;
		mControlObject = pControlObject;
		mCreatedDate = new Date();
		setStatus(Status.CREATED);

		Framework.logExit(mLogger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.framework.queue.IJob#cancel()
	 */
	@Override
	public void cancel() {
		Framework.logEntry(mLogger);

		synchronized (mStatus) {
			if (getStatus() != Status.QUEUED) { throw new IllegalStateException("Can only cancel a QUEUED job, this job  is " + getStatus()); }
			setStatus(Status.CANCELLED);
		}

		Framework.logExit(mLogger);
	}

	@Override
	public void complete() {
		Framework.logEntry(mLogger);

		synchronized (mStatus) {
			if (getStatus() != Status.RUNNING) { throw new IllegalStateException("Can only complete a RUNNING job, this job  is " + getStatus()); }
			setStatus(Status.COMPLETE);
		}

		Framework.logExit(mLogger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.queue.IJob#doJob()
	 */
	@Override
	public void doJob() {
		setStatus(Status.RUNNING);
		// do work
		// setStatus(Status.COMPLETE);
		// Only setStatus(Status.RUNNING); is left in so that subclassess can call super.doJob()
	}

	@Override
	public void error(final Throwable pThrowable) {
		Framework.logEntry(mLogger);

		synchronized (mStatus) {
			if (getStatus() != Status.RUNNING) { throw new IllegalStateException("Can only error a RUNNING job, this job  is " + getStatus()); }
			setStatus(Status.FAILED);
		}

		Framework.logExit(mLogger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.util.IJob#getControlObject()
	 */
	@Override
	public Object getControlObject() {
		return mControlObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.queue.IJob#getCreateDate()
	 */
	@Override
	public Date getCreateDate() {
		return mCreatedDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.util.IJob#getName()
	 */
	@Override
	public String getName() {
		return mName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.queue.IJob#getPriority()
	 */
	@Override
	public Priority getPriority() {
		return mPriority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.queue.IJob#getPercent()
	 */
	@Override
	public double getProgressPercent() {
		return 50.0d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.framework.queue.IJob#getProgressString()
	 */
	@Override
	public String getProgressString() {
		// TODO Auto-generated method stub
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.queue.IJob#getStatus()
	 */
	@Override
	public Status getStatus() {
		return mStatus;
	}

	/**
	 * Sets the status.
	 * 
	 * @param pStatus
	 *            the new status
	 */
	private void setStatus(final Status pStatus) {
		mStatus = pStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.queue.IJob#runBackground(int)
	 */
	@Override
	public void submit() {
		Framework.logEntry(mLogger);

		synchronized (mStatus) {
			if (getStatus() != Status.CREATED) { throw new IllegalStateException("Can only submit a CREATED job, this job  is " + getStatus()); }
			setStatus(Status.QUEUED);
			ExecuteQueue.getInstance().submit(this);
		}

		Framework.logExit(mLogger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.queue.IJob#terminateJob(com.ownimage.perception.queue.IJob)
	 */
	@Override
	public void terminate() {
		Framework.logEntry(mLogger);

		synchronized (mStatus) {
			if (getStatus() != Status.RUNNING) { throw new IllegalStateException("Can only terminate a RUNNING job, this job  is " + getStatus()); }
			setStatus(Status.TERMINATED);
		}

		Framework.logExit(mLogger);
	}

}
