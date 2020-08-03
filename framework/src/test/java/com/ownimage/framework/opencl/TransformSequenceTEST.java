package com.ownimage.framework.opencl;

import com.aparapi.Kernel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Vector;
import java.util.logging.LogManager;

public class TransformSequenceTEST {

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    class TransformSequence extends Kernel {

        private final Vector<ITransform> mTransforms = new Vector<ITransform>();

        private double[] mX;
        private double[] mY;
        private int[] mRGBA;

        private final double[] mResult = new double[10000];
        private double[] mDoubleParams = new double[1];
        private int[] mIntParams = new int[1];
        private int mTransformId = 0;

        public TransformSequence() {

            setExplicit(true);
            for (int i = 0; i < mResult.length; i++) {
                mResult[i] = i;
            }
            put(mResult);
        }

        public void setTransform(final ITransform pTransform) {
            mTransformId = pTransform.getId();
            mDoubleParams = pTransform.getDoubleParams();
            mIntParams = pTransform.getIntParams();

            put(mDoubleParams);
            put(mIntParams);
        }

        @Override
        public void run() {

            final int i = getGlobalId();
            final int pass = getPassId();

            if (mTransformId == 1) {
                mResult[i] = transformAdd(mDoubleParams[pass], mResult[i]);
            }
            if (mTransformId == 2) {
                mResult[i] = transformMultiply(mDoubleParams[pass], mResult[i]);
            }
        }

        private double transformMultiply(final double d, final double e) {
            return d * e;
        }

        private double transformAdd(final double d, final double e) {
            return d + e;
        }

        public void print(final int i) {
            System.out.println("pResult[" + i + "]=" + mResult[i]);
        }

        public void transform() {
            final int i = 0;
            for (final ITransform transform : mTransforms) {
                setTransform(transform);
                setExecutionMode(EXECUTION_MODE.JTP);
                execute(10000);
                System.out.println("ExecutionMode:" + getExecutionMode());
            }
            get(mResult);
        }

        private void add(final ITransform pTransform) {
            mTransforms.add(pTransform);
        }

    }

    @Test
    public void TrasnformSequence_TEST01() {

        final TransformSequence transformSequence = new TransformSequence();
        final ITransform transform1 = new AddTEST(2);
        final ITransform transform2 = new MultiplyTEST(3);
        final ITransform transform3 = new AddTEST(12);
        transformSequence.add(transform1);
        transformSequence.add(transform2);
        transformSequence.add(transform3);
        transformSequence.transform();
        transformSequence.print(500);
    }

}
