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

public class IntegerXY implements Serializable {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	public static final IntegerXY IntegerPoint00 = new IntegerXY(0, 0);

	private final int mX;
	private final int mY;
	transient private Integer mHashCode; /// calculating hash codes was taking a long time so they are now stored

	public IntegerXY(@NotNull IntegerPoint ip) {
		mX = ip.getX();
		mY = ip.getY();
	}

	public IntegerXY(final int pX, final int pY) {
		mX = pX;
		mY = pY;
	}

	public IntegerXY(@NotNull IntegerXY original) {
		mX = original.mX;
		mY = original.mY;
	}

	public IntegerXY add(final int pDx, final int pDy) {
		return new IntegerXY(getX() + pDx, getY() + pDy);
	}

	public IntegerXY add(final IntegerXY pPoint) {
		return new IntegerXY(getX() + pPoint.getX(), getY() + pPoint.getY());
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



	public int getX() {
		return mX;
	}


	public int getY() {
		return mY;
	}

    public boolean samePosition(final IntegerXY pO) {
        if (this == pO) return true;
        if (pO == null) return false;
        return getX() == pO.getX() && getY() == pO.getY();
    }


    public IntegerXY minus(final int pDx, final int pDy) {
		return new IntegerXY(getX() - pDx, getY() - pDy);
	}

	public IntegerXY minus(final IntegerXY pPoint) {
		return new IntegerXY(getX() - pPoint.getX(), getY() - pPoint.getY());
	}

	@Override
	public String toString() {
		return "IntegerPoint[mX=" + mX + ", mY=" + mY + "]";
	}
}