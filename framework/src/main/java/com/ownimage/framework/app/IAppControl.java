/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.app;

import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IView;

public interface IAppControl {

    IView getContent();

    int getHeight();

    MenuControl getMenu();

    String getTitle();

    int getWidth();

    int getX();

    int getY();

    void setHeight(int pHeight);

    void setView(IAppControlView pAppControlView);

    void setWidth(int pWidth);

    void setX(int pX);

    void setY(int pY);

}
