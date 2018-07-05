/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap.segment;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.util.Path;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.math.LineSegment;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.segment.SegmentFactory.SegmentType;

public class StraightSegment extends SegmentBase {

	public final static Version mVersion = new Version(4, 0, 2, "2014/05/30 20:48");
	public final static String mClassname = StraightSegment.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	LineSegment mLineSegment;

	StraightSegment(final IVertex pStart, final IVertex pEnd) {
		super(pStart, pEnd);
		vertexChange(null);
	}

	@Override
	public void addToPath(final Path pPath) {
        pPath.moveTo(getStartPixel().getUHVWPoint());
		pPath.lineTo(getEndPixel().getUHVWPoint());
	}

    @Override
    public void graffiti(final GrafittiHelper pGraphics) {

    }

    @Override
	public void attachToVertexes(final boolean pReCalcSegments) {
		super.attachToVertexes(pReCalcSegments);
		createInternalLineSegment();
	}

	@Override
	public boolean closerThan(final Point pPoint) {
		final double lambda = mLineSegment.closestLambda(pPoint);
		final double position = getStartPosition() + lambda * getLength();
		final double actualThickness = getActualThickness(position);
		return closerThan(pPoint, actualThickness);
	}

	@Override
	public boolean closerThan(final Point pPoint, final double pTolerance) {
		return mLineSegment.isCloserThan(pPoint, pTolerance);
	}

	@Override
	public double closestLambda(final Point pPoint) {
		final double lambda = mLineSegment.closestLambda(pPoint);
		return lambda;
	}

	@Override
	public ISegment copy(final IVertex pStartVertex, final IVertex pEndVertex) {
		return new StraightSegment(pStartVertex, pEndVertex);
	}

	private void createInternalLineSegment() {
		final Point a = getStartUHVWPoint();
		final Point b = getEndUHVWPoint();
		mLineSegment = new LineSegment(a, b);
	}

	@Override
	public ISegment deepCopy(final IVertex pOriginalStartVertex, final IVertex pCopyStartVertex, final IVertex pSegmentStartVertex) {
		final IVertex endVertex = getEndVertex().deepCopy(pOriginalStartVertex, pCopyStartVertex);
		final StraightSegment copy = SegmentFactory.createTempStraightSegment(pSegmentStartVertex, endVertex);
		copy.attachToVertexes(true);
		return copy;
	}

	@Override
	public double distance(final Point pUVHWPoint) {
		final double distance = mLineSegment.distance(pUVHWPoint);
		return distance;
	}

	public Vector getAB() {
		return mLineSegment.getAB();
	}

	@Override
	public synchronized Point getAveragePoint() {
		final int start = getStartIndex();
		final int end = getEndIndex();
		final int averageIndex = (start + end) / 2;
		return getStartVertex().getPixelChain().getPixel(averageIndex).getUHVWPoint();
	}

	@Override
	public Vector getEndTangentVector() {
		return getAB().normalize();
	}

	@Override
	public double getLength() {
		return getAB().length();
	}

	public LineSegment getLineSegment() {
		return mLineSegment;
	}

	@Override
	public double getMaxX() {
		return mLineSegment.getMaxX();
	}

	@Override
	public double getMaxY() {
		return mLineSegment.getMaxY();
	}

	@Override
	public double getMinX() {
		return mLineSegment.getMinX();
	}

	@Override
	public double getMinY() {
		return mLineSegment.getMinY();
	}

	@Override
	public Point getPointFromLambda(final double pLambda) {
		return mLineSegment.getPoint(pLambda);
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Straight;
	}

	@Override
	public Vector getStartTangentVector() {
		return getAB().minus().normalize();
	}

	public Point intersect(final StraightSegment pEndSegment) {
		return mLineSegment.intersect(pEndSegment.mLineSegment);
	}

	public boolean isParallel(final StraightSegment pSegment) {
		return mLineSegment.isParallel(pSegment.mLineSegment);
	}

	@Override
	public double length() {
		return mLineSegment.length();
	}

	@Override
	public void vertexChange(final IVertex pVertex) {
		createInternalLineSegment();
		super.vertexChange(pVertex);
	}

}
