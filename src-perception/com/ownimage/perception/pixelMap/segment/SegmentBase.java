/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;

import java.util.logging.Logger;

public abstract class SegmentBase implements ISegment {

    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final IVertex mStart;
    private final IVertex mEnd;
    private double mStartPosition;

    public SegmentBase(final IVertex pStart, final IVertex pEnd) {

        if (pStart == null) {
            throw new IllegalArgumentException("pStart must not be null.");
        }

        if (pEnd == null) {
            throw new IllegalArgumentException("pEnd must not be null.");
        }

        if (pStart.getPixelIndex() >= pEnd.getPixelIndex()) {
            throw new IllegalArgumentException("start index =(" + pStart.getPixelIndex() + ")must be less than end index =(" + pEnd.getPixelIndex() + ").");
        }

        mStart = pStart;
        mEnd = pEnd;
    }

    @Override
    public void attachToVertexes(PixelChain pPixelChain, final boolean pReCalcSegments) {
        mStart.setEndSegment(this);
        mEnd.setStartSegment(this);
        if (pReCalcSegments) {
            //pPixelChain.reCalcSegments();
            throw new RuntimeException("PixelChain must reCalcSegments remove recalc arg");
        }
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

    public boolean closerThan(PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    public abstract double distance(PixelChain pPixelChain, final Point pUVHWPoint);

    public double getActualThickness(final PixelChain pPixelChain, final double pPosition) {
        return pPixelChain.getActualThickness(pPosition);
    }

    public Point getControlPoint() {
        Framework.logEntry(mLogger);
        Framework.logExit(mLogger);
        return null;
    }

    @Override
    public int getEndIndex(PixelChain pPixelChain) {
        return mEnd.getPixelIndex();
    }

    @Override
    public Pixel getEndPoint(PixelChain pPixelChain) {
        return mEnd.getPixel(pPixelChain);
    }

    @Override
    public Line getEndTangent(PixelChain pPixelChain) {
        return new Line(getEndUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain).add(getEndTangentVector(pPixelChain)));
    }

    @Override
    public Point getEndUHVWPoint(PixelChain pPixelChain) {
        return mEnd.getUHVWPoint(pPixelChain);
    }

    @Override
    public IVertex getEndVertex(PixelChain pPixelChain) {
        return mEnd;
    }

    public double getMaxX(PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMaxY(PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinX(PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinY(PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPixelLength(PixelChain pPixelChain) {
        int length;

        if (getStartIndex(pPixelChain) == 0) {
            length = 1 + getEndIndex(pPixelChain);
        } else {
            length = getEndIndex(pPixelChain) - getStartIndex(pPixelChain);
        }

        return length;
    }

    @Override
    public int getStartIndex(PixelChain pPixelChain) {
        return mStart.getPixelIndex();
    }

    public double getStartPosition() {
        return mStartPosition;
    }

    @Override
    public Line getStartTangent(PixelChain pPixelChain) {
        return new Line(getStartUHVWPoint(pPixelChain), getStartUHVWPoint(pPixelChain).add(getStartTangentVector(pPixelChain)));
    }

    @Override
    public Point getStartUHVWPoint(PixelChain pPixelChain) {
        return mStart.getUHVWPoint(pPixelChain);
    }

    @Override
    public IVertex getStartVertex(PixelChain pPixelChain) {
        return mStart;
    }

    @Override
    public void graffiti(PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffiitLine(getStartUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain));
    }

    public double length() {
        throw new UnsupportedOperationException();
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


    @Override
    public void setStartPosition(PixelChain pPixelChain, final double pStartPosition) {
        mStartPosition = pStartPosition;
    }

    @Override
    public String toString() {
        return "SegmentBase[" + mStart + "," + mEnd + "]";
    }

    @Override
    public void vertexChange(PixelChain pPixelChain, final IVertex pVertex) {
        // TODO not sure that we need this call
        // throw new RuntimeException("need to do this in PixelMap");
        // final Date start = new Date();
        //getPixelMap().indexSegments();
        // final long time = new Date().getTime() - start.getTime();
        // mLogger.severe("Vertex time change " + time / 1000.0);
    }
}
