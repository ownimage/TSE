package com.ownimage.framework.opencl;

import org.junit.BeforeClass;

import java.util.logging.LogManager;

public class MultiplyTEST implements ITransform {

    private final double mMultiply;

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    public MultiplyTEST(final double pMultiply) {
        mMultiply = pMultiply;
    }

    @Override
    public void transform() {
        // TODO Auto-generated method stub

    }

    public static double transformMultiply(final double pDoubleParams, final double pC[]) {
        return pDoubleParams * pC[0];
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public double[] getDoubleParams() {
        return new double[]{mMultiply};
    }

    @Override
    public int[] getIntParams() {
        return new int[]{0};
    }

}
