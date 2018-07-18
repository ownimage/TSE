/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.util.Version;
import com.ownimage.perception.math.IntegerPoint;
import com.ownimage.perception.math.Point;

/**
 * The class Pixel provides a wrapper about the byte level information contained in the raw PixelMap array. This can get and set
 * information about this Pixel, this might mean reading information from adjacent pixels ... but this class NEVER sets values for
 * other Pixels, and it NEVER registers/deregisters Nodes with the PixelMap.
 */
public class Pixel extends IntegerPoint implements PixelConstants {

    public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
    public final static String mClassname = Pixel.class.getName();
    public final static Logger mLogger = Logger.getLogger(mClassname);
    public final static long serialVersionUID = 1L;
    private static final IntegerPoint[] mNeighbours = { //
            //
            new IntegerPoint(-1, -1), new IntegerPoint(0, -1), new IntegerPoint(1, -1), //
            new IntegerPoint(-1, 0), new IntegerPoint(0, 0), new IntegerPoint(1, 0), //
            new IntegerPoint(-1, 1), new IntegerPoint(0, 1), new IntegerPoint(1, 1) //
    };
    private static int[] mNeighbourOrder = {0, 1, 2, 5, 8, 7, 6, 3};
    private Point m_UHVW = null;
    transient private PixelMap mPixelMap;

    protected Pixel(final Pixel pPixel) {
        this(pPixel.getPixelMap(), pPixel.getX(), pPixel.getY());
        setUHVWPoint(pPixel.getUHVWPoint());
    }

    public Pixel(final PixelMap pPixelMap, final int pX, final int pY) {
        super(pX, pY);

        // TODO this has been commented out to make the deep copy work ... the problem is that the save/load transform does not
        // preserve the pixel map
        // if (pPixelMap == null) {
        // mLogger.severe("pPixelMap must not be mull");
        // throw new IllegalArgumentException("pPixelMap must not be mull");
        // }
        mPixelMap = pPixelMap;
    }

    public Pixel(final PixelChain pPixelChain, final int pIndex) {
        this(pPixelChain.getPixel(pIndex));
    }

    @Override
    public Pixel add(final IntegerPoint pPoint) {
        return new Pixel(getPixelMap(), getX() + pPoint.getX(), getY() + pPoint.getY());
    }

    public Set<Pixel> allEdgeNeighbours() {
        final HashSet<Pixel> allNeighbours = new HashSet<Pixel>();
        for (final Pixel pixel : getNeighbours()) {
            if (pixel.isEdge()) {
                allNeighbours.add(pixel);
            }
        }
        return allNeighbours;
    }

    public boolean calcIsNode() {
        return mPixelMap.calcIsNode(this);
    }

    protected synchronized void calcUHVWPoint() {
        final double y = (getY() + 0.5d) / getHeight();
        final double x = (getX() + 0.5d) / getHeight();
        m_UHVW = new Point(x, y);
    }

    public int countEdgeNeighbours() {
        int count = 0;

        for (final Pixel pixel : getNeighbours()) {
            if (pixel.isEdge()) {
                count++;
            }
        }

        return count;
    }

    public int countEdgeNeighboursTransitions() {
        final int[] loop = new int[]{NW, N, NE, E, SE, S, SW, W, NW};

        int count = 0;
        boolean currentState = getNeighbour(NW).isEdge();

        for (final int neighbour : loop) {
            if (currentState != getNeighbour(neighbour).isEdge()) {
                currentState = getNeighbour(neighbour).isEdge();
                count++;
            }
        }

        return count;
    }

    public Pixel deepCopy() {
        return new Pixel(this);
    }

    @Override
    public boolean equals(final Object pObj) {
        if (pObj instanceof Pixel) {
            final Pixel pixel = (Pixel) pObj;
            if (getPixelMap() == pixel.getPixelMap() //
                    && getX() == pixel.getX() // )
                    && getY() == pixel.getY()) {
                return true;
            }
        }
        return false;

    }

