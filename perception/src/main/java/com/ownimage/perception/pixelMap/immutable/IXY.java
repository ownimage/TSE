package com.ownimage.perception.pixelMap.immutable;

import com.google.common.base.MoreObjects;
import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable(prehash = true)
public interface IXY extends Comparable<IXY> {

    @Value.Parameter(order = 1)
    int getX();

    @Value.Parameter(order = 2)
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

    @Override
    default int compareTo(@NotNull IXY o) {
        var diff = getX() - o.getX();
        return diff != 0 ? diff : getY() - o.getY();
    }

    default String toIXYString() {
        return MoreObjects.toStringHelper("IXY")
                .omitNullValues()
                .add("x", getX())
                .add("y", getY())
                .toString();
    }
}
