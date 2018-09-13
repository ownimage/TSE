/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Point;

import java.awt.*;

public interface ISegmentGrafittiHelper {

    void grafittiLine(Point p1, Point p2);

    void grafittLine(Point p1, Point p2, Color pColor);

    void grafittiControlLine(Point p1, Point p2);

    void graffitiControlPoint(Point p1);

    void graffitiSelectedControlPoint(Point p1);

}
