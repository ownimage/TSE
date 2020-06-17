/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;

import java.util.Optional;
import java.util.logging.Logger;


public class SegmentFactory {

    public enum SegmentType {
        Straight, Curve, DoubleCurve
    }


    private final static Logger mLogger = Framework.getLogger();

    /**
     * Creates the curve segment starting at pStart, ending at pEnd and the gradient of the curve at the start and end is towards pP1.
     * will return a LineApproximation instead.
     *
     *
     * @param pPixelMap
     * @param pPixelChain the Pixel Chain performing this operation
     * @param pP1         the point that the start and end gradient goes through
     * @return the curve approximation
     */
    static public CurveSegment createTempCurveSegmentTowards(PixelMap pPixelMap, IPixelChain pPixelChain, int pSegmentIndex, Point pP1) {
        try {
            CurveSegment segment = new CurveSegment(pPixelMap, pPixelChain, pSegmentIndex, pP1);
            if (
                    segment.getA().length2() != 0
                    && segment.getMinX(pPixelMap, pPixelChain) > 0.0d
                    && segment.getMaxX(pPixelMap, pPixelChain) < (double) pPixelMap.getWidth() / pPixelMap.getHeight()
                    && segment.getMinY(pPixelMap, pPixelChain) > 0.0d
                    && segment.getMinY(pPixelMap, pPixelChain) < 1.0d
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

    static public Optional<CurveSegment> createOptionalTempCurveSegmentTowards(PixelMap pPixelMap, IPixelChain pPixelChain, int pSegmentIndex, Point pP1) {
        return Optional.ofNullable(createTempCurveSegmentTowards(pPixelMap, pPixelChain, pSegmentIndex, pP1));
    }


    static public StraightSegment createTempStraightSegment(PixelMap pPixelMap, IPixelChain pPixelChain, int pSegmentIndex) {
        return new StraightSegment(pPixelMap, pPixelChain, pSegmentIndex);
    }

}
