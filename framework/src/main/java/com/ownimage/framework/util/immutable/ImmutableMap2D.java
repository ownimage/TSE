package com.ownimage.framework.util.immutable;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.Framework;
import lombok.val;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class ImmutableMap2D<E> extends ImmutableNode<ImmutableMap2D.Map2D<E>> {

    public static final Logger mLogger = Framework.getLogger();

    public ImmutableMap2D(int pWidth, int pHeight, E pDefaultValue) {
        this(pWidth, pHeight, pDefaultValue, 1000);
    }

    public ImmutableMap2D(int width, int height, E pDefaultValue, int pDensity) {
        super(new Map2D<>(width, height, pDefaultValue, pDensity));
    }

    private ImmutableMap2D(Map2D<E> master) {
        super(master);
    }

    private ImmutableMap2D(ImmutableMap2D<E> previous, Consumer<Map2D<E>> pRedo, Consumer<Map2D<E>> pUndo) {
        super(previous, pRedo, pUndo);
    }

    public int width() {
        synchronized (getSynchronisationObject()) {
            return getMaster().width();
        }
    }

    public int height() {
        synchronized (getSynchronisationObject()) {
            return getMaster().height();
        }
    }

    public Optional<E> getOptional(int pX, int pY) {
        synchronized (getSynchronisationObject()) {
            return Optional.ofNullable(getMaster().get(pX, pY));
        }
    }

    public E get(int pX, int pY) {
        synchronized (getSynchronisationObject()) {
            return getMaster().get(pX, pY);
        }
    }

    public int size() {
        synchronized (getSynchronisationObject()) {
            return getMaster().mValues.size();
        }
    }

    public ImmutableMap2D<E> set(int pX, int pY, E pNewValue) {
        synchronized (getSynchronisationObject()) {
            E currentValue = getMaster().get(pX, pY);
            if (Objects.equals(pNewValue, currentValue)) {
                return this;
            }
            Consumer<Map2D<E>> redo = m -> m.set(pX, pY, pNewValue);
            Consumer<Map2D<E>> undo = m -> m.set(pX, pY, currentValue);
            return new ImmutableMap2D<>(this, redo, undo);
        }
    }

    public ImmutableMap2D<E> clear() {
        synchronized (getSynchronisationObject()) {
            Map2D<E> master = getMaster();
            Map2D empty = new Map2D<>(master);
            return new ImmutableMap2D<E>(empty);
        }
    }

    public ImmutableMap2D<E> forEach(Function<E, E> pFn) {
        synchronized (getSynchronisationObject()) {
            val master = getMaster();
            val copy = new Map2D<>(
                    master.mWidth,
                    master.mHeight,
                    pFn.apply(master.mDefaultValue),
                    master.mDensity
            );
            master.mValues.forEach((k, v) -> copy.mValues.put(k, pFn.apply(v)));
            return new ImmutableMap2D<>(copy);
        }
    }

    public static class Map2D<E> {
        private final int mWidth;
        private final int mHeight;
        private final E mDefaultValue;
        private final int mDensity;
        private final HashMap<IntegerPoint, E> mValues;

        public Map2D(int pWidth, int pHeight, E pDefaultValue, int pDensity) {
            mWidth = pWidth;
            mHeight = pHeight;
            mDefaultValue = pDefaultValue;
            mDensity = pDensity;
            mValues = new HashMap<>(mWidth * mHeight / mDensity);
        }

        public Map2D(Map2D<E> pFrom) {
            this(pFrom.mWidth, pFrom.mHeight, pFrom.mDefaultValue, pFrom.mDensity);
        }

        public int width() {
            return mWidth;
        }

        public int height() {
            return mHeight;
        }

        public E get(int pX, int pY) {
            checkXY(pX, pY);
            IntegerPoint key = new IntegerPoint(pX, pY);
            E value = mValues.get(key);
            return (value != null) ? value : mDefaultValue;
        }

        public void set(int pX, int pY, E pValue) {
            checkXY(pX, pY);
            IntegerPoint key = new IntegerPoint(pX, pY);
            if (Objects.equals(pValue, mDefaultValue)) {
                mValues.remove(key);
            } else {
                mValues.put(key, pValue);
            }
        }

        private void checkXY(int pX, int pY) {
            Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
            Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
            Framework.checkParameterLessThan(mLogger, pX, mWidth, "pX");
            Framework.checkParameterLessThan(mLogger, pY, mHeight, "pY");
        }
    }
}
