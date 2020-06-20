/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.ImmutablePixelChainContext;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.Services;
import lombok.val;

import java.awt.*;
import java.util.logging.Logger;

public abstract class SegmentBase implements ISegment {

    private final static Logger mLogger = Framework.getLogger();
    private final static long serialVersionUID = 1L;

    private final int mSegmentIndex;
    private final double mStartPosition;

    public SegmentBase(int pSegmentIndex) {
        this(pSegmentIndex, 0.0d);
    }

    SegmentBase(int pSegmentIndex, double pStartPosition) {
        mSegmentIndex = pSegmentIndex;
        mStartPosition = pStartPosition;
    }

    @Override
    public double calcError(PixelMap pPixelMap, IPixelChain pPixelChain) {
        double error = 0.0d;
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            Point uhvw = pPixelChain.getUHVWPoint(pPixelMap, i);
            double distance = distance(pPixelMap, pPixelChain, uhvw);

            error += distance * distance;
        }

        if (error < 0.0d) {
            mLogger.warning("-ve error");
        }

        return error;
    }

    @Override
    public double calcError(PixelMap pPixelMap, IPixelChain pPixelChain, Pixel pPixel) {
        val uhvw = pPixel.getUHVWMidPoint(pPixelMap);
        val distance = distance(pPixelMap, pPixelChain, uhvw);
        return distance * distance;
    }

    @Override
    public int getSegmentIndex() {
        return mSegmentIndex;
    }

    @Override
    public boolean closerThan(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint, double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public abstract double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint);

    double getActualThickness(IPixelMapTransformSource pSource, IPixelChain pPixelChain, double pPosition) {
        return pPixelChain.getActualThickness(pSource, pPosition);
    }

    @Override
    public int getEndIndex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex + 1).getPixelIndex();
    }

    @Override
    public Line getEndTangent(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return new Line(getEndUHVWPoint(pPixelMap, pPixelChain), getEndUHVWPoint(pPixelMap, pPixelChain).add(getEndTangentVector(pPixelMap, pPixelChain)));
    }

    @Override
    public Point getEndUHVWPoint(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getEndVertex(pPixelChain).getPosition();
    }

    @Override
    public IVertex getEndVertex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex + 1);
    }

    @Override
    public double getMaxX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPixelLength(IPixelChain pPixelChain) {
        int length;

        if (getStartIndex(pPixelChain) == 0) {
            length = 1 + getEndIndex(pPixelChain);
        } else {
            length = getEndIndex(pPixelChain) - getStartIndex(pPixelChain);
        }

        return length;
    }

    @Override
    public int getStartIndex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex).getPixelIndex();
    }

    double getStartPosition() {
        return mStartPosition;
    }

    @Override
    public Line getStartTangent(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return new Line(
                getStartUHVWPoint(pPixelMap, pPixelChain),
                getStartUHVWPoint(pPixelMap, pPixelChain).add(getStartTangentVector(pPixelMap, pPixelChain))
        );
    }

    @Override
    public Point getStartUHVWPoint(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getPosition();
    }

    @Override
    public IVertex getStartVertex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(mSegmentIndex);
    }

    @Override
    public void graffiti(PixelMap pPixelMap, IPixelChain pPixelChain, ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffitiLine(getStartUHVWPoint(pPixelMap, pPixelChain), getEndUHVWPoint(pPixelMap, pPixelChain), Color.GREEN);
    }

    public boolean noPixelFurtherThan(PixelMap pPixelMap, IPixelChain pPixelChain, double pDistance) {
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            Point uhvw = pPixelChain.getUHVWPoint(pPixelMap, i);
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
    public ISegment getNextSegment(IPixelChain pPixelChain) {
        var services = Services.getDefaultServices();
        var context = ImmutablePixelChainContext.of(null, pPixelChain);
        return services.getVertexService().getEndSegment(services, context, getEndVertex(pPixelChain));
    }

    @Override
    public ISegment getPreviousSegment(IPixelChain pPixelChain) {
        var services = Services.getDefaultServices();
        var context = ImmutablePixelChainContext.of(null, pPixelChain);
        return services.getVertexService().getStartSegment(services, context, getEndVertex(pPixelChain));
    }

    @Override
    public boolean containsPixelIndex(IPixelChain pPixelChain, int pIndex) {
        return getStartIndex(pPixelChain) <= pIndex && pIndex <= getEndIndex(pPixelChain);
    }
}
