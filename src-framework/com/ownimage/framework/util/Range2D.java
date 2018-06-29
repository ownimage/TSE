package com.ownimage.framework.util;

import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class Range2D {

    public static void forEach(int pX, int pY, BiConsumer<Integer, Integer> pFunction) {
        // The IntStream.range by default is not parallel.  It is only made parallel in the x direction.
        // For large x, y images the overhead of parallalizing in 2 dimensions might offset some of the
        // parallel advantages.
        IntStream.range(0, pX)
                .parallel().forEach(x -> {
            IntStream.range(0, pY)
                    .forEach(y -> {
                        pFunction.accept(x, y);
                    });
        });
    }
}
