/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.segment.ISegment;

import java.util.logging.Logger;

/**
 * The Vertex class, this class is immutable. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements IVertex {


    private final static Logger mLogger = Framework.getLogger();
    private static final long serialVersionUID = 1L;

    private int mVertexIndex;
    private int mPixelIndex;

    private Vertex(final int pVertexIndex, final int pPixelIndex) {
        mVertexIndex = pVertexIndex;
        mPixelIndex = pPixelIndex;
    }

    public static Vertex createVertex(PixelChain pPixelChain, int pVertexIndex, final int pPixelIndex) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelLength()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelLength() + ")");
        }

        return new Vertex(pVertexIndex, pPixelIndex);
    }

    @Override
    public int getVertexIndex() {
        return mVertexIndex;
    }

    /**
     * Calc tangent always generates a tangent line that goes in the direction of start to finish.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     */
    @Override
    public Line calcTangent(PixelChain pPixelChain) {
        Line tangent;
        if (getStartSegment(pPixelChain) == null && getEndSegment(pPixelChain) == null) {
            tangent = null;
        } else if (getStartSegment(pPixelChain) == null) {
            tangent = getEndSegment(pPixelChain).getStartTangent(pPixelChain);
            tangent = tangent.getReverse();
        } else if (getEndSegment(pPixelChain) == null) {
            tangent = getStartSegment(pPixelChain).getEndTangent(pPixelChain);
            } else {
            final Point startTangentPoint = getStartSegment(pPixelChain).getEndTangent(pPixelChain).getPoint(1.0d);
            final Point endTangentPoint = getEndSegment(pPixelChain).getStartTangent(pPixelChain).getPoint(1.0d);
                final Vector tangentVector = startTangentPoint.minus(endTangentPoint).normalize();

            tangent = new Line(getUHVWPoint(pPixelChain), getUHVWPoint(pPixelChain).add(tangentVector));
            }
        return tangent;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final IVertex pOther) {
        return mPixelIndex - pOther.getPixelIndex();
    }

    @Override
    public boolean equals(final Object pObject) {
        if (!(pObject instanceof Vertex)) {
            return false;
        }

        final Vertex other = (Vertex) pObject;
        return mPixelIndex == other.mPixelIndex;
    }

    @Override
    public ISegment getEndSegment(PixelChain pPixelChain) {
        return pPixelChain.getSegment(mVertexIndex);
    }

    @Override
    public int getPixelIndex() {
        return mPixelIndex;
    }

    @Override
    public Pixel getPixel(PixelChain pPixelChain) {
        return pPixelChain.getPixel(mPixelIndex);
    }

    @Override
    public ISegment getStartSegment(PixelChain pPixelChain) {
        return pPixelChain.getSegment(mVertexIndex - 1);
    }

    @Override
    public Point getUHVWPoint(PixelChain pPixelChain) {
        return getPixel(pPixelChain).getUHVWPoint();
    }

    @Override
    public boolean isDisconnected(PixelChain pPixelChain) {
        return getStartSegment(pPixelChain) == null && getEndSegment(pPixelChain) == null;
    }

    @Override
    public boolean isEnd(PixelChain pPixelChain) {
        return getStartSegment(pPixelChain) != null && getEndSegment(pPixelChain) == null;
    }

    @Override
    public boolean isMiddle(PixelChain pPixelChain) {
        return getStartSegment(pPixelChain) != null && getEndSegment(pPixelChain) != null;
    }

    @Override
    public boolean isStart(PixelChain pPixelChain) {
        return getStartSegment(pPixelChain) == null && getEndSegment(pPixelChain) != null;
    }

    @Override
    public String toString() {
        return "Vertex[Index=" + mPixelIndex + "]";
    }

}
