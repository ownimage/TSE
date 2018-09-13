/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelChain;

public class StraightSegment extends SegmentBase<StraightSegment> {


    public final static long serialVersionUID = 1L;

    private final LineSegment mLineSegment;

    StraightSegment(final PixelChain pPixelChain, final int pSegmentIndex) {
        super(pSegmentIndex);
        final Point a = getStartUHVWPoint(pPixelChain);
        final Point b = getEndUHVWPoint(pPixelChain);
        mLineSegment = new LineSegment(a, b);
    }

    @Override
    public void graffiti(final PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        super.graffiti(pPixelChain, pGraphics);
    }

    @Override
    public boolean closerThanActual(final IPixelMapTransformSource pTransformSource, final PixelChain pPixelChain, final Point pPoint, final double pMultiplier) {
        final double lambda = mLineSegment.closestLambda(pPoint);
        final double position = getStartPosition() + lambda * getLength(pPixelChain);
        final double actualThickness = getActualThickness(pTransformSource, pPixelChain, position) * pMultiplier;
        return closerThan(pPixelChain, pPoint, actualThickness);
    }

    @Override
    public boolean closerThan(final PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        return mLineSegment.isCloserThan(pPoint, pTolerance);
    }

    @Override
    public double closestLambda(final Point pPoint, final PixelChain pPixelChain) {
        return mLineSegment.closestLambda(pPoint);
    }

    @Override
    public double distance(final PixelChain pPixelChain, final Point pUVHWPoint) {
        return mLineSegment.distance(pUVHWPoint);
    }

    private Vector getAB() {
        return mLineSegment.getAB();
    }

    @Override
    public Vector getEndTangentVector(final PixelChain pPixelChain) {
        return getAB().normalize();
    }

    @Override
    public double getLength(final PixelChain pPixelChain) {
        return getAB().length();
    }

    @Override
    public double getMaxX(final PixelChain pPixelChain) {
        return mLineSegment.getMaxX();
    }

    @Override
    public double getMaxY(final PixelChain pPixelChain) {
        return mLineSegment.getMaxY();
    }

    @Override
    public double getMinX(final PixelChain pPixelChain) {
        return mLineSegment.getMinX();
    }

    @Override
    public double getMinY(final PixelChain pPixelChain) {
        return mLineSegment.getMinY();
    }

    @Override
    public Point getPointFromLambda(final PixelChain pPixelChain, final double pLambda) {
        return mLineSegment.getPoint(pLambda);
    }

    @Override
    public Vector getStartTangentVector(final PixelChain pPixelChain) {
        return getAB().minus().normalize();
    }

    @Override
    public String toString() {
        return "StraightSegment[" + super.toString() + "]";
    }

}
