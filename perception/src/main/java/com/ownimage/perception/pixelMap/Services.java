package com.ownimage.perception.pixelMap;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface Services {

    @NotNull VertexService getVertexService();

    static Services getDefaultServices() {
        return ImmutableServices.builder().vertexService(new VertexService()).build();
    }
}
