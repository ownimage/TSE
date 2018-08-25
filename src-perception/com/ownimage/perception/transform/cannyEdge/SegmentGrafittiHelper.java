/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.util.function.Function;

import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.segment.ISegmentGrafittiHelper;

public class SegmentGrafittiHelper implements ISegmentGrafittiHelper {

    final GrafittiHelper mGrafittiHelper;
    final Function<Point, Point> mTranslate;

    public SegmentGrafittiHelper(GrafittiHelper pGrafittiHelper, Function<Point, Point> pTranslate) {
        mGrafittiHelper = pGrafittiHelper;
        mTranslate = pTranslate;
    }

    @Override
    public void graffiitLine(final Point p1, final Point p2) {
        mGrafittiHelper.drawLine(mTranslate.apply(p1), mTranslate.apply(p2), Color.white);
    }

    @Override
    public void graffiitControlLine(final Point p1, final Point p2) {
        mGrafittiHelper.drawLine(mTranslate.apply(p1), mTranslate.apply(p2), Color.yellow);
    }

    @Override
    public void graffitiControlPoint(final Point p1) {
    }

    @Override
    public void graffitiSelectedControlPoint(final Point p1) {
    }
}
