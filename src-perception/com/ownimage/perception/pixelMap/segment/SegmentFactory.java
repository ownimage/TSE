/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.LineSegment;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Vertex;


public class SegmentFactory {

	public enum SegmentType {
		Straight, Curve, DoubleCurve
	}


    public final static Logger mLogger = Framework.getLogger();

	public final static long serialVersionUID = 1L;;

	static public ISegment changeSegmentType(final ISegment pSegment, final SegmentType pSegmentType) {
		ISegment newSegment = null;

		switch (pSegmentType) {
		case Curve:
			newSegment = changeToCurveSegment(pSegment);
			break;
		case DoubleCurve:
			newSegment = changeToDoubleCurveSegment(pSegment);
			break;
		case Straight:
			newSegment = changeToStraightSegment(pSegment);
			break;
		}
		return newSegment;
	}

	/**
	 * Changes the supplied pSegment to a CurveSegment. If the pSegment is already a CurvetSegment this will do nothing and return pSegment. Otherwise it will create a new CurveSegment from the start
	 * and end vertexes of pSegment and attach it to those vertexes. t is intended to be called from user action changing the types of segments.
	 * 
	 * @param pSegment
	 *            the segment. This must not be null.
	 * @return the Segment that joins the start and end vertexes.
	 */
	static public ISegment changeToCurveSegment(final ISegment pSegment) {
		if (pSegment == null) {
			throw new IllegalArgumentException("pSegment must not be null");
		}

		if (pSegment instanceof CurveSegment) {
			return pSegment;
		}

		final IVertex start = pSegment.getStartVertex();
		final IVertex end = pSegment.getEndVertex();
		Point through = start.getTangent().intersect(end.getTangent());

		if (through == null) {
			final LineSegment line = new LineSegment(start.getUHVWPoint(), end.getUHVWPoint());
			final LineSegment bisector = line.bisector();

			if (start.getTangent() == null && end.getTangent() == null) {
				through = bisector.getPoint(line.length() / 2.0d);

			} else if (start.getTangent() == null) {
				through = end.getTangent().intersect(bisector);

			} else if (end.getTangent() == null) {
				through = start.getTangent().intersect(bisector);

			}

			if (through == null) {
				through = bisector.getPoint(line.length() / 2.0d);
			}
		}

		final ISegment segment = createTempCurveSegmentThrough(start, end, through);
		segment.attachToVertexes(true);
		segment.getPixelMap().indexSegments();
		return segment;
	}

	/**
	 * Changes the supplied pSegment to a DoubleCurveSegment. If the pSegment is already a DoubleCurveSegment this will do nothing and return pSegment. Otherwise it will create a new
	 * DoubleCurveSegment from the start and end vertexes of pSegment and attach it to those vertexes. It is intended to be called from user action changing the types of segments.
	 * 
	 * @param pSegment
	 *            the segment. This must not be null.
	 * @return the Segment that joins the start and end vertexes.
	 */
	static public ISegment changeToDoubleCurveSegment(final ISegment pSegment) {
		if (pSegment == null) {
			throw new IllegalArgumentException("pSegment must not be null");
		}

		if (pSegment instanceof DoubleCurveSegment) {
			return pSegment;
		}

		final IVertex start = pSegment.getStartVertex();
		final IVertex end = pSegment.getEndVertex();
		final Point startPoint = start.getUHVWPoint();
		final Point endPoint = end.getUHVWPoint();
		final double length = startPoint.minus(endPoint).length();

		final Point throughPoint = startPoint.add(endPoint).add(start.getTangent().getAB().add(end.getTangent().getAB()).multiply(length)).multiply(0.5d);
		final IVertex throughVertex = Vertex.createVertex(pSegment.getPixelChain(), (pSegment.getEndIndex() + pSegment.getStartIndex()) / 2);
		throughVertex.getPixel().setUHVWPoint(throughPoint);

		ISegment segment = createTempDoubleCurveSegment(start, start.getTangent(), end, end.getTangent(), throughVertex);

		if (segment instanceof StraightSegment) {
			final LineSegment line = new LineSegment(startPoint, endPoint);
			final Line bisector = line.bisector();
			final Point target = bisector.getPoint(0.5d * length);
			final Line controlLine1 = new Line(startPoint, target);
			final Point controlPoint1 = controlLine1.getPoint(0.5d);
			final Line controlLine2 = new Line(endPoint, target);
			final Point controlPoint2 = controlLine2.getPoint(0.5d);

			segment = new DoubleCurveSegment(start, controlLine1, controlPoint1, controlPoint2, controlLine2, end, throughVertex);
		}

		segment.attachToVertexes(true);
		segment.getPixelMap().indexSegments();
		return segment;
	}

