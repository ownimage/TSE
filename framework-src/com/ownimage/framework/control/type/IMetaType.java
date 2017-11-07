/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.io.Serializable;

import com.ownimage.framework.util.Version;

public interface IMetaType<D> extends Serializable, Cloneable {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	public boolean isValid(D pValue);

}
