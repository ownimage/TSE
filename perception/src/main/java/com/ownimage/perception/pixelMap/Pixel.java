/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.immutable.PixelChain;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * The class Pixel provides a wrapper about the byte level information contained in the raw PixelMap array. This can get and set
 * information about this Pixel, this might mean reading information from adjacent pixels ... but this class NEVER sets values for
 * other Pixels, and it NEVER registers/deregisters Nodes with the PixelMap.
 */
public class Pixel extends IntegerPoint implements PixelConstants {

    private final static long serialVersionUID = 1L;

    private static final IntegerPoint[] mNeighbours = { //
            //
            new IntegerPoint(-1, -1), new IntegerPoint(0, -1), new IntegerPoint(1, -1), //
            new IntegerPoint(-1, 0), new IntegerPoint(0, 0), new IntegerPoint(1, 0), //
            new IntegerPoint(-1, 1), new IntegerPoint(0, 1), new IntegerPoint(1, 1) //
    };
    private static final Integer[] mNeighbourOrder = {0, 1, 2, 5, 8, 7, 6, 3};

    private Point mUHVW = null;

    private Pixel(Pixel pPixel) {
        this(pPixel.getX(), pPixel.getY());
    }

    public Pixel(IntegerPoint pIntegerPoint) {
        this(pIntegerPoint.getX(), pIntegerPoint.getY());
    }

    public Pixel(int pX, int pY) {
        super(pX, pY);
    }

    public Pixel(PixelChain pPixelChain, int pIndex) {
        this(pPixelChain.getPixel(pIndex));
    }


    @Override
    public String toString() {
        return "Pixel(" + getX() + ", " + getY() + ")";
    }

    @Override
    public Pixel add(IntegerPoint pPoint) {
        return new Pixel(getX() + pPoint.getX(), getY() + pPoint.getY());
    }


    private synchronized void calcUHVWMidPoint(int height) {
        double y = (getY() + 0.5d) / height;
        double x = (getX() + 0.5d) / height;
        mUHVW = new Point(x, y);
    }


    public Pixel getNeighbour(int pN) {
        return add(mNeighbours[pN]);
    }

    public Stream<Pixel> getNeighbours() {
        return Arrays.stream(mNeighbourOrder)
                .map(i -> mNeighbours[i])
                .map(this::add);
    }

    // UHVW = unit height variable width
    public synchronized Point getUHVWMidPoint(int height) {
        if (mUHVW == null) {
            calcUHVWMidPoint(height);
        }
        return mUHVW;
    }

    public boolean isNeighbour(Pixel pPixel) {
        // big question is are you a neighbour of yourself - YES
        return // pPixel.getPixelMap() == getPixelMap() && //
                Math.max(Math.abs(pPixel.getX() - getX()), Math.abs(pPixel.getY() - getY())) < 2;
    }


    public IntegerPoint toIntegerPoint() {
        return new IntegerPoint(getX(), getY());
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
