/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Path;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.segment.SegmentFactory.SegmentType;

// TODO need to question why there is a startTangent and endTangent when these can be retreived from the start/endCurve respectively.
public class DoubleCurveSegment extends SegmentBase {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    /**
     * The start tangent. This should match the start vertex tangent exactly.
     */
    private final Line mStartTangent;

    /**
     * The end tangent. This should match the start vertex tangent exactly.
     */
    private final Line mEndTangent;
    private final IVertex mThroughVertex;

    private CurveSegment mStartCurve;
    private CurveSegment mEndCurve;

    private Point mControlPoint1;
    private Point mControlPoint2;
    private int mCurrentControlPoint = 1;

    DoubleCurveSegment(final IVertex pStart, final Line pStartTangent, final CurveSegment pStartCurve, final IVertex pEnd, final Line pEndTangent, final CurveSegment pEndCurve, final IVertex pThrough) {
        super(pStart, pEnd);
        mStartTangent = pStartTangent;
        mStartCurve = pStartCurve;
        mEndTangent = pEndTangent;
        mEndCurve = pEndCurve;
        mThroughVertex = pThrough;

        mThroughVertex.setStartSegment(mStartCurve);
        mThroughVertex.setEndSegment(mEndCurve);
    }

    DoubleCurveSegment(final IVertex pStart, final CurveSegment pStartCurve, final IVertex pEnd, final CurveSegment pEndCurve, final IVertex pThrough) {
        super(pStart, pEnd);
        mStartCurve = pStartCurve;
        mStartTangent = mStartCurve.getStartTangent();
        mEndCurve = pEndCurve;
        mEndTangent = mEndCurve.getEndTangent();
        mThroughVertex = pThrough;

        mThroughVertex.setStartSegment(mStartCurve);
        mThroughVertex.setEndSegment(mEndCurve);
    }

    DoubleCurveSegment(final IVertex pStart, final Line pStartTangent, final Point pControlPoint1, final Point pControlPoint2, final Line pEndTangent, final IVertex pEnd, final IVertex pThroughVertex) {
        super(pStart, pEnd);
        mStartTangent = pStartTangent;
        mEndTangent = pEndTangent;
        mThroughVertex = pThroughVertex;
        mControlPoint1 = pControlPoint1;
        mControlPoint2 = pControlPoint2;
        controlPointChange();

        mThroughVertex.setStartSegment(mStartCurve);
        mThroughVertex.setEndSegment(mEndCurve);
    }

    @Override
    public void addToPath(final Path pPath) {
        mStartCurve.addToPath(pPath);
        mEndCurve.addToPath(pPath);
    }

    @Override
    public double calcError() {
        return mStartCurve.calcError() + mEndCurve.calcError();
    }

    @Override
    public boolean closerThan(final Point pPoint) {
        return mStartCurve.closerThan(pPoint) || mEndCurve.closerThan(pPoint);
    }

    @Override
    public boolean closerThan(final Point pPoint, final double pTolerance) {
        return mStartCurve.closerThan(pPoint, pTolerance) || mEndCurve.closerThan(pPoint, pTolerance);
    }

    @Override
    public double closestLambda(final Point pPoint) {
        final double lambda1 = mStartCurve.closestLambda(pPoint);
        final double lambda2 = mEndCurve.closestLambda(pPoint);

        final Point point1 = mStartCurve.getPointFromLambda(lambda1);
        final Point point2 = mEndCurve.getPointFromLambda(lambda1);

        final double distance1 = point1.distance(pPoint);
        final double distance2 = point2.distance(pPoint);

        if (distance1 < distance2) {
            return lambda1 / 2.0d;
        }
        return 0.5d + lambda2 / 2.0d;
    }

    private void controlPointChange() {
        mThroughVertex.getPixel().setUHVWPoint(mControlPoint1.add(mControlPoint2).multiply(0.5d));

        mStartCurve = (CurveSegment) SegmentFactory.createTempCurveSegmentTowards(getStartVertex(), mThroughVertex, mControlPoint1);
        mEndCurve = (CurveSegment) SegmentFactory.createTempCurveSegmentTowards(mThroughVertex, getEndVertex(), mControlPoint2);
    }

