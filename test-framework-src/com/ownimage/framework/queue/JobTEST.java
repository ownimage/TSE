/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.queue;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.queue.IJob.Priority;
import com.ownimage.framework.queue.IJob.Status;

public class JobTEST {

	class JobTest extends Job {

		private boolean mComplete = false;
		private Throwable mErrorThrown = null;
		private RuntimeException mErrorToBeThrown = null;
		private Thread mThread;

		public JobTest(final String pName, final Priority pPriority) {
			super(pName, pPriority);
		}

		public JobTest(final String pName, final Priority pPriority, final Object pControlObject) {
			super(pName, pPriority, pControlObject);
		}

		@Override
		public void complete() {
			super.complete();
			mComplete = true;
		}

		@Override
		public void doJob() {
			System.out.println(getName() + " started");
			super.doJob();

			mThread = Thread.currentThread();

			mLock.writeLock().lock();
			mLock.writeLock().unlock();
			if (mErrorToBeThrown != null) { throw mErrorToBeThrown; }
			System.out.println(getName() + " finished");
		}

		@Override
		public void error(final Throwable pThrowable) {
			super.error(pThrowable);
			mErrorThrown = pThrowable;
		}

		public Throwable getError() {
			return mErrorThrown;
		}

		public Thread getThread() {
			return mThread;
		}

		public boolean isComplete() {
			return mComplete;
		}

		public void setError(final RuntimeException pError) {
			mErrorToBeThrown = pError;
		}

		@Override
		public void terminate() {
			super.terminate();
		}
	}

	private final ReadWriteLock mLock = new ReentrantReadWriteLock();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Job job = new Job("name", Priority.NORMAL);
		ExecuteThread et = new ExecuteThread(job);
		FrameworkLogger.getInstance().init("logging.properies", "log\\Perception.log");
		FrameworkLogger.getInstance().setLevel("com.ownimage.framework.queue.ExecuteThread", Level.FINEST);
		// FrameworkLogger.getInstance().setLevel("com.ownimage.framework.queue.Job", Level.FINEST);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@After
	public void afterTest() {
		checkQueueEmpty();
	}

	@Before
	public void beforeTest() {
		checkQueueEmpty();
	}

	private void checkQueueEmpty() {
		sleep(100);
		assertEquals(0, ExecuteQueue.getInstance().getDepth());
	}

	@Before
	public void setUp() throws Exception {
	};

	private synchronized void sleep(final int pTime) {
		try {
			wait(pTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};

	@After
	public void tearDown() throws Exception {
	};

	// ctor String, prioritity
	@Test
	public void test01a_ctor() {
		System.out.println("test01a_ctor");
		String name = "myJob";
		IJob job = new Job(name, Priority.NORMAL);
		assertEquals(name, job.getName());
		assertEquals(Priority.NORMAL, job.getPriority());
	};

	// ctor String, prioritity
	@Test
	public void test01b_ctor() {
		System.out.println("test01b_ctor");
		String name = "test01b_ctor";
		IJob job = new Job(name, Priority.HIGH);
		assertEquals(name, job.getName());
		assertEquals(Priority.HIGH, job.getPriority());
	}

	// ctor String, prioritity, control object
	@Test
	public void test01c_ctor() {
		System.out.println("test01c_ctor");
		String name = "test01c_ctor";
		IJob job = new Job(name, Priority.HIGH, name);
		assertEquals(name, job.getName());
		assertEquals(Priority.HIGH, job.getPriority());
		assertEquals(name, job.getControlObject());
	}

	// ctor String, prioritity, control object
	@Test
	public void test01d_ctor() {
		System.out.println("test01d_ctor");
		String name = "test01c_ctor";
		IJob job = new Job(name, Priority.LOW, name);
		assertEquals(name, job.getName());
		assertEquals(Priority.LOW, job.getPriority());
		assertEquals(name, job.getControlObject());
	}

	// cancel from QUEUED
	@Test
	public void test02a_cancel() {
		System.out.println("test02a_cancel");
		String name = "test02a_cancel";
		JobTest job1 = new JobTest(name, Priority.LOW);
		JobTest job2 = new JobTest(name, Priority.LOW, this);
		JobTest job3 = new JobTest(name, Priority.LOW, this);

		assertEquals(Status.CREATED, job2.getStatus());

		try {
			mLock.writeLock().lock();
			job1.submit();
			job2.submit();
			assertEquals(Status.QUEUED, job2.getStatus());

			job3.submit();
			assertEquals(Status.CANCELLED, job2.getStatus());

		} finally {
			mLock.writeLock().unlock();
			checkQueueEmpty();

		}
	}

	// cancel from CREATED should throw exception
	@Test(expected = IllegalStateException.class)
	public void test02b_cancel() {
		System.out.println("test02b_cancel");
		String name = "test02b_cancel";
		IJob job1 = new JobTest(name, Priority.LOW);
		assertEquals(Status.CREATED, job1.getStatus());
		job1.cancel();
	}

	// complete from RUNNING
	@Test
	public void test03a_complete() {
		System.out.println("test03a_complete");

		String name = "test03a_complete";
		JobTest job1 = new JobTest(name, Priority.LOW);

		assertEquals(Status.CREATED, job1.getStatus());

		try {
			mLock.writeLock().lock();

			job1.submit();
			sleep(100);
			assertEquals(Status.RUNNING, job1.getStatus());

		} finally {
			mLock.writeLock().unlock();
			sleep(100);
			assertEquals(Status.COMPLETE, job1.getStatus());
		}
	}

	// error from RUNNING
	@Test
	public void test04a_error() {
		System.out.println("test04a_error");

		String name = "test04a_error";
		JobTest job1 = new JobTest(name, Priority.LOW);
		RuntimeException e = new RuntimeException("error");
		job1.setError(e);
		assertEquals(null, job1.getError());
		assertEquals(Status.CREATED, job1.getStatus());

		try {
			mLock.writeLock().lock();

			job1.submit();
			sleep(100);
			assertEquals(Status.RUNNING, job1.getStatus());

		} finally {
			mLock.writeLock().unlock();
			sleep(100);
			assertEquals(Status.FAILED, job1.getStatus());
			assertEquals(e, job1.getError());
		}
	}

	// terminate from RUNNING
	@Test
	public void test06a_terminate() {
		System.out.println("test06a_terminate");

		String name = "test06a_terminate";
		JobTest job1 = new JobTest(name, Priority.LOW, name);
		JobTest job2 = new JobTest("x", Priority.LOW, name);
		assertEquals(Status.CREATED, job1.getStatus());

		try {
			mLock.writeLock().lock();

			job1.submit();
			sleep(100);
			assertEquals(Status.RUNNING, job1.getStatus());
			job2.submit();

		} finally {
			mLock.writeLock().unlock();
			sleep(100);
			assertEquals(Status.TERMINATED, job1.getStatus());
		}
	}

}
