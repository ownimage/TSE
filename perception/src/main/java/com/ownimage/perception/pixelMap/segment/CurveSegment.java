/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.immutable.IPixelChain;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import org.jetbrains.annotations.NotNull;

/**
 * This class needs to remain here for the deserialization of existing transforms.
 */
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
        mA = getP0(pPixelChain).add(getP2(pPixelChain)).minus(getP1().multiply(2.0d));
        mB = getP1().minus(getP0(pPixelChain)).multiply(2.0d);
    }

    public CurveSegment(
            int pSegmentIndex, double pStartPosition, @NotNull Point pA, @NotNull Point pB, @NotNull Point pP1) {
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


}
