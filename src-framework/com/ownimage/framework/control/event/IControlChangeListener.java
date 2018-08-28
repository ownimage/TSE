/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.event;

public interface IControlChangeListener<C> {


    void controlChangeEvent(C pControl, boolean pIsMutating);

}