    @Override
    public ISegment copy(final IVertex pStartVertex, final IVertex pEndVertex) {
        final IVertex throughVertex = mThroughVertex.copy();
        return new DoubleCurveSegment(pStartVertex, mStartTangent, mControlPoint1, mControlPoint2, mEndTangent, pEndVertex, mThroughVertex);
    }

    @Override
    public ISegment deepCopy(final IVertex pOriginalPCStartVertex, final IVertex pCopyPCStartVertex, final IVertex pSegmentStartVertex) {
        return this;
    }

    @Override
    public double distance(final Point pUVHWPoint) {
        return Math.min(mStartCurve.distance(pUVHWPoint), mEndCurve.distance(pUVHWPoint));
    }

    @Override
    public Point getControlPoint() {
        return mCurrentControlPoint == 1 ? mControlPoint1 : mControlPoint2;
    }

    @Override
    public Line getEndTangent() {
        return mEndTangent;
    }

    @Override
    public Vector getEndTangentVector() {
        return mEndTangent.getAB();
    }

    @Override
    public double getLength() {
        return mStartCurve.getLength() + mEndCurve.getLength();
    }

    @Override
    public double getMaxX() {
        return Math.max(mStartCurve.getMaxX(), mEndCurve.getMaxX());
    }

    @Override
    public double getMaxY() {
        return Math.max(mStartCurve.getMaxY(), mEndCurve.getMaxY());
    }

    @Override
    public double getMinX() {
        return Math.min(mStartCurve.getMinX(), mEndCurve.getMinX());
    }

    @Override
    public double getMinY() {
        return Math.min(mStartCurve.getMinY(), mEndCurve.getMinY());
    }

    @Override
    public Point getPointFromLambda(final double pLambda) {
        if (pLambda < 0.5d) {
            final double lambda = 2.0d * pLambda;
            return mStartCurve.getPointFromLambda(lambda);
        } else {
            final double lambda = 2.0d * (pLambda - 0.5d);
            return mEndCurve.getPointFromLambda(lambda);
        }
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.DoubleCurve;
    }

    @Override
    public Line getStartTangent() {
        return mStartTangent;
    }

    @Override
    public Vector getStartTangentVector() {
        return mStartTangent.getAB();
    }

    @Override
    public void graffiti(final ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffiitControlLine(mStartCurve.getP0(), mStartCurve.getP1());
		pGraphics.graffiitControlLine(mStartCurve.getP1(), mEndCurve.getP1());
		pGraphics.graffiitControlLine(mEndCurve.getP1(), mEndCurve.getP2());
		pGraphics.graffitiControlPoint(mStartCurve.getP1());
		pGraphics.graffitiControlPoint(mEndCurve.getP1());
        super.graffiti(pGraphics);
        pGraphics.graffitiSelectedControlPoint(getControlPoint());
    }

    @Override
    public double length() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void nextControlPoint() {
        mCurrentControlPoint = 3 - mCurrentControlPoint;
    }

    @Override
    public void previousControlPoint() {
        nextControlPoint();
    }

    @Override
    public void setControlPoint(final Point pPoint) {
        if (mCurrentControlPoint == 1) {
            mControlPoint1 = pPoint;
        } else {
            mControlPoint2 = pPoint;
        }
        controlPointChange();
    }

    @Override
    public void setStartPosition(final double pStartPosition) {
        mStartCurve.setStartPosition(pStartPosition);
        mEndCurve.setStartPosition(pStartPosition + mStartCurve.getLength());
    }

    @Override
    public void vertexChange(final IVertex pVertex) {
        super.vertexChange(pVertex);
    }

    public CurveSegment getStartCurve() {
        return mStartCurve;
    }

    public CurveSegment getEndCurve() {
        return mEndCurve;
    }

    @Override
    public String toString() {
        return "DoubleCurveSegment[" + super.toString() + "]";
    }
}
