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
import com.ownimage.perception.pixelMap.immutable.Vertex;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PixelChain implements Serializable, Cloneable, com.ownimage.perception.pixelMap.immutable.PixelChain {

    private final static long serialVersionUID = 2L;

    private ImmutableVectorClone<Pixel> mPixels;
    private ImmutableVectorClone<Segment> mSegments;
    private ImmutableVectorClone<com.ownimage.perception.pixelMap.immutable.Vertex> mVertexes;
    private double mLength;
    private Thickness mThickness;

    @Override
    public Optional<Color> color() {
        return Optional.empty();
    }

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

    @Override
    public Thickness thickness() {
        return mThickness;
    }

    @Override
    public double length() {
        return mLength;
    }

    @Override
    public ImmutableVectorClone<Segment> segments() {
        return mSegments;
    }

    @Override
    public ImmutableVectorClone<Vertex> vertexes() {
        return mVertexes;
    }

    @Override
    public ImmutableVectorClone<Pixel> pixels() {
        return mPixels;
    }
}

