/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import com.ownimage.framework.util.Framework;

import java.io.Serializable;
import java.util.logging.Logger;

public class IntegerPoint implements Serializable, Comparable<IntegerPoint> {


    public final static Logger mLogger = Framework.getLogger();
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

	public int hashCode() {
        return java.util.Objects.hash(mX, mY);
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