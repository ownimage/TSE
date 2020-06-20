/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The Vertex class, this class is immutable. Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Vertex implements IVertex {

    private static final long serialVersionUID = 1L;

    private final int mVertexIndex;
    private final int mPixelIndex;
    private @NotNull Point mPosition;

    public Vertex(int pVertexIndex, int pPixelIndex, @NotNull Point pPosition) {
        mVertexIndex = pVertexIndex;
        mPixelIndex = pPixelIndex;
        mPosition = pPosition;
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

}
