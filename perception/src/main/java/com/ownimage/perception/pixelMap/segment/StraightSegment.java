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

public class StraightSegment implements ISegment {


    private final static long serialVersionUID = 1L;

    private final int segmentIndex;

    @Override
    public int getSegmentIndex() {
        return segmentIndex;
    }

    public double getStartPosition() {
        return startPosition;
    }

    private final double startPosition;
    private final LineSegment lineSegment;

    StraightSegment(PixelMap pPixelMap, IPixelChain pPixelChain, int pSegmentIndex) {
        this(pPixelMap, pPixelChain, pSegmentIndex, 0.0d);
    }

    private StraightSegment(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            int segmentIndex,
            double startPosition
    ) {
        this.segmentIndex = segmentIndex;
        this.startPosition = startPosition;
        Point a = getStartUHVWPoint(pPixelMap, pPixelChain);
        Point b = getEndUHVWPoint(pPixelMap, pPixelChain);
        this.lineSegment = new LineSegment(a, b);
    }

    private StraightSegment(int segmentIndex, double startPosition, LineSegment lineSegment) {
        this.segmentIndex = segmentIndex;
        this.startPosition = startPosition;
        this.lineSegment = lineSegment;
    }

    public LineSegment getLineSegment() {
        return lineSegment;
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
        double lambda = lineSegment.closestLambda(pPoint);
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
        return lineSegment.isCloserThan(pPoint, pTolerance);
    }

    @Override
    public double closestLambda(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint) {
        return lineSegment.closestLambda(pPoint);
    }

    @Override
    public double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint) {
        return lineSegment.distance(pUVHWPoint);
    }

    private Vector getAB() {
        return lineSegment.getAB();
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
        return lineSegment.getMaxX();
    }

    @Override
    public double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return lineSegment.getMaxY();
    }

    @Override
    public double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return lineSegment.getMinX();
    }

    @Override
    public double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return lineSegment.getMinY();
    }

    @Override
    public Point getPointFromLambda(PixelMap pPixelMap, IPixelChain pPixelChain, double pLambda) {
        return lineSegment.getPoint(pLambda);
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
        return new StraightSegment(getSegmentIndex(), pStartPosition, lineSegment);
    }

}
