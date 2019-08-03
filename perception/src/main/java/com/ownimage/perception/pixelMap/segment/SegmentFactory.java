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


    public final static Logger mLogger = Framework.getLogger();

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
    static public CurveSegment createTempCurveSegmentTowards(final PixelMap pPixelMap, final PixelChain pPixelChain, final int pSegmentIndex, final Point pP1) {
        try {
            final CurveSegment segment = new CurveSegment(pPixelMap, pPixelChain, pSegmentIndex, pP1);
            if (segment.getA().length2() != 0) {
                return segment;
            } else {
                return null;
            }
        } catch (final Throwable pT) {
            mLogger.severe(FrameworkLogger.throwableToString(pT));
        }
        return null;
    }

    static public Optional<CurveSegment> createOptionalTempCurveSegmentTowards(final PixelMap pPixelMap, final PixelChain pPixelChain, final int pSegmentIndex, final Point pP1) {
        return Optional.of(createTempCurveSegmentTowards(pPixelMap, pPixelChain, pSegmentIndex, pP1));
    }


    static public StraightSegment createTempStraightSegment(final PixelMap pPixelMap, final IPixelChain pPixelChain, final int pSegmentIndex) {
        return new StraightSegment(pPixelMap, pPixelChain, pSegmentIndex);
    }

}
