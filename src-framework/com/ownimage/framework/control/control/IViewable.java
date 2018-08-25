/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.control.control;

import com.ownimage.framework.view.IView;

public interface IViewable<C> {

	/**
	 * Creates the view.
	 *
	 * @return the view
	 */
	public IView createView();

	/**
	 * Checks if is enabled.
	 *
	 * @return true, if is enabled
	 */
	public boolean isEnabled();

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	public boolean isVisible();

	/**
	 * Sets the enabled.
	 *
	 * @param pEnabled
	 *            the new enabled
	 * @return this control
	 */
	public C setEnabled(boolean pEnabled);

	/**
	 * Sets the visible flag for this obejct and calls setVisible on all of the Views of this object.
	 *
	 * @param pVisible
	 *            the new visible
	 * @return this Control
	 */
	public C setVisible(boolean pVisible);

}
