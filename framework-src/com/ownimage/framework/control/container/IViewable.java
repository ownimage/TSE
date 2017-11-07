package com.ownimage.framework.control.container;

import com.ownimage.framework.view.IView;

public interface IViewable<V extends IView> {

	/**
	 * Creates the view.
	 *
	 * @return the view
	 */
	public V createView();

	public String getDisplayName();

}
