/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class Point3D extends Tuple3D<Point3D> {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	public Point3D(final double pX, final double pY, final double pZ) {
		super(pX, pY, pZ);
	}

	public Point3D(final Point pPoint) {
		this(pPoint, 0.0d);
	}

	public Point3D(final Point pPoint, final double pZ) {
		super(pPoint.getX(), pPoint.getY(), pZ);
	}

	@Override
	public Point3D createInstance(final double pX, final double pY, final double pZ) {
		return new Point3D(pX, pY, pZ);
	}

	// gives the vector from this Point to the Point give, i.e. A.to(B) gives AB or B-A
	public Vector3D to(final Point3D pB) {
		return new Vector3D(pB.getX() - getX(), pB.getY() - getY(), pB.getZ() - getZ());
	}
}
