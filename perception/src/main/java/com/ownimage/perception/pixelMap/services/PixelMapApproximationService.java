package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.control.control.ProgressControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class PixelMapApproximationService {

    private final static Logger logger = Framework.getLogger();

    private static PixelMapMappingService pixelMapMappingService = Services.getDefaultServices().getPixelMapMappingService();
    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();
    private static PixelService pixelService = Services.getDefaultServices().getPixelService();

    public ImmutablePixelMapData actionProcess(
            @NotNull ImmutablePixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            ProgressControl progres) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, transformSource).actionProcess(progres);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }
}
