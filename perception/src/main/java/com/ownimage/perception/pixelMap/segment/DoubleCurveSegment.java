/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;

import java.awt.*;

public class DoubleCurveSegment extends SegmentBase<DoubleCurveSegment> {

    public final static long serialVersionUID = 1L;

    private final CurveSegment mStartCurve;
    private final CurveSegment mEndCurve;

    DoubleCurveSegment(final PixelMap pPixelMap, final PixelChain pPixelChain, final CurveSegment pStartCurve, final CurveSegment pEndCurve) {
        super(pStartCurve.getSegmentIndex());
        mStartCurve = pStartCurve;
        mEndCurve = pEndCurve;
    }

    @Override
    public double calcError(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return mStartCurve.calcError(pPixelMap, pPixelChain) + mEndCurve.calcError(pPixelMap, pPixelChain);
    }

    @Override
    public boolean closerThanActual(final PixelMap pPixelMap, final PixelChain pPixelChain, final IPixelMapTransformSource pTransformSource, final Point pPoint, final double pMultiplier) {
        return mStartCurve.closerThanActual(pPixelMap, pPixelChain, pTransformSource, pPoint, pMultiplier) || mEndCurve.closerThanActual(pPixelMap, pPixelChain, pTransformSource, pPoint, pMultiplier);
    }

    @Override
    public boolean closerThan(final PixelMap pPixelMap, final PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        return mStartCurve.closerThan(pPixelMap, pPixelChain, pPoint, pTolerance) || mEndCurve.closerThan(pPixelMap, pPixelChain, pPoint, pTolerance);
    }

    @Override
    public double closestLambda(final Point pPoint, final PixelChain pPixelChain, final PixelMap pPixelMap) {
        final double lambda1 = mStartCurve.closestLambda(pPoint, pPixelChain, pPixelMap);
        final double lambda2 = mEndCurve.closestLambda(pPoint, pPixelChain, pPixelMap);

        final Point point1 = mStartCurve.getPointFromLambda(pPixelMap, pPixelChain, lambda1);
        final Point point2 = mEndCurve.getPointFromLambda(pPixelMap, pPixelChain, lambda1);

        final double distance1 = point1.distance(pPoint);
        final double distance2 = point2.distance(pPoint);

        if (distance1 < distance2) {
            return lambda1 / 2.0d;
        }
        return 0.5d + lambda2 / 2.0d;
    }

    @Override
    public double distance(final PixelMap pPixelMap, final PixelChain pPixelChain, final Point pUVHWPoint) {
        return Math.min(mStartCurve.distance(pPixelMap, pPixelChain, pUVHWPoint), mEndCurve.distance(pPixelMap, pPixelChain, pUVHWPoint));
    }

    @Override
    public Line getEndTangent(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return mEndCurve.getEndTangent(pPixelMap, pPixelChain);
    }

    @Override
    public Vector getEndTangentVector(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return mEndCurve.getEndTangent(pPixelMap, pPixelChain).getAB();
    }

    @Override
    public double getLength(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return mStartCurve.getLength(pPixelMap, pPixelChain) + mEndCurve.getLength(pPixelMap, pPixelChain);
    }

    @Override
    public double getMaxX(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return Math.max(mStartCurve.getMaxX(pPixelMap, pPixelChain), mEndCurve.getMaxX(pPixelMap, pPixelChain));
    }

    @Override
    public double getMaxY(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return Math.max(mStartCurve.getMaxY(pPixelMap, pPixelChain), mEndCurve.getMaxY(pPixelMap, pPixelChain));
    }

    @Override
    public double getMinX(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return Math.min(mStartCurve.getMinX(pPixelMap, pPixelChain), mEndCurve.getMinX(pPixelMap, pPixelChain));
    }

    @Override
    public double getMinY(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return Math.min(mStartCurve.getMinY(pPixelMap, pPixelChain), mEndCurve.getMinY(pPixelMap, pPixelChain));
    }

    @Override
    public Point getPointFromLambda(final PixelMap pPixelMap, final PixelChain pPixelChain, final double pLambda) {
        if (pLambda < 0.5d) {
            final double lambda = 2.0d * pLambda;
            return mStartCurve.getPointFromLambda(pPixelMap, pPixelChain, lambda);
        } else {
            final double lambda = 2.0d * (pLambda - 0.5d);
            return mEndCurve.getPointFromLambda(pPixelMap, pPixelChain, lambda);
        }
    }

    @Override
    public Line getStartTangent(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return mEndCurve.getStartTangent(pPixelMap, pPixelChain);
    }

    @Override
    public Vector getStartTangentVector(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return mEndCurve.getStartTangent(pPixelMap, pPixelChain).getAB();
    }

    @Override
    public void graffiti(final PixelMap pPixelMap, final PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        pGraphics.grafittiControlLine(mStartCurve.getP0(pPixelMap, pPixelChain), mStartCurve.getP1());
        pGraphics.grafittiControlLine(mStartCurve.getP1(), mEndCurve.getP1());
        pGraphics.grafittiControlLine(mEndCurve.getP1(), mEndCurve.getP2(pPixelChain, pPixelMap));
        pGraphics.graffitiControlPoint(mStartCurve.getP1());
        pGraphics.graffitiControlPoint(mEndCurve.getP1());
        super.graffiti(pPixelMap, pPixelChain, pGraphics);
        pGraphics.grafittLine(getStartUHVWPoint(pPixelMap, pPixelChain), getEndUHVWPoint(pPixelMap, pPixelChain), Color.RED);
    }

    private CurveSegment getStartCurve() {
        return mStartCurve;
    }

    private CurveSegment getEndCurve() {
        return mEndCurve;
    }

    @Override
    public String toString() {
        return "DoubleCurveSegment[" + super.toString() + "]";
    }

    @Override
    public DoubleCurveSegment withStartPosition(final PixelMap pPixelMap, final PixelChain pPixelChain, final double pStartPosition) {
        if (getStartPosition() == pStartPosition) return this;
        CurveSegment start = getStartCurve().withStartPosition(pPixelMap, pPixelChain, pStartPosition);
        CurveSegment end = getEndCurve().withStartPosition(pPixelMap, pPixelChain, pStartPosition + start.getLength(pPixelMap, pPixelChain));
        return new DoubleCurveSegment(pPixelMap, pPixelChain, start, end);
    }
}
