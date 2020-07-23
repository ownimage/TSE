package com.ownimage.perception.pixelMap.services;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface Services {

    @NotNull PixelMapService getPixelMapService();
    @NotNull PixelMapActionService getPixelMapActionService();
    @NotNull PixelMapChainGenerationService getPixelMapGenerationService();
    @NotNull PixelMapApproximationService getPixelMapApproximationService();
    @NotNull PixelMapTransformService getPixelMapTransformService();
    @NotNull PixelService getPixelService();
    @NotNull PixelChainService getPixelChainService();
    @NotNull VertexService getVertexService();

    static Services getDefaultServices() {
        var pixelMapService = new PixelMapService();
        var pixelMapActionService = new PixelMapActionService();
        var pixelMapChainGenerationService = new PixelMapChainGenerationService();
        var pixelMapApproximationService = new PixelMapApproximationService();
        var pixelMapTransformService = new PixelMapTransformService();
        var pixelService = new PixelService();
        var pixelChainService = new PixelChainService();
        var vertexService = new VertexService();
        return ImmutableServices.builder()
                .pixelMapService(pixelMapService)
                .pixelMapActionService(pixelMapActionService)
                .pixelMapGenerationService(pixelMapChainGenerationService)
                .pixelMapApproximationService(pixelMapApproximationService)
                .pixelMapTransformService(pixelMapTransformService)
                .pixelService(pixelService)
                .pixelChainService(pixelChainService)
                .vertexService(vertexService)
                .build();
    }
}
