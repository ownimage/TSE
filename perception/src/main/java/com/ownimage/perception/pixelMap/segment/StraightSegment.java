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
import com.ownimage.perception.pixelMap.immutable.PixelMap;

public class StraightSegment extends SegmentBase {


    private final static long serialVersionUID = 1L;

    private final LineSegment mLineSegment;

    StraightSegment(PixelMap pPixelMap, IPixelChain pPixelChain, int pSegmentIndex) {
        this(pPixelMap, pPixelChain, pSegmentIndex, 0.0d);
    }

    private StraightSegment(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            int pSegmentIndex,
            double pStartPosition
    ) {
        super(pSegmentIndex, pStartPosition);
        Point a = getStartUHVWPoint(pPixelMap, pPixelChain);
        Point b = getEndUHVWPoint(pPixelMap, pPixelChain);
        mLineSegment = new LineSegment(a, b);
    }

    private StraightSegment(int pSegmentIndex, double pStartPosition, LineSegment pLineSegment) {
        super(pSegmentIndex, pStartPosition);
        mLineSegment = pLineSegment;
    }

    @Override
    public void graffiti(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            ISegmentGrafittiHelper pGraphics
    ) {
        super.graffiti(pPixelMap, pPixelChain, pGraphics);
    }

    @Override
    public boolean closerThanActual(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            IPixelMapTransformSource pTransformSource,
            Point pPoint,
            double pMultiplier
    ) {
        double lambda = mLineSegment.closestLambda(pPoint);
        double position = getStartPosition() + lambda * getLength(pPixelMap, pPixelChain);
        double actualThickness = getActualThickness(pTransformSource, pPixelChain, position) * pMultiplier;
        return closerThan(pPixelMap, pPixelChain, pPoint, actualThickness);
    }

    @Override
    public boolean closerThan(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            Point pPoint,
            double pTolerance
    ) {
        return mLineSegment.isCloserThan(pPoint, pTolerance);
    }

    @Override
    public double closestLambda(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint) {
        return mLineSegment.closestLambda(pPoint);
    }

    @Override
    public double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint) {
        return mLineSegment.distance(pUVHWPoint);
    }

    private Vector getAB() {
        return mLineSegment.getAB();
    }

    @Override
    public Vector getEndTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getAB().normalize();
    }

    @Override
    public double getLength(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getAB().length();
    }

    @Override
    public double getMaxX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return mLineSegment.getMaxX();
    }

    @Override
    public double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return mLineSegment.getMaxY();
    }

    @Override
    public double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return mLineSegment.getMinX();
    }

    @Override
    public double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return mLineSegment.getMinY();
    }

    @Override
    public Point getPointFromLambda(PixelMap pPixelMap, IPixelChain pPixelChain, double pLambda) {
        return mLineSegment.getPoint(pLambda);
    }

    @Override
    public Vector getStartTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getAB().minus().normalize();
    }

    @Override
    public String toString() {
        return "StraightSegment[" + super.toString() + "]";
    }

    @Override
    public StraightSegment withStartPosition(double pStartPosition) {
        //noinspection FloatingPointEquality
        if (getStartPosition() == pStartPosition) {
            return this;
        }
        return new StraightSegment(getSegmentIndex(), pStartPosition, mLineSegment);
    }

    @Override
    public StraightSegment withSegmentIndex(int segmentIndex) {
        if (getSegmentIndex() == segmentIndex) {
            return this;
        }
        return new StraightSegment(segmentIndex, getStartPosition(), mLineSegment);
    }

}
