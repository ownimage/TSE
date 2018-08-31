/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.PixelChain;

// TODO need to question why there is a startTangent and endTangent when these can be retreived from the start/endCurve respectively.
public class DoubleCurveSegment extends SegmentBase {


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

    private final CurveSegment mStartCurve;
    private final CurveSegment mEndCurve;


    DoubleCurveSegment(final IVertex pStart, final Line pStartTangent, final CurveSegment pStartCurve, final IVertex pEnd, final Line pEndTangent, final CurveSegment pEndCurve, final IVertex pThrough, PixelChain pPixelChain) {
        super(pStart, pEnd);
        mStartTangent = pStartTangent;
        mStartCurve = pStartCurve;
        mEndTangent = pEndTangent;
        mEndCurve = pEndCurve;
        mThroughVertex = pThrough;

        mThroughVertex.setStartSegment(mStartCurve);
        mThroughVertex.setEndSegment(mEndCurve);
    }

    DoubleCurveSegment(PixelChain pPixelChain, final IVertex pStart, final CurveSegment pStartCurve, final IVertex pEnd, final CurveSegment pEndCurve, final IVertex pThrough) {
        super(pStart, pEnd);
        mStartCurve = pStartCurve;
        mStartTangent = mStartCurve.getStartTangent(pPixelChain);
        mEndCurve = pEndCurve;
        mEndTangent = mEndCurve.getEndTangent(pPixelChain);
        mThroughVertex = pThrough;

        mThroughVertex.setStartSegment(mStartCurve);
        mThroughVertex.setEndSegment(mEndCurve);
    }

    @Override
    public double calcError(final PixelChain pPixelChain) {
        return mStartCurve.calcError(pPixelChain) + mEndCurve.calcError(pPixelChain);
    }

    @Override
    public boolean closerThan(final PixelChain pPixelChain, final Point pPoint) {
        return mStartCurve.closerThan(pPixelChain, pPoint) || mEndCurve.closerThan(pPixelChain, pPoint);
    }

    @Override
    public boolean closerThan(PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        return mStartCurve.closerThan(pPixelChain, pPoint, pTolerance) || mEndCurve.closerThan(pPixelChain, pPoint, pTolerance);
    }

    @Override
    public double closestLambda(final Point pPoint, PixelChain pPixelChain) {
        final double lambda1 = mStartCurve.closestLambda(pPoint, pPixelChain);
        final double lambda2 = mEndCurve.closestLambda(pPoint, pPixelChain);

        final Point point1 = mStartCurve.getPointFromLambda(pPixelChain, lambda1);
        final Point point2 = mEndCurve.getPointFromLambda(pPixelChain, lambda1);

        final double distance1 = point1.distance(pPoint);
        final double distance2 = point2.distance(pPoint);

        if (distance1 < distance2) {
            return lambda1 / 2.0d;
        }
        return 0.5d + lambda2 / 2.0d;
    }

    @Override
    public double distance(PixelChain pPixelChain, final Point pUVHWPoint) {
        return Math.min(mStartCurve.distance(pPixelChain, pUVHWPoint), mEndCurve.distance(pPixelChain, pUVHWPoint));
    }

    @Override
    public Line getEndTangent(PixelChain pPixelChain) {
        return mEndTangent;
    }

    @Override
    public Vector getEndTangentVector(PixelChain pPixelChain) {
        return mEndTangent.getAB();
    }

    @Override
    public double getLength(PixelChain pPixelChain) {
        return mStartCurve.getLength(pPixelChain) + mEndCurve.getLength(pPixelChain);
    }

    @Override
    public double getMaxX(PixelChain pPixelChain) {
        return Math.max(mStartCurve.getMaxX(pPixelChain), mEndCurve.getMaxX(pPixelChain));
    }

    @Override
    public double getMaxY(PixelChain pPixelChain) {
        return Math.max(mStartCurve.getMaxY(pPixelChain), mEndCurve.getMaxY(pPixelChain));
    }

    @Override
    public double getMinX(PixelChain pPixelChain) {
        return Math.min(mStartCurve.getMinX(pPixelChain), mEndCurve.getMinX(pPixelChain));
    }

    @Override
    public double getMinY(PixelChain pPixelChain) {
        return Math.min(mStartCurve.getMinY(pPixelChain), mEndCurve.getMinY(pPixelChain));
    }

    @Override
    public Point getPointFromLambda(PixelChain pPixelChain, final double pLambda) {
        if (pLambda < 0.5d) {
            final double lambda = 2.0d * pLambda;
            return mStartCurve.getPointFromLambda(pPixelChain, lambda);
        } else {
            final double lambda = 2.0d * (pLambda - 0.5d);
            return mEndCurve.getPointFromLambda(pPixelChain, lambda);
        }
    }

    @Override
    public Line getStartTangent(PixelChain pPixelChain) {
        return mStartTangent;
    }

    @Override
    public Vector getStartTangentVector(PixelChain pPixelChain) {
        return mStartTangent.getAB();
    }

    @Override
    public void graffiti(PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffiitControlLine(mStartCurve.getP0(pPixelChain), mStartCurve.getP1());
		pGraphics.graffiitControlLine(mStartCurve.getP1(), mEndCurve.getP1());
        pGraphics.graffiitControlLine(mEndCurve.getP1(), mEndCurve.getP2(pPixelChain));
		pGraphics.graffitiControlPoint(mStartCurve.getP1());
		pGraphics.graffitiControlPoint(mEndCurve.getP1());
        super.graffiti(pPixelChain, pGraphics);
        pGraphics.graffitiSelectedControlPoint(getControlPoint());
    }

    @Override
    public double length() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStartPosition(PixelChain pPixelChain, final double pStartPosition) {
        mStartCurve.setStartPosition(pPixelChain, pStartPosition);
        mEndCurve.setStartPosition(pPixelChain, pStartPosition + mStartCurve.getLength(pPixelChain));
    }

    @Override
    public void vertexChange(PixelChain pPixelChain, final IVertex pVertex) {
        super.vertexChange(pPixelChain, pVertex);
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
