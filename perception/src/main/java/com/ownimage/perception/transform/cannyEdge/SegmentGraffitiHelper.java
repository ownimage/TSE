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

    private final GrafittiHelper mGrafittiHelper;
    final Function<Point, Point> mTranslate;

    public SegmentGraffitiHelper(GrafittiHelper pGrafittiHelper, Function<Point, Point> pTranslate) {
        mGrafittiHelper = pGrafittiHelper;
        mTranslate = pTranslate;
    }

    @Override
    public void graffitiLine(Point p1, Point p2, Color pColor) {
        mGrafittiHelper.drawLine(mTranslate.apply(p1), mTranslate.apply(p2), pColor);
    }

    @Override
    public void graffitiControlPoint(Point p1) {
    }

}
