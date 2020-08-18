/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.PixelConstants;

import java.io.Serializable;

/**
 * The class Pixel provides a wrapper about the byte level information contained in the raw PixelMap array. This can get and set
 * information about this Pixel, this might mean reading information from adjacent pixels ... but this class NEVER sets values for
 * other Pixels, and it NEVER registers/deregisters Nodes with the PixelMap.
 */
public class Pixel implements IXY, PixelConstants, Serializable {

    private final static long serialVersionUID = 1L;
    private final int mX;
    private final int mY;
    transient private Integer mHashCode; /// calculating hash codes was taking a long time so they are now stored
    private Point mUHVW = null;

    private Pixel(Pixel pPixel) {
        this(pPixel.getX(), pPixel.getY());
    }

    public Pixel(IXY pIntegerXY) {
        this(pIntegerXY.getX(), pIntegerXY.getY());
    }

    public Pixel(int pX, int pY) {
        this.mX = pX;
        this.mY = pY;
    }

    public Pixel(PixelChain pPixelChain, int pIndex) {
        this(pPixelChain.getPixel(pIndex));
    }

    public int getX() {
        return mX;
    }


    public int getY() {
        return mY;
    }

    @Override
    public String toString() {
        return "Pixel(" + getX() + ", " + getY() + ")";
    }


    // UHVW = unit height variable width
    @Override
    public synchronized Point getUHVWMidPoint(int height) {
        if (mUHVW == null) {
            synchronized (this) {
                if (mUHVW == null) {
                    mUHVW = IXY.super.getUHVWMidPoint(height);
                }
            }
        }
        return mUHVW;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return mX == ((Pixel)other).mX && mY == ((Pixel)other).mY;
    }


}
