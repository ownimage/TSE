package com.ownimage.framework.control.control;

import com.ownimage.framework.view.IView;

public interface IMouseControl {
	/**
	 * Sets the normalized value.
	 *
	 * @param pValue
	 *            the value
	 * @return true, if successful
	 * @see #setNormalizedValue(double, IView, boolean)
	 * @see #setValue(Object, IView, boolean)
	 */
	public void drag(final double pDelta);

	/**
	 * Sets the normalized value.
	 *
	 * @param pXDelta
	 *            the normalized x value delta
	 * @param pYDelta
	 *            the normalized y value delta
	 * @return true, if successful
	 */
	public void drag(final double pXDelta, final double pYDelta);

	public void dragEnd();

	public void dragStart();

	boolean isXYControl();

}
