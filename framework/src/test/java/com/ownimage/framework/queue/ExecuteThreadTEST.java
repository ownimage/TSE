/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.queue;

import com.ownimage.framework.queue.IJob.Priority;
import com.ownimage.framework.queue.IJob.Status;
import org.junit.*;

import static org.junit.Assert.*;

public class ExecuteThreadTEST {

    class JobTEST extends Job {

        private boolean mComplete = false;
        private Throwable mErrorThrown = null;
        private RuntimeException mErrorToBeThrown = null;
        private Thread mThread;

        public JobTEST(final String pName, final Priority pPriority) {
            super(pName, pPriority);
        }

        @Override
        public void complete() {
            super.complete();
            mComplete = true;
        }

        @Override
        public void doJob() {
            super.doJob();

            mThread = Thread.currentThread();

            if (mErrorToBeThrown != null) {
                throw mErrorToBeThrown;
            }
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
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Tests that an exception is thrown when there is a null job passed as a constructor.
     */
    @Test(expected = NullPointerException.class)
    public void test01_nullJob() {
        final ExecuteThread et = new ExecuteThread(null);
    }

    /**
     * Tests the valid constructor (not null) and the getJob method.
     */
    @Test
    public void test02_getJob() {
        final IJob job = new Job("myJob", Priority.NORMAL);
        final ExecuteThread et = new ExecuteThread(job);
        assertEquals(job, et.getJob());
    }

    ;

    /**
     * Tests that the job runs on a different thread and that the complete method is called on the Job. Tests that the thread ends
     * too.
     *
     * @throws InterruptedException
     */
    @Test
    public void test03a_run() throws InterruptedException {
        final JobTEST job = new JobTEST("myJob", Priority.NORMAL);
        assertEquals(job.getStatus(), Status.CREATED);
        final ExecuteThread et = new ExecuteThread(job);
        et.start();
        et.join(); // implicit test that the thread has ended
        assertFalse(Thread.currentThread() == job.getThread());
        assertTrue(job.isComplete());
        assertEquals(Status.COMPLETE, job.getStatus());
    }

    /**
     * Tests that the error method is called on the job if an error is thrown
     *
     * @throws InterruptedException
     */
    @Test
    public void test03b_run() throws InterruptedException {
        final IllegalStateException rte = new IllegalStateException("runtime excpetion");
        final JobTEST job = new JobTEST("myJob", Priority.NORMAL);
        job.setError(rte);
        final ExecuteThread et = new ExecuteThread(job);
        et.start();
        et.join(); // implicit test that the thread has ended
        assertFalse(Thread.currentThread() == job.getThread());
        assertFalse(job.isComplete());
        assertEquals(Status.FAILED, job.getStatus());
        assertEquals(rte, job.getError());
    }

}
