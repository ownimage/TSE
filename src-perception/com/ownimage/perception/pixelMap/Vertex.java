/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.segment.ISegment;

/**
 * The Class Vertex. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements IVertex {



    public final static Logger mLogger = Framework.getLogger();
    private static final long serialVersionUID = 1L;

    private final PixelChain mPixelChain;

    private int mIndex;

    private Pixel mPixel;

    private ISegment mStartSegment;
    private ISegment mEndSegment;

    private Line mTangent;

    /**
     * This is used to track whether the segments either side match the tangent.
     */
    private boolean mSmooth;

    private Vertex(final IVertex pOther) {
        mPixelChain = pOther.getPixelChain();
        mIndex = pOther.getIndex();
        mPixel = pOther.getPixel();
        mTangent = pOther.getTangent();
    }

    private Vertex(final PixelChain pPixelChain, final int pIndex, final ISegment pStartSegment, final ISegment pEndSegment) {

        mPixelChain = pPixelChain;
        mIndex = pIndex;
        mPixel = new Pixel(pPixelChain.getPixel(pIndex));

        mStartSegment = pStartSegment;
        mEndSegment = pEndSegment;
    }

    public static Vertex createVertex(final PixelChain mPixelChain, final int pIndex) {
        return new Vertex(mPixelChain, pIndex, null, null);
    }

    public static IVertex createVertex(final PixelChain mPixelChain, final int pIndex, final boolean pFixed) {
        final IVertex vertex = new Vertex(mPixelChain, pIndex, null, null);
        vertex.setFixed(pFixed);
        return vertex;
    }

    /**
     * Calc tangent always generates a tangent line that goes in the direction of start to finish.
     */
    private void calcTangent() {
        if (getStartSegment() == null && getEndSegment() == null) {
            return;
        }

        if (getStartSegment() == null) {
            Line tangent = getEndSegment().getStartTangent();
            mTangent = tangent.getReverse();
            return;
        }

        if (getEndSegment() == null) {
            mTangent = getStartSegment().getEndTangent();
            return;
        }

        final Point startTangentPoint = getStartSegment().getEndTangent().getPoint(1.0d);
        final Point endTangentPoint = getEndSegment().getStartTangent().getPoint(1.0d);
        final Vector tangentVector = startTangentPoint.minus(endTangentPoint).normalize();

        mTangent = new Line(getUHVWPoint(), getUHVWPoint().add(tangentVector));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final IVertex pVertex) {
        if (pVertex == null) {
            throw new NullPointerException("pVertex must not be  null");
        }

        if (samePosition(pVertex)) {
            return 0;
        }

        if (getX() > pVertex.getX() || getX() == pVertex.getX() && getY() > pVertex.getY()) {
            return 1;
        }

        return -1;
    }

    @Override
    public IVertex copy() {
        return new Vertex(this);
    }

    @Override
    public IVertex deepCopy(final IVertex pOriginalPCStartVertex, final IVertex pCopyPCStartVertex) {

        if (pOriginalPCStartVertex == this) {
            return pCopyPCStartVertex;
        }

        final Vertex copy = createVertex(getPixelChain(), getIndex());
        copy.mPixel = mPixel.deepCopy();
        if (mEndSegment != null) {
            if (pCopyPCStartVertex == null) { // this is the start of the process
                copy.mEndSegment = mEndSegment.deepCopy(this, copy, copy);
            }
            copy.mEndSegment = mEndSegment.deepCopy(pOriginalPCStartVertex, pCopyPCStartVertex, copy);
        }
        copy.mTangent = mTangent; // Lines are invarient

        return copy;
    }

    @Override
    public void delete() {
        Framework.logEntry(mLogger);

        getPixelChain().deleteVertex(this);

        Framework.logExit(mLogger);
    }

    @Override
    public boolean equals(final Object pObject) {
        if (!(pObject instanceof Vertex)) {
            return false;
        }

        final Vertex other = (Vertex) pObject;
        return mPixelChain == other.mPixelChain && mIndex == other.mIndex;
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
    public void setIndex(final int pIndex) {
        // if (mStartSegment != null && !(pIndex > mStartSegment.getStartIndex())) { throw new IllegalArgumentException("pIndex must be greater than mStartSegment's starting index"); }

        if (mEndSegment != null && !(pIndex < mEndSegment.getEndIndex())) {
            throw new IllegalArgumentException("pIndex must be less than than mEndSegment's end index");
        }

        if (isFixed()) {
            throw new IllegalStateException("Cannot set the index on a Vertex that isFixed()");
        }

        if (mIndex == pIndex) {
            return;
        }

        mIndex = pIndex;
        mPixel = new Pixel(mPixelChain, mIndex);

        if (mStartSegment != null) {
            mStartSegment.vertexChange(this);
        }

        if (mEndSegment != null) {
            mEndSegment.vertexChange(this);
        }
    }

    @Override
    public Pixel getPixel() {
        return mPixel;
    }

    @Override
    public void setPixel(final Pixel pPixel) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pPixel", pPixel);

        // need to comment out as PixelMap not set after transform.load
        // if (pPixel.getPixelMap() == null) { throw new IllegalArgumentException("pPixel.getPixelMap() must not be null");
        mPixel = pPixel;
        if (mStartSegment != null) {
            mStartSegment.vertexChange(this);
        }
        if (mEndSegment != null) {
            mEndSegment.vertexChange(this);
        }
        Framework.logExit(mLogger);
    }

    @Override
    public PixelChain getPixelChain() {
        return mPixelChain;
    }

    @Override
    public PixelMap getPixelMap() {
        return getPixelChain().getPixelMap();
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
    public synchronized Line getTangent() {
        if (mTangent == null) {
            calcTangent();
        }

        return mTangent;
    }

    @Override
    public void setTangent(final com.ownimage.perception.math.LineSegment pTangent) {
        mTangent = pTangent;
    }

    @Override
    public Point getUHVWPoint() {
        return mPixel.getUHVWPoint();
    }

    @Override
    public int getX() {
        Framework.logEntry(mLogger);
        final int x = getPixel().getX();
        Framework.logExit(mLogger);
        return x;
    }

    @Override
    public int getY() {
        Framework.logEntry(mLogger);
        final int y = getPixel().getY();
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
    public boolean isFixed() {
        return mPixel.isFixed();
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

    @Override
    public boolean samePosition(final IVertex pVertex) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pVertex", pVertex);

        final boolean result = getX() == pVertex.getX() && getY() == pVertex.getY();
        Framework.logExit(mLogger);
        return result;
    }

    @Override
    public String toString() {
        return "Vertex[Index=" + mIndex + " ,mX=" + getX() + " , getY()=" + getY() + "]";
    }

    @Override
    public boolean isSmooth() {
        return mSmooth;
    }

    @Override
    public void setSmooth(boolean pSmooth) {
        mSmooth = pSmooth;
    }
}
