package com.ownimage.framework.util.runWhenDirty;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class RunWhenDirtyFactoryTest {

    @Test
    public void create_01() {
        // GIVEN
        AtomicInteger counter = new AtomicInteger();
        Runnable cleaner = () -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException pE) {
                pE.printStackTrace();
            }
        };
        val underTest = RunWhenDirtyFactory.create(cleaner);
        // WHEN
        underTest.markDirty();
        // THEN
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void create_02() throws InterruptedException {
        // GIVEN
        AtomicInteger counter = new AtomicInteger();
        Runnable cleaner = () -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException pE) {
                pE.printStackTrace();
            }
        };
        val underTest = RunWhenDirtyFactory.create(cleaner);
        // WHEN
        Thread t1 = new Thread(underTest::markDirty);
        Thread t2 = new Thread(underTest::markDirty);
        Thread t3 = new Thread(underTest::markDirty);
        Thread t4 = new Thread(underTest::markDirty);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        // THEN
        Assert.assertEquals(2, counter.get());
    }

    @Test
    public void createOnNewThread_01() throws InterruptedException {
        // GIVEN
        AtomicInteger counter = new AtomicInteger();
        Runnable cleaner = () -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException pE) {
                pE.printStackTrace();
            }
        };
        val underTest = RunWhenDirtyFactory.createOnNewThread(cleaner);
        // WHEN
        underTest.markDirty();
        waitTillClean(underTest);
        // THEN
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void createOnNewThread_02() throws InterruptedException {
        // GIVEN
        AtomicInteger counter = new AtomicInteger();
        Runnable cleaner = () -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException pE) {
                pE.printStackTrace();
            }
        };
        val underTest = RunWhenDirtyFactory.createOnNewThread(cleaner);
        // WHEN
        Thread t1 = new Thread(underTest::markDirty);
        Thread t2 = new Thread(underTest::markDirty);
        Thread t3 = new Thread(underTest::markDirty);
        Thread t4 = new Thread(underTest::markDirty);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        waitTillClean(underTest);
        // THEN
        Assert.assertEquals(2, counter.get());
    }

    private void waitTillClean(IRunWhenDirty pTest) throws InterruptedException {
        while (pTest.isDirty()) {
            Thread.sleep(100);
        }
    }
}