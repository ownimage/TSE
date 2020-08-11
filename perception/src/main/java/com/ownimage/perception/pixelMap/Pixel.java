/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.immutable.PixelChain;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * The class Pixel provides a wrapper about the byte level information contained in the raw PixelMap array. This can get and set
 * information about this Pixel, this might mean reading information from adjacent pixels ... but this class NEVER sets values for
 * other Pixels, and it NEVER registers/deregisters Nodes with the PixelMap.
 */
public class Pixel extends IntegerPoint implements PixelConstants {

    final static Logger mLogger = Framework.getLogger();
    private final static long serialVersionUID = 1L;

    private static final IntegerPoint[] mNeighbours = { //
            //
            new IntegerPoint(-1, -1), new IntegerPoint(0, -1), new IntegerPoint(1, -1), //
            new IntegerPoint(-1, 0), new IntegerPoint(0, 0), new IntegerPoint(1, 0), //
            new IntegerPoint(-1, 1), new IntegerPoint(0, 1), new IntegerPoint(1, 1) //
    };
    private static final int[] mNeighbourOrder = {0, 1, 2, 5, 8, 7, 6, 3};

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

    public Iterable<Pixel> getNeighbours() {
        return new Neighbours();
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

    @Override
    public String toString() {
        return "Pixel(" + getX() + ", " + getY() + ")";
    }

    public IntegerPoint toIntegerPoint() {
        return new IntegerPoint(getX(), getY());
    }

    private class Neighbours implements Iterable<Pixel>, Iterator<Pixel> {

        private int mNext = 0;

        @Override
        public boolean hasNext() {
            return mNext < mNeighbourOrder.length;
        }

        @Override
        public Iterator<Pixel> iterator() {
            return this;
        }

        @Override
        public Pixel next() {
            if (hasNext()) {
                Pixel pixel = getNeighbour(mNeighbourOrder[mNext]);
                mNext++;
                return pixel;

            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
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
