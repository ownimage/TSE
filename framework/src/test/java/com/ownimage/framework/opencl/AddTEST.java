package com.ownimage.framework.opencl;

public class AddTEST implements ITransform {

    private final double mAdd;

    public AddTEST(final double pAdd) {
        mAdd = pAdd;
    }

    @Override
    public void transform() {
        // TODO Auto-generated method stub

    }

    public static double transformAdd(final double pDoubleParams, final int[] pIntParams, final double pC) {
        return pDoubleParams + pC;
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
