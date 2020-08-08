package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.segment.ISegmentGrafittiHelper;
import lombok.val;

import java.awt.*;
import java.io.Serializable;

public interface Segment extends Serializable {

    int getSegmentIndex();

    double getStartPosition();

    default double calcError(PixelMap pPixelMap, IPixelChain pPixelChain) {
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

    double getLength(PixelMap pPixelMap, IPixelChain pPixelChain);

    Point getPointFromLambda(PixelMap pPixelMap, IPixelChain pPixelChain, double pT);

    Segment withStartPosition(double pStartPosition);

    Segment toImmutable();

    boolean closerThanActual(PixelMap pPixelMap, IPixelChain pPixelChain, IPixelMapTransformSource pTransformSource, Point pPoint, double pMultiplier);

    double closestLambda(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint);

    Segment withSegmentIndex(int segmentIndex);

    default double calcError(PixelMap pPixelMap, IPixelChain pPixelChain, Pixel pPixel) {
        val uhvw = pPixel.getUHVWMidPoint(pPixelMap.height());
        val distance = distance(pPixelMap, pPixelChain, uhvw);
        return distance * distance;
    }

    default boolean closerThan(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint, double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint);

    default double getActualThickness(IPixelMapTransformSource pSource, IPixelChain pPixelChain, double pPosition) {
        return pPixelChain.getActualThickness(pSource, pPosition);
    }

    default int getEndIndex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex() + 1).getPixelIndex();
    }

    default Line getEndTangent(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return new Line(getEndUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain).add(getEndTangentVector(pPixelMap, pPixelChain)));
    }

    default Point getEndUHVWPoint(IPixelChain pPixelChain) {
        return getEndVertex(pPixelChain).getPosition();
    }

    default Vertex getEndVertex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex() + 1);
    }

    double getMaxX(PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain);

    default int getPixelLength(IPixelChain pPixelChain) {
        int length;

        if (getStartIndex(pPixelChain) == 0) {
            length = 1 + getEndIndex(pPixelChain);
        } else {
            length = getEndIndex(pPixelChain) - getStartIndex(pPixelChain);
        }

        return length;
    }

    Vector getEndTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain);

    default int getStartIndex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex()).getPixelIndex();
    }

    Vector getStartTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain);

    default Line getStartTangent(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return new Line(
                getStartUHVWPoint(pPixelChain),
                getStartUHVWPoint(pPixelChain).add(getStartTangentVector(pPixelMap, pPixelChain))
        );
    }

    default Point getStartUHVWPoint(IPixelChain pPixelChain) {
        return getStartVertex(pPixelChain).getPosition();
    }

    default Vertex getStartVertex(IPixelChain pPixelChain) {
        return pPixelChain.getVertex(getSegmentIndex());
    }

    default void graffiti(PixelMap pPixelMap, IPixelChain pPixelChain, ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffitiLine(getStartUHVWPoint(pPixelChain), getEndUHVWPoint(pPixelChain), Color.GREEN);
    }

    default boolean noPixelFurtherThan(PixelMap pPixelMap, IPixelChain pPixelChain, double pDistance) {
        for (int i = getStartIndex(pPixelChain); i <= getEndIndex(pPixelChain); i++) {
            Point uhvw = pPixelChain.getUHVWPoint(pPixelMap, i);
            if (distance(pPixelMap, pPixelChain, uhvw) > pDistance) {
                return false;
            }
        }
        return true;
    }

    default Segment getNextSegment(IPixelChain pixelChain) {
        return pixelChain.getSegment(getEndVertex(pixelChain).getVertexIndex());
    }

    default Segment getPreviousSegment(IPixelChain pixelChain) {
        return pixelChain.getSegment(getEndVertex(pixelChain).getVertexIndex() - 1);
    }

    default boolean containsPixelIndex(IPixelChain pPixelChain, int pIndex) {
        return getStartIndex(pPixelChain) <= pIndex && pIndex <= getEndIndex(pPixelChain);
    }
}
