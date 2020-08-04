/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.immutable.VertexData;

import java.io.Serializable;

public interface ISegment extends Serializable, Cloneable {


    long serialVersionUID = 1L;

    /**
     * This returns the sum of the distances that all of the pixels in the original IPixelChain (covered by this segment) are.
     *
     * @return the sum of the distances.
     */
    double calcError(PixelMap pPixelMap, IPixelChain pPixelChain);

    boolean closerThanActual(PixelMap pPixelMap, IPixelChain pPixelChain, IPixelMapTransformSource pTransformSource, Point pPoint, double pMultiplier);

    double calcError(PixelMap pPixelMap, IPixelChain pPixelChain, Pixel pPixel);

    int getSegmentIndex();

    boolean closerThan(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint, double pTolerance);

    double closestLambda(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint);

    double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint);

    /**
     * Gets the end index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end index
     */
    int getEndIndex(IPixelChain pPixelChain);

    Line getEndTangent(PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the end tangent.
     *
     * @param pPixelMap   the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end tangent. This is a vector that points along the tangent to a point beyond the end, i.e. towards a point that it would join with.
     */
    Vector getEndTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the end uhvw point.
     *
     * @param pPixelMap   the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end uhvw point
     */
    Point getEndUHVWPoint(PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the end Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end Vertex
     */
    VertexData getEndVertex(IPixelChain pPixelChain);

    double getLength(PixelMap pPixelMap, IPixelChain pPixelChain);

    int getPixelLength(IPixelChain pPixelChain);

    Point getPointFromLambda(PixelMap pPixelMap, IPixelChain pPixelChain, double pT);

    /**
     * Gets the start index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start index
     */
    int getStartIndex(IPixelChain pPixelChain);

    Line getStartTangent(PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the start tangent. This is a vector that points along the tangent to a point before the start, i.e. towards a point that it would join with.
     *
     * @param pPixelMap   the PixelMap performing the this operation
     * @param pPixelChain the owning PixelChain
     * @return the start tangent
     */
    Vector getStartTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the start uhvw point.
     *
     * @param pPixelMap   the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start uhvw point
     */
    Point getStartUHVWPoint(PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the start Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start Vertex
     */
    VertexData getStartVertex(IPixelChain pPixelChain);

    void graffiti(PixelMap pPixelMap, IPixelChain pPixelChain, ISegmentGrafittiHelper pGraphics);

    double getMaxX(PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain);

    ISegment withStartPosition(double pStartPosition);

    double getStartPosition();

    ISegment withSegmentIndex(int segmentIndex);

    ISegment getNextSegment(IPixelChain pPixelChain);

    ISegment getPreviousSegment(IPixelChain pPixelChain);

    boolean containsPixelIndex(IPixelChain pPixelChain, int pIndex);
}
