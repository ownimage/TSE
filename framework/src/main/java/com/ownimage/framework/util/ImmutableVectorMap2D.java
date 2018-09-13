/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import io.vavr.collection.Vector;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ImmutableVectorMap2D<T> {

    public static Logger mLogger = Framework.getLogger();

    private final int mWidth;
    private final int mHeight;
    private final Vector<Vector<T>> mXY;

    public ImmutableVectorMap2D(final int pX, final int pY, final Supplier<T> pSupplier) {
        Framework.checkParameterGreaterThan(mLogger, pX, 0, "pX");
        Framework.checkParameterGreaterThan(mLogger, pY, 0, "pY");

        mWidth = pX;
        mHeight = pY;
        mXY = Vector.empty().fill(pY, () -> Vector.empty().fill(pX, pSupplier));
    }

    private ImmutableVectorMap2D(final int pX, final int pY, final Vector<Vector<T>> pXY) {
        mWidth = pX;
        mHeight = pY;
        mXY = pXY;
    }

    public Optional<T> get(final int pX, final int pY) {
        return checkXY(pX, pY) ? Optional.of(mXY.get(pY).get(pX)) : Optional.empty();
    }

    public Optional<ImmutableVectorMap2D<T>> set(final int pX, final int pY, final T pValue) {
        if (!checkXY(pX, pY)) return Optional.empty();
        final Vector<T> y = mXY.get(pY).update(pX, pValue);
        final Vector<Vector<T>> xy = mXY.update(pY, y);
        return Optional.of(new ImmutableVectorMap2D<T>(mWidth, mHeight, xy));
    }

    private boolean checkXY(final int pX, final int pY) {
        try {
            Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
            Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
            Framework.checkParameterLessThan(mLogger, pX, mWidth, "pX");
            Framework.checkParameterLessThan(mLogger, pY, mHeight, "pY");
            return true;
        } catch (final Throwable pT) {
            return false;
        }
    }

}