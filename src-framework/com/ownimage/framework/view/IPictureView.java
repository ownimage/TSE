/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view;

import java.util.function.Consumer;

public interface IPictureView extends IView {

    public void redrawGrafitti(Consumer<IGrafittiImp> pGrafitti);

    public void updateGrafitti(Consumer<IGrafittiImp> pGrafitti);

}
