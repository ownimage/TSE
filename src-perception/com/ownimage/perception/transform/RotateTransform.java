/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.awt.Color;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.control.grafitti.Path;
import com.ownimage.perception.math.KMath;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.render.ITransformResult;

public class RotateTransform extends BaseTransform {

	public static enum Rotate {
		Free(0.0d), None(0.0d), CCW(0.75d), CW(0.25d), By180(0.5d);

		private double m01;

		private Rotate(final double p01) {
			m01 = p01;
		}

		public double get01() {
			return m01;
		}

		public double getRadians() {
			return m01 * Math.PI * 2.0d;
		}
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	private final static Logger mLogger = Framework.getLogger();

	private final BooleanControl mPointRotateControl;

	private boolean mPointRotate;
	private final BooleanControl mExtendControl;

	private boolean mExtend;
	private final ObjectControl<Rotate> mRotateQuartersControl;

	private final DoubleControl mRotateControl;
	private final ColorControl mOOBColorControl;

	private double mRotate;
	/* The scale factor if the extend option is chosen. This is how much larger the source is than the target. */
	private double mScale;

	public RotateTransform(final Perception pPerception) {
		super(pPerception);

		mPointRotateControl = new BooleanControl("Point", "point", getContainer(), false);
		mExtendControl = new BooleanControl("Extend", "extend", getContainer(), false);
		mRotateQuartersControl = new ObjectControl<>("Output", "output", getContainer(), Rotate.Free, Rotate.values());
		mRotateControl = new DoubleControl("Rotate", "rotate", getContainer(), 0.3d);
		mOOBColorControl = new ColorControl("Out of Bounds", "OOB", getContainer(), Color.WHITE);

		setValues();

		addXControl(mRotateControl);
	}

	@Override
	public void controlChangeEvent(final Object pControl, final boolean pIsMutating) {
		Framework.logEntry(mLogger);

		super.controlChangeEvent(pControl, pIsMutating);
		if (pControl == mRotateQuartersControl) {
			final boolean enabled = mRotateQuartersControl.getValue() == Rotate.Free;
			mRotateControl.setEnabled(enabled);
		}

		Framework.logExit(mLogger);
	}

	@Override
	public String getDisplayName() {
		return "Rotate";
	}

	@Override
	public String getPropertyName() {
		return "rotate";
	}

	@Override
	public String getTransformName() {
		return "rotateTransform";
	}

	@Override
	public void grafitti(final GrafittiHelper pGrafittiHelper) {
		if (mPointRotate) {
			pointGraffiti(pGrafittiHelper);
		} else {
			linearGraffiti(pGrafittiHelper);
		}
	}

	private void linearGraffiti(final GrafittiHelper pGrafittiHelper) {
		final double mid = KMath.mod1(0.5d + mRotateControl.getValue());
		pGrafittiHelper.drawVerticalLine(mid, getGrafitiColor1());

		final double end = KMath.mod1(mRotateControl.getValue());
		pGrafittiHelper.drawVerticalLine(end, getGrafitiColor2());

		pGrafittiHelper.drawString("Center", mid, 0.0d);
		pGrafittiHelper.drawString("Edge", end, 0.0d);
		pGrafittiHelper.drawString("Edge", end - 1.0d, 0.0d);
	}

	private void linearRotate(final ITransformResult pRenderResult) {
		double x;
		x = pRenderResult.getX() + mRotate;
		x = KMath.mod1(x);
		pRenderResult.setX(x);
	}

	private void pointGraffiti(final GrafittiHelper pGrafittiHelper) {
		Path path = new Path();
		path.moveTo(new Point(-2.0d, 0.0d).rotate(mRotate).add(Point.Point0505));
		path.lineTo(new Point(2.0d, 0.0d).rotate(mRotate).add(Point.Point0505));
		pGrafittiHelper.drawPath(path, getGrafitiColor1());

		path = new Path();
		path.moveTo(new Point(0.0d, -2.0d).rotate(mRotate).add(Point.Point0505));
		path.lineTo(new Point(0.0d, 2.0d).rotate(mRotate).add(Point.Point0505));
		pGrafittiHelper.drawPath(path, getGrafitiColor2());

		pGrafittiHelper.drawSlider(mRotate / (2.0d * Math.PI), GrafittiHelper.Orientation.Horizontal, 0.2d, getGrafitiColor1(),
				false);
		pGrafittiHelper.drawString(mRotateControl.getDisplayName(), 0.0d, 0.2d);

	}

	private void pointRotate(final ITransformResult pRenderResult) {
		Point in = new Point(pRenderResult.getX(), pRenderResult.getY());
		Point delta = in.minus(Point.Point0505);
		if (mExtend) {
			delta = delta.multiply(mScale);
		}
		final Point out = Point.Point0505.add(delta.rotate(mRotate));

		if (!out.isInsideUnitSquare()) {
			pRenderResult.setColor(mOOBColorControl.getValue());
		}
		pRenderResult.setX(out.getX());
		pRenderResult.setY(out.getY());
	}

	@Override
	public void setValues() {
		mPointRotate = mPointRotateControl.getValue();
		mExtend = mExtendControl.getValue();

		final double rotate = mRotateQuartersControl.getValue() == Rotate.Free ? mRotateControl.getValue() : mRotateQuartersControl.getValue().get01();

		mRotate = mPointRotate ? 2.0d * Math.PI * rotate : rotate;

		final Point p = Point.Point11.rotate(mRotate);
		mScale = Math.max(Math.abs(p.getX()), Math.abs(p.getY()));

	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		if (mPointRotate) {
			pointRotate(pRenderResult);
		} else {
			linearRotate(pRenderResult);
		}
	}

}
