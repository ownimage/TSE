/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class SSpline {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final Point mPointA;
    private final Point mPointB;

    private final Range mMiddleSource;
    private final Range mMiddleTarget;

    double mGradient;
    double mGradientRatio;

    public SSpline(final Point pA, final Point pB, final double pGradientRaito) {
        mPointA = pA;
        mPointB = pB;
        mGradientRatio = pGradientRaito;

        mMiddleSource = new Range(pA.getX(), pB.getX());
        mMiddleTarget = new Range(pA.getY(), pB.getY());

        mGradient = (mPointB.getY() - mPointA.getY()) / (mPointB.getX() - mPointA.getX());

    }

    public double evaluate(final double pX) {
        if (pX < mPointA.getX()) {
            return evaluateLower(pX);
        }
        if (pX < mPointB.getX()) {
            return evaluateMiddle(pX);
        }
        return evaluateUpper(pX);
    }

    public double evaluateLower(final double pX) {
        return evaluateMixed(pX, mPointA);
    }

    public double evaluateMiddle(final double pX) {
        return mMiddleTarget.getValue(pX, mMiddleSource);
    }

    double evaluateMixed(final double pX, final Point pA) {
        double A = (pA.getX() * mGradient - pA.getY()) / (2.0d * pA.getX() * pA.getX() * pA.getX());
        double C = (3.0d * pA.getY() - pA.getX() * mGradient) / (2 * pA.getX());

        CubicEquation cubic = new CubicEquation(A, 0.0d, C, 0.0d);

        double gradient = mGradientRatio * pA.getY() / pA.getX();

        if (C >= gradient) {
            return cubic.evaluate(pX);
        }

        // else we need to calculate the line and spline
        double dx = (3.0 * pA.getY() - 3.0d * gradient * pA.getX()) / (mGradient - gradient);
        double x3 = pA.getX() - dx;

        // if in linear section
        if (pX < x3) {
            return pX * gradient;
        }

        // else fit spline
        double A2 = (mGradient * dx - gradient * dx) / (3.0d * dx * dx * dx);
        CubicEquation cubic2 = new CubicEquation(A2, 0.0d, gradient, 0.0d);
        double y = x3 * gradient + cubic2.evaluate(pX - x3);
        return y;
    }

    public double evaluateUpper(final double pX) {
        return 1.0d - evaluateMixed(1.0d - pX, Point.Point11.minus(mPointB));
    }
}
