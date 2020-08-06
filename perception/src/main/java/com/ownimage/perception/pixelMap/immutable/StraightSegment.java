package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;

public interface StraightSegment extends Segment {

    @Override
    int getSegmentIndex();

    @Override
    double getStartPosition();

    LineSegment getLineSegment();

    @Override
    default boolean closerThanActual(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            IPixelMapTransformSource pTransformSource,
            Point pPoint,
            double pMultiplier
    ) {
        double lambda = getLineSegment().closestLambda(pPoint);
        double position = getStartPosition() + lambda * getLength(pPixelMap, pPixelChain);
        double actualThickness = getActualThickness(pTransformSource, pPixelChain, position) * pMultiplier;
        return closerThan(pPixelMap, pPixelChain, pPoint, actualThickness);
    }

    @Override
    default boolean closerThan(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            Point pPoint,
            double pTolerance
    ) {
        return getLineSegment().isCloserThan(pPoint, pTolerance);
    }

    @Override
    default double closestLambda(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint) {
        return getLineSegment().closestLambda(pPoint);
    }

    @Override
    default double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint) {
        return getLineSegment().distance(pUVHWPoint);
    }

    default Vector getAB() {
        return getLineSegment().getAB();
    }

    @Override
    default Vector getEndTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getAB().normalize();
    }

    @Override
    default double getLength(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getAB().length();
    }

    @Override
    default double getMaxX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getLineSegment().getMaxX();
    }

    @Override
    default double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getLineSegment().getMaxY();
    }

    @Override
    default double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getLineSegment().getMinX();
    }

    @Override
    default double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getLineSegment().getMinY();
    }

    @Override
    default Point getPointFromLambda(PixelMap pPixelMap, IPixelChain pPixelChain, double pLambda) {
        return getLineSegment().getPoint(pLambda);
    }

    @Override
    default Vector getStartTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getAB().minus().normalize();
    }

    @Override
    default StraightSegment withStartPosition(double pStartPosition) {
        //noinspection FloatingPointEquality
        if (getStartPosition() == pStartPosition) {
            return this;
        }
        return new com.ownimage.perception.pixelMap.segment.StraightSegment(getSegmentIndex(), pStartPosition, getLineSegment());
    }

    @Override
    default StraightSegment withSegmentIndex(int segmentIndex) {
        if (getSegmentIndex() == segmentIndex) {
            return this;
        }
        return new com.ownimage.perception.pixelMap.segment.StraightSegment(segmentIndex, getStartPosition(), getLineSegment());
    }
}
