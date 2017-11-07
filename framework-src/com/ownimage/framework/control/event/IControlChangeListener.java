/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.event;

import com.ownimage.framework.util.Version;

public interface IControlChangeListener<C> {

	public final static Version mVersion = new Version(5, 0, 0, "2016/02/23 06:55");

	public void controlChangeEvent(C pControl, boolean pIsMutating);

}
