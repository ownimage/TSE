/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Version;

public class QuarticEquation {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	@SuppressWarnings("unused")
	private final static Logger mLogger = Logger.getLogger(QuarticEquation.class.getName());

	double mA;
	double mB;
	double mC;
	double mD;
	double mE;

	public QuarticEquation() {
		this(0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
	}

	public QuarticEquation(final double pA, final double pB, final double pC, final double pD, final double pE) {
		mA = pA;
		mB = pB;
		mC = pC;
		mD = pD;
		mE = pE;
	}

	public QuarticEquation add(final QuarticEquation pQE) {
		return new QuarticEquation(mA + pQE.mA, mB + pQE.mB, mC + pQE.mC, mD + pQE.mD, mE + pQE.mE);
	}

	public CubicEquation differentiate() {
		return new CubicEquation(4.0d * mA, 3.0d * mB, 2.0 * mC, mD);
	}

	public QuarticEquation divide(final double pScale) {
		return new QuarticEquation(mA / pScale, mB / pScale, mC / pScale, mD / pScale, mE / pScale);
	}

	public double evaluate(final double pX) {
		return mA * pX * pX * pX * pX + mB * pX * pX * pX + mC * pX * pX + mD * pX + mE;
	}

	public QuarticEquation minus(final QuarticEquation pQE) {
		return new QuarticEquation(mA - pQE.mA, mB - pQE.mB, mC - pQE.mC, mD - pQE.mD, mE - pQE.mE);
	}

	public QuarticEquation multiply(final double pScale) {
		return new QuarticEquation(mA * pScale, mB * pScale, mC * pScale, mD * pScale, mE * pScale);
	}

	@Override
	public String toString() {
		return "QuarticEquation(" + mA + ", " + mB + ", " + mC + ", " + mD + ", " + mE + ")";
	}

}
