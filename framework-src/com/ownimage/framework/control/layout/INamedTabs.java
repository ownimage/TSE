package com.ownimage.framework.control.layout;

import com.ownimage.framework.view.ISingleSelectView;

public interface INamedTabs extends IViewable<ISingleSelectView> {

	public int getSelectedIndex();

	public String[] getTabNames();

	public IViewable<?> getViewable(final String pTabName);

	public void setSelectedIndex(final int pInt, final ISingleSelectView pView);

}
