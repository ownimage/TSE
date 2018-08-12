/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class LineSegment extends Line implements Comparable<LineSegment>, ITestableLine {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	private final double mMinX;
	private final double mMinY;
	private final double mMaxX;
	private final double mMaxY;
	private Double mLength;

	private final Vector mNormal;
	private final Double mK;

	public LineSegment(final Point pA, final Point pB) {
		super(pA, pB);
		mMinX = Math.min(pA.getX(), pB.getX());
		mMinY = Math.min(pA.getY(), pB.getY());
		mMaxX = Math.max(pA.getX(), pB.getX());
		mMaxY = Math.max(pA.getY(), pB.getY());

		mNormal = getNormal();
		mK = getA().toVector().dot(mNormal);
	}

	/**
	 * Constructs a bisector LineSegment that starts at the midpoint of A and B, and ends at (the midpoint of A and B) plus
	 * getNormal(). Note that getNormal() will be of unit length.
	 * 
	 * @return the line bisector
	 */
	public LineSegment bisector() {
		final Point start = getA().add(getB()).multiply(0.5d);
		final Point end = start.add(getNormal());
		final LineSegment bisector = new LineSegment(start, end);
		return bisector;
	}

	@Override
	public boolean closerThan(final Point pPoint, final double pTolerance) {
		final Point closest = closestPoint(pPoint);
		final double distance = closest.minus(pPoint).length();
		return distance < pTolerance;
	}

	@Override
	public double closestLambda(final Point pPoint) {
		final double lambda = super.closestLambda(pPoint);
		// double boundedLambda = new Range(0.0d, 1.0d).getBoundedFraction(lambda);
		final double boundedLambda = lambda < 0.0d ? 0.0d : lambda > 1.0d ? 1.0d : lambda;
		return boundedLambda;
	}

	@Override
	public int compareTo(final LineSegment pLS) {
		if (mMinX != pLS.mMinX) { return (int) Math.signum(mMinX - pLS.mMinX); }
		return (int) Math.signum(mMinY - pLS.mMinY);
	}

	@Override
	public double getMaxX() {
		return mMaxX;
	}

	@Override
	public double getMaxY() {
		return mMaxY;
	}

	@Override
	public double getMinX() {
		return mMinX;
	}

	@Override
	public double getMinY() {
		return mMinY;
	}

	@Override
	public synchronized double length() {
		if (mLength == null) {
			mLength = getA().minus(getB()).length();
		}
		return mLength;
	}

	@Override
	public String toString() {
		return "LineApproximation: A=" + getA().toString() + " : B=" + getB().toString();
	}

}
