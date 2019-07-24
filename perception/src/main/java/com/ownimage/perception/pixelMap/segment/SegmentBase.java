/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import lombok.val;

import java.util.logging.Logger;

public abstract class SegmentBase implements ISegment {

    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final int mSegmentIndex;
    private final double mStartPosition;

    public SegmentBase(final int pSegmentIndex) {
        this(pSegmentIndex, 0.0d);
    }

    public SegmentBase(final int pSegmentIndex, final double pStartPosition) {
        mSegmentIndex = pSegmentIndex;
        mStartPosition = pStartPosition;
    }

    @Override
    public double calcError(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        double error = 0.0d;
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            final Point uhvw = pPixelChain.getUHVWPoint(i, pPixelMap);
            final double distance = distance(pPixelMap, pPixelChain, uhvw);

            error += distance * distance;
        }

        if (error < 0.0d) {
            mLogger.warning("-ve error");
        }

        return error;
    }

    @Override
    public double calcError(final PixelMap pPixelMap, final PixelChain pPixelChain, final Pixel pPixel) {
        val uhvw = pPixel.getUHVWMidPoint(pPixelMap);
        val distance = distance(pPixelMap, pPixelChain, uhvw);
        return distance * distance;
    }

    @Override
    public int getSegmentIndex() {
        return mSegmentIndex;
    }

    public boolean closerThan(final PixelMap pPixelMap, final PixelChain pPixelChain, final Point pPoint, final double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    public abstract double distance(final PixelMap pPixelMap, PixelChain pPixelChain, final Point pUVHWPoint);

    double getActualThickness(final IPixelMapTransformSource pSource, final PixelChain pPixelChain, final double pPosition) {
        return pPixelChain.getActualThickness(pSource, pPosition);
    }

    @Override
    public int getEndIndex(final PixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex + 1).getPixelIndex();
    }

    @Override
    public Line getEndTangent(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return new Line(getEndUHVWPoint(pPixelMap, pPixelChain), getEndUHVWPoint(pPixelMap, pPixelChain).add(getEndTangentVector(pPixelMap, pPixelChain)));
    }

    @Override
    public Point getEndUHVWPoint(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return getEndVertex(pPixelChain).getUHVWPoint(pPixelMap, pPixelChain);
    }

    @Override
    public IVertex getEndVertex(final PixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex + 1);
    }

    public double getMaxX(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMaxY(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinX(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinY(final PixelMap pPixelMap, final PixelChain pPixelChain) {
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
    public Line getStartTangent(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return new Line(
                getStartUHVWPoint(pPixelMap, pPixelChain),
                getStartUHVWPoint(pPixelMap, pPixelChain).add(getStartTangentVector(pPixelMap, pPixelChain))
        );
    }

    @Override
    public Point getStartUHVWPoint(final PixelMap pPixelMap, final PixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getUHVWPoint(pPixelMap, pPixelChain);
    }

    @Override
    public IVertex getStartVertex(final PixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex);
    }

    @Override
    public void graffiti(final PixelMap pPixelMap, final PixelChain pPixelChain, final ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffitiLine(getStartUHVWPoint(pPixelMap, pPixelChain), getEndUHVWPoint(pPixelMap, pPixelChain));
    }

    public boolean noPixelFurtherThan(final PixelMap pPixelMap, final PixelChain pPixelChain, final double pDistance) {
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            final Point uhvw = pPixelChain.getUHVWPoint(i, pPixelMap);
            if (distance(pPixelMap, pPixelChain, uhvw) > pDistance) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "SegmentBase[" + mSegmentIndex + "]";
    }

    @Override
    public ISegment getNextSegment(final PixelChain pPixelChain) {
        return getEndVertex(pPixelChain).getEndSegment(pPixelChain);
    }

    @Override
    public ISegment getPreviousSegment(final PixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getStartSegment(pPixelChain);
    }

    @Override
    public boolean containsPixelIndex(final PixelChain pPixelChain, final int pIndex) {
        return getStartIndex(pPixelChain) <= pIndex && pIndex <= getEndIndex(pPixelChain);
    }
}
