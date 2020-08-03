package com.ownimage.framework.opencl;

import org.junit.BeforeClass;

import java.util.logging.LogManager;

public class AddTEST implements ITransform {

    private final double mAdd;

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    public AddTEST(final double pAdd) {
        mAdd = pAdd;
    }

    @Override
    public void transform() {
        // TODO Auto-generated method stub

    }

    public static double transformAdd(final double pDoubleParams, final double[] pC) {
        return pDoubleParams + pC[0];
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public double[] getDoubleParams() {
        return new double[]{mAdd};
    }

    @Override
    public int[] getIntParams() {
        return new int[]{0};
    }
}
