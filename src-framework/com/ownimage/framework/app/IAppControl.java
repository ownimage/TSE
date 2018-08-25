/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.app;

import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IView;
import com.ownimage.perception.app.Properties;

public interface IAppControl {

	public IView getContent();

	public int getHeight();

	public MenuControl getMenu();

	public Properties getProperties();

	public String getTitle();

	public int getWidth();

	public int getX();

	public int getY();

	public void setHeight(int pHeight);

	public void setView(IAppControlView pAppControlView);

	public void setWidth(int pWidth);

	public void setX(int pX);

	public void setY(int pY);

}
