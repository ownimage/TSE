/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelMap;

public class StraightSegment extends SegmentBase {


    public final static long serialVersionUID = 1L;

    private final LineSegment mLineSegment;

    StraightSegment(final PixelMap pPixelMap, final IPixelChain pPixelChain, final int pSegmentIndex) {
        this(pPixelMap, pPixelChain, pSegmentIndex, 0.0d);
    }

    StraightSegment(
            final PixelMap pPixelMap,
            final IPixelChain pPixelChain,
            final int pSegmentIndex,
            final double pStartPosition
    ) {
        super(pSegmentIndex, pStartPosition);
        final Point a = getStartUHVWPoint(pPixelMap, pPixelChain);
        final Point b = getEndUHVWPoint(pPixelMap, pPixelChain);
        mLineSegment = new LineSegment(a, b);
    }

    private StraightSegment(int pSegmentIndex, double pStartPosition, final LineSegment pLineSegment) {
        super(pSegmentIndex, pStartPosition);
        mLineSegment = pLineSegment;
    }

    @Override
    public void graffiti(
            final PixelMap pPixelMap,
            final IPixelChain pPixelChain,
            final ISegmentGrafittiHelper pGraphics
    ) {
        super.graffiti(pPixelMap, pPixelChain, pGraphics);
    }

    @Override
    public boolean closerThanActual(
            final PixelMap pPixelMap,
            final IPixelChain pPixelChain,
            final IPixelMapTransformSource pTransformSource,
            final Point pPoint,
            final double pMultiplier
    ) {
        final double lambda = mLineSegment.closestLambda(pPoint);
        final double position = getStartPosition() + lambda * getLength(pPixelMap, pPixelChain);
        final double actualThickness = getActualThickness(pTransformSource, pPixelChain, position) * pMultiplier;
        return closerThan(pPixelMap, pPixelChain, pPoint, actualThickness);
    }

    @Override
    public boolean closerThan(
            final PixelMap pPixelMap,
            final IPixelChain pPixelChain,
            final Point pPoint,
            final double pTolerance
    ) {
        return mLineSegment.isCloserThan(pPoint, pTolerance);
    }

    @Override
    public double closestLambda(final Point pPoint, final IPixelChain pPixelChain, final PixelMap pPixelMap) {
        return mLineSegment.closestLambda(pPoint);
    }

    @Override
    public double distance(final PixelMap pPixelMap, final IPixelChain pPixelChain, final Point pUVHWPoint) {
        return mLineSegment.distance(pUVHWPoint);
    }

    private Vector getAB() {
        return mLineSegment.getAB();
    }

    @Override
    public Vector getEndTangentVector(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getAB().normalize();
    }

    @Override
    public double getLength(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getAB().length();
    }

    @Override
    public double getMaxX(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return mLineSegment.getMaxX();
    }

    @Override
    public double getMaxY(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return mLineSegment.getMaxY();
    }

    @Override
    public double getMinX(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return mLineSegment.getMinX();
    }

    @Override
    public double getMinY(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return mLineSegment.getMinY();
    }

    @Override
    public Point getPointFromLambda(final PixelMap pPixelMap, final IPixelChain pPixelChain, final double pLambda) {
        return mLineSegment.getPoint(pLambda);
    }

    @Override
    public Vector getStartTangentVector(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return getAB().minus().normalize();
    }

    @Override
    public String toString() {
        return "StraightSegment[" + super.toString() + "]";
    }

    @Override
    public StraightSegment withStartPosition(final double pStartPosition) {
        if (getStartPosition() == pStartPosition) return this;
        return new StraightSegment(getSegmentIndex(), getStartPosition(), mLineSegment);
    }

}
