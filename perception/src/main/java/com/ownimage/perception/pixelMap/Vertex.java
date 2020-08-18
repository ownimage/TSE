/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import lombok.Getter;

/**
 * This class needs to remain here for the deserialization of existing transforms.
 * The Vertex class, this class is immutable. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements com.ownimage.perception.pixelMap.immutable.Vertex {

    private static final long serialVersionUID = 1L;

    @Getter
    private int mVertexIndex;

    @Getter
    private int mPixelIndex;

    @Getter
    private Point mPosition;

    private Vertex() {
    }

}
