/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.PixelChain;

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
     * @param pPixelChain the Pixel Chain performing this operation
     * @param pP1         the point that the start and end gradient goes through
     * @return the curve approximation
     */
    static public CurveSegment createTempCurveSegmentTowards(PixelChain pPixelChain, final int pSegmentIndex, final Point pP1) {
        try {
            final CurveSegment segment = new CurveSegment(pPixelChain, pSegmentIndex, pP1);
            if (segment.getA().length2() != 0) {
                return segment;
            } else {
                return null;
            }
        } catch (Throwable pT) {
            mLogger.severe(FrameworkLogger.throwableToString(pT));
        }
        return null;
    }

    //public static ISegment createTempDoubleCurveSegment(PixelChain pPixelChain, final int pSegmentIndex, final CurveSegment pStartCurve, final CurveSegment pEndCurve, final IVertex pThrough) {
    public static ISegment createTempDoubleCurveSegment(final PixelChain pPixelChain, final int pSegmentIndex, final Point pP1, final IVertex pMidVertex, final Point pP2) {

        try {
            // note that no checking is done that the parameters give a sensible DoubleCurve
            CurveSegment startCurve = new CurveSegment(pPixelChain, pSegmentIndex, pP1) {
                @Override
                public IVertex getEndVertex(PixelChain pPixelChain) {
                    return pMidVertex;
                }
            };
            CurveSegment endCurve = new CurveSegment(pPixelChain, pSegmentIndex, pP2) {
                @Override
                public IVertex getStartVertex(PixelChain pPixelChain) {
                    return pMidVertex;
                }
            };
            return new DoubleCurveSegment(pPixelChain, startCurve, endCurve);

        } catch (Throwable pT) {
            return null;
        }
    }

    static public StraightSegment createTempStraightSegment(PixelChain pPixelChain, final int pSegmentIndex) {
        return new StraightSegment(pPixelChain, pSegmentIndex);
    }

}
