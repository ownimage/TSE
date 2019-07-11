/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class Pixel provides a wrapper about the byte level information contained in the raw PixelMap array. This can get and set
 * information about this Pixel, this might mean reading information from adjacent pixels ... but this class NEVER sets values for
 * other Pixels, and it NEVER registers/deregisters Nodes with the PixelMap.
 */
public class Pixel extends IntegerPoint implements PixelConstants {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;
    private static final IntegerPoint[] mNeighbours = { //
            //
            new IntegerPoint(-1, -1), new IntegerPoint(0, -1), new IntegerPoint(1, -1), //
            new IntegerPoint(-1, 0), new IntegerPoint(0, 0), new IntegerPoint(1, 0), //
            new IntegerPoint(-1, 1), new IntegerPoint(0, 1), new IntegerPoint(1, 1) //
    };
    private static final int[] mNeighbourOrder = {0, 1, 2, 5, 8, 7, 6, 3};
    private Point mUHVW = null;

    protected Pixel(final Pixel pPixel) {
        this(pPixel.getX(), pPixel.getY());
    }

    public Pixel(final IntegerPoint pIntegerPoint) {
        this(pIntegerPoint.getX(), pIntegerPoint.getY());
    }

    public Pixel(final int pX, final int pY) {
        super(pX, pY);
    }

    public Pixel(final PixelChain pPixelChain, final int pIndex) {
        this(pPixelChain.getPixel(pIndex));
    }

    @Override
    public Pixel add(final IntegerPoint pPoint) {
        return new Pixel(getX() + pPoint.getX(), getY() + pPoint.getY());
    }

    public Set<Pixel> allEdgeNeighbours(final PixelMap pPixelMap) {
        final HashSet<Pixel> allNeighbours = new HashSet<Pixel>();
        for (final Pixel pixel : getNeighbours()) {
            if (pixel.isEdge(pPixelMap)) {
                allNeighbours.add(pixel);
            }
        }
        return allNeighbours;
    }

    public boolean calcIsNode(final PixelMap pPixelMap) {
        return pPixelMap.calcIsNode(this);
    }

    protected synchronized void calcUHVWMidPoint(final PixelMap pPixelMap) {
        final double y = (getY() + 0.5d) / pPixelMap.getHeight();
        final double x = (getX() + 0.5d) / pPixelMap.getHeight();
        mUHVW = new Point(x, y);
    }

    public int countEdgeNeighbours(final PixelMap pPixelMap) {
        int count = 0;

        for (final Pixel pixel : getNeighbours()) {
            if (pixel.isEdge(pPixelMap)) {
                count++;
            }
        }

        return count;
    }

    public int countEdgeNeighboursTransitions(final PixelMap pPixelMap) {
        final int[] loop = new int[]{NW, N, NE, E, SE, S, SW, W, NW};

        int count = 0;
        boolean currentState = getNeighbour(NW).isEdge(pPixelMap);

        for (final int neighbour : loop) {
            if (currentState != getNeighbour(neighbour).isEdge(pPixelMap)) {
                currentState = getNeighbour(neighbour).isEdge(pPixelMap);
                count++;
            }
        }

        return count;
    }

    public Pixel getNeighbour(final int pN) {
        final Pixel pixel = add(mNeighbours[pN]);
        return pixel;
    }

//	public int getNeigbourEdgeCount() {
//		int cnt = 0;
//		for (Pixel p : getNeighbours()) {
//			if (p.isEdge()) {
//				cnt++;
//			}
//		}
//		return cnt;
//	}

    public Iterable<Pixel> getNeighbours() {
        return new Neighbours();
    }

    public Vector<Pixel> getNodeNeighbours(final PixelMap pPixelMap) {
        Framework.logEntry(mLogger);
        if (mLogger.isLoggable(Level.FINEST)) {
            mLogger.finest("Pixel = " + this);
        }

        final Vector<Pixel> allNeighbours = new Vector<Pixel>();
        for (final Pixel pixel : getNeighbours()) {
            if (pixel.isNode(pPixelMap)) {
                allNeighbours.add(pixel);
            }
        }

        if (mLogger.isLoggable(Level.FINEST)) {
            mLogger.finest("Returning " + allNeighbours);
        }
        Framework.logExit(mLogger);

        return allNeighbours;
    }

    public int countNodeNeighbours(final PixelMap pPixelMap) {
        return getNodeNeighbours(pPixelMap).size();
    }

    // UHVW = unit height variable width
    public synchronized Point getUHVWMidPoint(final PixelMap pPixelMap) {
        if (mUHVW == null) {
            calcUHVWMidPoint(pPixelMap);
        }
        return mUHVW;
    }

    public boolean isEdge(final PixelMap pPixelMap) {
        return pPixelMap.getData(this, EDGE);
    }

    public void setEdge(final PixelMap pPixelMap, final boolean pValue) {
        pPixelMap.setEdge(this, pValue);
    }

    public void setInChain(final PixelMap pPixelMap, final boolean pValue) {
        pPixelMap.setInChain(this, pValue);
    }

    public boolean isNeighbour(final Pixel pPixel) {
        // big question is are you a neighbour of yourself - YES
        return // pPixel.getPixelMap() == getPixelMap() && //
                Math.max(Math.abs(pPixel.getX() - getX()), Math.abs(pPixel.getY() - getY())) < 2;
    }

    public boolean isNode(final PixelMap pPixelMap) {
        return pPixelMap.getData(this, NODE);
    }

    public boolean isInChain(final PixelMap pPixelMap) {
        return pPixelMap.getData(this, IN_CHAIN);
    }

    public Optional<Node> getNode(final PixelMap pPixelMap) {
        return pPixelMap.getNode(this);
    }

    public boolean isUnVisitedEdge(final PixelMap pPixelMap) {
        return isEdge(pPixelMap) && !isVisited(pPixelMap);
    }

    public boolean isVisited(final PixelMap pPixelMap) {
        return pPixelMap.getData(this, VISITED);
    }

    public void setVisited(final PixelMap pPixelMap, final boolean pValue) {
        pPixelMap.setVisited(this, pValue);
    }

//    public void printNeighbours(final int pSize) {
//        for (int dy = -pSize; dy <= pSize; dy++) {
//            for (int dx = -pSize; dx <= pSize; dx++) {
//                final int x = getX() + dx;
//                final int y = getY() - dy;
//                final Pixel pixel = new Pixel(getPixelMap(), x, y);
//                // mLogger System.out.print(pixel.getPixelMap().getValue(pixel) + "(" + x + "," + y + ")\t");
//                // mLogger System.out.print(pixel.getPixelMap().getValue(pixel) + "\t");
//                // if (pixel.isNode())
//                // mLogger System.out.print("O");
//                // else if (pixel.isEdge())
//                // mLogger System.out.print("#");
//                // else
//                // mLogger System.out.print(".");
//            }
//            mLogger.info(() -> );
//        }
//    }
//
//    public boolean thin() {
//        // return getPixelMap().thin(this);
//        return true;
//    }

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
            final boolean b = mNext < mNeighbourOrder.length;
            return b;
        }

        @Override
        public Iterator<Pixel> iterator() {
            return this;
        }

        @Override
        public Pixel next() {
            if (hasNext()) {
                final Pixel pixel = getNeighbour(mNeighbourOrder[mNext]);
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
    public boolean equals(final Object pO) {
        if (this == pO) return true;
        if (pO == null || getClass() != pO.getClass()) return false;
        return super.equals(pO);
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
