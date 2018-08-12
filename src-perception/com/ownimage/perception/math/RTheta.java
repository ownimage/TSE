package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

/**
 * This immutable class represents a point in PolarCordinates.
 */
public class RTheta {

	public enum Quadrant {
		TopLeft(Point.Point01)//
		, TopRight(Point.Point11) //
		, BottomLeft(Point.Point00) //
		, BottomRight(Point.Point10);

		private Point mPoint;

		Quadrant(final Point pPoint) {
			mPoint = pPoint;
		}

		public Point getCorner() {
			return mPoint;
		}
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

    private final static Logger mLogger = Framework.getLogger();

	private final double mR;
	private final double mTheta;
	private final Quadrant mQuadrant;

	public RTheta(final double pR, final double pTheta) {
		mR = pR;
		mTheta = pTheta;
		mQuadrant = (mTheta < Math.PI / 2.0d) ? Quadrant.TopRight : //
				(mTheta < Math.PI) ? Quadrant.BottomRight : //
						(mTheta < 3 * Math.PI / 2.0d) ? Quadrant.BottomLeft : Quadrant.TopLeft;
	}

	public Quadrant getQuadrant() {
		return mQuadrant;
	}

	public double getR() {
		return mR;
	}

	public double getTheta() {
		return mTheta;
	}

	public RTheta withR(final double pR) {
		return new RTheta(pR, mTheta);
	}

	public RTheta withTheta(final double pTheta) {
		return new RTheta(mR, pTheta);
	}

}
