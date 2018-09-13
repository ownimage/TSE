/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import com.ownimage.framework.util.Framework;

import java.util.logging.Logger;

public class QuadraticEquation {

	public class Root {
		private final double mUpper;
		private final double mLower;

		public Root(final double pUpper, final double pLower) {
			mUpper = pUpper;
			mLower = pLower;
		}

		public double getLower() {
			return mLower;
		}

		public double getUpper() {
			return mUpper;
		}

		@Override
		public String toString() {
			return "Root(" + mUpper + "," + mLower + ")";
		}
	}


    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();
	double mA;
	double mB;
	double mC;

	Root mRoot;

	/**
	 * Instantiates a new quadratic equation. This is an immutable class.
	 */
	public QuadraticEquation() {
		this(0.0d, 0.0d, 0.0d);
	}

	public QuadraticEquation(final double pA, final double pB, final double pC) {
		mA = pA;
		mB = pB;
		mC = pC;
	}

	public QuadraticEquation add(final QuadraticEquation pQE) {
		return new QuadraticEquation(mA + pQE.mA, mB + pQE.mB, mC + pQE.mC);
	}

	public LinearEquation differentiate() {
		return new LinearEquation(2.0d * mA, mB);
	}

	public QuadraticEquation divide(final double pScale) {
		return new QuadraticEquation(mA / pScale, mB / pScale, mC / pScale);
	}

	public double evaluate(final double pX) {
		return mA * pX * pX + mB * pX + mC;
	}

	public QuadraticEquation minus(final QuadraticEquation pQE) {
		return new QuadraticEquation(mA - pQE.mA, mB - pQE.mB, mC - pQE.mC);
	}

	public QuadraticEquation multiply(final double pScale) {
		return new QuadraticEquation(mA * pScale, mB * pScale, mC * pScale);
	}

	public synchronized Root solve() {
		if (mRoot == null) {
			double sqrt = Math.sqrt(mB * mB - 4.0d * mA * mC);
			double upper = (-mB + sqrt) / (2.0d * mA);
			double lower = (-mB - sqrt) / (2.0d * mA);
			mRoot = new Root(upper, lower);
		}
		return mRoot;
	}

	@Override
	public String toString() {
		return "QuadraticEquation(" + mA + "," + mB + "," + mC + ")";
	}
}
