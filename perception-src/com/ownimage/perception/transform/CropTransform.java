/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.KMath;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.render.ITransformResult;

public class CropTransform extends BaseTransform implements IControlValidator {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	private final static Logger mLogger = Framework.getLogger();

	public static double x() {
		return 1.0d;
	}

	private double mBottom;
	private double mTop;
	private double mLeft;

	private double mRight;
	private final DoubleControl mBottomControl;
	private final DoubleControl mTopControl;
	private final DoubleControl mLeftControl;

	private final DoubleControl mRightControl;

	public CropTransform(final Perception pPerception) {
		super(pPerception);

		mBottomControl = new DoubleControl("Bottom", "bottom", getContainer(), 0.1d);
		mTopControl = new DoubleControl("Top", "top", getContainer(), 0.9d);
		mLeftControl = new DoubleControl("Left", "left", getContainer(), 0.1d);
		mRightControl = new DoubleControl("Right", "right", getContainer(), 0.9d);
		setValues();

		mBottomControl.addControlValidator(this);
		mTopControl.addControlValidator(this);
		mLeftControl.addControlValidator(this);
		mRightControl.addControlValidator(this);

		addYControl(mTopControl);
		addXControl(mLeftControl);
		addXControl(mRightControl);
		addYControl(mBottomControl);
	}

	@Override
	public String getDisplayName() {
		return "Crop";
	}

	@Override
	public String getPropertyName() {
		return "crop";
	}

	@Override
	public void grafitti(final GrafittiHelper pGrafittiHelper) {
		pGrafittiHelper.drawHorizontalLine(mTop, getGrafitiColor1(), isControlSelected(mTopControl));
		pGrafittiHelper.drawHorizontalLine(mBottom, getGrafitiColor1(), isControlSelected(mBottomControl));
		pGrafittiHelper.drawVerticalLine(mLeft, getGrafitiColor1(), isControlSelected(mLeftControl));
		pGrafittiHelper.drawVerticalLine(mRight, getGrafitiColor1(), isControlSelected(mRightControl));
	}

	public void setCrop(final double pLeft, final double pBottom, final double pRight, final double pTop, final boolean pEnabled) {
		mLeftControl.setValue(pLeft);
		mBottomControl.setValue(pBottom);
		mRightControl.setValue(pRight);
		mTopControl.setValue(pTop);
		setUseTransform(pEnabled);
	}

	// public void setCrop(final Rectangle pBounds, final boolean pEnabled) {
	// setCrop(pBounds.getLeft(), pBounds.getBottom(), pBounds.getRight(), pBounds.getTop(), pEnabled);
	// }

	@Override
	public void setValues() {
		mBottom = KMath.mod1inc(mBottomControl.getValue());
		mTop = KMath.mod1inc(mTopControl.getValue());
		mLeft = KMath.mod1inc(mLeftControl.getValue());
		mRight = KMath.mod1inc(mRightControl.getValue());
	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pRenderResult, "pRenderResult");

		double x = pRenderResult.getX();
		double y = pRenderResult.getY();

		final Point point = new Point( //
				mLeft + x * (mRight - mLeft), //
				mBottom + y * (mTop - mBottom) //
		);

		pRenderResult.setPoint(point);

		Framework.logExit(mLogger);
	}

	@Override
	public boolean validateControl(final Object pControl) {
		final boolean rv = mBottomControl.getValidateValue() < mTopControl.getValidateValue() //
				&& mLeftControl.getValidateValue() < mRightControl.getValidateValue();
		return rv;
	}

}
