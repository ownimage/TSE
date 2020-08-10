/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutableCurveSegment;
import com.ownimage.perception.pixelMap.immutable.ImmutableStraightSegment;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Logger;


public class SegmentFactory {

    private final static Logger mLogger = Framework.getLogger();

    /**
     * Creates the curve segment starting at pStart, ending at pEnd and the gradient of the curve at the start and end is towards pP1.
     * will return a LineApproximation instead.
     *
     * @param pPixelMap
     * @param the       Pixel Chain performing this operation
     * @param pP1       the point that the start and end gradient goes through
     * @return the curve approximation
     */
    static public ImmutableCurveSegment createTempCurveSegmentTowards(
            @NotNull PixelMap pPixelMap, @NotNull PixelChain pixelChain, int segmentIndex, @NotNull Point p1) {
        try {
            var p0 = pixelChain.getVertex(segmentIndex).getPosition();
            var p2 = pixelChain.getVertex(segmentIndex + 1).getPosition();
            var a = p0.add(p2).minus(p1.multiply(2.0d));
            var b = p1.minus(p0).multiply(2.0d);
            var segment = ImmutableCurveSegment.builder()
                    .segmentIndex(segmentIndex)
                    .startPosition(0.0d)
                    .a(a)
                    .b(b)
                    .p1(p1)
                    .build();
            if (
                    segment.getA().length2() != 0
                            && segment.getMinX(pPixelMap, pixelChain) > 0.0d
                            && segment.getMaxX(pPixelMap, pixelChain) < (double) pPixelMap.width() / pPixelMap.height()
                            && segment.getMinY(pPixelMap, pixelChain) > 0.0d
                            && segment.getMinY(pPixelMap, pixelChain) < 1.0d
            ) {
                return segment;
            } else {
                return null;
            }
        } catch (Exception pT) {
            mLogger.severe(FrameworkLogger.throwableToString(pT));
        }
        return null;
    }

    static public Optional<ImmutableCurveSegment> createOptionalTempCurveSegmentTowards(
            @NotNull PixelMap pPixelMap, @NotNull PixelChain pPixelChain, int pSegmentIndex, @NotNull Point pP1) {
        return Optional.ofNullable(createTempCurveSegmentTowards(pPixelMap, pPixelChain, pSegmentIndex, pP1));
    }


    static public ImmutableStraightSegment createTempStraightSegment(PixelChain pixelChain, int segmentIndex) {
        var temp = ImmutableStraightSegment
                .of(segmentIndex, 0.0d, new LineSegment(Point.Point00, Point.Point11));
        Point a = temp.getStartUHVWPoint(pixelChain);
        Point b = temp.getEndUHVWPoint(pixelChain);
        var lineSegment = new LineSegment(a, b);
        return temp.withLineSegment(lineSegment);
    }

}
