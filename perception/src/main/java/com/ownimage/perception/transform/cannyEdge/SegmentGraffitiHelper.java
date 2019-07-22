/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.segment.ISegmentGrafittiHelper;

import java.awt.*;
import java.util.function.Function;

public class SegmentGraffitiHelper implements ISegmentGrafittiHelper {

    final GrafittiHelper mGrafittiHelper;
    final Function<Point, Point> mTranslate;

    public SegmentGraffitiHelper(final GrafittiHelper pGrafittiHelper, final Function<Point, Point> pTranslate) {
        mGrafittiHelper = pGrafittiHelper;
        mTranslate = pTranslate;
    }

    @Override
    public void graffitiLine(final Point p1, final Point p2) {
        graffitiLine(p1, p2, Color.white);
    }

    @Override
    public void graffitiLine(final Point p1, final Point p2, final Color pColor) {
        mGrafittiHelper.drawLine(mTranslate.apply(p1), mTranslate.apply(p2), pColor);
    }

    @Override
    public void graffitiControlLine(final Point p1, final Point p2) {
        mGrafittiHelper.drawLine(mTranslate.apply(p1), mTranslate.apply(p2), Color.black);
    }

    @Override
    public void graffitiControlPoint(final Point p1) {
    }

    @Override
    public void graffitiSelectedControlPoint(final Point p1) {
    }

}
