/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.util.Framework;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.logging.Logger;

public class ImmutableIXY implements IXY, Serializable {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public static final ImmutableIXY IntegerPoint00 = new ImmutableIXY(0, 0);

    private final int mX;
    private final int mY;
    transient private Integer mHashCode; /// calculating hash codes was taking a long time so they are now stored

    private ImmutableIXY(int x, int y) {
        this.mX = x;
        this.mY = y;
    }

    public static ImmutableIXY copyOf(@NotNull IXY ip) {
        return new ImmutableIXY(ip.getX(), ip.getY());
    }

    public static ImmutableIXY of(final int pX, final int pY) {
    	return new ImmutableIXY(pX, pY);
    }

    public static ImmutableIXY of(IXY ixy) {
        if (ixy instanceof ImmutableIXY) {
            return (ImmutableIXY) ixy;
        }
        return new ImmutableIXY(ixy.getX(), ixy.getY());
    }

    public int hashCode() {
        if (mHashCode != null) return mHashCode;
        mHashCode = java.util.Objects.hash(mX, mY);
        return mHashCode;
    }

    @Override
    public boolean equals(final Object pObj) {
        boolean value = false;
        if (pObj != null && pObj instanceof ImmutableIXY) {
            final ImmutableIXY ip = (ImmutableIXY) pObj;
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