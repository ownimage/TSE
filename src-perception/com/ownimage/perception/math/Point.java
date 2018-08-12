/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.io.Serializable;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

// TODO: Auto-generated Javadoc
/**
 * The Class Point.
 */
public class Point implements Serializable {

	/** The Constant mVersion. */
	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	/** The Constant mLogger. */
	@SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

	/** The Constant Point00. */
	public final static Point Point00 = new Point(0.0d, 0.0d);

	/** The Constant Point11. */
	public final static Point Point11 = new Point(1.0d, 1.0d);

	/** The Constant Point01. */
	public final static Point Point01 = new Point(0.0d, 1.0d);

	/** The Constant Point10. */
	public final static Point Point10 = new Point(1.0d, 0.0d);

	/** The Constant Point0505. */
	public final static Point Point0505 = new Point(0.5d, 0.5d);

	/** The m x. */
	private double mX;

	/** The m y. */
	private double mY;

	/**
	 * Instantiates a new point.
	 * 
	 * @param pX
	 *            the p x
	 * @param pY
	 *            the p y
	 */
	public Point(final double pX, final double pY) {
		setX(pX);
		setY(pY);
	}

	/**
	 * Instantiates a new point.
	 * 
	 * @param pPoint
	 *            the point
	 */
	public Point(final IntegerPoint pPoint) {
		this(pPoint.getX(), pPoint.getY());
	}

	/**
	 * Instantiates a new point.
	 * 
	 * @param pPoint
	 *            the point
	 */
	public Point(final Point pPoint) {
		this(pPoint.getX(), pPoint.getY());
	}

	/**
	 * Adds this point to pIn and returns a new point. Point is immutable.
	 * 
	 * @param pIn
	 *            the in
	 * @return the point
	 */
	public Point add(final Point pIn) {
		return new Point(getX() + pIn.getX(), getY() + pIn.getY());
	}

	/**
	 * Calculates the Point at the same X position but on the bottom of the unit square, i.e. (mX, 0).
	 *
	 * @return the point
	 */
	public Point bottom() {
		return new Point(getX(), 0.0d);
	}

	public double distance(final Point pIn) {
		Point delta = minus(pIn);
		double distance = delta.length();
		return distance;
	}

	//
	// public Point multiply(IControlPrimative<Double> pScale) {
	// return multiply(pScale.getDouble());
	// }
	//
	/**
	 * Divide.
	 *
	 * @param pScale
	 *            the scale
	 * @return the point
	 */
	public Point divide(final double pScale) {
		return new Point(getX() / pScale, getY() / pScale);
	}
	//
	// public double dot(final PointControl pIn) {
	// return dot(pIn.getPoint());
	// }

	/**
	 * Dot.
	 *
	 * @param pOther
	 *            the other vector
	 * @return the double
	 */
	public double dot(final Point pOther) {
		return getX() * pOther.getX() + getY() * pOther.getY();
	}

	@Override
	public boolean equals(final Object pObj) {
		if (pObj instanceof Point) {
			Point other = (Point) pObj;
			return this.mX == other.mX && this.mY == other.mY;
		}
		return super.equals(pObj);
	}

	public Point getMidpoint(final Point pOther) {
		return new Point((mX + pOther.mX) / 2.0d, (mY + pOther.mY) / 2.0d);
	}

	/**
	 * Gets the x.
	 * 
	 * @return the x
	 */
	public double getX() {
		return mX;
	}

	/**
	 * Gets the y.
	 * 
	 * @return the y
	 */
	public double getY() {
		return mY;
	}

	public boolean isInsideUnitSquare() {
		return 0 <= mX && mX <= 1.0d && 0 < mY && mY <= 1.0d;
	}

	/**
	 * Return the point on the left edge of the unit square with the same y value as this point.
	 *
	 * @return the point
	 */
	public Point left() {
		return new Point(0.0d, getY());
	}

