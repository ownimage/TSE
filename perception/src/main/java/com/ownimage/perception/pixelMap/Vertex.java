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
import com.ownimage.perception.pixelMap.segment.ISegment;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The Vertex class, this class is immutable. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements IVertex {

    private static final long serialVersionUID = 1L;

    private final int mVertexIndex;
    private final int mPixelIndex;
    private final @NotNull Point mPosition;

    public Vertex(int pVertexIndex, int pPixelIndex, @NotNull Point pPosition) {
        mVertexIndex = pVertexIndex;
        mPixelIndex = pPixelIndex;
        mPosition = pPosition;
    }

    @Override
    public int getVertexIndex() {
        return mVertexIndex;
    }



    /**
     * Calculates an approximate tangent line to the PixelChain at this point.  This is done by counting forward and
     * backwards pLength pixels and calculating a vector between these, this vector is added to the UHVW point that
     * represents this Vertex to generate a tangent.
     *
     * @param pPixelMap      the PixelMap performing this operation
     * @param pPixelChainthe PixelChain performing this operation
     * @param pLength        the length in Pixels to count each way
     * @return the calculated tangent
     */
    public Line calcLocalTangent(PixelMap pPixelMap, IPixelChain pPixelChain, int pLength) {
        val ltStartIndex = KMath.max(getPixelIndex() - pLength, 0);
        val ltEndIndex = KMath.min(getPixelIndex() + pLength, pPixelChain.getMaxPixelIndex());
        val ltStartPoint = pPixelChain.getUHVWPoint(pPixelMap, ltStartIndex);
        val ltEndPoint = pPixelChain.getUHVWPoint(pPixelMap, ltEndIndex);
        val tangentDirection = ltEndPoint.minus(ltStartPoint).normalize();
        var thisPosition = getUHVWPoint(pPixelMap, pPixelChain);
        return new Line(thisPosition, thisPosition.add(tangentDirection));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IVertex pOther) {
        return mPixelIndex - pOther.getPixelIndex();
    }


    @Override
    public ISegment getEndSegment(IPixelChain pPixelChain) {
        return pPixelChain.getSegment(mVertexIndex);
    }

    @Override
    public int getPixelIndex() {
        return mPixelIndex;
    }

    @Override
    public Pixel getPixel(IPixelChain pPixelChain) {
        return pPixelChain.getPixel(mPixelIndex);
    }

    @Override
    public ISegment getStartSegment(IPixelChain pPixelChain) {
        return pPixelChain.getSegment(mVertexIndex - 1);
    }

    @Override
    public Point getUHVWPoint(PixelMap pPixelMap, IPixelChain pPixelChain) {
        // Can not remove this lightly as it means that the existing transforms are no longer readable
        // also this might be null when it is read from a serialisation
        //noinspection ConstantConditions
        return mPosition != null ? mPosition : getPixel(pPixelChain).getUHVWMidPoint(pPixelMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vertex vertex = (Vertex) o;
        return mVertexIndex == vertex.mVertexIndex &&
                mPixelIndex == vertex.mPixelIndex &&
                Objects.equals(mPosition, vertex.mPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mVertexIndex, mPixelIndex, mPosition);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        return new StringBuilder()
                .append("Vertex { ")
                .append("vertexIndex: ").append(mVertexIndex)
                .append(", pixelIndex: ").append(mPixelIndex)
                .append(", position: ").append(mPosition)
                .append(" }")
                .toString();
    }

}
