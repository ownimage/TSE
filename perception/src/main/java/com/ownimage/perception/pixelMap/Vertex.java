/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;

import java.util.Objects;

/**
 * This class needs to remain here for the deserialization of existing transforms.
 * The Vertex class, this class is immutable. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements com.ownimage.perception.pixelMap.immutable.Vertex {

    private static final long serialVersionUID = 1L;

    private int mVertexIndex;

    private int mPixelIndex;

    private Point mPosition;

    private Vertex() {

    }

    @Override
    public int getVertexIndex() {
        return mVertexIndex;
    }


    @Override
    public int getPixelIndex() {
        return mPixelIndex;
    }


    @Override
    public Point getPosition() {
        return mPosition;
    }

    @Override
    public com.ownimage.perception.pixelMap.immutable.Vertex withVertexIndex(int vertexIndex) {
        return ImmutableVertex.of(vertexIndex, mPixelIndex, mPosition);
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
                Objects.equals(mPosition, vertex.mPosition); // legacy serialisations might have null position
    }

    @Override
    public int hashCode() {
        return Objects.hash(mVertexIndex, mPixelIndex, mPosition);
    }


}
