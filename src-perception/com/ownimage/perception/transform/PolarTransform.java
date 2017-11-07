/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.PolarCoordinates;
import com.ownimage.perception.math.RTheta;
import com.ownimage.perception.render.ITransformResult;

public class PolarTransform extends BaseTransform {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	private final static Logger mLogger = Logger.getLogger(PolarTransform.class.getName());

	private final DoubleControl mRotateSlider;
	private final ColorControl mColor = new ColorControl("Color", "color", getContainer(), Color.WHITE);

	double mRotate;

	public PolarTransform(final Perception pPerception) {
		super(pPerception);

		mRotateSlider = new DoubleControl("Rotate", "rotate", getContainer(), 0.5d, 0.0d, 1.0d);
		setValues();

		addXControl(mRotateSlider);
	}

	@Override
	public String getDisplayName() {
		return "Polar";
	}

	@Override
	public String getPropertyName() {
		return "polar";
	}

	@Override
	public void grafitti(final GrafittiHelper pGrafittiHelper) {
		pGrafittiHelper.drawVerticalLine(mRotate, getGrafitiColor1(), isControlSelected(mRotateSlider));
	}

	@Override
	public void setValues() {
		mRotate = mRotateSlider.getValue();
	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		Point in = new Point(pRenderResult.getX(), pRenderResult.getY());

		double fx = 0;
		double fy = 0;

		try {
			final PolarCoordinates pc = PolarCoordinates.getCircleInUnitSquare();
			final RTheta rTheta = pc.getPolarCoordinate(in);
			fx = mod1(mRotate + rTheta.getTheta() / (2.0 * Math.PI));
			fy = rTheta.getR();
			if (fy < 1.0) {
				pRenderResult.setXY(fx, fy);
			} else {
				pRenderResult.setColor(mColor.getValue());
			}
		} catch (final Exception pEx) {
			if (mLogger.isLoggable(Level.FINEST)) {
				mLogger.finest("PolarTrasform Error:" + FrameworkLogger.throwableToString(pEx));
			}
		}
	}

}
