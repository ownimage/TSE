/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.io.Serializable;

public interface IMetaType<D> extends Serializable, Cloneable {


    public boolean isValid(D pValue);

}
