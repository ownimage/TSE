package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.ISegmentGrafittiHelper;
import com.ownimage.perception.pixelMap.segment.StraightSegment;

interface ISegmentService {


    void graffiti(
            PixelChainContext pixelChainContext, StraightSegment straightSegment, ISegmentGrafittiHelper pGraphics
    );


    boolean closerThanActual(
            PixelChainContext pixelChainContext, StraightSegment straightSegment, IPixelMapTransformSource pTransformSource,
            Point pPoint,
            double pMultiplier
    );


    boolean closerThan(
            PixelChainContext pixelChainContext, StraightSegment straightSegment, Point point,
            double tolerance
    );

    double closestLambda(PixelChainContext pixelChainContext, StraightSegment straightSegment, Point point);


    double distance(PixelChainContext pixelChainContext, StraightSegment straightSegment, Point pUVHWPoint);


    Vector getEndTangentVector(PixelMap pixelMap, StraightSegment straightSegment, IPixelChain pPixelChain);


    double getLength(PixelChainContext pixelChainContext, StraightSegment straightSegment);

    double getMaxX(PixelMap pixelMap, StraightSegment straightSegment, IPixelChain pPixelChain);


    double getMaxY(PixelMap pixelMap, StraightSegment straightSegment, IPixelChain pPixelChain);


    double getMinX(PixelMap pixelMap, StraightSegment straightSegment, IPixelChain pPixelChain);


    double getMinY(PixelMap pixelMap, StraightSegment straightSegment, IPixelChain pPixelChain);


    Point getPointFromLambda(PixelChainContext pixelChainContext, StraightSegment straightSegment, double lambda);


    Vector getStartTangentVector(PixelMap pixelMap, StraightSegment straightSegment, IPixelChain pPixelChain);


    String toString();


    StraightSegment withStartPosition(double pStartPosition);
}
