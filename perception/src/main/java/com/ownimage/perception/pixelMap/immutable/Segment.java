package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.segment.ISegmentGrafittiHelper;

import java.awt.*;
import java.io.Serializable;

public interface Segment extends Serializable {

    int getSegmentIndex();

    double getStartPosition();

    default double calcError(PixelMap pPixelMap, PixelChain pPixelChain) {
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

    double getLength(PixelMap pPixelMap, PixelChain pPixelChain);

    Point getPointFromLambda(PixelMap pPixelMap, PixelChain pPixelChain, double pT);

    Segment withStartPosition(double pStartPosition);

    Segment toImmutable();

    boolean closerThanActual(PixelMap pPixelMap, PixelChain pPixelChain, IPixelMapTransformSource pTransformSource, Point pPoint, double pMultiplier);

    double closestLambda(PixelMap pPixelMap, PixelChain pPixelChain, Point pPoint);

    Segment withSegmentIndex(int segmentIndex);

    default double calcError(PixelMap pPixelMap, PixelChain pPixelChain, Pixel pPixel) {
        var uhvw = pPixel.getUHVWMidPoint(pPixelMap.height());
        var distance = distance(pPixelMap, pPixelChain, uhvw);
        return distance * distance;
    }

    default boolean closerThan(PixelMap pPixelMap, PixelChain pPixelChain, Point pPoint, double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    double distance(PixelMap pPixelMap, PixelChain pPixelChain, Point pUVHWPoint);

    default double getActualThickness(IPixelMapTransformSource pSource, PixelChain pPixelChain, double pPosition) {
        return pPixelChain.actualThickness(pSource, pPosition);
    }

    default int getEndIndex(PixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex() + 1).getPixelIndex();
    }

    default Line getEndTangent(PixelMap pPixelMap, PixelChain pPixelChain) {
        return new Line(getEndUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain).add(getEndTangentVector(pPixelMap, pPixelChain)));
    }

    default Point getEndUHVWPoint(PixelChain pPixelChain) {
        return getEndVertex(pPixelChain).getPosition();
    }

    default Vertex getEndVertex(PixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex() + 1);
    }

    double getMaxX(PixelMap pPixelMap, PixelChain pPixelChain);

    double getMaxY(PixelMap pPixelMap, PixelChain pPixelChain);

    double getMinX(PixelMap pPixelMap, PixelChain pPixelChain);

    double getMinY(PixelMap pPixelMap, PixelChain pPixelChain);

    default int getPixelLength(PixelChain pPixelChain) {
        int length;

        if (getStartIndex(pPixelChain) == 0) {
            length = 1 + getEndIndex(pPixelChain);
        } else {
            length = getEndIndex(pPixelChain) - getStartIndex(pPixelChain);
        }

        return length;
    }

    Vector getEndTangentVector(PixelMap pPixelMap, PixelChain pPixelChain);

    default int getStartIndex(PixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex()).getPixelIndex();
    }

    Vector getStartTangentVector(PixelMap pPixelMap, PixelChain pPixelChain);

    default Line getStartTangent(PixelMap pPixelMap, PixelChain pPixelChain) {
        return new Line(
                getStartUHVWPoint(pPixelChain),
                getStartUHVWPoint(pPixelChain).add(getStartTangentVector(pPixelMap, pPixelChain))
        );
    }

    default Point getStartUHVWPoint(PixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getPosition();
    }

    default Vertex getStartVertex(PixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex());
    }

    default void graffiti(PixelMap pPixelMap, PixelChain pPixelChain, ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffitiLine(getStartUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain), Color.GREEN);
    }

    default boolean noPixelFurtherThan(PixelMap pPixelMap, PixelChain pPixelChain, double pDistance) {
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            Point uhvw = pPixelChain.getUHVWPoint(pPixelMap, i);
            if (distance(pPixelMap, pPixelChain, uhvw) > pDistance) {
                return false;
            }
        }
        return true;
    }

    default Segment getNextSegment(PixelChain pixelChain) {
        return pixelChain.getSegment(getEndVertex(pixelChain).getVertexIndex());
    }

    default Segment getPreviousSegment(PixelChain pixelChain) {
        return pixelChain.getSegment(getEndVertex(pixelChain).getVertexIndex() - 1);
    }

    default boolean containsPixelIndex(PixelChain pPixelChain, int pIndex) {
        return getStartIndex(pPixelChain) <= pIndex && pIndex <= getEndIndex(pPixelChain);
    }
}
