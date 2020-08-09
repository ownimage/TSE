/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.LineSegment;

import java.util.Objects;

/**
 * This class needs to remain here for the deserialization of existing transforms.
 */
@Deprecated
public class StraightSegment extends SegmentBase implements com.ownimage.perception.pixelMap.immutable.StraightSegment {

    private final static long serialVersionUID = 1L;

    private final LineSegment mLineSegment;

    /* This is only here to allow access for testing ... it is called through reflection */
    private StraightSegment(int pSegmentIndex, double pStartPosition, LineSegment pLineSegment) {
        super(pSegmentIndex, pStartPosition);
        mLineSegment = pLineSegment;
    }

    @Override
    public LineSegment getLineSegment() {
        return mLineSegment;
    }

    @Override
    public String toString() {
        return "StraightSegment{ getLineSegment(): " + Objects.toString(getLineSegment()) + "} ";
    }

}
