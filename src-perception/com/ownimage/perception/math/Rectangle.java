/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.io.Serializable;
import java.util.logging.Logger;

import com.ownimage.framework.util.Version;

// TODO: Auto-generated Javadoc
/**
 * The Class Point.
 */
public class Rectangle implements Serializable {

	/** The Constant mVersion. */
	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	/** The Constant mLogger. */
	@SuppressWarnings("unused")
	private final static Logger mLogger = Logger.getLogger(Rectangle.class.getName());

	private final double mX1;

	private final double mY1;

	private final double mX2;

	private final double mY2;

	public Rectangle(final double pX1, final double pY1, final double pX2, final double pY2) {
		mX1 = pX1;
		mY1 = pY1;
		mX2 = pX2;
		mY2 = pY2;
	}

	public Rectangle(final Point pBottomLeft, final Point pTopRight) {
		mX1 = pBottomLeft.getX();
		mY1 = pBottomLeft.getY();
		mX2 = pTopRight.getX();
		mY2 = pTopRight.getY();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		Rectangle other = (Rectangle) obj;
		if (Double.doubleToLongBits(mX1) != Double.doubleToLongBits(other.mX1)) { return false; }
		if (Double.doubleToLongBits(mX2) != Double.doubleToLongBits(other.mX2)) { return false; }
		if (Double.doubleToLongBits(mY1) != Double.doubleToLongBits(other.mY1)) { return false; }
		if (Double.doubleToLongBits(mY2) != Double.doubleToLongBits(other.mY2)) { return false; }
		return true;
	}

	public double getX1() {
		return mX1;
	}

	public double getX2() {
		return mX2;
	}

	public double getY1() {
		return mY1;
	}

	public double getY2() {
		return mY2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mX1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mX2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mY1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mY2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "Rectangle [mX1=" + mX1 + ", mX2=" + mX2 + ", mY1=" + mY1 + ", mY2=" + mY2 + "]";
	}
}
