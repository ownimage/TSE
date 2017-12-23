/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.awt.Color;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.DoubleMetaType.DisplayType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.render.ITransformResult;

public class BorderTransform extends BaseTransform {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	private final static Logger mLogger = Framework.getLogger();

	private double mBottom;
	private double mTop;
	private double mLeft;
	private double mRight;
	private Color mBorderColor;

	private final DoubleControl mBottomControl;
	private final DoubleControl mTopControl;
	private final DoubleControl mLeftControl;
	private final DoubleControl mRightControl;
	private final ColorControl mBorderColorControl;

	public BorderTransform(final Perception pPerception) {
		super("Border", "border");

		DoubleMetaType modelZeroHalf = new DoubleMetaType(0.0d, 0.5d, 0.05d, DisplayType.BOTH);
		DoubleMetaType modelHalfOne = new DoubleMetaType(0.5d, 1d, 0.05d, DisplayType.BOTH);
		mTopControl = new DoubleControl("Top", "top", getContainer(), 1.0d, modelHalfOne);
		mLeftControl = new DoubleControl("Left", "left", getContainer(), 0.0d, modelZeroHalf);
		mRightControl = new DoubleControl("Right", "right", getContainer(), 1.0d, modelHalfOne);
		mBottomControl = new DoubleControl("Bottom", "bottom", getContainer(), 0.0, modelZeroHalf);
		mBorderColorControl = new ColorControl("Color", "color", getContainer(), Color.WHITE);
		setValues();

		addYControl(mTopControl);
		addXControl(mLeftControl);
		addXControl(mRightControl);
		addYControl(mBottomControl);
	}

	@Override
	public void grafitti(final GrafittiHelper pGrafittiHelper) {
		pGrafittiHelper.drawHorizontalLine(mTop, getGrafitiColor1(), isControlSelected(mTopControl));
		pGrafittiHelper.drawHorizontalLine(mBottom, getGrafitiColor1(), isControlSelected(mBottomControl));
		pGrafittiHelper.drawVerticalLine(mLeft, getGrafitiColor1(), isControlSelected(mLeftControl));
		pGrafittiHelper.drawVerticalLine(mRight, getGrafitiColor1(), isControlSelected(mRightControl));
	}

	@Override
	public void setValues() {
		mBottom = mBottomControl.getValue();
		mTop = mTopControl.getValue();
		mLeft = mLeftControl.getValue();
		mRight = mRightControl.getValue();
		mBorderColor = mBorderColorControl.getValue();
	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pRenderResult, "pRenderResult");

		double x = pRenderResult.getX();
		double y = pRenderResult.getY();

		if (y < mBottom || y > mTop || x < mLeft || x > mRight) {
			pRenderResult.setColor(mBorderColor);
		}

		Point pOut = new Point( //
				(x - mLeft) / (mRight - mLeft), //
				(y - mBottom) / (mTop - mBottom) //
		);
		pRenderResult.setPoint(pOut);

		Framework.logExit(mLogger);
	}

}
