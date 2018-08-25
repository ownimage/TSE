/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public abstract class Tuple3D<T extends Tuple3D> {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	private final double mX;
	private final double mY;
	private final double mZ;

	public Tuple3D(final double pX, final double pY, final double pZ) {
		mX = pX;
		mY = pY;
		mZ = pZ;
	}

	public T add(final Vector3D pOther) {
		return createInstance(mX + pOther.getX(), mY + pOther.getY(), mZ + pOther.getZ());
	}

	public abstract T createInstance(final double pX, final double pY, final double pZ);

	public double getX() {
		return mX;
	}

	public double getY() {
		return mY;
	}

	public double getZ() {
		return mZ;
	}

	public double length() {
		return Math.sqrt(length2());
	}

	public double length2() {
		return mX * mX + mY * mY + mZ * mZ;
	}

	public Vector3D minus(final Tuple3D pOther) {
		return new Vector3D(mX - pOther.getX(), mY - pOther.getY(), mZ - pOther.getZ());
	}

	public T plus(final Vector3D pOther) {
		return add(pOther);
	}

	public T scale(final double pScale) {
		return createInstance(mX * pScale, mY * pScale, mZ * pScale);
	}

	public Vector3D subtract(final Tuple3D pOther) {
		return minus(pOther);
	}

}
