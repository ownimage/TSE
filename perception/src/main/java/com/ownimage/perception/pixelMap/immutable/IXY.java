package com.ownimage.perception.pixelMap.immutable;

import org.immutables.value.Value;

@Value.Immutable(prehash = true)
public interface IXY extends XY {

    @Override
    @Value.Parameter(order = 1)
    int getX();

    @Override
    @Value.Parameter(order = 2)
    int getY();

}
