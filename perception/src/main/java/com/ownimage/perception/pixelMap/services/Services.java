package com.ownimage.perception.pixelMap.services;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface Services {

    @NotNull VertexService getVertexService();
    @NotNull PixelChainService getPixelChainService();

    static Services getDefaultServices() {
        var pixelChainService = new PixelChainService();
        var vertexService = new VertexService();
        return ImmutableServices.builder()
                .vertexService(vertexService)
                .pixelChainService(pixelChainService)
                .build();
    }
}
