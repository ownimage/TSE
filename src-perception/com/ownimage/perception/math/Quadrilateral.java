/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class Quadrilateral {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	private final LineSegment mAB;
	private final LineSegment mBC;
	private final LineSegment mDC;
	private final LineSegment mAD;

	/**
	 * Instantiates a new quadrilateral. a b c d should form a clockwise set.
	 * 
	 * @param pA
	 *            the point a
	 * @param pB
	 *            the point b
	 * @param pC
	 *            the point c
	 * @param pD
	 *            the point d
	 */
	public Quadrilateral(final Point pA, final Point pB, final Point pC, final Point pD) {
		mAB = new LineSegment(pA, pB);
		mBC = new LineSegment(pB, pC);
		mDC = new LineSegment(pD, pC);
		mAD = new LineSegment(pA, pD);
	}

	/**
	 * Calculates the distance of a Point from two Lines. 0 means that the point lies on AB, 1 means that the point lies on DC.
	 * 
	 * @param pAB
	 *            the line AB
	 * @param pDC
	 *            the line DC
	 * @param pPoint
	 *            the point
	 * @return the distance from AB to DC that the point is
	 */
	private double distance(final LineSegment pAB, final LineSegment pDC, final Point pPoint, final boolean pParallel) {
		double u = 0.0d;

		Vector abNormal = pAB.getNormal();
		double a = pAB.getA().toVector().dot(abNormal);
		double b = pDC.getA().toVector().dot(abNormal);
		if (pParallel || pAB.isParallel(pDC)) { // parallel
			double p = pPoint.toVector().dot(abNormal);
			u = (p - a) / (b - a);
		} else { // need intersection method
			Point intersect1 = pAB.intersect(pDC);
			Line line = new Line(intersect1, pPoint);
			if (line.isParallel(pAB)) {
				u = 0.0d;
			} else {
				Line ad = new Line(pAB.getA(), pDC.getA());
				Point intersect2 = line.intersect(ad);
				double p = intersect2.toVector().dot(abNormal);
				u = (p - a) / (b - a);
			}
		}
		return u;
	}

	public Point mapFromQuadrilateral(final Quadrilateral mFromQuad, final Point pPoint) {
		Point interim = mFromQuad.mapToUnitSquare(pPoint);
		Point rv = mapFromUnitSquare(interim);
		return rv;
	}

	public Point mapFromUnitSquare(final Point pPoint) {
		Point bottom = mAB.getPoint(pPoint.getX());
		Point top = mDC.getPoint(pPoint.getX());
		LineSegment vertical = new LineSegment(bottom, top);
		return vertical.getPoint(pPoint.getY());
	}

	public Point mapToQuadrilateral(final Quadrilateral mToQuad, final Point pPoint) {
		Point interim = mapToUnitSquare(pPoint);
		Point rv = mToQuad.mapFromUnitSquare(interim);
		return rv;
	}

	/**
	 * Map to unit square.
	 * 
	 * @param pPoint
	 *            the point
	 * @return the point
	 */
	public Point mapToUnitSquare(final Point pPoint) {
		return mapToUnitSquare(pPoint, false);
	}

	public Point mapToUnitSquare(final Point pPoint, final boolean pParallel) {
		double u = distance(mAD, mBC, pPoint, pParallel);
		double v = distance(mAB, mDC, pPoint, pParallel);
		return new Point(u, v);
	}

	@Override
	public String toString() {
		return "Quadrilateral(mAB=" + mAB + ", mBC=" + mBC + ", mDC=" + mDC + ", mAD=" + mAD + ")";
	}

}
