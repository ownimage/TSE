/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import java.awt.Color;
import java.util.logging.Logger;

import com.ownimage.perception.control.combo.PictureControl;
import com.ownimage.perception.control.combo.PointControl;
import com.ownimage.perception.control.group.IControlContainer;
import com.ownimage.perception.transform.GraphicsHelper;
import com.ownimage.perception.transform.ITransform;
import com.ownimage.perception.util.Version;

public class EdgePointControl extends PointControl {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = EdgePointControl.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final PictureControl mPictureControl;
	private final ITransform mTransform;

	public EdgePointControl(final ITransform pTransform, final PictureControl pPictureControl, final IControlContainer pParent, final String pDisplayName, final String pPropertyName) {
		super(pParent, pDisplayName, pPropertyName);
		mTransform = pTransform;
		mPictureControl = pPictureControl;
	}

	@Override
	public void graffiti(final GraphicsHelper pGraphics) {
		final double xSize = (double) mPictureControl.getSize() / mTransform.getWidth();
		final double ySize = (double) mPictureControl.getSize() / mTransform.getHeight();
		final Color color = mTransform.getControlSelector().isControlSelected(this) ? pGraphics.getGrafitiColor1() : pGraphics.getGrafitiColor2();
		pGraphics.drawSquare(getPoint(), 0.02d, null, color);
		pGraphics.drawRectangle(mXControl.getDouble(), mYControl.getDouble(), mXControl.getDouble() + xSize, mYControl.getDouble() + ySize, color);
		pGraphics.drawString(getDisplayName(), getPoint());
	}

}
