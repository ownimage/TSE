/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

public class Range {


    public static final Range ZeroToOne = new Range(0.0d, 1.0d);
	public static final Range LowerThird = new Range(0.0d, 1.0d / 3.0d);
	public static final Range MiddleThird = new Range(1.0d / 3.0d, 2.0d / 3.0d);
	public static final Range UpperThird = new Range(2.0d / 3.0d, 1.0d);

	private double mMin;
	private double mMax;

	public Range(double pMin, double pMax) {
		setMin(pMin);
		setMax(pMax);
	}

	private void setMin(double pMin) {
		mMin = pMin;
	}

	private void setMax(double pMax) {
		mMax = pMax;
	}

	private double getMin() {
		return mMin;
	}

	private double getMax() {
		return mMax;
	}

	public double getFraction(double pX) {
		return (pX - getMin()) / (getMax() - getMin());
	}

	public double getBoundedFraction(double pX) {
		double fraction = getFraction(pX);
		return fraction < 0.0d ? 0.0d : fraction > 1.0d ? 1.0d : fraction;
	}

	public double getBoundedValue(double pX) {
		double fraction = getBoundedFraction(pX);
		return getValue(fraction);
	}

	private double getValue(double pFraction) {
		return getMin() + pFraction * (getMax() - getMin());
	}

	public boolean contains(double pX) {
		return getMin() <= pX && pX <= getMax();
	}

	public double getValue(double pX, Range pFromRange) {
		double fraction = pFromRange.getFraction(pX);
		return getValue(fraction);
	}

}
