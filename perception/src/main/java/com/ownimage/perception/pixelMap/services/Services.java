package com.ownimage.perception.pixelMap.services;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface Services {

    @NotNull PixelMapService getPixelMapService();
    @NotNull PixelService getPixelService();
    @NotNull PixelChainService getPixelChainService();
    @NotNull VertexService getVertexService();

    static Services getDefaultServices() {
        var pixelMapService = new PixelMapService();
        var pixelService = new PixelService();
        var pixelChainService = new PixelChainService();
        var vertexService = new VertexService();
        return ImmutableServices.builder()
                .pixelMapService(pixelMapService)
                .pixelService(pixelService)
                .pixelChainService(pixelChainService)
                .vertexService(vertexService)
                .build();
    }
}
