/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.perception.math.Point;

public interface ISegmentGrafittiHelper {

    void graffiitLine(Point p1, Point p2);

    void graffiitControlLine(Point p1, Point p2);

    void graffitiControlPoint(Point p1);

    void graffitiSelectedControlPoint(Point p1);

}
