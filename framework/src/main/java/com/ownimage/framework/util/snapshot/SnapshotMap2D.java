/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util.snapshot;

import com.ownimage.framework.util.Framework;

import java.util.HashMap;
import java.util.logging.Logger;


public class SnapshotMap2D<T> extends Snapshot<HashMap<Object, T>> {

    public static final Logger mLogger = Framework.getLogger();

    private final int mWidth;
    private final int mHeight;
    private final T mDefaultValue;

    public SnapshotMap2D(final int pWidth, final int pHeight, final T pDefaultValue) {
        super(new HashMap<>());
        Framework.checkParameterGreaterThan(mLogger, pWidth, 0, "pWidth");
        Framework.checkParameterGreaterThan(mLogger, pHeight, 0, "pHeight");

        mWidth = pWidth;
        mHeight = pHeight;
        mDefaultValue = pDefaultValue;
    }

    private SnapshotMap2D(final SnapshotMap2D<T> pOther, Link<HashMap<Object, T>> link) {
        super(pOther, link);
        mWidth = pOther.mWidth;
        mHeight = pOther.mHeight;
        mDefaultValue = pOther.mDefaultValue;
    }

    public SnapshotMap2D<T> snapshot() {
        synchronized (mSharedSyncObject) {
            final Link<HashMap<Object, T>> link = createLinkReadyForNewSnapshot();
            return new SnapshotMap2D<>(this, link);
        }
    }

    public T get(final int pX, final int pY) {
        checkXY(pX, pY);
        final Long key = generateKey(pX, pY);
        synchronized (mSharedSyncObject) {
            T value = getMaster().get(key);
            if (value != null) return value;
            else return mDefaultValue;
        }
    }

    public void set(final int pX, final int pY, final T pValue) {
        checkXY(pX, pY);
        final Long key = generateKey(pX, pY);
        synchronized (mSharedSyncObject) {
            final T oldValue = getMaster().put(key, pValue);
            addChangeLogEntry(key, m -> m.put(key, pValue), m -> m.put(key, oldValue));
        }
    }

    private Long generateKey(final int pX, final int pY) {
        return (long) pX * mWidth + pY;
    }

    private void checkXY(final int pX, final int pY) {
        Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
        Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
        Framework.checkParameterLessThan(mLogger, pX, mWidth, "pX");
        Framework.checkParameterLessThan(mLogger, pY, mHeight, "pY");
    }
}

