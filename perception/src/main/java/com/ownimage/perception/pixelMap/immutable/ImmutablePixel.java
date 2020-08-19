/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Point;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * The class Pixel provides a wrapper about the byte level information contained in the raw PixelMap array. This can get and set
 * information about this Pixel, this might mean reading information from adjacent pixels ... but this class NEVER sets values for
 * other Pixels, and it NEVER registers/deregisters Nodes with the PixelMap.
 */
public class ImmutablePixel implements Pixel, Serializable {

    private final static long serialVersionUID = 1L;
    private final int x;
    private final int y;
    private final int hashCode;
    private final Point uhvwCenter;

    public ImmutablePixel(int pX, int pY, @NotNull Point uhvwCenter) {
        this.x = pX;
        this.y = pY;
        this.uhvwCenter = uhvwCenter;
        this.hashCode = Objects.hash(x, y, uhvwCenter);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public synchronized Point getUHVWMidPoint() {
        return uhvwCenter;
    }

    @Override
    public String toString() {
        return "Pixel(" + getX() + ", " + getY() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutablePixel that = (ImmutablePixel) o;
        return x == that.x &&
                y == that.y &&
                uhvwCenter.equals(that.uhvwCenter);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
