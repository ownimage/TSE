/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.immutable.PixelMap;

public class CurveSegment extends SegmentBase implements com.ownimage.perception.pixelMap.immutable.CurveSegment {

    private final static long serialVersionUID = 1L;

    private final Point mP1;
    private final Point mA;
    private final Point mB;

    CurveSegment(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            int pSegmentIndex,
            Point pP1
    ) {
        this(pPixelMap, pPixelChain, pSegmentIndex, pP1, 0.0d);
    }

    private CurveSegment(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            int pSegmentIndex,
            Point pP1,
            double pStartPosition
    ) {
        super(pSegmentIndex, pStartPosition);
        mP1 = pP1;
        mA = getP0(pPixelMap, pPixelChain).add(getP2(pPixelMap, pPixelChain)).minus(getP1().multiply(2.0d));
        mB = getP1().minus(getP0(pPixelMap, pPixelChain)).multiply(2.0d);
    }

    private CurveSegment(
            int pSegmentIndex,
            Point pP1,
            Point pA,
            Point pB,
            double pStartPosition
    ) {
        super(pSegmentIndex, pStartPosition);
        mP1 = pP1;
        mA = pA;
        mB = pB;
    }


    @Override
    public Point getA() {
        return mA;
    }

    @Override
    public Point getB() {
        return mB;
    }


    @Override
    public Point getP1() {
        return mP1;
    }

    @Override
    public String toString() {
        return "CurveSegment[" + super.toString() + "]";
    }

    @Override
    public CurveSegment withStartPosition(
            double pStartPosition
    ) {
        //noinspection FloatingPointEquality
        if (getStartPosition() == pStartPosition) {
            return this;
        }
        return new CurveSegment(getSegmentIndex(), mP1, mA, mB, pStartPosition);
    }

    @Override
    public CurveSegment withSegmentIndex(int segmentIndex) {
        //noinspection FloatingPointEquality
        if (getSegmentIndex() == segmentIndex) {
            return this;
        }
        return new CurveSegment(segmentIndex, mP1, mA, mB, getStartPosition());
    }
}
