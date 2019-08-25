package com.ownimage.perception.transform;

import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.queue.ExecuteQueue;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.render.RenderService;
import lombok.val;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;

public class OpenCLTest {

    RenderService mRenderService = new RenderService();

    class Red extends BaseTransform {
        private final float mA;

        public Red(final float pA) {
            super("Red", "red");
            mA = pA;
        }

        @Override
        public void transform(ITransformResult pRenderResult) {
            hardSum(10000);
            pRenderResult.setRGBA(1.0f, 0.0f, 0.0f, mA);
        }
    }

    class Green extends BaseTransform {
        private final float mA;

        public Green(final float pA) {
            super("Green", "green");
            mA = pA;
        }

        @Override
        public void transform(ITransformResult pRenderResult) {
            hardSum(10000);
            pRenderResult.setRGBA(1.0f, 1.0f, 0.0f, mA);
        }
    }

    private void hardSum(final int pCount) {
        int total = 0;
        for (int x = 0; x < pCount; x++) {
            total += x;
        }
    }

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        FrameworkLogger.getInstance().init("logging.properties", "Perception.log");
        FXViewFactory.setAsViewFactory(false);
        new OpenCLTest().warmup();
    }

    private void warmup() throws InterruptedException {
        val red = new Red(0.5f);
        red.setPreviousTransform(new Green(1.0f));
        val picture = new PictureType(1000, 1000);
        mRenderService.getRenderJobBuilder("testReason", picture, red).build();
        waitTillExecuteQueueEmpty(1000);
    }

    // Basic test to check that the render service can be waited for to get the result
    @Test
    public void openCLTest_01() throws InterruptedException {
        // GIVEN
        val underTest = new Red(1.0f);
        val picture = new PictureType(1000, 1000);
        val renderJob = mRenderService.getRenderJobBuilder("testReason", picture, underTest).build();
        // WHEN
        renderJob.run();
        waitTillExecuteQueueEmpty(1000);
        // THEN
        renderJob.getDuration().ifPresent(d -> System.out.println("Duration = " + d.toMillis()));
        Assert.assertEquals(Color.RED, picture.getColor(10, 10).get());
    }

    private synchronized void waitTillExecuteQueueEmpty(final long pMillis) throws InterruptedException {
        while (ExecuteQueue.getInstance().isBusy()) wait(pMillis);
    }

    // Basic test to ensure that Transforms can be chained
    @Test
    public void openCLTest_02() throws InterruptedException {
        // GIVEN
        val underTest = new Red(0.5f);
        underTest.setPreviousTransform(new Green(1.0f));
        val picture = new PictureType(1000, 1000);
        val renderJob = mRenderService.getRenderJobBuilder("testReason", picture, underTest).build();
        // WHEN
        renderJob.run();
        waitTillExecuteQueueEmpty(1000);
        // THEN
        renderJob.getDuration().ifPresent(d -> System.out.println("Duration = " + d.toMillis()));
        Assert.assertEquals(new Color(255, 128, 0), picture.getColor(10, 10).get());
    }

}
