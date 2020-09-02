/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.io.Serializable;

public interface IMetaType<D> extends Serializable, Cloneable {

    boolean isValid(D value);

    default String getString(D value) {
        return value.toString();
    }
}
