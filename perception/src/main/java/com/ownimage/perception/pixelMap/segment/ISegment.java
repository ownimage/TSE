/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.*;

import java.io.Serializable;

public interface ISegment extends Serializable, Cloneable {


    long serialVersionUID = 1L;

    /**
     * This returns the sum of the distances that all of the pixels in the original PixelChain (covered by this segment) are.
     *
     * @return the sum of the distances.
     */
    double calcError(final PixelMap pPixelMap, final PixelChain pPixelChain);

    boolean closerThanActual(final PixelMap pPixelMap, final PixelChain pPixelChain, final IPixelMapTransformSource pTransformSource, Point pPoint, double pMultiplier);

    double calcError(PixelMap pPixelMap, PixelChain pPixelChain, Pixel pPixel);

    int getSegmentIndex();

    boolean closerThan(final PixelMap pPixelMap, PixelChain pPixelChain, Point pPoint, double pTolerance);

    double closestLambda(final Point pPoint, PixelChain pPixelChain, final PixelMap pPixelMap);

    /**
     * Gets the end index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end index
     */
    int getEndIndex(PixelChain pPixelChain);

    Line getEndTangent(final PixelMap pPixelMap, PixelChain pPixelChain);

    /**
     * Gets the end tangent.
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end tangent. This is a vector that points along the tangent to a point beyond the end, i.e. towards a point that it would join with.
     */
    Vector getEndTangentVector(final PixelMap pPixelMap, PixelChain pPixelChain);

    /**
     * Gets the end uhvw point.
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end uhvw point
     */
    Point getEndUHVWPoint(final PixelMap pPixelMap, PixelChain pPixelChain);

    /**
     * Gets the end Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end Vertex
     */
    IVertex getEndVertex(PixelChain pPixelChain);

    double getLength(final PixelMap pPixelMap, PixelChain pPixelChain);

    int getPixelLength(PixelChain pPixelChain);

    Point getPointFromLambda(final PixelMap pPixelMap, PixelChain pPixelChain, double pT);

    /**
     * Gets the start index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start index
     */
    int getStartIndex(PixelChain pPixelChain);

    Line getStartTangent(final PixelMap pPixelMap, PixelChain pPixelChain);

    /**
     * Gets the start tangent. This is a vector that points along the tangent to a point before the start, i.e. towards a point that it would join with.
     *
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @return the start tangent
     */
    Vector getStartTangentVector(final PixelMap pPixelMap, PixelChain pPixelChain);

    /**
     * Gets the start uhvw point.
     *
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start uhvw point
     */
    Point getStartUHVWPoint(final PixelMap pPixelMap, PixelChain pPixelChain);

    /**
     * Gets the start Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start Vertex
     */
    IVertex getStartVertex(PixelChain pPixelChain);

    void graffiti(final PixelMap pPixelMap, PixelChain pPixelChain, ISegmentGrafittiHelper pGraphics);

    double getMaxX(final PixelMap pPixelMap, PixelChain pPixelChain);

    double getMaxY(final PixelMap pPixelMap, PixelChain pPixelChain);

    double getMinX(final PixelMap pPixelMap, PixelChain pPixelChain);

    double getMinY(final PixelMap pPixelMap, PixelChain pPixelChain);

    ISegment withStartPosition(final PixelMap pPixelMap, PixelChain pPixelChain, double pStartPosition);

    ISegment getNextSegment(final PixelChain pPixelChain);

    ISegment getPreviousSegment(final PixelChain pPixelChain);

    boolean containsPixelIndex(final PixelChain pPixelChain, int pIndex);
}
