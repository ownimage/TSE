/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Point;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Interface IVertex represents a joining point between two segments that approximate part of a PixelChain.
 * The vertex is associated with a Pixel in the PixelChain i.e. it has an integer (x, y)
 * position on the map. However the Pixel that it maps to might be moved slightly in terms of its UHVW double (x, y)
 * position to improve the closeness of the overall approximatio.
 */
public interface IVertex extends Serializable {

    int getPixelIndex();

    int getVertexIndex();

    Point getPosition();

    IVertex withVertexIndex(int vertexIndex);

    default ImmutableVertexData toImmutable(IVertex vertex) {
        return ImmutableVertexData.of(getPixelIndex(), getVertexIndex(), getPosition());
    }

    default boolean sameValue(IVertex vertex) {
        if (this == vertex) {
            return true;
        }
        return getVertexIndex() == vertex.getVertexIndex() &&
                getPixelIndex() == vertex.getPixelIndex() &&
                Objects.equals(getPosition(), vertex.getPosition()); // legacy serialisations might have null position
    }

}
