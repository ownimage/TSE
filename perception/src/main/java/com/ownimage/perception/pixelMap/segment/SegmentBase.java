/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.perception.pixelMap.immutable.Segment;

public abstract class SegmentBase implements Segment {

    private final static long serialVersionUID = 1L;

    private final int mSegmentIndex;
    private final double mStartPosition;

    public SegmentBase(int pSegmentIndex) {
        this(pSegmentIndex, 0.0d);
    }

    SegmentBase(int pSegmentIndex, double pStartPosition) {
        mSegmentIndex = pSegmentIndex;
        mStartPosition = pStartPosition;
    }

    @Override
    public int getSegmentIndex() {
        return mSegmentIndex;
    }

    @Override
    public double getStartPosition() {
        return mStartPosition;
    }

}
