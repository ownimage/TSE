/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.container;

import com.ownimage.framework.view.ISingleSelectView;

public interface INamedContainerList extends IViewable<ISingleSelectView> {

	public IContainer getContainer(final String pTabName);

	public int getSelectedIndex();

	public String[] getTabNames();

	public void setSelectedIndex(final int pInt, final ISingleSelectView pView);

}
