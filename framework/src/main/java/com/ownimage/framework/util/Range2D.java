/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import com.ownimage.framework.math.IntegerPoint;
import lombok.val;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Range2D implements Iterable<IntegerPoint> {

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
        for (int x = mXFrom; x < mXTo; x = x + mXStep)
            for (int y = mYFrom; y < mYTo; y = y + mYStep)
                pFunction.accept(x, y);
    }

    /**
     * Possible performance impacts
     *
     * @param pFunction
     */
    @Deprecated
    public void forEachParallel(final BiConsumer<Integer, Integer> pFunction) {
        stream().parallel().forEach(i -> pFunction.accept(i.getX(), i.getY()));
    }

    public Iterator<IntegerPoint> iterator() {
        return new Iterator<>() {

            private int mX = mXFrom;
            private int mY = mYFrom;
            private IntegerPoint mNext = new IntegerPoint(mX, mY);

            @Override
            public boolean hasNext() {
                return mNext.getX() < mXTo && mNext.getY() < mYTo;
            }

            @Override
            public IntegerPoint next() {
                if (!hasNext()) throw new NoSuchElementException();
                val current = mNext;
                mY += mYStep;
                if (mY >= mYTo) {
                    mY = mYFrom;
                    mX += mXStep;
                }
                mNext = new IntegerPoint(mX, mY);
                return current;
            }
        };
    }

    /**
     * There are concerns that making this into a parallel stream might impact performance
     *
     * @return
     */
    public Stream<IntegerPoint> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
