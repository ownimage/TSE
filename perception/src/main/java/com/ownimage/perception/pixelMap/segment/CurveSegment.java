/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.*;
import com.ownimage.framework.math.CubicEquation.Root;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelMap;

import java.awt.*;
import java.util.logging.Logger;

public class CurveSegment extends SegmentBase {

    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final Point mP1;
    private final Point mA;
    private final Point mB;

    CurveSegment(final PixelMap pPixelMap, final IPixelChain pPixelChain, final int pSegmentIndex, final Point pP1) {
        this(pPixelMap, pPixelChain, pSegmentIndex, pP1, 0.0d);
    }

    public CurveSegment(final PixelMap pPixelMap, final IPixelChain pPixelChain, final int pSegmentIndex, final Point pP1, final double pStartPosition) {
        super(pSegmentIndex, pStartPosition);
        mP1 = pP1;
        mA = getP0(pPixelMap, pPixelChain).add(getP2(pPixelMap, pPixelChain)).minus(getP1().multiply(2.0d));
        mB = getP1().minus(getP0(pPixelMap, pPixelChain)).multiply(2.0d);
    }

    @Override
    public boolean closerThanActual(final PixelMap pPixelMap, final IPixelChain pPixelChain, final IPixelMapTransformSource pTransformSource, final Point pPoint, final double pMultiplier) {
        final double lambda = closestLambda(pPoint, pPixelChain, pPixelMap);
        final double position = getStartPosition() + lambda * getLength(pPixelMap, pPixelChain);
        final double actualThickness = getActualThickness(pTransformSource, pPixelChain, position) * pMultiplier;
        return closerThan(pPixelMap, pPixelChain, pPoint, actualThickness);
    }

    @Override
    public boolean closerThan(final PixelMap pPixelMap, final IPixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        // TODO is this used any more?
        final double distance = distance(pPixelMap, pPixelChain, pPoint);
        return distance < pTolerance;
    }

