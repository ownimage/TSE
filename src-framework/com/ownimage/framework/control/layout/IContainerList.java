package com.ownimage.framework.control.layout;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.view.ISingleSelectView;

public interface IContainerList extends IViewable<ISingleSelectView> {

	public IContainer getContainer(final int pTab);

	public int getCount();

	public int getSelectedIndex();

    public void setSelectedIndex(IContainer pContainer);

    public void setSelectedIndex(final int pSelectedIndex);

	public void setSelectedIndex(final int pSelectedIndex, final ISingleSelectView pView);

}
