package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface VertexData {
    @NotNull int getVertexIndex();
    @NotNull int getPixelIndex();
    @NotNull Point getPosition();
}
