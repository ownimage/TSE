/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap.segment;

import java.io.Serializable;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Path;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.math.ITestableLine;
import com.ownimage.perception.math.Intersect3D;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.SegmentFactory.SegmentType;

public interface ISegment extends ITestableLine, Serializable {

    public final static Version mVersion = new Version(4, 0, 1, "2014/05/30 20:48");
    public final static String mClassname = ISegment.class.getName();
    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public void addToPath(Path pPath);

    /**
     * Attach to vertexes. A number of segments might be defines as being between the same two vertexes, this is done to see which one of the segments most closely approximates the underlying pixels.
     * When one is finally chosen as the best this segment needs to be attached to the its vertexes, or more precisely the vertexes need to be updated to they attach to this segment.
     */
    public void attachToVertexes(boolean pReCalcSegments);

    /**
     * This returns the sum of the distances that all of the pixels in the original PixelChain (covered by this segment) are.
     *
     * @return the sum of the distances.
     */
    public double calcError();

    public boolean closerThan(Point pPoint);

    public double closestLambda(final Point pPoint);

    public Point closestPoint(Point pUhvw);

    public ISegment copy(IVertex pStartVertex, IVertex pEndVertex);

    public ISegment deepCopy(IVertex pOriginalPCStartVertex, IVertex pCopyPCStartVertex, final IVertex pSegmentStartVertex);

    public void delete();

    /**
     * This returns the Point that is the mean of all of the Points that correspond to the Pixels in the Pixel chain that are approximated by this Segment.
     *
     * @return the average Point
     */
    public Point getAveragePoint();

    public Point getControlPoint();

    /**
     * Gets the end index (into the mPixels) of this segment.
     *
     * @return the end index
     */
    public int getEndIndex();

    /**
     * Gets the end Pixel of this segment.
     *
     * @return the end Pixel
     */
    public Pixel getEndPixel();

    public Line getEndTangent();

    /**
     * Gets the end tangent.
     *
     * @return the end tangent. This is a vector that points along the tangent to a point beyond the end, i.e. towards a point that it would join with.
     */
    public Vector getEndTangentVector();

    /**
     * Gets the end uhvw point.
     *
     * @return the end uhvw point
     */
    public Point getEndUHVWPoint();

    /**
     * Gets the end Vertex of this segment.
     *
     * @return the end Vertex
     */
    public IVertex getEndVertex();

    public double getLength();

    public PixelChain getPixelChain();

    /**
     * Pixel length is defined as the number of pixels represented by this Segment. It is defined as: if the startIndex is 0 then (1 + endIndex), else (endIndex - startIndex). This definition means
     * that if you add all the pixelLenghs of the segments that approximate a PixelChain then you get the length of the chain.
     *
     * @return the length in Pixels
     */
    public int getPixelLength();

    public PixelMap getPixelMap();

    public Point getPointFromLambda(double pT);

    public SegmentType getSegmentType();

    /**
     * Gets the start index (into the mPixels) of this segment.
     *
     * @return the start index
     */
    public int getStartIndex();

    /**
     * Gets the start Pixel of this segment.
     *
     * @return the start Pixel
     */
    public Pixel getStartPixel();

    public double getStartPosition();

    public Line getStartTangent();

    /**
     * Gets the start tangent. This is a vector that points along the tangent to a point before the start, i.e. towards a point that it would join with.
     *
     * @return the start tangent
     */
    public Vector getStartTangentVector();

    /**
     * Gets the start uhvw point.
     *
     * @return the start uhvw point
     */
    public Point getStartUHVWPoint();

    /**
     * Gets the start Vertex of this segment.
     *
     * @return the start Vertex
     */
    public IVertex getStartVertex();

    public void graffiti(ISegmentGrafittiHelper pGraphics);

    public Intersect3D intersect3D(Point pUhvw);

    public void nextControlPoint();

    /**
     * Checks each pixel in the original PixelChain to see if it is further than pDistance away. Note that all the checks are done in the UHVW format. The check starts at the mStart end and works
     * forwards.
     *
     * @param pDistance the distance
     * @return returns false the first time that a pixel fails the test. It will return true if all pixels are closer than mDistance
     */
    public boolean noPixelFurtherThan(double pDistance);

    /**
     * Checks each pixel in the original PixelChain to see if it is further than mDistance away. Note that all the checks are done in the UHVW format. The check starts at the mEnd end and works
     * backwards. This is included so the the user has the chance to influence the performance if they can determine which end is likely to fail quickest.
     *
     * @param pDistance the distance
     * @return returns false the first time that a pixel fails the test. It will return true if all pixels are closer than mDistance
     */
    public boolean noPixelFurtherThanReverse(double pDistance);

    public void previousControlPoint();

    public void setControlPoint(Point pPoint);

    public void setStartPosition(double pStartPosition);

    public void vertexChange(IVertex pVertex);

}
