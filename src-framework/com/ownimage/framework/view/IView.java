/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.event.IControlChangeListener;

public interface IView extends IControlChangeListener<IControl<?, ?, ?, ?>> {

    public void redraw();

    public void setEnabled(boolean pEnabled);

    public void setVisible(boolean pVisible);

}
