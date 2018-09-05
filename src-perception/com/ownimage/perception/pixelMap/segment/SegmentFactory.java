/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

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
	 * Creates the curve segment starting at pStart, ending at pEnd and the gradient of the curve at the start and end is towards pP1. In the event that the line collapses to a straight line then it
	 * will return a LineApproximation instead.
	 *
	 *
	 * @param pPixelChain the Pixel Chain performing this operation
	 * @param pStart
	 *            the start vertex
	 * @param pEnd
	 *            the end vertex
	 * @param pP1
	 *            the point that the start and end gradient goes through
	 * @return the curve approximation
	 */
	static public ISegment createTempCurveSegmentTowards(PixelChain pPixelChain, final IVertex pStart, final IVertex pEnd, final Point pP1) {
		try {
			final CurveSegment segment = new CurveSegment(pPixelChain, pStart, pEnd, pP1);
			if (segment.getA().length2() != 0) {
				return segment;
			} else {
				return null; // createTempStraightSegment(pStart, pEnd);
			}
		} catch (Throwable pT) {
			mLogger.severe("Error " + pT.getMessage());
		}
		return null;
	}

	public static ISegment createTempDoubleCurveSegment(PixelChain pPixelChain, final IVertex pStartVertex, final Point pP1, final IVertex pMidVertex, final Point pP2, final IVertex pEndVertex) {
		// note that no checking is done that the parameters give a sensible DoubleCurve
		CurveSegment startCurve = (CurveSegment) createTempCurveSegmentTowards(pPixelChain, pStartVertex, pMidVertex, pP1);
		CurveSegment endCurve = (CurveSegment) createTempCurveSegmentTowards(pPixelChain, pMidVertex, pEndVertex, pP2);

		// Vector x = startCurve.getEndTangentVector();
		// Vector y = endCurve.getStartTangentVector();

		if (startCurve != null && endCurve != null) {
			return new DoubleCurveSegment(pPixelChain, pStartVertex, startCurve, pEndVertex, endCurve, pMidVertex);
		} else {
			return null;
		}
	}

	static public StraightSegment createTempStraightSegment(final IVertex pStart, final IVertex pEnd, PixelChain pPixelChain) {
		return new StraightSegment(pPixelChain, pStart, pEnd);
	}

}
