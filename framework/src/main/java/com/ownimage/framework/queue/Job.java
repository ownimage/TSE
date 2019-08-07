/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.queue;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import lombok.NonNull;

import java.util.Date;
import java.util.logging.Logger;

/**
 * The abstract Class Job. Subclasses of this must implement the doJob and suspend methods of the IJob interface.
 */
public class Job implements IJob {


    private final static Logger mLogger = Framework.getLogger();

    /**
     * The Name.
     */
    private final String mName;

    /**
     * The Control object.
     */
    private final Object mControlObject;

    /**
     * The Created date field records the time that the job was created.
     */
    private final Date mCreatedDate;

    /**
     * The Priority of the job. This is set when the job is submitted, i.e. runBackground(...), this is because if the job is
     * runImmediate the priority is irrelevant.
     */
    private final Priority mPriority;

    /**
     * The Status of the Job.
     */
    private Status mStatus;

    /**
     *
     */
    private Runnable mDo;

    /**
     * Instantiates a new job.
     *
     * @param pName the name
     */
    public Job(final String pName, final Priority pPriority) {
        this(pName, pPriority, null);
    }

    /**
     * Instantiates a new job with a control object.
     *
     * @param pName          the name
     * @param pControlObject the control object
     */
    public Job(
            @NonNull String pName,
            @NonNull final Priority pPriority,
            final Object pControlObject
    ) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNullOrEmpty(mLogger, pName, "pName");

        mName = pName;
        mPriority = pPriority;
        mControlObject = pControlObject;
        mCreatedDate = new Date();
        setStatus(Status.CREATED);

        Framework.logExit(mLogger);
    }

    public Job(@NonNull final String pName, @NonNull final Priority pPriority, final Runnable pDo) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNullOrEmpty(mLogger, pName, "pName");

        mName = pName;
        mPriority = pPriority;
        mControlObject = null;
        mDo = pDo;
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
    public synchronized void cancel() {
        Framework.logEntry(mLogger);

        if (getStatus() != Status.QUEUED) {
            throw new IllegalStateException("Can only cancel a QUEUED job, this job  is " + getStatus());
        }
        setStatus(Status.CANCELLED);

        Framework.logExit(mLogger);
    }

    @Override
    public synchronized void complete() {
        Framework.logEntry(mLogger);

        if (getStatus() != Status.RUNNING) {
            throw new IllegalStateException("Can only complete a RUNNING job, this job  is " + getStatus());
        }
        setStatus(Status.COMPLETE);

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
        if (mDo != null) {
            try {
                mDo.run();
            } catch (Throwable pT) {
                mLogger.severe(FrameworkLogger.throwableToString(pT));
                setStatus(Status.COMPLETE);
            } finally {
                setStatus(Status.FAILED);
            }
        }

    }

    @Override
    public void error(final Throwable pThrowable) {
        Framework.logEntry(mLogger);

        if (getStatus() != Status.RUNNING) {
            throw new IllegalStateException("Can only error a RUNNING job, this job  is " + getStatus());
        }
        setStatus(Status.FAILED);

        Framework.logExit(mLogger);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.util.IJob#getControlObject()
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
     * @see com.ownimage.framework.util.IJob#getName()
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
    public synchronized Status getStatus() {
        return mStatus;
    }

    /**
     * Sets the status.
     *
     * @param pStatus the new status
     */
    private void setStatus(final Status pStatus) {
        mStatus = pStatus;
    }

    /**
     * Submits the job to the ExecuteQueue to be run in the background with the specified priority. This will call the ExecuteQueues
     * runBackground which control the interaction with other running jobs.
     */
    public synchronized void submit() {
        Framework.logEntry(mLogger);

        ExecuteQueue.getInstance().submit(this);

        Framework.logExit(mLogger);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.queue.IJob#terminateJob(com.ownimage.perception.queue.IJob)
     */
    @Override
    public synchronized void terminate() {
        Framework.logEntry(mLogger);

        setStatus(Status.TERMINATED);

        Framework.logExit(mLogger);
    }

    @Override
    public synchronized void queued() {
        if (getStatus() != IJob.Status.CREATED) {
            throw new IllegalStateException("Can only submit a CREATED job, this job  is " + getStatus());
        }

        setStatus(Status.QUEUED);
    }
}
