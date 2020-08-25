package com.ownimage.perception.pixelMap.services;

import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.Segment;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.stream.IntStream;

public class PixelMapTestSupport {

    public Tuple2<Integer, Integer> countUniquePixelChainsAndSegmentsInIndex(@NotNull ImmutablePixelMap pixelMap) {
        var pixelChains = new HashSet<PixelChain>();
        var segments = new HashSet<Segment>();
        IntStream.range(0, pixelMap.width())
                .forEach(x -> IntStream.range(0, pixelMap.height())
                        .mapToObj(y -> pixelMap.segmentIndex().getOptional(x, y))
                        .filter(s -> s.isPresent())
                        .flatMap(s -> s.get().stream())
                        .forEach(t2 -> {
                            pixelChains.add(t2._1);
                            segments.add(t2._2);
                        })
                );
        return new Tuple2<>(pixelChains.size(), segments.size());
    }
}
