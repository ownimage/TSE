/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class KMath {


    public final static String mClassname = KMath.class.getName();
    public final static Logger mLogger = Framework.getLogger();

	public final static double SQRT2 = Math.sqrt(2.0d);

	/**
	 * Returns a value that in within certain bounds. If the value supplied is smaller than the minimum it returns the minimum. If
	 * it is greater than the maximum it returns the maximum. Otherwise it returns the value supplied
	 * 
	 * @param pValue
	 *            the value
	 * @param pMin
	 *            the minimum
	 * @param pMax
	 *            the maximum
	 * @return the bounded value
	 */
	public static double inBounds(final double pValue, final double pMin, final double pMax) {
		final double value = pValue > pMax ? pMax //
				: pValue < pMin ? pMin //
						: pValue;

		return value;
	}

	/**
	 * Returns a value that in within certain bounds. If the value supplied is smaller than the minimum it returns the minimum. If
	 * it is greater than the maximum it returns the maximum. Otherwise it returns the value supplied
	 * 
	 * @param pValue
	 *            the value
	 * @param pMin
	 *            the minimum
	 * @param pMax
	 *            the maximum
	 * @return the bounded value
	 */
	public static int inBounds(final int pValue, final int pMin, final int pMax) {
		final int value = pValue > pMax ? pMax //
				: pValue < pMin ? pMin //
						: pValue;

		return value;
	}

	public static boolean inRange01inc(final double pLambda) {
		return 0.0d <= pLambda && pLambda <= 1.0d;
	}

	/*
	 * finds the intersection of two lines. line 1 is: a + b * lambda <br\> line 2 is: c + d * mu <br\> This returns the value of
	 * lamba where the two lines meet, or null if they do not. <br\> Note as this will be called many times I am trying to avoid
	 * throwing an exception. The implementation uses Point here when b and d should be vectors. The parameter naming convention is
	 * broken here for code readability.
	 */
	public static Double intersect(final Point a, final Point b, final Point c, final Point d) {
		try {
			// check for parallel
			if (b.getY() == 0 && d.getY() == 0) { // they are parallel
				return null;
			}
			if (b.getY() != 0.0d && d.getY() != 0.0d && b.getX() / b.getY() == d.getX() / d.getY()) { // they are parallel
				return null;
			}

			// lambda = (d * mu + c - a) / b
			// in mX
			// A = dx / bx
			// B = (cx - ax) / bx
			// (1) lambda = A * mu + B
			// in mY
			// C = dy / by
			// D = (cy - ay) / by
			// (2) lambda = C * mu + D
			// solving (1) and (2)
			// lambda = (CB - AD) / (C - A)
			final double A = d.getX() / b.getX();
			final double B = (c.getX() - a.getX()) / b.getX();
			final double C = d.getY() / b.getY();
			final double D = (c.getY() - a.getY()) / b.getY();
			final double lambda = (C * B - A * D) / (C - A);
			return lambda;

		} catch (final Exception pEx) {
			return null;
		}
	}

	/**
	 * Limit01 restricts a value to be in the range 0.0d to 1.0d. Values in this range are preserved. Values less that 0.0d are
	 * mapped to 0.0d, and values greater than 1.0d are mapped to 1.0d.
	 * 
	 * @param pD
	 *            the input variable
	 * @return the double in the range 0.0d ... 1.0d
	 */
	public static double limit01(final double pD) {
		if (pD < 0.0d) { return 0.0d; }
		if (pD > 1.0d) { return 1.0d; }
		return pD;
	}

	public static void main(final String[] pArgs) {
		final double lambda = intersect(new Point(0, 1), new Point(1, 1), new Point(5, 0), new Point(0, 1));
        mLogger.info(() -> "lambda: " + lambda);
	}

	// commented out as javac gets confused ... but Eclipse compiles?
	public static double max(final double... pNum) {
		double max = pNum[0];
		for (final double d : pNum) {
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	public static int max(final int... pNum) {
		int max = pNum[0];
		for (final int d : pNum) {
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	public static double min(final double... pNum) {
		double min = pNum[0];
		for (final double d : pNum) {
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	public static int min(final int... pNum) {
		int min = pNum[0];
		for (final int d : pNum) {
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	/**
	 * Mod returns the a value v such that 0.0d <= v && v < pRange. It does this by recursively adding or subtracting pRange from
	 * the value until this is true. This method is designed for cases where v only +/1 one pRange away from the desired value.
	 * 
	 * @param pValue
	 *            the value
	 * @param pRange
	 *            the range value must be > 0.0d
	 * @return the value
	 */
	public static double mod(final double pValue, final double pRange) {
		if (pRange <= 0.0d) { throw new IllegalArgumentException("pRange must be > 0.0d, value supplied " + pRange); }

		if (0.0d <= pValue && pValue < pRange) { return pValue; }

		if (pValue < 0.0d) { return mod(pValue + pRange, pRange); }

		if (pValue >= pRange) { return mod(pValue - pRange, pRange); }

		throw new RuntimeException("Can not compute mod for pValue: " + pValue + " and pRange: " + pRange);
	}

	/**
	 * Mod returns x modulus y. This is a zero or positive number positive.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the modulus
	 */
	public static int mod(final int x, final int y) {
		int result = x % y;
		return result < 0 ? result + y : result;
	}

	public static double mod1(final double pD) {
		if (0.0d <= pD && pD < 1.0d) { return pD; }
		if (pD < 0) { return mod1(pD + 1.0d); }
		if (pD >= 1) { return mod1(pD - 1.0d); }
		return 0.0;
	}

	/**
	 * Return a value in the range 0 .. 1. This is very similar to mod1 except mod1(1.0d) return 0.0d, mod1inc(1.0d) returns 1.0d)
	 * 
	 * @param pD
	 *            the input
	 * @return the mod1inc of pD
	 */
	public static double mod1inc(final double pD) {
		if (pD == 1.0d) { return 1.0d; }
		return mod1(pD);
	}

	public static QuadraticEquation.Root quadratic(final double pA, final double pB, final double pC) {
		final QuadraticEquation qe = new QuadraticEquation(pA, pB, pC);
		return qe.solve();

	}

	/**
	 * The Sigma function is to map smoothly between two values. s(0) = 1, s(1)= 0, s'(0) = 0, and s'(1) = 0.
	 *
	 * @param pX
	 *            the x
	 * @return the double
	 */
	public static double sigma(final double pX) {
		return 2 * pX * pX * pX - 3 * pX * pX + 1;
	}

	public static int signum(final int pNum) {
		if (pNum > 0) { return 1; }
		if (pNum < 0) { return -1; }
		return 0;
	}

	public static double square(final double pD) {
		return pD * pD;
	}

	public static int square(final int pI) {
		mLogger.entering(mClassname, "square");
		// FrameworkLogger.logParams(mLogger, "pI", pI);
		final int pi2 = pI * pI;

		mLogger.exiting(mClassname, "square", pi2);
		return pi2;
	}
}
