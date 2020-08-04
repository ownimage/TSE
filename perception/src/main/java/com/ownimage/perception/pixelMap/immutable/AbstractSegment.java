/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.segment.ISegmentGrafittiHelper;
import lombok.val;

import java.awt.*;
import java.io.Serializable;

public abstract class AbstractSegment implements Serializable, Cloneable {

    public abstract int getSegmentIndex();

    public abstract double getStartPosition();

    public double calcError(PixelMap pPixelMap, IPixelChain pPixelChain) {
        double error = 0.0d;
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            Point uhvw = pPixelChain.getUHVWPoint(pPixelMap, i);
            double distance = distance(pPixelMap, pPixelChain, uhvw);

            error += distance * distance;
        }

//        if (error < 0.0d) {
//            mLogger.warning("-ve error");
//        }

        return error;
    }


    @Override
    public String toString() {
        return "SegmentBase[" + getSegmentIndex() + "]";
    }

    public abstract double getLength(PixelMap pPixelMap, IPixelChain pPixelChain);

    public abstract Point getPointFromLambda(PixelMap pPixelMap, IPixelChain pPixelChain, double pT);

    public abstract AbstractSegment withStartPosition(double pStartPosition);

    public abstract boolean closerThanActual(PixelMap pPixelMap, IPixelChain pPixelChain, IPixelMapTransformSource pTransformSource, Point pPoint, double pMultiplier);

    public abstract double closestLambda(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint);

    public abstract AbstractSegment withSegmentIndex(int segmentIndex);

    public double calcError(PixelMap pPixelMap, IPixelChain pPixelChain, Pixel pPixel) {
        val uhvw = pPixel.getUHVWMidPoint(pPixelMap.height());
        val distance = distance(pPixelMap, pPixelChain, uhvw);
        return distance * distance;
    }

    public boolean closerThan(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint, double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    public abstract double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint);

    public double getActualThickness(IPixelMapTransformSource pSource, IPixelChain pPixelChain, double pPosition) {
        return pPixelChain.getActualThickness(pSource, pPosition);
    }

    public int getEndIndex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex() + 1).getPixelIndex();
    }

    public Line getEndTangent(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return new Line(getEndUHVWPoint(pPixelMap, pPixelChain), getEndUHVWPoint(pPixelMap, pPixelChain).add(getEndTangentVector(pPixelMap, pPixelChain)));
    }

    public Point getEndUHVWPoint(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getEndVertex(pPixelChain).getPosition();
    }

    public Vertex getEndVertex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex() + 1);
    }

    public double getMaxX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        throw new UnsupportedOperationException();
    }

    public int getPixelLength(IPixelChain pPixelChain) {
        int length;

        if (getStartIndex(pPixelChain) == 0) {
            length = 1 + getEndIndex(pPixelChain);
        } else {
            length = getEndIndex(pPixelChain) - getStartIndex(pPixelChain);
        }

        return length;
    }

    public abstract Vector getEndTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain);

    public int getStartIndex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex()).getPixelIndex();
    }

    public abstract Vector getStartTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain);

    public Line getStartTangent(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return new Line(
                getStartUHVWPoint(pPixelMap, pPixelChain),
                getStartUHVWPoint(pPixelMap, pPixelChain).add(getStartTangentVector(pPixelMap, pPixelChain))
        );
    }

    public Point getStartUHVWPoint(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getPosition();
    }

    public Vertex getStartVertex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex());
    }

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

    public AbstractSegment getNextSegment(IPixelChain pixelChain) {
        return pixelChain.getSegment(getEndVertex(pixelChain).getVertexIndex());
    }

    public AbstractSegment getPreviousSegment(IPixelChain pixelChain) {
        return pixelChain.getSegment(getEndVertex(pixelChain).getVertexIndex() - 1);
    }


    public boolean containsPixelIndex(IPixelChain pPixelChain, int pIndex) {
        return getStartIndex(pPixelChain) <= pIndex && pIndex <= getEndIndex(pPixelChain);
    }

}
