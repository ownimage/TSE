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
     * This returns the sum of the distances that all of the pixels in the original IPixelChain (covered by this segment) are.
     *
     * @return the sum of the distances.
     */
    double calcError(final PixelMap pPixelMap, final IPixelChain pPixelChain);

    boolean closerThanActual(final PixelMap pPixelMap, final IPixelChain pPixelChain, final IPixelMapTransformSource pTransformSource, Point pPoint, double pMultiplier);

    double calcError(PixelMap pPixelMap, IPixelChain pPixelChain, Pixel pPixel);

    int getSegmentIndex();

    boolean closerThan(final PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint, double pTolerance);

    double closestLambda(final PixelMap pPixelMap, IPixelChain pPixelChain, final Point pPoint);

    double distance(final PixelMap pPixelMap, IPixelChain pPixelChain, final Point pUVHWPoint);

    /**
     * Gets the end index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end index
     */
    int getEndIndex(IPixelChain pPixelChain);

    Line getEndTangent(final PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the end tangent.
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end tangent. This is a vector that points along the tangent to a point beyond the end, i.e. towards a point that it would join with.
     */
    Vector getEndTangentVector(final PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the end uhvw point.
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end uhvw point
     */
    Point getEndUHVWPoint(final PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the end Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end Vertex
     */
    IVertex getEndVertex(IPixelChain pPixelChain);

    double getLength(final PixelMap pPixelMap, IPixelChain pPixelChain);

    int getPixelLength(final IPixelChain pPixelChain);

    Point getPointFromLambda(final PixelMap pPixelMap, IPixelChain pPixelChain, double pT);

    /**
     * Gets the start index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start index
     */
    int getStartIndex(IPixelChain pPixelChain);

    Line getStartTangent(final PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the start tangent. This is a vector that points along the tangent to a point before the start, i.e. towards a point that it would join with.
     *
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @param pPixelChain
     * @return the start tangent
     */
    Vector getStartTangentVector(final PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the start uhvw point.
     *
     *
     * @param pPixelMap the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start uhvw point
     */
    Point getStartUHVWPoint(final PixelMap pPixelMap, IPixelChain pPixelChain);

    /**
     * Gets the start Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start Vertex
     */
    IVertex getStartVertex(IPixelChain pPixelChain);

    void graffiti(final PixelMap pPixelMap, IPixelChain pPixelChain, ISegmentGrafittiHelper pGraphics);

    double getMaxX(final PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMaxY(final PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMinX(final PixelMap pPixelMap, IPixelChain pPixelChain);

    double getMinY(final PixelMap pPixelMap, IPixelChain pPixelChain);

    ISegment withStartPosition(double pStartPosition);

    ISegment getNextSegment(final IPixelChain pPixelChain);

    ISegment getPreviousSegment(final IPixelChain pPixelChain);

    boolean containsPixelIndex(final IPixelChain pPixelChain, int pIndex);
}
