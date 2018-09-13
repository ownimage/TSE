package com.ownimage.framework.opencl;

import com.aparapi.Kernel;
import org.junit.Test;

public class OpenCLTEST {

    class TestKernel extends Kernel {

        private final double[] pResult = new double[10000];
        private final double[] pAdd = new double[1];
        private final double[] pMul = new double[1];

        public TestKernel() {
            super();

            pAdd[0] = 3d;
            pMul[0] = 2d;

            setExplicit(true);
            put(pResult);
            put(pAdd);
            put(pMul);
        }

        @Override
        public void run() {
            final int i = getGlobalId();
            // TODO pResult[i] = AddTEST.add(pAdd, pResult[i]);
            // TODO pResult[i] = MultiplyTEST.mul(pMul, pResult[i]);
        }

        public void print(final int i) {
            System.out.println("pResult[" + i + "]=" + pResult[i]);
        }

        public void get() {
            get(pResult);
        }
    }

    @Test
    public void openCL_test01() {
        final TestKernel kernel = new TestKernel();
        kernel.execute(10000);
        System.out.println("ExecutionMode:" + kernel.getExecutionMode());
        kernel.get();
        kernel.print(500);
    }
}
