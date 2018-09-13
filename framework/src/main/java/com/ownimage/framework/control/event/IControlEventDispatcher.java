/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.event;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.view.IView;

public interface IControlEventDispatcher<C> {


    public void addControlChangeListener(IControlChangeListener<IControl<?, ?, ?, ?>> pListener);

    public void addControlValidator(IControlValidator pValidator);

    public void fireControlChangeEvent(C pControl);

    public void fireControlChangeEvent(C pControl, IView pView, boolean pIsMutating);

    public boolean fireControlValidate(C pControl);

    public void removeControlChangeListener(IControlChangeListener<C> pLIstener);

    public void removeControlValidator(IControlValidator pValidator);

}
