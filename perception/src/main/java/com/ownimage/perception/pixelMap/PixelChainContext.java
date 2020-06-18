package com.ownimage.perception.pixelMap;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface PixelChainContext {
    @Value.Parameter(order = 1)
    @NotNull PixelMap getPixelMap();

    @Value.Parameter(order = 2)
    @NotNull IPixelChain getPixelChain();
}
