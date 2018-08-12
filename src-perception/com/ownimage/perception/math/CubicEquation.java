/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class CubicEquation {

	public class Root {
		private final int mNumberOfRoots;
		private final double mRoot1;
		private final double mRoot2;
		private final double mRoot3;

		private Root(final int pNumberOfRoots, final double pRoot1, final double pRoot2, final double pRoot3) {
			super();
			mNumberOfRoots = pNumberOfRoots;
			mRoot1 = pRoot1;
			mRoot2 = pRoot2;
			mRoot3 = pRoot3;
		}

		public int getNumberOfRoots() {
			return mNumberOfRoots;
		}

		public double getRoot1() {
			return mRoot1;
		}

		public double getRoot2() {
			if (mNumberOfRoots < 2) { throw new IndexOutOfBoundsException(); }

			return mRoot2;
		}

		public double getRoot3() {
			if (mNumberOfRoots < 3) { throw new IndexOutOfBoundsException(); }

			return mRoot3;
		}

	}


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	double mA;
	double mB;
	double mC;

	double mD;

	public CubicEquation() {
		this(0.0d, 0.0d, 0.0d, 0.0d);
	}

	public CubicEquation(final double pA, final double pB, final double pC, final double pD) {
		mA = pA;
		mB = pB;
		mC = pC;
		mD = pD;
	}

	public CubicEquation add(final CubicEquation pCE) {
		return new CubicEquation(mA + pCE.mA, mB + pCE.mB, mC + pCE.mC, mD + pCE.mD);
	}

	public QuadraticEquation differentiate() {
		return new QuadraticEquation(3.0d * mA, 2.0d * mB, mC);
	}

	public CubicEquation divide(final double pScale) {
		return new CubicEquation(mA / pScale, mB / pScale, mC / pScale, mD / pScale);
	}

	public double evaluate(final double pX) {
		return mA * pX * pX * pX + mB * pX * pX + mC * pX + mD;
	}

	public double getA() {
		return mA;
	}

	public double getB() {
		return mB;
	}

	public double getC() {
		return mC;
	}

	public double getD() {
		return mD;
	}

	public CubicEquation minus(final CubicEquation pCE) {
		return new CubicEquation(mA - pCE.mA, mB - pCE.mB, mC - pCE.mC, mD - pCE.mD);
	}

	public CubicEquation multiply(final double pScale) {
		return new CubicEquation(mA * pScale, mB * pScale, mC * pScale, mD * pScale);
	}

	public synchronized Root solve() {
		edu.rit.numeric.Cubic cubic = new edu.rit.numeric.Cubic();
		cubic.solve(mA, mB, mC, mD);
		return new Root(cubic.nRoots, cubic.x1, cubic.x2, cubic.x3);
	}

	@Override
	public String toString() {
		return "CubicEquation(" + mA + "," + mB + "," + mC + ", " + mD + ")";
	}
}
