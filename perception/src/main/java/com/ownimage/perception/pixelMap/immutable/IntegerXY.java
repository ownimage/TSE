/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.Framework;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.logging.Logger;

public class IntegerXY implements IXY, Serializable {


	public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	public static final IntegerXY IntegerPoint00 = new IntegerXY(0, 0);

	private final int mX;
	private final int mY;
	transient private Integer mHashCode; /// calculating hash codes was taking a long time so they are now stored

	public IntegerXY(@NotNull IXY ip) {
		mX = ip.getX();
		mY = ip.getY();
	}

	public IntegerXY(final int pX, final int pY) {
		mX = pX;
		mY = pY;
	}

	public IntegerXY(@NotNull IntegerPoint ip) {
		mX = ip.getX();
		mY = ip.getY();
	}

	public static IntegerXY of(IXY ixy) {
		if (ixy instanceof IntegerXY) {
			return (IntegerXY) ixy;
		}
		return new IntegerXY(ixy);
	}

	public int hashCode() {
		if (mHashCode != null) return mHashCode;
		mHashCode = java.util.Objects.hash(mX, mY);
		return mHashCode;
	}

	@Override
	public boolean equals(final Object pObj) {
		boolean value = false;
		if (pObj != null && pObj instanceof IntegerXY) {
			final IntegerXY ip = (IntegerXY) pObj;
			value = ip.mX == mX && ip.mY == mY;
		}
		return value;
	}


	@Override
	public int getX() {
		return mX;
	}


	@Override
	public int getY() {
		return mY;
	}

	@Override
	public String toString() {
		return "IntegerPoint[mX=" + mX + ", mY=" + mY + "]";
	}
}