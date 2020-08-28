/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.util.function.Function;

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

    private T value;

    public StrongReference() {
    }

    public StrongReference(T value) {
        this.value = value;
    }

    public static <T> StrongReference<T> of(T value) {
        return new StrongReference<>(value);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void update(Function<T, T> update) {
        value = update.apply(value);
    }

}
