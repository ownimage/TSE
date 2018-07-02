package com.ownimage.framework.util;

import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class Range2D {

    /**
     * This will call the supplied funciton with all the x, y  values in the range 0..pX, 0..pY.  Note that the end values are exclusive.
     *
     * @param pX        x end exclusive
     * @param pY        y end exclusive
     * @param pFunction function to be called
     */
    public static void forEach(int pX, int pY, BiConsumer<Integer, Integer> pFunction) {
        forEach(0, pX, 0, pY, pFunction);
    }

    /**
     * This will call the supplied funciton with the x,y values in the ranges specified
     *
     * @param pXFrom    x start inclusive
     * @param pXTo      x end exclusive
     * @param pYFrom    y start inclusive
     * @param pYTo      y end exclusive
     * @param pFunction function to be called
     */
    public static void forEach(int pXFrom, int pXTo, int pYFrom, int pYTo, BiConsumer<Integer, Integer> pFunction) {
        // not this does not delegate to the version with steps so that it does not incur the computational overhead
        IntStream.range(pXFrom, pXTo)
                .forEach(x -> {
                    IntStream.range(pYFrom, pYTo)
                            .forEach(y -> {
                                pFunction.accept(x, y);
                            });
                });
    }

    public static void forEach(int pXFrom, int pXTo, int pXStep, int pYFrom, int pYTo, int pYStep, BiConsumer<Integer, Integer> pFunction) {
        IntStream.range(0, Math.floorDiv(pXTo - pXFrom, pXStep))
                .forEach(x -> {
                    IntStream.range(0, Math.floorDiv(pYTo - pYFrom, pYStep))
                            .forEach(y -> {
                                pFunction.accept(pXFrom + x * pXStep, pYFrom + y * pYStep);
                            });
                });
    }
}
