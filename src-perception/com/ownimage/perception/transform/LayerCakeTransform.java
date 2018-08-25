/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.DoubleMetaType.DisplayType;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.render.ITransformResult;

public class LayerCakeTransform extends BaseTransform {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	private final BooleanControl mMirrorTop;
	private final BooleanControl m180Top;
	private final BooleanControl mHalfWidth;
	private final DoubleControl mCutOffSlider;

	private double mCutOffHeight;

	public LayerCakeTransform(final Perception pPerception) {
		super("Layer Cake", "layerCake");

		mCutOffSlider = new DoubleControl("CutOff", "cutoff", getContainer(), 0.5d, new DoubleMetaType(1.0d / 6.0d, 1.0d / 1.2d, 0.01d, DisplayType.BOTH));
		mMirrorTop = new BooleanControl("Mirror top", "mirror", getContainer(), true);
		m180Top = new BooleanControl("180 top", "180top", getContainer(), true);
		mHalfWidth = new BooleanControl("Half width", "half", getContainer(), true);

		setValues();

		addYControl(mCutOffSlider);
	}

	@Override
	public void grafitti(final GrafittiHelper pGrafittiHelper) {
		Framework.logEntry(mLogger);

		pGrafittiHelper.drawHorizontalLine(mCutOffSlider.getValue(), getGrafitiColor1(), false);
		pGrafittiHelper.drawString(mCutOffSlider, 0.0, mCutOffSlider.getValue());

		Framework.logExit(mLogger);
	}

	@Override
	public void setValues() {
		Framework.logEntry(mLogger);

		mCutOffHeight = mCutOffSlider.getValue(); // 1.0 / (1.0 + mCutOff);

		mLogger.finest("mCutOffSlider.getValue():" + mCutOffSlider.getValue());
		Framework.logExit(mLogger);
	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pRenderResult, "pRenderResult");

		double x = pRenderResult.getPoint().getX();
		double y = 0;

		if (mHalfWidth.getValue()) {
			x = 0.5 * x;
		}

		if (pRenderResult.getPoint().getY() <= mCutOffHeight) {
			// original layer
			y = pRenderResult.getPoint().getY() / mCutOffHeight;

		} else {
			// inverted layer
			y = (1.0 - pRenderResult.getPoint().getY()) / (1.0 - mCutOffHeight);

			if (mMirrorTop.getValue()) {
				x = 1.0 - x;
			}
			if (m180Top.getValue()) {
				x += 0.5;
			}
		}

		x = mod1(x);

		pRenderResult.setPoint(new Point(x, y));
		Framework.logExit(mLogger);
	}
}
