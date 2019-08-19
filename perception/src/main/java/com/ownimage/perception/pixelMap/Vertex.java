/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.segment.ISegment;
import lombok.val;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * The Vertex class, this class is immutable. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements IVertex {


    private final static Logger mLogger = Framework.getLogger();
    private static final long serialVersionUID = 1L;

    private final int mVertexIndex;
    private final int mPixelIndex;
    private final Point mPosition;

    private Vertex(final int pVertexIndex, final int pPixelIndex, final Point pPosition) {
        mVertexIndex = pVertexIndex;
        mPixelIndex = pPixelIndex;
        mPosition = pPosition;
    }

    public static Vertex createVertex(final IPixelChain pPixelChain, final int pVertexIndex, final int pPixelIndex) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelCount()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelCount() + ")");
        }

        return new Vertex(pVertexIndex, pPixelIndex, null);
    }

    public static Vertex createVertex(final IPixelChain pPixelChain, final int pVertexIndex, final int pPixelIndex, final Point pPosition) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelCount()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelCount() + ")");
        }

        return new Vertex(pVertexIndex, pPixelIndex, pPosition);
    }

    @Override
    public int getVertexIndex() {
        return mVertexIndex;
    }

    @Override
    public boolean isPositionSpecified() {
        return mPosition != null;
    }

    /**
     * Calc tangent always generates a tangent line that goes in the direction of start to finish.
     *  @param pPixelChain the Pixel Chain performing this operation
     * @param pPixelMap
     */
    @Override
    public Line calcTangent(final IPixelChain pPixelChain, final PixelMap pPixelMap) {
        Line tangent;
        ISegment startSegment = getStartSegment(pPixelChain);
        ISegment endSegment = getEndSegment(pPixelChain);

        if (startSegment == null && endSegment == null) {
            tangent = null;

        } else if (startSegment == null) {
            tangent = endSegment.getStartTangent(pPixelMap, pPixelChain);
            tangent = tangent.getReverse();

        } else if (endSegment == null) {
            tangent = startSegment.getEndTangent(pPixelMap, pPixelChain);

        } else {
            return calcTangent(
                    getUHVWPoint(pPixelMap, pPixelChain),
                    startSegment.getEndTangent(pPixelMap, pPixelChain),
                    endSegment.getStartTangent(pPixelMap, pPixelChain)
            );
        }
        return tangent;
    }

    public static Line calcTangent(final Point pPoint, final Line pStartTangent, final Line pEndTangent) {
        final Point startTangentPoint = pEndTangent.getPoint(1.0d);
        final Point endTangentPoint = pStartTangent.getPoint(1.0d);
        final Vector tangentVector = startTangentPoint.minus(endTangentPoint).normalize();

        return new Line(pPoint, pPoint.add(tangentVector));
    }

    /**
     * Calculates an approximate tangent line to the PixelChain at this point.  This is done by counting forward and
     * backwards pLength pixels and calculating a vector between these, this vector is added to the UHVW point that
     * represents this Vertex to generate a tangent.
     *
     * @param pPixelMap
     * @param pPixelChain
     * @param pLength
     * @return
     */
    @Override
    public Line calcLocalTangent(final PixelMap pPixelMap, final IPixelChain pPixelChain, final int pLength) {
        val ltStartIndex = KMath.max(getPixelIndex() - pLength, 0);
        val ltEndIndex = KMath.min(getPixelIndex() + pLength, pPixelChain.getMaxPixelIndex());
        val ltStartPoint = pPixelChain.getUHVWPoint(pPixelMap, ltStartIndex);
        val ltEndPoint = pPixelChain.getUHVWPoint(pPixelMap, ltEndIndex);
        val tangentDirection = ltEndPoint.minus(ltStartPoint).normalize();
        val thisPosition = getUHVWPoint(pPixelMap, pPixelChain);
        return new Line(thisPosition, thisPosition.add(tangentDirection));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final IVertex pOther) {
        return mPixelIndex - pOther.getPixelIndex();
    }


    @Override
    public ISegment getEndSegment(final IPixelChain pPixelChain) {
        return pPixelChain.getSegment(mVertexIndex);
    }

    @Override
    public int getPixelIndex() {
        return mPixelIndex;
    }

    @Override
    public Pixel getPixel(final IPixelChain pPixelChain) {
        return pPixelChain.getPixel(mPixelIndex);
    }

    @Override
    public ISegment getStartSegment(final IPixelChain pPixelChain) {
        return pPixelChain.getSegment(mVertexIndex - 1);
    }

    @Override
    public Point getUHVWPoint(final PixelMap pPixelMap, final IPixelChain pPixelChain) {
        return mPosition != null ? mPosition : getPixel(pPixelChain).getUHVWMidPoint(pPixelMap);
    }

    @Override
    public IVertex withPosition(IPixelChain pPixelChain, Point pPosition) {
        return createVertex(pPixelChain, mVertexIndex, mPixelIndex, pPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return mVertexIndex == vertex.mVertexIndex &&
                mPixelIndex == vertex.mPixelIndex &&
                Objects.equals(mPosition, vertex.mPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mVertexIndex, mPixelIndex, mPosition);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder()
                .append("Vertex { ")
                .append("vertexIndex: ").append(mVertexIndex)
                .append(", pixelIndex: ").append(mPixelIndex);

        if (mPosition != null) {
            sb.append(", position: ").append(mPosition);
        }

        sb.append(" }");

        return sb.toString();
    }

}
