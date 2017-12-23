/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;

public class WoodcutTransform extends BaseTransform {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	private final static Logger mLogger = Framework.getLogger();

	private final PictureControl mEtchPicture;
	private final IntegerControl mLines = new IntegerControl("Lines", "lines", getContainer(), 50, 10, 100, 10);
	private final DoubleControl mMinThickness = new DoubleControl("Min Thickness", "minThickness", getContainer(), 0.5d, 0.0d, 0.5d);
	private final DoubleControl mScale = new DoubleControl("Scale", "scale", getContainer(), 1.0d, 0.0d, 1.0d);
	private final ColorControl mTopLineColor = new ColorControl("Top Line Color", "topLineColor", getContainer(), Color.BLACK);
	private final ColorControl mBottomLineColor = new ColorControl("Bottom Line Color", "bottomLineColor", getContainer(), Color.GRAY);
	private final BooleanControl mUseBackground = new BooleanControl("Use Background", "useBackground", getContainer(), false);
	private final ColorControl mBackground = new ColorControl("Background", "background", getContainer(), Color.WHITE);
	private final DoubleControl mBackgroundTransparency = new DoubleControl("Background Transparency", "backgroundTransparency", getContainer(), 0.5d);

	public WoodcutTransform(final Perception pPerception) {
		super("Wood Cut", "woodCut");
		PictureType etch = new PictureType(getProperties().getColorOOBProperty(), 100, mLines.getValue());
		mEtchPicture = new PictureControl("Etch", "etch", NullContainer, etch);

	}

	private void createEtch() {
		System.out.println("createEtch");
		int w = getWidth();
		PictureType etch = new PictureType(getProperties().getColorOOBProperty(), w, mLines.getValue());
		mEtchPicture.setValue(etch);
		getPerception().getRenderService().transform(mEtchPicture, getPreviousTransform(), null);
		// TODO should make this part of the
		// TransformBase
	}

	@Override
	public void grafitti(final GrafittiHelper pGrafittiHelper) {
	}

	@Override
	public void setPreviousTransform(final ITransform pPreviousTransform) {
		super.setPreviousTransform(pPreviousTransform);
		createEtch();
	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pRenderResult, "pRenderResult");

		double x = pRenderResult.getX();
		double y = pRenderResult.getY();

		Color ct = mEtchPicture.getValue().getColor(x, y);
		double thickness = mScale.getValue() * ((255 - ct.getRed()) / 512.0f);

		double lines = mLines.getValue();
		double yl = (y - 0.5d / lines) * lines; // the y value in the lines space (-.5 to lines - .5)
		if (yl < -0.5d) {
			yl = -0.5d;
			mLogger.log(Level.SEVERE, "yl should be >= -0.5d, but is " + yl);
		}
		if (yl > lines - 0.5) {
			yl = lines - 0.5d;
			mLogger.log(Level.SEVERE, "yl should be < lines-0.5d, but is " + yl);
		}

		double line = Math.round(yl);
		double delta = yl - line;

		if (thickness > mMinThickness.getValue() && Math.abs(delta) < thickness) {
			if (delta >= 0.0d) {
				pRenderResult.setColor(mTopLineColor.getValue());
			} else {
				pRenderResult.setColor(mBottomLineColor.getValue());
			}
		} else if (mUseBackground.getValue()) {
			float[] b = mBackground.getValue().getColorComponents(null);
			double t = mBackgroundTransparency.getValue();
			float a = (float) t;
			Color c = new Color(b[0], b[1], b[2], a);
			pRenderResult.setColor(c);
		}
		Framework.logExit(mLogger);
	}
}
