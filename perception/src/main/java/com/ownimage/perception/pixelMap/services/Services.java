package com.ownimage.perception.pixelMap.services;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface Services {

    @NotNull PixelMapService getPixelMapService();
    @NotNull PixelMapChainGenerationService pixelMapChainGenerationService();
    @NotNull PixelMapApproximationService getPixelMapApproximationService();
    @NotNull PixelMapTransformService getPixelMapTransformService();
    @NotNull PixelMapMappingService getPixelMapMappingService();
    @NotNull PixelService getPixelService();
    @NotNull PixelChainService getPixelChainService();
    @NotNull VertexService getVertexService();

    static Services getDefaultServices() {
        var pixelMapService = new PixelMapService();
        var pixelMapChainGenerationService = new PixelMapChainGenerationService();
        var pixelMapApproximationService = new PixelMapApproximationService();
        var pixelMapTransformService = new PixelMapTransformService();
        var pixelMapMappingService = new PixelMapMappingService();
        var pixelService = new PixelService();
        var pixelChainService = new PixelChainService();
        var vertexService = new VertexService();
        return ImmutableServices.builder()
                .pixelMapService(pixelMapService)
                .pixelMapChainGenerationService(pixelMapChainGenerationService)
                .pixelMapApproximationService(pixelMapApproximationService)
                .pixelMapTransformService(pixelMapTransformService)
                .pixelMapMappingService(pixelMapMappingService)
                .pixelService(pixelService)
                .pixelChainService(pixelChainService)
                .vertexService(vertexService)
                .build();
    }
}