    @Override
    public double closestLambda(final Point pUVHWPoint, final IPixelChain pPixelChain, final PixelMap pPixelMap) {
        // Note this is closely related to distance
        final Point C = getP0(pPixelMap, pPixelChain).minus(pUVHWPoint);

        final double a = mA.length2();
        final double b = 2.0d * mA.dot(mB);
        final double c = 2.0d * mA.dot(C) + mB.length2();
        final double d = 2.0d * mB.dot(C);
        final double e = C.length2();

        final QuarticEquation distanceSquared = new QuarticEquation(a, b, c, d, e);
        final CubicEquation differential = distanceSquared.differentiate();

        final Root root = differential.solve();

        final double t1 = KMath.limit01(root.getRoot1());
        final Point p1 = getPointFromLambda(pPixelMap, pPixelChain, t1);
        final Vector delta1 = p1.minus(pUVHWPoint);
        final double distance1 = delta1.length();
        if (root.getNumberOfRoots() == 1) {
            return t1;
        }

        // I do need the p2 point in this to prevent strange results
        final double t2 = KMath.limit01(root.getRoot2());
        final Point p2 = getPointFromLambda(pPixelMap, pPixelChain, t2);
        final Vector delta2 = p2.minus(pUVHWPoint);
        final double distance2 = delta2.length();

        final double t3 = KMath.limit01(root.getRoot3());
        final Point p3 = getPointFromLambda(pPixelMap, pPixelChain, t3);
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
    public double distance(final PixelMap pPixelMap, final IPixelChain pPixelChain, final Point pUVHWPoint) {
        // note this is closely related to closestLambda
        final Point C = getP0(pPixelMap, pPixelChain).minus(pUVHWPoint);

        final double a = mA.length2();
        final double b = 2.0d * mA.dot(mB);
        final double c = 2.0d * mA.dot(C) + mB.length2();
        final double d = 2.0d * mB.dot(C);
        final double e = C.length2();

        final QuarticEquation distanceSquared = new QuarticEquation(a, b, c, d, e);
        final CubicEquation differential = distanceSquared.differentiate();

        final Root root = differential.solve();

        final double t1 = KMath.limit01(root.getRoot1());
        final Point p1 = getPointFromLambda(pPixelMap, pPixelChain, t1);
        final Vector delta1 = p1.minus(pUVHWPoint);
        final double distance1 = delta1.length();
        if (root.getNumberOfRoots() == 1) {
            return distance1;
        }

        // I do need the p2 point in this to prevent strange results
        final double t2 = KMath.limit01(root.getRoot2());
        final Point p2 = getPointFromLambda(pPixelMap, pPixelChain, t2);
        final Vector delta2 = p2.minus(pUVHWPoint);
        final double distance2 = delta2.length();

        final double t3 = KMath.limit01(root.getRoot3());
        final Point p3 = getPointFromLambda(pPixelMap, pPixelChain, t3);
        final Vector delta3 = p3.minus(pUVHWPoint);
        final double distance3 = delta3.length();

        return KMath.min(distance1, distance2, distance3);
    }

    public Point getA() {
        return mA;
    }

    @Override
    public Vector getEndTangentVector(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getP2P1(pPixelChain, pPixelMap).normalize();
    }

    @Override
    public double getLength(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        // TODO needs improvement
        return getP0P1(pPixelMap, pPixelChain).length() + getP2P1(pPixelChain, pPixelMap).length();
    }

    @Override
    public double getMaxX(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return KMath.max(getStartUHVWPoint(pPixelMap, pPixelChain).getX(), getEndUHVWPoint(pPixelMap, pPixelChain).getX(), getP1().getX());
    }

    @Override
    public double getMaxY(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return KMath.max(getStartUHVWPoint(pPixelMap, pPixelChain).getY(), getEndUHVWPoint(pPixelMap, pPixelChain).getY(), getP1().getY());
    }

    @Override
    public double getMinX(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return KMath.min(getStartUHVWPoint(pPixelMap, pPixelChain).getX(), getEndUHVWPoint(pPixelMap, pPixelChain).getX(), getP1().getX());
    }

    @Override
    public double getMinY(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return KMath.min(getStartUHVWPoint(pPixelMap, pPixelChain).getY(), getEndUHVWPoint(pPixelMap, pPixelChain).getY(), getP1().getY());
    }

    Point getP0(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getStartUHVWPoint(pPixelMap, pPixelChain);
    }

    /**
     * Gets the Vector from P0 to P1.
     *
     * @param pPixelMap   the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the Vector
     */
    private Vector getP0P1(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getP1().minus(getP0(pPixelMap, pPixelChain));
    }

    public Point getP1() {
        return mP1;
    }

    Point getP2(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getEndUHVWPoint(pPixelMap, pPixelChain);
    }

    /**
     * Gets the Vector from P2 to P1
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @param pPixelMap   the PixelMap performing the this operation
     * @return the Vector
     */
    private Vector getP2P1(final IPixelChain pPixelChain, final PixelMap pPixelMap) {
        return getP2(pPixelMap, pPixelChain).minus(getP1());
    }

    @Override
    public Point getPointFromLambda(final PixelMap pPixelMap, final IPixelChain pPixelChain, final double pT) {
        return getP0(pPixelMap, pPixelChain).multiply((1.0d - pT) * (1.0d - pT)) //
                .add(getP1().multiply(2.0d * (1.0d - pT) * pT)) //
                .add(getP2(pPixelMap, pPixelChain).multiply(pT * pT));
    }

    @Override
    public Vector getStartTangentVector(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getP0P1(pPixelMap, pPixelChain).minus().normalize();
    }

    @Override
    public void graffiti(
            final PixelMap pPixelMap,
            final IPixelChain pPixelChain,
            final ISegmentGrafittiHelper pGraphics
    ) {
        var c = (getStartVertex(pPixelChain).isPositionSpecified() || getEndVertex(pPixelChain).isPositionSpecified())
                ? Color.RED : Color.BLACK;
        pGraphics.graffitiLine(getP0(pPixelMap, pPixelChain), getP1(), c);
        pGraphics.graffitiLine(getP1(), getP2(pPixelMap, pPixelChain), c);
        pGraphics.graffitiLine(getP0(pPixelMap, pPixelChain), getP2(pPixelMap, pPixelChain), c);
        //super.graffiti(pPixelMap, pPixelChain, pGraphics);
        pGraphics.graffitiControlPoint(getP1());
    }

    @Override
    public String toString() {
        return "CurveSegment[" + super.toString() + "]";
    }

    @Override
    public CurveSegment withStartPosition(
            final PixelMap pPixelMap,
            final IPixelChain pPixelChain,
            final double pStartPosition
    ) {
        if (getStartPosition() == pStartPosition) return this;
        return new CurveSegment(pPixelMap, pPixelChain, getSegmentIndex(), getP1(), pStartPosition);
    }
}
