/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Point;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Objects;

@Value.Immutable(prehash = true)
public interface Vertex extends Serializable {


    @Value.Parameter(order = 1)
    int getVertexIndex();

    @Value.Parameter(order = 2)
    int getPixelIndex();

    @Value.Parameter(order = 3)
    Point getPosition();

    default Vertex withVertexIndex(int vertexIndex) {
        return ImmutableVertex.of(vertexIndex, getPixelIndex(), getPosition());
    }

    default boolean sameValue(Vertex vertex) {
        if (this == vertex) {
            return true;
        }
        return getVertexIndex() == vertex.getVertexIndex() &&
                getPixelIndex() == vertex.getPixelIndex() &&
                Objects.equals(getPosition(), vertex.getPosition()); // legacy serialisations might have null position
    }

}
