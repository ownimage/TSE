package com.ownimage.perception.pixelMap.services;

import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PixelMapMappingService {

    public ImmutablePixelMapData toImmutablePixelMapData(@NotNull PixelMap pixelMap) {
        return ImmutablePixelMapData.builder()
                .width(pixelMap.getWidth())
                .height(pixelMap.getHeight())
                .is360(pixelMap.is360())
                .data(pixelMap.getData())
                .nodes(pixelMap.getImmutableNodeMap())
                .pixelChains(pixelMap.getPixelChains())
                .segmentIndex(pixelMap.getSegmentIndex())
                .segmentCount(pixelMap.getSegmentCount())
                .autoTrackChanges(pixelMap.isAutoTrackChanges())
                .build();
    }

    public PixelMap toPixelMap(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, @Nullable IPixelMapTransformSource transformSource) {
        return new PixelMap(pixelMapData.width(), pixelMapData.height(), pixelMapData.is360(), transformSource)
                .withData(pixelMapData.data())
                .withNodes(pixelMapData.nodes())
                .withPixelChains(pixelMapData.pixelChains().toCollection())
                .withSegmentIndex(pixelMapData.segmentIndex())
                .withSegmentCount(pixelMapData.segmentCount())
                .withAutoTrackChanges(pixelMapData.autoTrackChanges());
    }
}
