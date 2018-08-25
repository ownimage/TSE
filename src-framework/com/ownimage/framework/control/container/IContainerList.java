/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.container;

import com.ownimage.framework.view.ISingleSelectView;

public interface IContainerList extends IViewable<ISingleSelectView> {

	public IContainer getContainer(final int pTab);

	public int getCount();

	public int getSelectedIndex();

	public void setSelectedIndex(final int pSelectedIndex);

	public void setSelectedIndex(final int pSelectedIndex, final ISingleSelectView pView);

}
