/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import java.util.logging.Logger;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.PixelChain;

public abstract class SegmentBase<T extends SegmentBase> implements ISegment<T> {

    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private int mSegmentIndex;
    private double mStartPosition;

    public SegmentBase(final int pSegmentIndex) {
        mSegmentIndex = pSegmentIndex;
    }

    protected void setSegmentIndex(final int pSegmentIndex) {
        mSegmentIndex = pSegmentIndex;
    }

    protected void setStartPosition(final double pStartPosition) {
        mStartPosition = pStartPosition;
    }

    @Override
    public double calcError(final PixelChain pPixelChain) {
        double error = 0.0d;
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            final Point uhvw = pPixelChain.getUHVWPoint(i);
            final double distance = distance(pPixelChain, uhvw);

            error += distance * distance;
        }

        if (error < 0.0d) {
            System.err.println("-ve error");
        }

        return error;
    }

    @Override
    public int getSegmentIndex() {
        return mSegmentIndex;
    }

    public boolean closerThan(final PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    public abstract double distance(PixelChain pPixelChain, final Point pUVHWPoint);

    double getActualThickness(final IPixelMapTransformSource pSource, final PixelChain pPixelChain, final double pPosition) {
        return pPixelChain.getActualThickness(pSource, pPosition);
    }

    public Point getControlPoint() {
        Framework.logEntry(mLogger);
        Framework.logExit(mLogger);
        return null;
    }

    @Override
    public int getEndIndex(final PixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex + 1).getPixelIndex();
    }

    @Override
    public Line getEndTangent(final PixelChain pPixelChain) {
        return new Line(getEndUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain).add(getEndTangentVector(pPixelChain)));
    }

    @Override
    public Point getEndUHVWPoint(final PixelChain pPixelChain) {
        return getEndVertex(pPixelChain).getUHVWPoint(pPixelChain);
    }

    @Override
    public IVertex getEndVertex(final PixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex + 1);
    }

    public double getMaxX(final PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMaxY(final PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinX(final PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinY(final PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPixelLength(final PixelChain pPixelChain) {
        final int length;

        if (getStartIndex(pPixelChain) == 0) {
            length = 1 + getEndIndex(pPixelChain);
        } else {
            length = getEndIndex(pPixelChain) - getStartIndex(pPixelChain);
        }

        return length;
    }

    @Override
    public int getStartIndex(final PixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getPixelIndex();
    }

    double getStartPosition() {
        return mStartPosition;
    }

    @Override
    public Line getStartTangent(final PixelChain pPixelChain) {
        return new Line(getStartUHVWPoint(pPixelChain), getStartUHVWPoint(pPixelChain).add(getStartTangentVector(pPixelChain)));
    }

    @Override
    public Point getStartUHVWPoint(final PixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getUHVWPoint(pPixelChain);
    }

    @Override
    public IVertex getStartVertex(final PixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex);
    }

    @Override
    public void graffiti(final PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        pGraphics.grafittiLine(getStartUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain));
    }

    public boolean noPixelFurtherThan(final PixelChain pPixelChain, final double pDistance) {
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            final Point uhvw = pPixelChain.getUHVWPoint(i);
            if (distance(pPixelChain, uhvw) > pDistance) {
                return false;
            }
        }
        return true;
    }

    protected void setStartPosition(final PixelChain pPixelChain, final double pStartPosition) {
        mStartPosition = pStartPosition;
    }

    @Override
    public String toString() {
        return "SegmentBase[" + mSegmentIndex + "]";
    }

    @Override
    public T withSegmentIndex(final int pSegmentIndex) {
        if (pSegmentIndex == mSegmentIndex) return (T) this;
        try {
            final T clone = (T) clone();
            clone.setSegmentIndex(pSegmentIndex);
            return (T) this;
        } catch (final CloneNotSupportedException pCNSE) {
            throw new RuntimeException("Cannot clone", pCNSE);
        }
    }

    @Override
    public T withStartPosition(final PixelChain pPixelChain, final double pStartPosition) {
        if (pStartPosition == mStartPosition) return (T) this;
        try {
            final T clone = (T) clone();
            clone.setStartPosition(pPixelChain, pStartPosition);
            return clone;
        } catch (final CloneNotSupportedException pCNSE) {
            throw new RuntimeException("Cannot clone", pCNSE);
        }
    }

}
