/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view;

import java.util.function.Consumer;

public interface IPictureView extends IView {

    public void drawCursor(Consumer<IGrafittiImp> pGrafitti);

    public void drawGrafitti(Consumer<IGrafittiImp> pGrafitti);

    public void updateGraffiti(Consumer<IGrafittiImp> pGrafitti);

}