	/**
	 * Changes the supplied pSegment to a StraightSegment. If the pSegment is already a StraightSegment this will do nothing and return pSegment. Otherwise it will create a new StrightSegment from the
	 * start and end vertexes of pSegment and attach it to those vertexes. It is intended to be called from user action changing the types of segments.
	 * 
	 * @param pSegment
	 *            the segment. This must not be null.
	 * @return the StraightSegment that joins the start and end vertexes.
	 */
	static public ISegment changeToStraightSegment(final ISegment pSegment) {
		if (pSegment == null) {
			throw new IllegalArgumentException("pSegment must not be null");
		}

		if (pSegment instanceof StraightSegment) {
			return pSegment;
		}

		final StraightSegment segment = new StraightSegment(pSegment.getStartVertex(), pSegment.getEndVertex());
		segment.attachToVertexes(true);
		segment.getPixelMap().indexSegments();
		return segment;
	}

	/**
	 * Creates the curve segment starting at pStart, ending at pEnd and going through pThrough. In the event that the line collapses to a straight line then it will return a LineApproximation instead.
	 * 
	 * @param pStart
	 *            the start vertex
	 * @param pEnd
	 *            the end vertex
	 * @param pThrough
	 *            the point that the curve goes through
	 * @return the curve approximation
	 */
	static public ISegment createTempCurveSegmentThrough(final IVertex pStart, final IVertex pEnd, final Point pThrough) {
		final Point point1 = pThrough.multiply(2.0d).minus(pStart.getUHVWPoint().multiply(0.5d)).minus(pEnd.getUHVWPoint().multiply(0.5d));
		final CurveSegment segment = new CurveSegment(pStart, pEnd, point1);
		if (segment.getA().length2() != 0) {
			return segment;
		} else {
			return createTempStraightSegment(pStart, pEnd);
		}
	}

	/**
	 * Creates the curve segment starting at pStart, ending at pEnd and the gradient of the curve at the start and end is towards pP1. In the event that the line collapses to a straight line then it
	 * will return a LineApproximation instead.
	 * 
	 * @param pStart
	 *            the start vertex
	 * @param pEnd
	 *            the end vertex
	 * @param pP1
	 *            the point that the start and end gradient goes through
	 * @return the curve approximation
	 */
	static public ISegment createTempCurveSegmentTowards(final IVertex pStart, final IVertex pEnd, final Point pP1) {
		try {
			final CurveSegment segment = new CurveSegment(pStart, pEnd, pP1);
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

	public static ISegment createTempDoubleCurveSegment(final IVertex pStartVertex, final Line pStartTangent, final IVertex pEndVertex, final Line pEndTangent, final IVertex pThrough) {

		final Line midSlope = new Line(pStartVertex.getUHVWPoint(), pEndVertex.getUHVWPoint());

		final Line midTangent = new Line(pThrough.getUHVWPoint(), pThrough.getUHVWPoint().add(midSlope.getAB()));
		final Point startP1 = midTangent.intersect(pStartTangent);
		final Point endP1 = midTangent.intersect(pEndTangent);

		if (startP1 == null || endP1 == null) {
			return SegmentFactory.createTempStraightSegment(pStartVertex, pEndVertex);
		}

		final ISegment startCurve = SegmentFactory.createTempCurveSegmentTowards(pStartVertex, pThrough, startP1);
		final ISegment endCurve = SegmentFactory.createTempCurveSegmentTowards(pThrough, pEndVertex, endP1);

		// test for error
		if (pStartTangent.closestLambda(startP1) < 0.0d || midTangent.closestLambda(startP1) > 0.0d //
				|| midTangent.closestLambda(endP1) < 0.0d || pEndTangent.closestLambda(endP1) > 0.0d //
				|| !(startCurve instanceof CurveSegment) || !(endCurve instanceof CurveSegment) //
		) {
			return null;
		}

		return new DoubleCurveSegment(pStartVertex, pStartTangent, (CurveSegment) startCurve, pEndVertex, pEndTangent, (CurveSegment) endCurve, pThrough);
	}

	public static ISegment createTempDoubleCurveSegment(final IVertex pStartVertex, final Point pP1, final IVertex pMidVertex, final Point pP2, final IVertex pEndVertex) {
		// note that no checking is done that the parameters give a sensible DoubleCurve
		CurveSegment startCurve = (CurveSegment) createTempCurveSegmentTowards(pStartVertex, pMidVertex, pP1);
		CurveSegment endCurve = (CurveSegment) createTempCurveSegmentTowards(pMidVertex, pEndVertex, pP2);

		// Vector x = startCurve.getEndTangentVector();
		// Vector y = endCurve.getStartTangentVector();

		if (startCurve != null && endCurve != null) {
			return new DoubleCurveSegment(pStartVertex, startCurve, pEndVertex, endCurve, pMidVertex);
		} else {
			return null;
		}
	}

	static public StraightSegment createTempStraightSegment(final IVertex pStart, final IVertex pEnd) {
		final StraightSegment segment = new StraightSegment(pStart, pEnd);
		return segment;
	}

}
