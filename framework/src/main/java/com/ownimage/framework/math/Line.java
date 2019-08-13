/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import Jama.Matrix;
import com.ownimage.framework.util.Framework;
import lombok.val;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Line implements Serializable {

    /**
     *
     */


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public static final Line XAxis = new Line(Point.Point00, Point.Point10);
    public static final Line YAxis = new Line(Point.Point00, Point.Point01);

    private final Point mA;
    private final Point mB;
    private final Vector mAB;
    private LinearEquation mLinearEquation;

    public Line(final double pM, final double pC) {
        this(new Point(0.0d, pC), new Point(1.0d, pC + pM));
    }

    public Line(final LinearEquation pLE) {
        this(pLE.getM(), pLE.getC());
    }

    public Line(final Point pA, final double pGradient) {
        this(pA, pA.add(new Point(1.0d, pGradient)));
    }

    public Line(final Point pA, final Point pB) {
        mA = pA;
        mB = pB;
        mAB = mB.minus(mA);
    }

    public double closestLambda(final Point pPoint) {
        final Vector pax = mA.minus(pPoint);

        final double lambda = -pax.dot(mAB) / mAB.length2();
        return lambda;
    }

    public Point closestPoint(final Point pPoint) {
        return getPoint(closestLambda(pPoint));
    }

    public double distance(final Point pPoint) {
        return closestPoint(pPoint).minus(pPoint).length();
    }

    public Point getA() {
        return mA;
    }

    public Vector getAB() {
        return mAB;
    }

    public Vector getANormal() {
        return mB.minus(mA).getANormal();
    }

    public Point getB() {
        return mB;
    }

    public synchronized LinearEquation getLinearEquation() {
        if (mLinearEquation == null) {
            final double m = mAB.gradient();
            final double c = intersect(YAxis).getY();
            mLinearEquation = new LinearEquation(m, c);
        }
        return mLinearEquation;
    }

    public Vector getNormal() {
        return getANormal().normalize();
    }

    public Point getPoint(final double pLambda) {
        return mA.add(mAB.multiply(pLambda));

    }

    /**
     * Gets the reverse line. If the original Line is A, B, then the reverse line is A, A-(AB), i.e. it starts in the same place,
     * but goes in the opposite direction.
     *
     * @return the reverse
     */
    public Line getReverse() {
        return new Line(mA, mA.minus(mAB));
    }

    public Point intersect(final Line pLine) {
        try {
            final Matrix m = new Matrix(new double[][]{{getAB().getX(), -pLine.getAB().getX()}, {getAB().getY(), -pLine.getAB().getY()}});
            final Matrix b = new Matrix(new double[][]{{pLine.getA().getX() - getA().getX()}, {pLine.getA().getY() - getA().getY()}});
            final Matrix x = m.solve(b);
            return mA.add(mAB.multiply(x.get(0, 0)));
        } catch (final RuntimeException pEx) {
            return null;
        }
    }

    public boolean isCloserThan(final Point pPoint, final double pDistance) {
        return distance(pPoint) < pDistance;
    }

    public boolean isParallel(final Line pLine) {
        final double deltaX = mA.getX() - mB.getX();
        final double otherDeltaX = pLine.mA.getX() - pLine.mB.getX();

        final double deltaY = mA.getY() - mB.getY();
        final double otherDeltaY = pLine.mA.getY() - pLine.mB.getY();

        if (deltaX == 0 && otherDeltaX == 0) {
            return true;
        }
        if (deltaY == 0 && otherDeltaY == 0) {
            return true;
        }
        if (deltaX == 0 || otherDeltaX == 0) {
            return false;
        }
        if (deltaY == 0 || otherDeltaY == 0) {
            return false;
        }
        return getNormal().minus(pLine.getNormal()).length2() < 1.0e-10;

        // return getLinearEquation().getM() == pLine.getLinearEquation().getM();
    }

    @Override
    public String toString() {
        return "(Line A=" + mA + " B=" + mB + ")";
    }

    public Stream<Point> stream(final int mCount) {
        return IntStream.rangeClosed(0, mCount)
                .mapToDouble(i -> (double) i / mCount)
                .mapToObj(this::getPoint);
    }

    public Stream<Point> streamFromCenter(final int mCount) {
        return IntStream.rangeClosed(0, mCount)
                .map(i -> KMath.fromCenter(i, mCount))
                .mapToDouble(i -> (double) i / mCount)
                .mapToObj(this::getPoint);
    }

}
