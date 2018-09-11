/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.segment;

import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.PixelChain;

import java.io.Serializable;

public interface ISegment extends Serializable {


    long serialVersionUID = 1L;

    /**
     * This returns the sum of the distances that all of the pixels in the original PixelChain (covered by this segment) are.
     *
     * @return the sum of the distances.
     */
    double calcError(final PixelChain pPixelChain);

    boolean closerThanActual(final PixelChain pPixelChain, Point pPoint, double pMultiplier);

    int getSegmentIndex();

    boolean closerThan(PixelChain pPixelChain, Point pPoint, double pTolerance);

    double closestLambda(final Point pPoint, PixelChain pPixelChain);

    /**
     * Gets the end index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end index
     */
    int getEndIndex(PixelChain pPixelChain);

    Line getEndTangent(PixelChain pPixelChain);

    /**
     * Gets the end tangent.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end tangent. This is a vector that points along the tangent to a point beyond the end, i.e. towards a point that it would join with.
     */
    Vector getEndTangentVector(PixelChain pPixelChain);

    /**
     * Gets the end uhvw point.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end uhvw point
     */
    Point getEndUHVWPoint(PixelChain pPixelChain);

    /**
     * Gets the end Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the end Vertex
     */
    IVertex getEndVertex(PixelChain pPixelChain);

    double getLength(PixelChain pPixelChain);

    int getPixelLength(PixelChain pPixelChain);

    Point getPointFromLambda(PixelChain pPixelChain, double pT);

    /**
     * Gets the start index (into the mPixels) of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start index
     */
    int getStartIndex(PixelChain pPixelChain);

    Line getStartTangent(PixelChain pPixelChain);

    /**
     * Gets the start tangent. This is a vector that points along the tangent to a point before the start, i.e. towards a point that it would join with.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start tangent
     */
    Vector getStartTangentVector(PixelChain pPixelChain);

    /**
     * Gets the start uhvw point.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start uhvw point
     */
    Point getStartUHVWPoint(PixelChain pPixelChain);

    /**
     * Gets the start Vertex of this segment.
     *
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the start Vertex
     */
    IVertex getStartVertex(PixelChain pPixelChain);

    void graffiti(PixelChain pPixelChain, ISegmentGrafittiHelper pGraphics);

    void setStartPosition(PixelChain pPixelChain, double pStartPosition);

    double getMaxX(PixelChain pPixelChain);

    double getMaxY(PixelChain pPixelChain);

    double getMinX(PixelChain pPixelChain);

    double getMinY(PixelChain pPixelChain);

}
