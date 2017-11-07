/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import java.awt.Color;
import java.util.logging.Logger;

import com.ownimage.perception.control.combo.PointControl;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.GraphicsHelper;
import com.ownimage.perception.util.Version;

public class PreviewPosition extends PointControl {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = PreviewPosition.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private double mWidth = 0.1d;
	private double mHeight = 0.1d;
	private final CannyEdgeTransform mTransform;

	public PreviewPosition(final CannyEdgeTransform pParent, final String pDisplayName, final String pPropertyName) {
		super(pParent, pDisplayName, pPropertyName);

		if (pParent == null) {
			throw new IllegalArgumentException("pParent must not be null.");
		}

		mTransform = pParent;
	}

	public double getHeight() {
		return mHeight;
	}

	public double getWidth() {
		return mWidth;
	}

	@Override
	public void graffiti(final GraphicsHelper pGraphics) {
		final Color color = mTransform.getControlSelector().isControlSelected(this) ? pGraphics.getGrafitiColor1() : pGraphics.getGrafitiColor2();
		pGraphics.drawSquare(getPoint(), 0.02d, null, color);
		pGraphics.drawRectangle(mXControl.getDouble(), mYControl.getDouble(), mXControl.getDouble() + mWidth, mYControl.getDouble() + mHeight, color);
		pGraphics.drawString(getDisplayName(), getPoint());
	}

	public PreviewPosition setSize(final double pWidth, final double pHeight) {
		mWidth = pWidth;
		mHeight = pHeight;
		return this;
	}

	@Override
	public String toString() {
		return "PreviewPosition extends(" + super.toString() + "), width=" + mWidth + ", height=" + mHeight;
	}

};
