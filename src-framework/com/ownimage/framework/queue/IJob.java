/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013, 2015, 2015 ownimage.com, Keith Hart
 */

package com.ownimage.framework.queue;

import java.util.Date;

import com.ownimage.framework.util.Version;

/**
 * The Interface IJob represents an activity that is going to be run by the {@link ExecuteQueue}. These jobs are typically very
 * parallel in nature and the {@link ExecuteQueue} ensures that only one of these jobs is running at a time to prevent the system
 * from being overwhelmed and also prevents contention issues between multiple processes.
 */
public interface IJob {

	/**
	 * The Priority of the Job.
	 */
	public enum Priority {
		HIGHEST, HIGH, NORMAL, LOW, LOWEST
	}

	/**
	 * The Status for the Job. Note there is no setStatus, it is up to the implementation of the Job to keep track of this so that
	 * it can be reported through the getStatus method. When it is created it will be CREATED, once submitted to the ExecuteQueue
	 * (by invoking submit) it will be QUEUED. When it comes to the front of the queue the ExecuteQueue will call doJob on it and
	 * the status will change to RUNNING. If the job runs to completion then the status will change to COMPLETE. Whilst the job is
	 * RUNNING the ExecuteQueue can invoke suspend which should cause the job to halt in a timely fashion in such a manner that a
	 * subsequent call to doJob can cause it to run to completion, the status should change back to QUEUED. When the job is either
	 * QUEUED or RUNNING then terminateJob can be called which will cause the job to halt in a timely fashion and as it will never
	 * be restarted any resources that will no longer be required can be released at this stage, the job will go into a status of
	 * TERMINATED. Whilst the job is QUEUED the ExecuteQueue can invoke cancel which will mean that the job will never execute and
	 * the status will change to CANCELLED. If an exception is thrown then the status of the job will be changed to FAILED.
	 */
	public enum Status {
		CREATED, QUEUED, RUNNING, TERMINATED, COMPLETE, CANCELLED, FAILED
	};

	public final static Version mVersion = new Version(5, 0, 0, "2015/053/19 08:02");;

	/**
	 * Sends a request to cancel the job; this must only be called by the ExecuteQueue. Whilst the job is QUEUED the ExecuteQueue
	 * can invoke cancel which will mean that the job will never execute and the status will change to CANCELLED.
	 */
	public void cancel();

	/**
	 * Complete is called by the Framework when the doJob method has run to completion successfully.
	 */
	public void complete();

	/**
	 * The doJob method must only be invoked by the ExecuteQueue when this job comes to the front of the queue. This method must
	 * execute synchronously, it is free to spawn threads (up to the thread count property) but it MUST ensure that they are all
	 * complete before the method returns. It may be called multiple times if there are intervening calls to the suspend method.
	 * Note this should only be called by the ExecuteQueue as the ExecuteQueue will ensure that other Jobs are suspended during the
	 * execution of this job. This should only be called by the ExecuteQueue to prevent it spawning a whole new batch of threads in
	 * addition to those running in the ExecuteQueue. Use RunBackground to properly interact with the EventQueue to run the job.
	 * Note the method is named to prevent confusion with the Thread start and run methods.
	 */
	public void doJob();

	/**
	 * Error is called by the Framework when the doJob method has thrown an exception.
	 *
	 * @param pThrowable
	 *            the exception that was thrown by the doJob method when it was called.
	 */
	public void error(Throwable pThrowable);

	/**
	 * Gets the control object. This is used to determine if one job in the ExecuteQueue can be used to terminate another. IJobs
	 * that do not have a ControlObject should not be prematurely terminated.
	 * 
	 * @return the control object
	 */
	public Object getControlObject();

	/**
	 * Gets the date that the IJob was created. This will be used by the ExecuteQueue to determine the order that the IJobs that are
	 * run in. Earlier IJobs of the same priority will run first.
	 * 
	 * @return the creates the date
	 */
	public Date getCreateDate();

	/**
	 * Gets the name of this Job. This will be used by the ExecuteQueue to name the underlying implementation threads..
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the priority that the IJob has. Before the submit this will return NULL, afterwards it will return the priority that the
	 * job was submitted with. This will be used by the ExecuteQueue to determine the order that the IJobs that are run in. NOTE the
	 * IJob with the lowest priority will run first.
	 * 
	 * @return the priority
	 */
	public Priority getPriority();

	/**
	 * Gets the percent complete that the job is. This will be an estimate that is dependent on the implementation. If it is not
	 * possible to provide an estimate than it is valid to return 50.0d.
	 * 
	 * @return the percent
	 */
	public double getProgressPercent();

	/**
	 * Gets a string that represents the progress of the job. This might be e.g. "41/88 done". This is used to give feedback to the
	 * user and so should be as meaningful as possible, but concise. This should not be onerous to collect. If there is no sensible
	 * feedback that can be given (e.g. the job may have been submitted to the GPU and until it comes back finished there is no
	 * intermediate progress that can be given) it is valid to return either null or "".
	 * 
	 * @return the progress string
	 */
	public String getProgressString();

	/**
	 * Gets the status of the Job
	 * 
	 * @return the status
	 */
	public Status getStatus();

	/**
	 * Submits the job to the ExecuteQueue to be run in the background with the specified priority. This will call the ExecuteQueues
	 * runBackground which control the interaction with other running jobs.
	 * 
	 * @param pPriority
	 *            the priority
	 */
	public void submit();

	/**
	 * Sends a request to terminate the job; this must only be called by the ExecuteQueue. Whilst the job is RUNNING or QUEUED
	 * terminate can be called. The job should terminate in a timely fashion and release all unneeded resources.
	 */
	public void terminate();
}
