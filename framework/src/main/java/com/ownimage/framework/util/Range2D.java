/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class Range2D {

    private final int mXFrom;
    private final int mXTo;
    private final int mXStep;
    private final int mYFrom;
    private final int mYTo;
    private final int mYStep;

    /**
     * This will call the supplied funciton with all the x, y  values in the range 0..pX, 0..pY.  Note that the end values are exclusive.
     *
     * @param pX x end exclusive
     * @param pY y end exclusive
     */
    public Range2D(final int pX, final int pY) {
        this(0, pX, 0, pY);
    }

    /**
     * This will create a Range2D with the specified values and a step size of 1
     *
     * @param pXFrom x start inclusive
     * @param pXTo   x end exclusive
     * @param pYFrom y start inclusive
     * @param pYTo   y end exclusive
     */
    public Range2D(final int pXFrom, final int pXTo, final int pYFrom, final int pYTo) {
        this(pXFrom, pXTo, 1, pYFrom, pYTo, 1);
    }

    /**
     * This will create a Range2D with the specified values
     *
     * @param pXFrom x start inclusive
     * @param pXTo   x end exclusive
     * @param pXStep x step size
     * @param pYFrom y start inclusive
     * @param pYTo   y end exclusive
     * @param pYStep y step size
     */
    public Range2D(final int pXFrom, final int pXTo, final int pXStep, final int pYFrom, final int pYTo, final int pYStep) {
        mXFrom = pXFrom;
        mXTo = pXTo;
        mXStep = pXStep;
        mYFrom = pYFrom;
        mYTo = pYTo;
        mYStep = pYStep;
    }

    public void forEach(final BiConsumer<Integer, Integer> pFunction) {
        IntStream.range(0, Math.floorDiv(mXTo - mXFrom, mXStep))
                .forEach(x -> IntStream.range(0, Math.floorDiv(mYTo - mYFrom, mYStep))
                        .forEach(y -> pFunction.accept(mXFrom + x * mXStep, mYFrom + y * mYStep)));
    }

    public void forEachParallel(final BiConsumer<Integer, Integer> pFunction) {
        IntStream.range(0, Math.floorDiv(mXTo - mXFrom, mXStep)).parallel()
                .forEach(x -> IntStream.range(0, Math.floorDiv(mYTo - mYFrom, mYStep))
                        .forEach(y -> pFunction.accept(mXFrom + x * mXStep, mYFrom + y * mYStep)));
    }
}
