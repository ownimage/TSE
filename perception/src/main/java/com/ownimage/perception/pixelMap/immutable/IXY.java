package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import org.jetbrains.annotations.NotNull;

public interface IXY {

    int getX();

    int getY();

    static ImmutableIXY of(@NotNull IntegerPoint ip) {
        return ImmutableIXY.of(ip.getX(),ip.getY());
    }

    default ImmutableIXY add(final int pDx, final int pDy) {
        return ImmutableIXY.of(getX() + pDx, getY() + pDy);
    }

    default IXY add(final IXY pPoint) {
        return ImmutableIXY.of(getX() + pPoint.getX(), getY() + pPoint.getY());
    }

    default boolean samePosition(final IXY pO) {
        if (this == pO) return true;
        if (pO == null) return false;
        return getX() == pO.getX() && getY() == pO.getY();
    }

    default Point getUHVWMidPoint(int height) {
        double y = (getY() + 0.5d) / height;
        double x = (getX() + 0.5d) / height;
        return new Point(x, y);
    }
}
