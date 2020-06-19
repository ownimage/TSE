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
    private  @NotNull Point mPosition;

    public Vertex(int pVertexIndex, int pPixelIndex, @NotNull Point pPosition) {
        mVertexIndex = pVertexIndex;
        mPixelIndex = pPixelIndex;
        mPosition = pPosition;
    }

    @Override
    public int getVertexIndex() {
        return mVertexIndex;
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
//        if (mPosition == null) {
//            mPosition = getPixel(pPixelChain).getUHVWMidPoint(pPixelMap);
//        }
        return mPosition;
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
