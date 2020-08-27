package com.ownimage.perception.pixelMap.immutable;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable(prehash = true)
public interface IXY extends XY {

    @Value.Parameter(order = 1)
    int getX();

    @Value.Parameter(order = 2)
    int getY();

    static ImmutableIXY of(@NotNull XY xy) {
        return ImmutableIXY.of(xy.getX(), xy.getY());
    }
}