	/**
	 * Length.
	 * 
	 * @return the double
	 */
	public double length() {
		return Math.sqrt(length2());
	}

	// /**
	// * Return the point 1 unit to the left of this with the same y value as this point.
	// *
	// * @return the point
	// */
	// public Point left1() {
	// return this.minus(Point10);
	// }

	/**
	 * Length squared.
	 * 
	 * @return the double
	 */
	public double length2() {
		return getX() * getX() + getY() * getY();
	}

	/**
	 * Creates a Point that is (0,0) minus the original point..
	 *
	 * @return the point
	 */
	public Vector minus() {
		return new Vector(-mX, -mY);
	}

	// public Point add(PointControl pIn) {
	// return add(pIn.getPoint());
	// }
	//
	/**
	 * Subtracts pIn from this and returns a new point. Point is immutable.
	 *
	 * @param pIn
	 *            the in
	 * @return the vector
	 */
	public Vector minus(final Point pIn) {
		return new Vector(getX() - pIn.getX(), getY() - pIn.getY());
	}

	//
	// /**
	// * Length taxi cab.
	// *
	// * @return the double
	// */
	// public double lengthTaxiCab() {
	// return Math.abs(getX()) + Math.abs(getY());
	// }
	//
	/**
	 * Multiply this point by a scale factor and return a new point. Point is immutable..
	 *
	 * @param pScale
	 *            the scale
	 * @return the point
	 */
	public Point multiply(final double pScale) {
		return new Point(getX() * pScale, getY() * pScale);
	}

	// /**
	// * Return the point 1 unit to the right of this with the same y value as this point.
	// *
	// * @return the point
	// */
	// public Point right1() {
	// return this.add(Point10);
	// }
	//
	// /**
	// * Flip horizontal.
	// *
	// * @return the point
	// */
	// public Point flipHorizontal() {
	// return new Point(1.0d - getX(), getY());
	// }
	//
	// /**
	// * Flip horizontal.
	// *
	// * @param pFlip
	// * the flip
	// * @return the point
	// */
	// public Point flipHorizontal(boolean pFlip) {
	// if (pFlip)
	// return flipHorizontal();
	// else
	// return this; // safe as Point is immutable
	// }
	//

	/**
	 * Return the point on the right edge of the unit square with the same y value as this point.
	 *
	 * @return the point
	 */
	public Point right() {
		return new Point(1.0d, getY());
	}

	//
	// /**
	// * Mod1inc.
	// *
	// * @return the point
	// */
	// public Point mod1inc() {
	// return new Point(KMath.mod1inc(getX()), KMath.mod1inc(getY()));
	// }
	//
	/**
	 * Rotate.
	 *
	 * @param pAngle
	 *            the angle
	 * @return the point
	 */
	public Point rotate(final double pAngle) {
		double x = getX() * Math.cos(pAngle) - getY() * Math.sin(pAngle);
		double y = getY() * Math.cos(pAngle) + getX() * Math.sin(pAngle);
		return new Point(x, y);
	}

	//
	// public Vector minus(PointControl pIn) {
	// return minus(pIn.getPoint());
	// }

