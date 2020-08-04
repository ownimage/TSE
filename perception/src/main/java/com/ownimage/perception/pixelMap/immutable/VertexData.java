/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Point;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface VertexData extends Serializable, IVertex {

    @Override
    @Value.Parameter(order = 1)
    int getPixelIndex();

    @Override
    @Value.Parameter(order = 2)
    int getVertexIndex();

    @Override
    @Value.Parameter(order = 3)
    Point getPosition();

}
