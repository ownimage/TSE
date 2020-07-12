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

    public Set<Pixel> allEdgeNeighbours(PixelMap pPixelMap) {
        HashSet<Pixel> allNeighbours = new HashSet<>();
        for (Pixel pixel : getNeighbours()) {
            if (pixel.isEdge(pPixelMap)) {
                allNeighbours.add(pixel);
            }
        }
        return allNeighbours;
    }

    private synchronized void calcUHVWMidPoint(int height) {
        double y = (getY() + 0.5d) / height;
        double x = (getX() + 0.5d) / height;
        mUHVW = new Point(x, y);
    }

    public int countEdgeNeighbours(PixelMap pPixelMap) {
        int count = 0;

        for (Pixel pixel : getNeighbours()) {
            if (pixel.isEdge(pPixelMap)) {
                count++;
            }
        }

        return count;
    }

    public int countEdgeNeighboursTransitions(PixelMap pPixelMap) {
        int[] loop = new int[]{NW, N, NE, E, SE, S, SW, W, NW};

        int count = 0;
        boolean currentState = getNeighbour(NW).isEdge(pPixelMap);

        for (int neighbour : loop) {
            if (currentState != getNeighbour(neighbour).isEdge(pPixelMap)) {
                currentState = getNeighbour(neighbour).isEdge(pPixelMap);
                count++;
            }
        }

        return count;
    }

    public Pixel getNeighbour(int pN) {
        return add(mNeighbours[pN]);
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

    public Vector<Pixel> getNodeNeighbours(PixelMap pPixelMap) {
        Framework.logEntry(mLogger);
        if (mLogger.isLoggable(Level.FINEST)) {
            mLogger.finest("Pixel = " + this);
        }

        Vector<Pixel> allNeighbours = new Vector<>();
        for (Pixel pixel : getNeighbours()) {
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

    public int countNodeNeighbours(PixelMap pPixelMap) {
        return getNodeNeighbours(pPixelMap).size();
    }

    // UHVW = unit height variable width
    public synchronized Point getUHVWMidPoint(int height) {
        if (mUHVW == null) {
            calcUHVWMidPoint(height);
        }
        return mUHVW;
    }

    public boolean isEdge(PixelMap pPixelMap) {
        return pPixelMap.getData(this, EDGE);
    }

    public void setEdge(PixelMap pPixelMap, boolean pValue) {
        pPixelMap.setEdge(this, pValue);
    }

    public void setInChain(PixelMap pPixelMap, boolean pValue) {
        pPixelMap.setInChain(this, pValue);
    }

    public boolean isNeighbour(Pixel pPixel) {
        // big question is are you a neighbour of yourself - YES
        return // pPixel.getPixelMap() == getPixelMap() && //
                Math.max(Math.abs(pPixel.getX() - getX()), Math.abs(pPixel.getY() - getY())) < 2;
    }

    public boolean isNode(PixelMap pPixelMap) {
        return pPixelMap.getData(this, NODE);
    }

    public boolean isInChain(PixelMap pPixelMap) {
        return pPixelMap.getData(this, IN_CHAIN);
    }

    public Optional<Node> getNode(PixelMap pPixelMap) {
        return pPixelMap.getNode(this);
    }

    public boolean isUnVisitedEdge(PixelMap pPixelMap) {
        return isEdge(pPixelMap) && !isVisited(pPixelMap);
    }

    public boolean isVisited(PixelMap pPixelMap) {
        return pPixelMap.getData(this, VISITED);
    }

    public void setVisited(PixelMap pPixelMap, boolean pValue) {
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