	//
	// /**
	// * Floor.
	// *
	// * @return the point
	// */
	// public Point floor() {
	// return new Point(Math.floor(mX), Math.floor(mY));
	// }
	//
	// /**
	// * Integer pointfloor.
	// *
	// * @return the integer point
	// */
	// public IntegerPoint integerPointfloor() {
	// return new IntegerPoint((int) Math.floor(mX), (int) Math.floor(mY));
	// }
	//
	// /**
	// * Over floor.
	// *
	// * @return the point
	// */
	// public Point overFloor() {
	// return this.minus(floor());
	// }
	//
	// /**
	// * Checks if is in unit square.
	// *
	// * @return true, if is in unit square
	// */
	// public boolean isInUnitSquare() {
	// return 0.0d <= mX && mX <= 1.0d && 0.0d <= mY && mY <= 1.0d;
	// }
	//
	// /**
	// * Calculates where a line starting at (0.5, 0.5) passing through this point intersects the unit square.
	// *
	// * @return the point (u, v) such that u is the point on the edge of the square where u = 0 corresponds to (0,0), u = 0.25
	// corresponds to (0, 1), u = 0.5 corresponds to (1, 1), u= 0.75
	// corresponds
	// * to (1, 0) and, u = 1.0 corresponds back to (0, 0). v corresponds t the fraction of the way from the centre to the unit
	// square this point is, v = 0 is at the centre, v = 1 is at the
	// * edge.
	// */
	// public Point intersectUnitSquare() {
	// double u = 0;
	// double v = 0;
	//
	// if (Point0505.equals(this))
	// return Point00;
	//
	// Line line = new Line(Point0505, this);
	//
	// if (mX < 0.5d) {
	// LineSegment AB = new LineSegment(Point00, Point01);
	// Point intersect = AB.intersect(line);
	// if (KMath.inRange01inc(intersect.getY())) {
	// u = 0.25d * intersect.getY();
	// v = Point0505.minus(this).length() / Point0505.minus(intersect).length();
	// return new Point(u, v);
	// }
	// }
	// if (mY > 0.5d) {
	// LineSegment BC = new LineSegment(Point01, Point11);
	// Point intersect = BC.intersect(line);
	// if (KMath.inRange01inc(intersect.getX())) {
	// u = 0.25d + 0.25d * intersect.getX();
	// v = Point0505.minus(this).length() / Point0505.minus(intersect).length();
	// return new Point(u, v);
	// }
	// }
	//
	// LineSegment CD = new LineSegment(Point11, Point10);
	// Point intersect = CD.intersect(line);
	// if (KMath.inRange01inc(intersect.getY())) {
	// u = 0.75d - 0.25d * intersect.getY();
	// v = Point0505.minus(this).length() / Point0505.minus(intersect).length();
	// return new Point(u, v);
	// }
	//
	// LineSegment DA = new LineSegment(Point10, Point00);
	// intersect = DA.intersect(line);
	// if (KMath.inRange01inc(intersect.getX())) {
	// u = 1.0d - 0.25d * intersect.getX();
	// v = Point0505.minus(this).length() / Point0505.minus(intersect).length();
	// return new Point(u, v);
	// }
	//
	// return Point00;
	// }
	//
	public Point scaleX(final double pScale) {
		return new Point(mX * pScale, mY);
	}

	public Point scaleY(final double pScale) {
		return new Point(mX, mY * pScale);
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see java.lang.Object#equals(java.lang.Object)
	// */
	// @Override
	// public boolean equals(Object pObj) {
	// return pObj instanceof Point && getX() == ((Point) pObj).getX() && getY() == ((Point) pObj).getY();
	// }
	//

	//
	// public Point rotate(IControlPrimative<Double> pAngle) {
	// return rotate(pAngle.getDouble());
	// }
	//
	/**
	 * Sets the x.
	 *
	 * @param x
	 *            the new x
	 */
	protected void setX(final double x) {
		mX = x;
	}

	/**
	 * Sets the y.
	 *
	 * @param y
	 *            the new y
	 */
	protected void setY(final double y) {
		mY = y;
	}

	/**
	 * Calculates the Point at the same X position but on the top of the unit square, i.e. (mX, 1).
	 *
	 * @return the point
	 */
	public Point top() {
		return new Point(getX(), 1.0d);
	}

	//
	// public Point divide(IControlPrimative<Double> pScale) {
	// return divide(pScale.getDouble());
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see java.lang.Object#toString()
	// */
	// @Override
	// public String toString() {
	// return "Point (" + getX() + ", " + getY() + ")";
	// }
	//
	/**
	 * To vector.
	 *
	 * @return the vector
	 */
	public Vector toVector() {
		return new Vector(getX(), getY());
	}
}