    private int getHeight() {
        return getPixelMap().getHeight();
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

    public Iterable<Pixel> getNodeNeighbours() {
        mLogger.entering(mClassname, "getNodeNeighbours");
        if (mLogger.isLoggable(Level.FINEST)) {
            mLogger.finest("Pixel = " + this);
        }

        final Vector<Pixel> allNeighbours = new Vector<Pixel>();
        for (final Pixel pixel : getNeighbours()) {
            if (pixel.isNode()) {
                allNeighbours.add(pixel);
            }
        }

        if (mLogger.isLoggable(Level.FINEST)) {
            mLogger.finest("Returning " + allNeighbours);
        }
        mLogger.exiting(mClassname, "getNodeNeighbours");

        return allNeighbours;
    }

    PixelMap getPixelMap() {
        return mPixelMap;
    }

    public void setPixelMap(final PixelMap pPixelMap) {
        if (pPixelMap == null) {
            throw new IllegalArgumentException("pPixelMap must not be null");
        }

        // note below we allow values being set to the same value as nodes may be multiply set from different PixelChains
        if (mPixelMap != null && mPixelMap != pPixelMap) {
            throw new IllegalStateException("mPixelMap has already been set ... it can not be changed");
        }

        mPixelMap = pPixelMap;
    }

    // UHVW = unit height variable width
    public synchronized Point getUHVWPoint() {
        if (m_UHVW == null) {
            calcUHVWPoint();
        }
        return m_UHVW;
    }

    public synchronized void setUHVWPoint(final Point pUHVW) {
        m_UHVW = pUHVW;
    }

    private int getWidth() {
        return getPixelMap().getWidth();
    }

    @Override
    public int hashCode() {
        return getPixelMap().getWidth() * getY() + getX();
    }

    public boolean hasNodalNeighbours() {
        boolean hNN = false;

        for (final Pixel pixel : getNeighbours()) {
            if (pixel.isNode()) {
                hNN = true;
                break;
            }
        }

        return hNN;
    }

    public boolean isEdge() {
        return getPixelMap().getData(this, EDGE);
    }

    public void setEdge(final boolean pValue) {
        getPixelMap().setEdge(this, pValue);
    }

    public boolean isFixed() {
        return false;
        // TODO need to fix this getPixelMap().getData(this, FIXED);
    }

    public void setFixed(final boolean pValue) {
        getPixelMap().setData(this, pValue, FIXED);
    }

    public boolean isInChain() {
        return getPixelMap().getData(this, IN_CHAIN);
    }

    public void setInChain(final boolean pValue) {
        getPixelMap().setData(this, pValue, IN_CHAIN);
    }

    public boolean isNeighbour(final Pixel pPixel) {
        // big question is are you a neighbour of yourself - YES
        return // pPixel.getPixelMap() == getPixelMap() && //
                Math.max(Math.abs(pPixel.getX() - getX()), Math.abs(pPixel.getY() - getY())) < 2;
    }

    public boolean isNode() {
        return getPixelMap().getData(this, NODE);
    }

    public void setNode(final boolean pValue) {
        getPixelMap().setNode(this, pValue);
    }

    public boolean isUnVisitedEdge() {
        return isEdge() && !isVisited();
    }

    public boolean isVisited() {
        return getPixelMap().getData(this, VISITED);
    }

    public void setVisited(final boolean pValue) {
        getPixelMap().setData(this, pValue, VISITED);
    }

//    public void printNeighbours(final int pSize) {
//        for (int dy = -pSize; dy <= pSize; dy++) {
//            for (int dx = -pSize; dx <= pSize; dx++) {
//                final int x = getX() + dx;
//                final int y = getY() - dy;
//                final Pixel pixel = new Pixel(getPixelMap(), x, y);
//                // System.out.print(pixel.getPixelMap().getValue(pixel) + "(" + x + "," + y + ")\t");
//                // System.out.print(pixel.getPixelMap().getValue(pixel) + "\t");
//                // if (pixel.isNode())
//                // System.out.print("O");
//                // else if (pixel.isEdge())
//                // System.out.print("#");
//                // else
//                // System.out.print(".");
//            }
//            System.out.println();
//        }
//    }
//
//    public boolean thin() {
//        // return getPixelMap().thin(this);
//        return true;
//    }

    public Point toPoint() {
        return getPixelMap().pixelToPoint(this);
    }

    @Override
    public String toString() {
        return "Pixel(" + getX() + ", " + getY() + ")";
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

}
