/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Version;

public class Vector3D extends Tuple3D<Vector3D> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = Vector3D.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	public Vector3D(final double pX, final double pY, final double pZ) {
		super(pX, pY, pZ);
	}

	@Override
	public Vector3D createInstance(final double pX, final double pY, final double pZ) {
		return new Vector3D(pX, pY, pZ);
	}

	public double dot(final Vector3D pVector) {
		return getX() * pVector.getX() + getY() * pVector.getY() + getZ() * pVector.getZ();
	}

	public Vector3D normalize() {
		final double length = length();
		if (length == 0.0d) { throw new IllegalStateException("Unable to normalize 0 length tuple"); }

		return scale(1.0d / length);
	}
}
