package com.ownimage.perception.pixelMap.immutable;

import org.immutables.value.Value;

@Value.Immutable(prehash = true)
public interface IXY extends XY {

    @Value.Parameter(order = 1)
    int getX();

    @Value.Parameter(order = 2)
    int getY();

}
