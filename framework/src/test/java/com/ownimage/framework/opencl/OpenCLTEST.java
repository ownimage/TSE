package com.ownimage.framework.opencl;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

public class OpenCLTEST {

    public static int SIZE = 10000000;
    final double[] mResult = new double[SIZE];
    final double[] mAdd = new double[]{3.0d};
    final double[] mMul = new double[]{4.0d};

    class TestKernel extends Kernel {

        TestKernel() {
            super();
        }

        @Override
        public void run() {
            final int i = getGlobalId();
            //mResult[i] = mResult[i] + mResult[i];
            //    mResult[i] = AddTEST.transformAdd(mResult[i], mAdd);
        }
    }

    @Test
    public void openCL_test01() {
        val result = mResult;
        val add = mAdd;

        class Test01Kernel extends Kernel {

            Test01Kernel() {
                super();
            }

            @Override
            public void run() {
                final int i = getGlobalId();
                //mResult[i] = mResult[i] + mResult[i];
                result[i] = AddTEST.transformAdd(result[i], add);
            }
        }

        Device device = KernelManager.instance().bestDevice();
        Range range = device.createRange(SIZE);

        Kernel kernel = new Test01Kernel();

        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        kernel.setExplicit(true);
        kernel.put(result);
        kernel.put(add);

        kernel.execute(range);
        kernel.get(result);
        kernel.dispose();
        Assert.assertEquals(3.0d, mResult[500], 0.0d);
        Assert.assertEquals(Kernel.EXECUTION_MODE.GPU, kernel.getExecutionMode());

        val sb = new StringBuilder();
        KernelManager.instance().reportDeviceUsage(sb, true);
        System.out.println(sb.toString());
    }

    @Test
    public void openCL_test02() {
        final double[] mResult = new double[SIZE];
        final double[] mAdd = new double[]{3.0d};

        class Test02Kernel extends Kernel {

            Test02Kernel() {
                super();
            }

            @Override
            public void run() {
                final int i = getGlobalId();
                //mResult[i] = mResult[i] + mResult[i];
                mResult[i] = AddTEST.transformAdd(mResult[i], mAdd);
            }
        }

        Device device = KernelManager.instance().bestDevice();
        Range range = device.createRange(SIZE);

        Kernel kernel = new Test02Kernel();

        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        kernel.setExplicit(true);
        kernel.put(mResult);
        kernel.put(mAdd);
        //put(mMul);

        kernel.execute(range);
        kernel.get(mResult);
        kernel.dispose();
        Assert.assertEquals(3.0d, mResult[500], 0.0d);
        Assert.assertEquals(Kernel.EXECUTION_MODE.GPU, kernel.getExecutionMode());

        val sb = new StringBuilder();
        KernelManager.instance().reportDeviceUsage(sb, true);
        System.out.println(sb.toString());
    }

    @Test
    public void openCL_test03() {
        val result = mResult;
        val add = mAdd;

        Device device = KernelManager.instance().bestDevice();
        Range range = device.createRange(SIZE);

        Kernel kernel = getKernelAdd(result, add);

        kernel.execute(range);
        kernel.get(result);
        kernel.dispose();
        Assert.assertEquals(3.0d, mResult[500], 0.0d);
        Assert.assertEquals(Kernel.EXECUTION_MODE.GPU, kernel.getExecutionMode());

        val sb = new StringBuilder();
        KernelManager.instance().reportDeviceUsage(sb, true);
        System.out.println(sb.toString());
    }

    private Kernel getKernelAdd(double[] pResult, double[] pAdd) {
        class TestKernel extends Kernel {

            TestKernel() {
                super();
            }

            @Override
            public void run() {
                final int i = getGlobalId();
                //mResult[i] = mResult[i] + mResult[i];
                pResult[i] = AddTEST.transformAdd(pResult[i], pAdd);
            }
        }

        Device device = KernelManager.instance().bestDevice();
        Range range = device.createRange(SIZE);

        Kernel kernel = new TestKernel();

        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        kernel.setExplicit(true);
        kernel.put(pResult);
        kernel.put(pAdd);
        return kernel;
    }

    @Test
    public void openCL_test04() {
        val result = mResult;
        val add = mAdd;
        val mul = mMul;

        Device device = KernelManager.instance().bestDevice();
        Range range = device.createRange(SIZE);

        Kernel kernel = getKernel04(result, add, mul);

        kernel.execute(range);
        kernel.get(result);
        kernel.dispose();
        Assert.assertEquals(12.0d, mResult[500], 0.0d);
        Assert.assertEquals(Kernel.EXECUTION_MODE.GPU, kernel.getExecutionMode());

        val sb = new StringBuilder();
        KernelManager.instance().reportDeviceUsage(sb, true);
        System.out.println(sb.toString());
    }

    private Kernel getKernel04(double[] pResult, double[] pAdd, double[] pMul) {
        class TestKernel extends Kernel {

            TestKernel() {
                super();
            }

            @Override
            public void run() {
                final int i = getGlobalId();
                //mResult[i] = mResult[i] + mResult[i];
                pResult[i] = AddTEST.transformAdd(pResult[i], pAdd);
                pResult[i] = MultiplyTEST.transformMultiply(pResult[i], pMul);
            }
        }

        Device device = KernelManager.instance().bestDevice();
        Range range = device.createRange(SIZE);

        Kernel kernel = new TestKernel();

        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        kernel.setExplicit(true);
        kernel.put(pResult);
        kernel.put(pAdd);
        kernel.put(pMul);
        return kernel;
    }

    @Test
    public void openCL_test05() {
        val result = mResult;
        val add = mAdd;
        val mul = mMul;
        int[] op = new int[]{1};


        Device device = KernelManager.instance().bestDevice();
        Range range = device.createRange(SIZE);

        Kernel kernel = getKernel05(result, add, mul, op);

        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        kernel.setExplicit(true);
        kernel.put(result);
        kernel.put(add);
        kernel.put(mul);
        kernel.put(op);
        kernel.execute(range);

        op[0] = 2;
        kernel.put(op);
        kernel.execute(range);
        kernel.get(result);
        kernel.dispose();
        Assert.assertEquals(12.0d, mResult[500], 0.0d);
        Assert.assertEquals(Kernel.EXECUTION_MODE.GPU, kernel.getExecutionMode());

        val sb = new StringBuilder();
        KernelManager.instance().reportDeviceUsage(sb, true);
        System.out.println(sb.toString());
    }

    private Kernel getKernel05(double[] pResult, double[] pAdd, double[] pMul, int[] pOperation) {
        class TestKernel extends Kernel {

            TestKernel() {
                super();
            }

            @Override
            public void run() {
                final int i = getGlobalId();
                if (pOperation[0] == 1) pResult[i] = AddTEST.transformAdd(pResult[i], pAdd);
                if (pOperation[0] == 2) pResult[i] = MultiplyTEST.transformMultiply(pResult[i], pMul);
            }
        }
        return new TestKernel();
    }
}
