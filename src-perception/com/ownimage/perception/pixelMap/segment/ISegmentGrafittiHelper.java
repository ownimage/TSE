/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.perception.pixelMap.segment;

import com.ownimage.perception.math.Point;

public interface ISegmentGrafittiHelper {

    public void graffiitLine(Point p1, Point p2);

    public void graffiitControlLine(Point p1, Point p2);

    public void graffitiControlPoint(Point p1);

    public void graffitiSelectedControlPoint(Point p1);

}
