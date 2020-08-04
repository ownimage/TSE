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

@Value.Immutable
public interface VertexData extends Serializable {

    @Value.Parameter(order = 1)
    int getPixelIndex();

    @Value.Parameter(order = 2)
    int getVertexIndex();

    @Value.Parameter(order = 3)
    Point getPosition();

    VertexData withVertexIndex(int vertexIndex);

    default ImmutableVertexData toImmutable(VertexData vertex) {
        return ImmutableVertexData.of(getPixelIndex(), getVertexIndex(), getPosition());
    }

    default boolean sameValue(VertexData vertex) {
        if (this == vertex) {
            return true;
        }
        return getVertexIndex() == vertex.getVertexIndex() &&
                getPixelIndex() == vertex.getPixelIndex() &&
                Objects.equals(getPosition(), vertex.getPosition()); // legacy serialisations might have null position
    }

}
