package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.util.immutable.Immutable2DArray;
import com.ownimage.framework.util.immutable.ImmutableMap;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import io.vavr.Tuple2;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
public interface PixelMap {

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
    default ImmutableMap<PixelMapGridPosition, Node> nodes() {
        return new ImmutableMap();
    }

    @Value.Default
    default ImmutableSet<ImmutablePixelChain> pixelChains() {
        return new ImmutableSet<>();
    }

    @Value.Default
    default Immutable2DArray<ImmutableSet<Tuple2<PixelChain, Segment>>> segmentIndex() {
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

    ImmutablePixelMap withData(@NotNull ImmutableMap2D<Byte> data);

    ImmutablePixelMap withNodes(@NotNull ImmutableMap<PixelMapGridPosition, Node> nodes);

    ImmutablePixelMap withPixelChains(@NotNull ImmutableSet<ImmutablePixelChain> pixelChains);

    ImmutablePixelMap withSegmentIndex(@NotNull Immutable2DArray<ImmutableSet<Tuple2<PixelChain, Segment>>> segmentIndex);

    ImmutablePixelMap withAutoTrackChanges(boolean autoTrackChanges);
}
