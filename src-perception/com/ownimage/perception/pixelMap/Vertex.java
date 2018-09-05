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
 * The Class Vertex. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements IVertex {


    public final static Logger mLogger = Framework.getLogger();
    private static final long serialVersionUID = 1L;

    private int mIndex;

    private ISegment mStartSegment;
    private ISegment mEndSegment;

    private Line mTangent;

    /**
     * This is used to track whether the segments either side match the tangent.
     */
    private boolean mSmooth;

    private Vertex(PixelChain pPixelChain, final IVertex pOther) {
        mIndex = pOther.getIndex();
        mTangent = pOther.calcTangent(pPixelChain);
    }

    private Vertex(final PixelChain pPixelChain, final int pIndex, final ISegment pStartSegment, final ISegment pEndSegment) {
        mIndex = pIndex;

        mStartSegment = pStartSegment;
        mEndSegment = pEndSegment;
    }

    public static Vertex createVertex(final PixelChain mPixelChain, final int pIndex) {
        return new Vertex(mPixelChain, pIndex, null, null);
    }

    /**
     * Calc tangent always generates a tangent line that goes in the direction of start to finish.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     */
    @Override
    public Line calcTangent(PixelChain pPixelChain) {
        if (mTangent == null) {
            if (getStartSegment() == null && getEndSegment() == null) {
                mTangent = null;
            } else if (getStartSegment() == null) {
                Line tangent = getEndSegment().getStartTangent(pPixelChain);
                mTangent = tangent.getReverse();
            } else if (getEndSegment() == null) {
                mTangent = getStartSegment().getEndTangent(pPixelChain);
            } else {
                final Point startTangentPoint = getStartSegment().getEndTangent(pPixelChain).getPoint(1.0d);
                final Point endTangentPoint = getEndSegment().getStartTangent(pPixelChain).getPoint(1.0d);
                final Vector tangentVector = startTangentPoint.minus(endTangentPoint).normalize();

                mTangent = new Line(getUHVWPoint(pPixelChain), getUHVWPoint(pPixelChain).add(tangentVector));
            }
        }
        return mTangent;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final IVertex pOther) {
        return mIndex - pOther.getIndex();
    }

    @Override
    public boolean equals(final Object pObject) {
        if (!(pObject instanceof Vertex)) {
            return false;
        }

        final Vertex other = (Vertex) pObject;
        return mIndex == other.mIndex;
    }

    @Override
    public ISegment getEndSegment() {
        return mEndSegment;
    }

    @Override
    public void setEndSegment(final ISegment pEndSegment) {
        mEndSegment = pEndSegment;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public void setIndex(PixelChain pPixelChain, final int pIndex) {
        // if (mStartSegment != null && !(pIndex > mStartSegment.getStartIndex())) { throw new IllegalArgumentException("pIndex must be greater than mStartSegment's starting index"); }

        if (mEndSegment != null && !(pIndex < mEndSegment.getEndIndex(pPixelChain))) {
            throw new IllegalArgumentException("pIndex must be less than than mEndSegment's end index");
        }

        if (isFixed(pPixelChain)) {
            throw new IllegalStateException("Cannot set the index on a Vertex that isFixed()");
        }

        if (mIndex == pIndex) {
            return;
        }

        mIndex = pIndex;

        if (mStartSegment != null) {
            mStartSegment.vertexChange(pPixelChain, this);
        }

        if (mEndSegment != null) {
            mEndSegment.vertexChange(pPixelChain, this);
        }
    }

    @Override
    public Pixel getPixel(PixelChain pPixelChain) {
        return pPixelChain.getPixel(mIndex);
    }

    @Override
    public ISegment getStartSegment() {
        return mStartSegment;
    }

    @Override
    public void setStartSegment(final ISegment pStartSegment) {
        mStartSegment = pStartSegment;
    }

    @Override
    public Line getTangent() {
        return mTangent;
    }

    @Override
    public void setTangent(final com.ownimage.perception.math.LineSegment pTangent) {
        mTangent = pTangent;
    }

    @Override
    public Point getUHVWPoint(PixelChain pPixelChain) {
        return getPixel(pPixelChain).getUHVWPoint();
    }

    @Override
    public int getX(PixelChain pPixelChain) {
        Framework.logEntry(mLogger);
        final int x = getPixel(pPixelChain).getX();
        Framework.logExit(mLogger);
        return x;
    }

    @Override
    public int getY(PixelChain pPixelChain) {
        Framework.logEntry(mLogger);
        final int y = getPixel(pPixelChain).getY();
        Framework.logExit(mLogger);
        return y;
    }

    @Override
    public boolean isDisconnected() {
        return getStartSegment() == null && getEndSegment() == null;
    }

    @Override
    public boolean isEnd() {
        return getStartSegment() != null && getEndSegment() == null;
    }

    @Override
    public boolean isFixed(PixelChain pPixelChain) {
        return getPixel(pPixelChain).isFixed();
    }

    @Override
    public void setFixed(final boolean pFixed) {
        //mPixel.setFixed(pFixed);
    }

    @Override
    public boolean isMiddle() {
        return getStartSegment() != null && getEndSegment() != null;
    }

    @Override
    public boolean isStart() {
        return getStartSegment() == null && getEndSegment() != null;
    }

    private boolean samePosition(PixelChain pPixelChain, final IVertex pVertex) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pVertex", pVertex);

        final boolean result = getX(pPixelChain) == pVertex.getX(pPixelChain) && getY(pPixelChain) == pVertex.getY(pPixelChain);
        Framework.logExit(mLogger);
        return result;
    }

    @Override
    public String toString() {
        return "Vertex[Index=" + mIndex + "]";
    }

}
