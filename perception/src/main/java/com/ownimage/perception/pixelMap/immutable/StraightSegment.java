package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import org.immutables.value.Value;

@Value.Immutable
public interface StraightSegment extends Segment {

    @Override
    @Value.Parameter(order = 1)
    int getSegmentIndex();

    @Override
    @Value.Parameter(order = 2)
    double getStartPosition();

    @Value.Parameter(order = 3)
    LineSegment getLineSegment();

    @Override
    default Segment toImmutable() {
        return ImmutableStraightSegment.copyOf(this);
    }

    @Override
    default boolean closerThanActual(
            PixelMap pPixelMap,
            PixelChain pPixelChain,
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
            PixelChain pPixelChain,
            Point pPoint,
            double pTolerance
    ) {
        return getLineSegment().isCloserThan(pPoint, pTolerance);
    }

    @Override
    default double closestLambda(PixelMap pPixelMap, PixelChain pPixelChain, Point pPoint) {
        return getLineSegment().closestLambda(pPoint);
    }

    @Override
    default double distance(PixelMap pPixelMap, PixelChain pPixelChain, Point pUVHWPoint) {
        return getLineSegment().distance(pUVHWPoint);
    }

    default Vector getAB() {
        return getLineSegment().getAB();
    }

    @Override
    default Vector getEndTangentVector(PixelMap pPixelMap, PixelChain pPixelChain) {
        return getAB().normalize();
    }

    @Override
    default double getLength(PixelMap pPixelMap, PixelChain pPixelChain) {
        return getAB().length();
    }

    @Override
    default double getMaxX(PixelMap pPixelMap, PixelChain pPixelChain) {
        return getLineSegment().getMaxX();
    }

    @Override
    default double getMaxY(PixelMap pPixelMap, PixelChain pPixelChain) {
        return getLineSegment().getMaxY();
    }

    @Override
    default double getMinX(PixelMap pPixelMap, PixelChain pPixelChain) {
        return getLineSegment().getMinX();
    }

    @Override
    default double getMinY(PixelMap pPixelMap, PixelChain pPixelChain) {
        return getLineSegment().getMinY();
    }

    @Override
    default Point getPointFromLambda(PixelMap pPixelMap, PixelChain pPixelChain, double pLambda) {
        return getLineSegment().getPoint(pLambda);
    }

    @Override
    default Vector getStartTangentVector(PixelMap pPixelMap, PixelChain pPixelChain) {
        return getAB().minus().normalize();
    }

    @Override
    default StraightSegment withStartPosition(double startPosition) {
        return ImmutableStraightSegment.copyOf(this).withStartPosition(startPosition);
    }

    @Override
    default StraightSegment withSegmentIndex(int segmentIndex) {
        return ImmutableStraightSegment.copyOf(this).withSegmentIndex(segmentIndex);
    }
}
