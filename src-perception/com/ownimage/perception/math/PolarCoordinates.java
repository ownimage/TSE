package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

/**
 * Allows co-ordinates to be mapped from Cartesian to Polar and back.
 */
public class PolarCoordinates {


    private final static Logger mLogger = Framework.getLogger();

	private Point mCentre;

	private final double mScale;

	public PolarCoordinates() {
		this(Point.Point00, 1.0d);
	}

	public PolarCoordinates(final Point pCentre) {
		this(pCentre, 1.0d);
	}

	/**
	 * Instantiates a new polar coordinates system. Note that this is a mutable object. A system set up with centre Point.Poitn0505,
	 * scale 0.5 will map the points on r = 1 onto a circle that just fits into the unit square.
	 * 
	 * @param pCentre
	 *            the centre
	 * @param pScale
	 *            the scale
	 */
	public PolarCoordinates(final Point pCentre, final double pScale) {
		mCentre = pCentre;
		mScale = pScale;
	}

	public static PolarCoordinates getCircleInUnitSquare() {
		return new PolarCoordinates(Point.Point0505, 0.5d);
	}

	public Point getCartesian(final double pR, final double pTheta) {
		return new Point(mCentre.getX() + getScale() * pR * Math.sin(pTheta), mCentre.getY() + getScale() * pR * Math.cos(pTheta));
	}

	public Point getCartesian(final RTheta pRTheta) {
		return getCartesian(pRTheta.getR(), pRTheta.getTheta());
	}

	public Point getCentre() {
		return mCentre;
	}

	/**
	 * Gets the polar coordinate represented by the Point pPoint. The mX-axis corresponds to theta = 0.
	 * 
	 * @param pPoint
	 *            the point
	 * @return the polar coordinate
	 */
	public RTheta getPolarCoordinate(final Point pPoint) {
		Point delta = pPoint.minus(mCentre);
		double r = delta.length() / mScale;

		double theta = Math.atan(delta.getX() / delta.getY());
		if (delta.getY() < 0) {
			theta = theta + Math.PI;
		}

		if (theta < 0) {
			theta += 2.0 * Math.PI;
		}

		if (theta > 2 * Math.PI) {
			theta -= 2.0 * Math.PI;
		}

		return new RTheta(r, theta);
	}

	public double getScale() {
		return mScale;
	}

	/**
	 * Sets the centre.
	 * 
	 * @param pCentre
	 *            the centre
	 * @return this to allow for chaining. e.g. pc.setCentre(centre).otherOpetation(mX, mY, z)
	 */
	public PolarCoordinates setCentre(final Point pCentre) {
		mCentre = pCentre;
		return this;
	}

}
