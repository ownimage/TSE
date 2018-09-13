package com.ownimage.framework.opencl;

import com.aparapi.Kernel;
import org.junit.Test;

import java.util.Vector;

public class TransformSequenceTEST {

    class TransformSequence extends Kernel {

        private Vector<ITransform> mTransforms = new Vector<ITransform>();

        private double[] mX;
        private double[] mY;
        private int[] mRGBA;

        private double[] mResult = new double[10000];
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

        public void setTransform(ITransform pTransform) {
            mTransformId = pTransform.getId();
            mDoubleParams = pTransform.getDoubleParams();
            mIntParams = pTransform.getIntParams();

            put(mDoubleParams);
            put(mIntParams);
        }

        @Override
        public void run() {

            int i = getGlobalId();
            int pass = getPassId();

            if (mTransformId == 1) {
                mResult[i] = transformAdd(mDoubleParams[pass], mResult[i]);
            }
            if (mTransformId == 2) {
                mResult[i] = transformMultiply(mDoubleParams[pass], mResult[i]);
            }
        }

        private double transformMultiply(double d, double e) {
            return d * e;
        }

        private double transformAdd(double d, double e) {
            return d + e;
        }

        public void print(int i) {
            System.out.println("pResult[" + i + "]=" + mResult[i]);
        }

        public void transform() {
            int i = 0;
            for (ITransform transform : mTransforms) {
                setTransform(transform);
                setExecutionMode(EXECUTION_MODE.JTP);
                execute(10000);
                System.out.println("ExecutionMode:" + getExecutionMode());
            }
            get(mResult);
        }

        private void add(ITransform pTransform) {
            mTransforms.add(pTransform);
        }

    }

    @Test
    public void TrasnformSequence_TEST01() {

        TransformSequence transformSequence = new TransformSequence();
        ITransform transform1 = new AddTEST(2);
        ITransform transform2 = new MultiplyTEST(3);
        ITransform transform3 = new AddTEST(12);
        transformSequence.add(transform1);
        transformSequence.add(transform2);
        transformSequence.add(transform3);
        transformSequence.transform();
        transformSequence.print(500);
    }

}
