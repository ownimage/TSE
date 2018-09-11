/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.perception.math.LineSegment;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.PixelChain;

public class StraightSegment extends SegmentBase {


    public final static long serialVersionUID = 1L;

    private final LineSegment mLineSegment;

    StraightSegment(PixelChain pPixelChain, final int pSegmentIndex) {
        super(pSegmentIndex);
        final Point a = getStartUHVWPoint(pPixelChain);
        final Point b = getEndUHVWPoint(pPixelChain);
        mLineSegment = new LineSegment(a, b);
    }

    @Override
    public void graffiti(PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        super.graffiti(pPixelChain, pGraphics);
    }

    @Override
    public boolean closerThanActual(final PixelChain pPixelChain, final Point pPoint, double pMultiplier) {
        final double lambda = mLineSegment.closestLambda(pPoint);
        final double position = getStartPosition() + lambda * getLength(pPixelChain);
        final double actualThickness = getActualThickness(pPixelChain, position) * pMultiplier;
        return closerThan(pPixelChain, pPoint, actualThickness);
    }

    @Override
    public boolean closerThan(PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        return mLineSegment.isCloserThan(pPoint, pTolerance);
    }

    @Override
    public double closestLambda(final Point pPoint, PixelChain pPixelChain) {
        return mLineSegment.closestLambda(pPoint);
    }

    @Override
    public double distance(PixelChain pPixelChain, final Point pUVHWPoint) {
        return mLineSegment.distance(pUVHWPoint);
    }

    public Vector getAB() {
        return mLineSegment.getAB();
    }

    @Override
    public Vector getEndTangentVector(PixelChain pPixelChain) {
        return getAB().normalize();
    }

    @Override
    public double getLength(PixelChain pPixelChain) {
        return getAB().length();
    }

    @Override
    public double getMaxX(PixelChain pPixelChain) {
        return mLineSegment.getMaxX();
    }

    @Override
    public double getMaxY(PixelChain pPixelChain) {
        return mLineSegment.getMaxY();
    }

    @Override
    public double getMinX(PixelChain pPixelChain) {
        return mLineSegment.getMinX();
    }

    @Override
    public double getMinY(PixelChain pPixelChain) {
        return mLineSegment.getMinY();
    }

    @Override
    public Point getPointFromLambda(PixelChain pPixelChain, final double pLambda) {
        return mLineSegment.getPoint(pLambda);
    }

    @Override
    public Vector getStartTangentVector(PixelChain pPixelChain) {
        return getAB().minus().normalize();
    }

    @Override
    public double length() {
        return mLineSegment.length();
    }


    @Override
    public String toString() {
        return "StraightSegment[" + super.toString() + "]";
    }


}
