package com.ownimage.perception.opencl;

public class MultiplyTEST implements ITransform {

	private double mMultiply;

	public MultiplyTEST(double pMultiply) {
		mMultiply = pMultiply;
	}

	@Override
	public void transform() {
		// TODO Auto-generated method stub

	}

	public static double transformMultiply(double pDoubleParams, int[] pB, double pC) {
		return pDoubleParams * pC;
	}

	@Override
	public int getId() {
		return 2;
	}

	@Override
	public double[] getDoubleParams() {
		return new double[] { mMultiply };
	}

	@Override
	public int[] getIntParams() {
		return new int[] { 0 };
	}

}
