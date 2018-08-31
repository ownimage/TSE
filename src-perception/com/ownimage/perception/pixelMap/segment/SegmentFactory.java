/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.PixelChain;

import java.util.logging.Logger;


public class SegmentFactory {

	public enum SegmentType {
		Straight, Curve, DoubleCurve
	}


    public final static Logger mLogger = Framework.getLogger();

    ;

	/**
	 * Creates the curve segment starting at pStart, ending at pEnd and going through pThrough. In the event that the line collapses to a straight line then it will return a LineApproximation instead.
	 *
	 *
	 * @param pPixelChain
	 * @param pStart
	 *            the start vertex
	 * @param pEnd
	 *            the end vertex
	 * @param pThrough
	 *            the point that the curve goes through
	 * @return the curve approximation
	 */
	static public ISegment createTempCurveSegmentThrough(PixelChain pPixelChain, final IVertex pStart, final IVertex pEnd, final Point pThrough) {
		final Point point1 = pThrough.multiply(2.0d).minus(pStart.getUHVWPoint(pPixelChain).multiply(0.5d)).minus(pEnd.getUHVWPoint(pPixelChain).multiply(0.5d));
		final CurveSegment segment = new CurveSegment(pPixelChain, pStart, pEnd, point1);
		if (segment.getA().length2() != 0) {
			return segment;
		} else {
			return createTempStraightSegment(pStart, pEnd, pPixelChain);
		}
	}

	/**
	 * Creates the curve segment starting at pStart, ending at pEnd and the gradient of the curve at the start and end is towards pP1. In the event that the line collapses to a straight line then it
	 * will return a LineApproximation instead.
	 *
	 *
	 * @param pPixelChain
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

	public static ISegment createTempDoubleCurveSegment(PixelChain pPixelChain, final IVertex pStartVertex, final Line pStartTangent, final IVertex pEndVertex, final Line pEndTangent, final IVertex pThrough) {

		final Line midSlope = new Line(pStartVertex.getUHVWPoint(pPixelChain), pEndVertex.getUHVWPoint(pPixelChain));

		final Line midTangent = new Line(pThrough.getUHVWPoint(pPixelChain), pThrough.getUHVWPoint(pPixelChain).add(midSlope.getAB()));
		final Point startP1 = midTangent.intersect(pStartTangent);
		final Point endP1 = midTangent.intersect(pEndTangent);

		if (startP1 == null || endP1 == null) {
			return SegmentFactory.createTempStraightSegment(pStartVertex, pEndVertex, pPixelChain);
		}

		final ISegment startCurve = SegmentFactory.createTempCurveSegmentTowards(pPixelChain, pStartVertex, pThrough, startP1);
		final ISegment endCurve = SegmentFactory.createTempCurveSegmentTowards(pPixelChain, pThrough, pEndVertex, endP1);

		// test for error
		if (pStartTangent.closestLambda(startP1) < 0.0d || midTangent.closestLambda(startP1) > 0.0d //
				|| midTangent.closestLambda(endP1) < 0.0d || pEndTangent.closestLambda(endP1) > 0.0d //
				|| !(startCurve instanceof CurveSegment) || !(endCurve instanceof CurveSegment) //
		) {
			return null;
		}

		return new DoubleCurveSegment(pStartVertex, pStartTangent, (CurveSegment) startCurve, pEndVertex, pEndTangent, (CurveSegment) endCurve, pThrough, pPixelChain);
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
		final StraightSegment segment = new StraightSegment(pPixelChain, pStart, pEnd);
		return segment;
	}

}
