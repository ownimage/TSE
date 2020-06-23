package com.ownimage.perception.pixelMap.services;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface Services {

    @NotNull VertexService getVertexService();

    static Services getDefaultServices() {
        var vertexService = new VertexService();
        vertexService.setVertexService(vertexService);
        return ImmutableServices.builder().vertexService(vertexService).build();
    }
}
