/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.PixelMapGridPosition;
import org.jetbrains.annotations.NotNull;

/**
 * The class Pixel provides a wrapper about the byte level information contained in the raw PixelMap array. This can get and set
 * information about this Pixel, this might mean reading information from adjacent pixels ... but this class NEVER sets values for
 * other Pixels, and it NEVER registers/deregisters Nodes with the PixelMap.
 */
public class Pixel extends PixelMapGridPosition implements PixelConstants {

    private final static long serialVersionUID = 1L;
    private Point mUHVW = null;

    private Pixel(Pixel pPixel) {
        this(pPixel.getX(), pPixel.getY());
    }

    public Pixel(PixelMapGridPosition pPixelMapGridPosition) {
        this(pPixelMapGridPosition.getX(), pPixelMapGridPosition.getY());
    }

    public Pixel(int pX, int pY) {
        super(pX, pY);
    }

    public Pixel(PixelChain pPixelChain, int pIndex) {
        this(pPixelChain.getPixel(pIndex));
    }

    public Pixel add(@NotNull PixelMapGridPosition pmgp) {
        return new Pixel(getX()+pmgp.getX(), getY()+pmgp.getY());
    }

    @Override
    public String toString() {
        return "Pixel(" + getX() + ", " + getY() + ")";
    }


    // UHVW = unit height variable width
    public synchronized Point getUHVWMidPoint(int height) {
        if (mUHVW == null) {
            synchronized (this) {
                if (mUHVW == null) {
                    mUHVW = calcUHVWMidPoint(toPixelMapGridPosition(), height);
                }
            }
        }
        return mUHVW;
    }

    public Point calcUHVWMidPoint(@NotNull PixelMapGridPosition pixel, int height) {
        double y = (pixel.getY() + 0.5d) / height;
        double x = (pixel.getX() + 0.5d) / height;
        return new Point(x, y);
    }

    public PixelMapGridPosition toPixelMapGridPosition() {
        return new PixelMapGridPosition(getX(), getY());
    }

    @Override
    public boolean equals(Object pO) {
        if (this == pO) {
            return true;
        }
        if (pO == null || getClass() != pO.getClass()) {
            return false;
        }
        return super.equals(pO);
    }


}
