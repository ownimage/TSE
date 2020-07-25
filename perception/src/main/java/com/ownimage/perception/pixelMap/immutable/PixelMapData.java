package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.immutable.Immutable2DArray;
import com.ownimage.framework.util.immutable.ImmutableMap;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.segment.ISegment;
import io.vavr.Tuple2;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface PixelMapData {

    @Value.Default
    default int width() {
        return 10;
    }

    @Value.Default
    default int height() {
        return 10;
    }

    @Value.Default
    default boolean is360() {
        return false;
    }

    @Value.Default
    default ImmutableMap2D<Byte> data() {
        return new ImmutableMap2D<>(width(), height(), (byte) 0);
    }

    @Value.Default
    default ImmutableMap<IntegerPoint, Node> nodes() {
        return new ImmutableMap();
    }

    @Value.Default
    default ImmutableSet<PixelChain> pixelChains() {
        return new ImmutableSet<>();
    }

    @Value.Default
    default Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> segmentIndex() {
        return new Immutable2DArray<>(width(), height(), 10);
    }

    @Value.Default
    default int segmentCount() {
        return 0;
    }

    @Value.Default
    default boolean autoTrackChanges() {
        return false;
    }

    default double getAspectRatio() {
        return (double) width() / height();
    }

    ImmutablePixelMapData withData(@NotNull ImmutableMap2D<Byte> data);

    ImmutablePixelMapData withNodes(@NotNull ImmutableMap<IntegerPoint, Node> nodes);

    ImmutablePixelMapData withPixelChains(@NotNull ImmutableSet<PixelChain> pixelChains);

    ImmutablePixelMapData withSegmentIndex(@NotNull Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> segmentIndex);

    ImmutablePixelMapData withAutoTrackChanges(boolean autoTrackChanges);
}
