package com.ownimage.perception.pixelMap.services;

import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PixelMapMappingService {

    public ImmutablePixelMapData toImmutablePixelMapData(@NotNull PixelMapData pixelMap) {
        if (pixelMap instanceof ImmutablePixelMapData) {
            return (ImmutablePixelMapData) pixelMap;
        }
        return ImmutablePixelMapData.builder()
                .width(pixelMap.width())
                .height(pixelMap.height())
                .is360(pixelMap.is360())
                .data(pixelMap.data())
                .nodes(pixelMap.nodes())
                .pixelChains(pixelMap.pixelChains())
                .segmentIndex(pixelMap.segmentIndex())
                .segmentCount(pixelMap.segmentCount())
                .autoTrackChanges(pixelMap.autoTrackChanges())
                .build();
    }

    public PixelMap toPixelMap(
            @NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData,
            @Nullable IPixelMapTransformSource transformSource) {
        return new PixelMap(pixelMapData.width(), pixelMapData.height(), pixelMapData.is360(), transformSource)
                .withData(pixelMapData.data())
                .withNodes(pixelMapData.nodes())
                .withPixelChains(pixelMapData.pixelChains().toCollection())
                .withSegmentIndex(pixelMapData.segmentIndex())
                .withSegmentCount(pixelMapData.segmentCount())
                .withAutoTrackChanges(pixelMapData.autoTrackChanges());
    }
}
