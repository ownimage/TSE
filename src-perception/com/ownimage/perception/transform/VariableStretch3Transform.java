package com.ownimage.perception.transform;

import java.util.logging.Logger;

import Jama.Matrix;

import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.KMath;
import com.ownimage.perception.math.LinearEquation;
import com.ownimage.perception.math.QuadraticEquation;
import com.ownimage.perception.render.ITransformResult;

public class VariableStretch3Transform extends BaseTransform implements IControlValidator {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	@SuppressWarnings("unused")
	private final static Logger mLogger = Framework.getLogger();
	private final DoubleControl mCutoffControl;

	private final DoubleControl mHighControl;
	private final DoubleControl mHighestControl;
	private final DoubleControl mLowControl;
	private final DoubleControl mLowestControl;
	private final BooleanControl mRestrictWidthControl;
	private final DoubleControl mCentreControl;
	private final DoubleControl mWidthControl;
	private final DoubleControl mBlendControl;
	private double mCentre;

	private double mWidth;
	private double mBlend;
	private boolean mRestrictWidth;
	private QuadraticEquation mUpperQE;
	private QuadraticEquation mLowerQE;
	private LinearEquation mMidLE;

	public VariableStretch3Transform(final Perception pPerception) {
		super(pPerception);

		mCutoffControl = new DoubleControl("Cutoff", "cutoff", getContainer(), 0.8d);
		mHighestControl = new DoubleControl("Highest", "highest", getContainer(), 0.6d);
		mHighControl = new DoubleControl("High", "high", getContainer(), 0.5d);
		mLowControl = new DoubleControl("Low", "low", getContainer(), 0.4d);
		mLowestControl = new DoubleControl("Lowest", "lowest", getContainer(), 0.2d);
		mRestrictWidthControl = new BooleanControl("Restrict Width", "restrictWidth", getContainer(), true);
		mCentreControl = new DoubleControl("Center", "center", getContainer(), 0.5d, 0.0d, 0.999d);
		mWidthControl = new DoubleControl("Width", "width", getContainer(), 0.25d, 0.0d, 0.5d);
		mBlendControl = new DoubleControl("Blend", "blend", getContainer(), 0.125d, 0.0d, 0.25d);

		mHighControl.addControlValidator(this);
		mLowControl.addControlValidator(this);

		setValues();

		addXControl(mCentreControl);
		addXControl(mWidthControl);
		addXControl(mBlendControl);
		addYControl(mCutoffControl);
		addYControl(mHighControl);
		addYControl(mHighestControl);
		addYControl(mLowControl);
		addYControl(mLowestControl);
	}

	@Override
	public String getDisplayName() {
		return "Variable Stretch";
	}

	@Override
	public String getPropertyName() {
		return "variableStretch";
	}

	@Override
	public String getTransformName() {
		return "variableStretchTransform";
	}

	// @Override
	// public Color transform(final Point pIn) {
	// final double yIn = pIn.getY();
	// double y = 0;
	//
	// if (yIn > mHighestControl.getValue()) {
	// y = mUpperQE.evaluate(yIn);
	//
	// } else if (yIn > mLowestControl.getValue()) {
	// y = mMidLE.evaluate(yIn);
	//
	// } else {
	// y = mLowerQE.evaluate(yIn);
	// }
	//
	// if (mRestrictWidth) {
	// // now do the horizontal blend
	// final double xIn = pIn.getX();
	// double x;
	// x = Math.min(Math.abs(xIn - mCentre), Math.abs(mCentre - xIn + 1.0));
	// x = Math.min(x, Math.abs(mCentre - xIn - 1.0));
	//
	// if (x < mWidth) {
	// // do nothing
	// } else if (x < mWidth + mBlend) {
	// // blend
	// final double s = KMath.sigma((x - mWidth) / mBlend); // sigma(0)=1
	// y = s * y + (1 - s) * yIn;
	//
	// } else {
	// // outside transform area so revert to yIn
	// y = yIn;
	// }
	// }
	//
	// return getColorFromPreviousTransform(new Point(pIn.getX(), y));
	// }

