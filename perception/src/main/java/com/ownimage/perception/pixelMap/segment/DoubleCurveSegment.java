/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import java.awt.*;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelChain;

public class DoubleCurveSegment extends SegmentBase<DoubleCurveSegment> {

    public final static long serialVersionUID = 1L;

    /**
     * The start tangent. This should match the start vertex tangent exactly.
     */
    private final Line mStartTangent;

    /**
     * The end tangent. This should match the start vertex tangent exactly.
     */
    private final Line mEndTangent;

    private CurveSegment mStartCurve;
    private CurveSegment mEndCurve;

    DoubleCurveSegment(PixelChain pPixelChain, final CurveSegment pStartCurve, final CurveSegment pEndCurve) {
        super(pStartCurve.getSegmentIndex());
        mStartCurve = pStartCurve;
        mStartTangent = mStartCurve.getStartTangent(pPixelChain);
        mEndCurve = pEndCurve;
        mEndTangent = mEndCurve.getEndTangent(pPixelChain);
    }

    @Override
    public double calcError(final PixelChain pPixelChain) {
        return mStartCurve.calcError(pPixelChain) + mEndCurve.calcError(pPixelChain);
    }

    @Override
    public boolean closerThanActual(final IPixelMapTransformSource pTransformSource, final PixelChain pPixelChain, final Point pPoint, double pMultiplier) {
        return mStartCurve.closerThanActual(pTransformSource, pPixelChain, pPoint, pMultiplier) || mEndCurve.closerThanActual(pTransformSource, pPixelChain, pPoint, pMultiplier);
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
        pGraphics.grafittiControlLine(mStartCurve.getP0(pPixelChain), mStartCurve.getP1());
        pGraphics.grafittiControlLine(mStartCurve.getP1(), mEndCurve.getP1());
        pGraphics.grafittiControlLine(mEndCurve.getP1(), mEndCurve.getP2(pPixelChain));
        pGraphics.graffitiControlPoint(mStartCurve.getP1());
        pGraphics.graffitiControlPoint(mEndCurve.getP1());
        super.graffiti(pPixelChain, pGraphics);
        pGraphics.graffitiSelectedControlPoint(getControlPoint());
        pGraphics.grafittLine(getStartUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain), Color.RED);
    }

    @Override
    protected void setStartPosition(PixelChain pPixelChain, final double pStartPosition) {
        mStartCurve.setStartPosition(pPixelChain, pStartPosition);
        mEndCurve.setStartPosition(pPixelChain, pStartPosition + mStartCurve.getLength(pPixelChain));
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

    /**
     * This is a MUTATING method that sets the segment index on this object.
     *
     * @param pSegmentIndex the new segment index for this copy.
     */
    protected void setSegmentIndex(int pSegmentIndex) {
        super.setSegmentIndex(pSegmentIndex);
        mStartCurve = mStartCurve.withSegmentIndex(pSegmentIndex);
        mEndCurve = mEndCurve.withSegmentIndex(pSegmentIndex);
    }
}
