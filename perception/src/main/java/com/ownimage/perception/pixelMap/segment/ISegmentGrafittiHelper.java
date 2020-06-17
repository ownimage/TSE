/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Point;

import java.awt.*;

public interface ISegmentGrafittiHelper {

    void graffitiLine(Point p1, Point p2, Color pColor);

    void graffitiControlPoint(Point p1);

}
