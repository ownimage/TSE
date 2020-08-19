/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Point;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface Pixel extends IXY, Serializable {

    static ImmutablePixel of(@NotNull IXY ixy, int height) {
        return of(ixy.getX(), ixy.getY(), height);
    }

    static ImmutablePixel of(int x, int y, int height) {
        double dx = (x + 0.5d) / height;
        double dy = (y + 0.5d) / height;
        var center = new Point(dx, dy);
        return new ImmutablePixel(x, y, center);
    }

    int getX();

    int getY();

    Point getUHVWMidPoint();

    @Override
    default Point getUHVWMidPoint(int height) {
        return getUHVWMidPoint();
    }
}
