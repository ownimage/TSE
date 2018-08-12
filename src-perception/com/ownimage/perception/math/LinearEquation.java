/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.io.Serializable;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class LinearEquation implements Serializable {


    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

	private final double mM;
	private final double mC;

	public LinearEquation(final double pM, final double pC) {
		mM = pM;
		mC = pC;
	}

	public LinearEquation add(final LinearEquation pLE) {
		return new LinearEquation(mM + pLE.mM, mC + pLE.mC);
	}

	public LinearEquation divide(final double pScale) {
		return new LinearEquation(mM / pScale, mC / pScale);
	}

	public double evaluate(final double pX) {
		return mM * pX + mC;
	}

	public double getC() {
		return mC;
	}

	public double getM() {
		return mM;
	}

	public Point getPoint(final double pX) {
		return new Point(pX, evaluate(pX));
	}

	public LinearEquation minus(final LinearEquation pLE) {
		return new LinearEquation(mM - pLE.mM, mC - pLE.mC);
	}

	public LinearEquation multiply(final double pScale) {
		return new LinearEquation(mM * pScale, mC * pScale);
	}

	public QuadraticEquation multiply(final LinearEquation pLE) {
		return new QuadraticEquation(mM * pLE.mM, mM * pLE.mC + mC * pLE.mM, mC * pLE.mC);
	}

	public double solve() {
		return -mC / mM;
	}

	public QuadraticEquation square() {
		return multiply(this);
	}

	@Override
	public String toString() {
		return "LinearEquation(" + mM + "," + mC + ")";
	}

}
