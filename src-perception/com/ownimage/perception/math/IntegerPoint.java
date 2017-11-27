/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.math;

import java.io.Serializable;
import java.util.logging.Logger;

import com.ownimage.framework.util.Version;

public class IntegerPoint implements Serializable, Comparable<IntegerPoint> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = IntegerPoint.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	public static final IntegerPoint IntegerPoint00 = new IntegerPoint(0, 0);

	private final int mX;
	private final int mY;

	public IntegerPoint(final int pX, final int pY) {
		mX = pX;
		mY = pY;
	}

	public IntegerPoint add(final int pDx, final int pDy) {
		return new IntegerPoint(getX() + pDx, getY() + pDy);
	}

	public IntegerPoint add(final IntegerPoint pPoint) {
		return new IntegerPoint(getX() + pPoint.getX(), getY() + pPoint.getY());
	}

	@Override
	public int compareTo(final IntegerPoint pOther) {
		return mX == pOther.mX ? mY - pOther.mY : mX - pOther.mX;
	}

	@Override
	public boolean equals(final Object pObj) {
		boolean value = false;
		if (pObj != null && pObj instanceof IntegerPoint) {
			final IntegerPoint ip = (IntegerPoint) pObj;
			value = ip.mX == mX && ip.mY == mY;
		}
		return value;
	}

	public IntegerPoint getEast() {
		return new IntegerPoint(getX() + 1, getY());
	}

	public IntegerPoint getNorth() {
		return new IntegerPoint(getX(), getY() + 1);
	}

	public IntegerPoint getNorthEast() {
		return new IntegerPoint(getX() + 1, getY() + 1);
	}

	public IntegerPoint getNorthWest() {
		return new IntegerPoint(getX() - 1, getY() + 1);
	}

	public IntegerPoint getSouth() {
		return new IntegerPoint(getX(), getY() - 1);
	}

	public IntegerPoint getSouthEast() {
		return new IntegerPoint(getX() + 1, getY() - 1);
	}

	public IntegerPoint getSouthWest() {
		return new IntegerPoint(getX() - 1, getY() - 1);
	}

	public IntegerPoint getWest() {
		return new IntegerPoint(getX() - 1, getY());
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	public IntegerPoint minus(final int pDx, final int pDy) {
		return new IntegerPoint(getX() - pDx, getY() - pDy);
	}

	public IntegerPoint minus(final IntegerPoint pPoint) {
		return new IntegerPoint(getX() - pPoint.getX(), getY() - pPoint.getY());
	}

	@Override
	public String toString() {
		return "IntegerPoint[mX=" + mX + ", mY=" + mY + "]";
	}
}