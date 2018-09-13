package com.ownimage.framework.opencl;

public class MultiplyTEST implements ITransform {

    private final double mMultiply;

    public MultiplyTEST(final double pMultiply) {
        mMultiply = pMultiply;
    }

    @Override
    public void transform() {
        // TODO Auto-generated method stub

    }

    public static double transformMultiply(final double pDoubleParams, final int[] pB, final double pC) {
        return pDoubleParams * pC;
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
