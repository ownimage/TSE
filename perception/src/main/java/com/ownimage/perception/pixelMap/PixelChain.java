/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.Segment;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;

public class PixelChain implements Serializable, Cloneable, com.ownimage.perception.pixelMap.immutable.PixelChain {

    private final static long serialVersionUID = 2L;

    @Getter
    private ImmutableVectorClone<Pixel> mPixels;

    @Getter
    private ImmutableVectorClone<Segment> mSegments;

    @Getter
    private ImmutableVectorClone<com.ownimage.perception.pixelMap.immutable.Vertex> mVertexes;

    @Getter
    private double mLength;

    @Getter
    private Thickness mThickness;

    @Override
    public boolean equals(Object pO) {
        if (this == pO) {
            return true;
        }
        if (pO == null || getClass() != pO.getClass()) {
            return false;
        }
        PixelChain that = (PixelChain) pO;
        return Double.compare(that.mLength, mLength) == 0 &&
                mPixels.equals(that.mPixels) &&
                mSegments.equals(that.mSegments) &&
                mVertexes.equals(that.mVertexes) &&
                mLength == that.mLength &&
                mThickness == that.mThickness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPixels, mSegments, mVertexes, mLength, mThickness);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");
        sb.append(mPixels.stream().map(Pixel::toString).collect(Collectors.joining(", ")));
        sb.append(" ]\n");
        return sb.toString();
    }

}

