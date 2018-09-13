/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import java.util.logging.Logger;

import com.ownimage.framework.math.CubicEquation;
import com.ownimage.framework.math.CubicEquation.Root;
import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.QuarticEquation;
import com.ownimage.framework.math.Vector;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelChain;

public class CurveSegment extends SegmentBase<CurveSegment> {

    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final Point mP1;
    private final Point mA;
    private final Point mB;

    CurveSegment(final PixelChain pPixelChain, final int pSegmentIndex, final Point pP1) {
        super(pSegmentIndex);
        mP1 = pP1;
        mA = getP0(pPixelChain).add(getP2(pPixelChain)).minus(getP1().multiply(2.0d));
        mB = getP1().minus(getP0(pPixelChain)).multiply(2.0d);
    }

    @Override
    public boolean closerThanActual(final IPixelMapTransformSource pTransformSource, final PixelChain pPixelChain, final Point pPoint, final double pMultiplier) {
        final double lambda = closestLambda(pPoint, pPixelChain);
        final double position = getStartPosition() + lambda * getLength(pPixelChain);
        final double actualThickness = getActualThickness(pTransformSource, pPixelChain, position) * pMultiplier;
        return closerThan(pPixelChain, pPoint, actualThickness);
    }


    @Override
    public boolean closerThan(final PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        // TODO is this used any more?
        final double distance = distance(pPixelChain, pPoint);
        return distance < pTolerance;
    }

    @Override
    public double closestLambda(final Point pUVHWPoint, final PixelChain pPixelChain) {
        // Note this is closely related to distance
        final Point C = getP0(pPixelChain).minus(pUVHWPoint);

        final double a = mA.length2();
        final double b = 2.0d * mA.dot(mB);
        final double c = 2.0d * mA.dot(C) + mB.length2();
        final double d = 2.0d * mB.dot(C);
        final double e = C.length2();

        final QuarticEquation distanceSquared = new QuarticEquation(a, b, c, d, e);
        final CubicEquation differential = distanceSquared.differentiate();

        final Root root = differential.solve();

        final double t1 = KMath.limit01(root.getRoot1());
        final Point p1 = getPointFromLambda(pPixelChain, t1);
        final Vector delta1 = p1.minus(pUVHWPoint);
        final double distance1 = delta1.length();
        if (root.getNumberOfRoots() == 1) {
            return t1;
        }

        // I do need the p2 point in this to prevent strange results
        final double t2 = KMath.limit01(root.getRoot2());
        final Point p2 = getPointFromLambda(pPixelChain, t2);
        final Vector delta2 = p2.minus(pUVHWPoint);
        final double distance2 = delta2.length();

        final double t3 = KMath.limit01(root.getRoot3());
        final Point p3 = getPointFromLambda(pPixelChain, t3);
        final Vector delta3 = p3.minus(pUVHWPoint);
        final double distance3 = delta3.length();

        if (distance1 <= distance2 && distance1 <= distance3) {
            return t1;
        }
        if (distance2 <= distance3) {
            return t2;
        }
        return t3;
    }

    @Override
    public double distance(final PixelChain pPixelChain, final Point pUVHWPoint) {
        // note this is closely related to closestLambda
        final Point C = getP0(pPixelChain).minus(pUVHWPoint);

        final double a = mA.length2();
        final double b = 2.0d * mA.dot(mB);
        final double c = 2.0d * mA.dot(C) + mB.length2();
        final double d = 2.0d * mB.dot(C);
        final double e = C.length2();

        final QuarticEquation distanceSquared = new QuarticEquation(a, b, c, d, e);
        final CubicEquation differential = distanceSquared.differentiate();

        final Root root = differential.solve();

        final double t1 = KMath.limit01(root.getRoot1());
        final Point p1 = getPointFromLambda(pPixelChain, t1);
        final Vector delta1 = p1.minus(pUVHWPoint);
        final double distance1 = delta1.length();
        if (root.getNumberOfRoots() == 1) {
            return distance1;
        }

        // I do need the p2 point in this to prevent strange results
        final double t2 = KMath.limit01(root.getRoot2());
        final Point p2 = getPointFromLambda(pPixelChain, t2);
        final Vector delta2 = p2.minus(pUVHWPoint);
        final double distance2 = delta2.length();

        final double t3 = KMath.limit01(root.getRoot3());
        final Point p3 = getPointFromLambda(pPixelChain, t3);
        final Vector delta3 = p3.minus(pUVHWPoint);
        final double distance3 = delta3.length();

        return KMath.min(distance1, distance2, distance3);
    }

    public Point getA() {
        return mA;
    }

    @Override
    public Vector getEndTangentVector(final PixelChain pPixelChain) {
        return getP2P1(pPixelChain).normalize();
    }

    @Override
    public double getLength(final PixelChain pPixelChain) {
        // TODO needs improvement
        return getP0P1(pPixelChain).length() + getP2P1(pPixelChain).length();
    }


    public double getMaxX(final PixelChain pPixelChain) {
        return KMath.max(getStartUHVWPoint(pPixelChain).getX(), getEndUHVWPoint(pPixelChain).getX(), getP1().getX());
    }

    public double getMaxY(final PixelChain pPixelChain) {
        return KMath.max(getStartUHVWPoint(pPixelChain).getY(), getEndUHVWPoint(pPixelChain).getY(), getP1().getY());
    }

    public double getMinX(final PixelChain pPixelChain) {
        return KMath.min(getStartUHVWPoint(pPixelChain).getX(), getEndUHVWPoint(pPixelChain).getX(), getP1().getX());
    }

    public double getMinY(final PixelChain pPixelChain) {
        return KMath.min(getStartUHVWPoint(pPixelChain).getY(), getEndUHVWPoint(pPixelChain).getY(), getP1().getY());
    }

    Point getP0(final PixelChain pPixelChain) {
        return getStartUHVWPoint(pPixelChain);
    }

    /**
     * Gets the Vector from P0 to P1.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the Vector
     */
    private Vector getP0P1(final PixelChain pPixelChain) {
        return getP1().minus(getP0(pPixelChain));
    }

    public Point getP1() {
        return mP1;
    }

    Point getP2(final PixelChain pPixelChain) {
        return getEndUHVWPoint(pPixelChain);
    }

    /**
     * Gets the Vector from P2 to P1
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the Vector
     */
    private Vector getP2P1(final PixelChain pPixelChain) {
        return getP2(pPixelChain).minus(getP1());
    }

    @Override
    public Point getPointFromLambda(final PixelChain pPixelChain, final double pT) {
        return getP0(pPixelChain).multiply((1.0d - pT) * (1.0d - pT)) //
                .add(getP1().multiply(2.0d * (1.0d - pT) * pT)) //
                .add(getP2(pPixelChain).multiply(pT * pT));
    }

    @Override
    public Vector getStartTangentVector(final PixelChain pPixelChain) {
        return getP0P1(pPixelChain).minus().normalize();
    }

    @Override
    public void graffiti(final PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        pGraphics.grafittiControlLine(getP0(pPixelChain), getP1());
        pGraphics.grafittiControlLine(getP1(), getP2(pPixelChain));
        super.graffiti(pPixelChain, pGraphics);
        pGraphics.graffitiControlPoint(getP1());
    }

    @Override
    public String toString() {
        return "CurveSegment[" + super.toString() + "]";
    }

}