	@Override
	public void grafitti(final GrafittiHelper pGrafittiHelper) {
		pGrafittiHelper.drawHorizontalLine(mCutoffControl.getValue(), getGrafitiColor1(), isControlSelected(mCutoffControl));
		pGrafittiHelper.drawHorizontalLine(mHighestControl.getValue(), getGrafitiColor2(), isControlSelected(mHighestControl));
		pGrafittiHelper.drawHorizontalLine(mHighControl.getValue(), getGrafitiColor3(), isControlSelected(mHighControl));
		pGrafittiHelper.drawHorizontalLine(mLowControl.getValue(), getGrafitiColor2(), isControlSelected(mLowControl));
		pGrafittiHelper.drawHorizontalLine(mLowestControl.getValue(), getGrafitiColor3(), isControlSelected(mLowestControl));

		pGrafittiHelper.drawString(mCutoffControl, 0.0d, mCutoffControl.getValue());
		pGrafittiHelper.drawString(mHighestControl, 0.0d, mHighestControl.getValue());
		pGrafittiHelper.drawString(mHighControl, 0.0d, mHighControl.getValue());
		pGrafittiHelper.drawString(mLowControl, 0.0d, mLowControl.getValue());
		pGrafittiHelper.drawString(mLowestControl, 0.0d, mLowestControl.getValue());

		if (mRestrictWidth) {
			pGrafittiHelper.drawVerticalLine(mCentre, getGrafitiColor1(), isControlSelected(mCentreControl));
			pGrafittiHelper.drawVerticalLine(mod1(mCentre + mWidth), getGrafitiColor2(), isControlSelected(mWidthControl));
			pGrafittiHelper.drawVerticalLine(mod1(mCentre - mWidth), getGrafitiColor2(), isControlSelected(mWidthControl));
			pGrafittiHelper.drawVerticalLine(mod1(mCentre + mWidth + mBlend), getGrafitiColor3(), isControlSelected(mBlendControl));
			pGrafittiHelper.drawVerticalLine(mod1(mCentre - mWidth - mBlend), getGrafitiColor3(), isControlSelected(mBlendControl));

			pGrafittiHelper.drawString(mCentreControl, mCentre, 0.0d);
			pGrafittiHelper.drawString(mWidthControl, mod1(mCentre + mWidth), 0.0d);
			pGrafittiHelper.drawString(mWidthControl, mod1(mCentre - mWidth), 0.0d);
			pGrafittiHelper.drawString(mBlendControl, mod1(mCentre + mWidth + mBlend), 0.0d);
			pGrafittiHelper.drawString(mBlendControl, mod1(mCentre - mWidth - mBlend), 0.0d);
		}
	}

	@Override
	public void setValues() {
		final double cutoff = mCutoffControl.getValue();
		final double highest = mHighestControl.getValue();
		final double high = mHighControl.getValue();
		final double low = mLowControl.getValue();
		final double lowest = mLowestControl.getValue();

		mRestrictWidth = mRestrictWidthControl.getValue();
		mCentre = mCentreControl.getValue();
		mWidth = mWidthControl.getValue();
		mBlend = mBlendControl.getValue();

		final Matrix m1 = new Matrix(new double[][] { { lowest, 1.0d }, { highest, 1.0d } });
		final Matrix m1solve = m1.solve(new Matrix(new double[][] { { low }, { high } }));
		final double m = m1solve.get(0, 0);
		final double c = m1solve.get(1, 0);
		mMidLE = new LinearEquation(m, c);

		final Matrix m2 = new Matrix(new double[][] { { 2 * lowest, 1.0d }, { lowest * lowest, lowest } });
		final Matrix m2solve = m2.solve(new Matrix(new double[][] { { m }, { low } }));
		final double a1 = m2solve.get(0, 0);
		final double b1 = m2solve.get(1, 0);
		mLowerQE = new QuadraticEquation(a1, b1, 0.0d);

		final Matrix m3 = new Matrix(new double[][] { { 1.0d, 1.0d, 1.0d }, { 2 * highest, 1.0d, 0.0d }, { highest * highest, highest, 1.0d } });
		final Matrix m3solve = m3.solve(new Matrix(new double[][] { { cutoff }, { m }, { high } }));
		final double a2 = m3solve.get(0, 0);
		final double b2 = m3solve.get(1, 0);
		final double c2 = m3solve.get(2, 0);
		mUpperQE = new QuadraticEquation(a2, b2, c2);

	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		Framework.logEntry(mLogger);

		final double yIn = pRenderResult.getY();
		double y = 0;

		if (yIn > mHighestControl.getValue()) {
			y = mUpperQE.evaluate(yIn);

		} else if (yIn > mLowestControl.getValue()) {
			y = mMidLE.evaluate(yIn);

		} else {
			y = mLowerQE.evaluate(yIn);
		}

		final double xIn = pRenderResult.getX();
		double x = xIn;
		if (mRestrictWidth) {
			// now do the horizontal blend
			x = Math.min(Math.abs(xIn - mCentre), Math.abs(mCentre - xIn + 1.0));
			x = Math.min(x, Math.abs(mCentre - xIn - 1.0));

			if (x < mWidth) {
				// do nothing
			} else if (x < mWidth + mBlend) {
				// blend
				final double s = KMath.sigma((x - mWidth) / mBlend); // sigma(0)=1
				y = s * y + (1 - s) * yIn;

			} else {
				// outside transform area so revert to yIn
				y = yIn;
			}
		}

		pRenderResult.setXY(xIn, y);

		Framework.logExit(mLogger);
	}

	@Override
	public boolean validateControl(final Object pControl) {
		try {
			final boolean valid = mHighControl.getValidateValue() > mLowControl.getValidateValue();
			return valid;
		} catch (Throwable pt) {
			int a = 1;
			return false;
		}
	}
}
