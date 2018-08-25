/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util;

/**
 * This is a strong reference to a object so that the value can be set from a lambda where you might get a `variable must be final or effectively final` message.
 * <p>
 * Example usage {@code
 * StrongReference<PictureType> preview = new StrongReference<>();
 * getResizedPictureTypeIfNeeded(500, null).ifPresent(preview::set);}
 *
 * @param <T> the type of the Reference
 */
public class StrongReference<T> {

    private T mValue;

    public StrongReference() {
    }

    public StrongReference(final T pValue) {
        mValue = pValue;
    }

    public T get() {
        return mValue;
    }

    public void set(final T pValue) {
        mValue = pValue;
    }

}
