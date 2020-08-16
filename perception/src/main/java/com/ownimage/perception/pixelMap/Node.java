/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.perception.pixelMap.immutable.PixelChain;

import java.util.Vector;

/**
 * This class is never instantiated within the code.
 * The reason that has a Vector<PixelChain> rather than an ImmutableVectorClone<ImmutablePixelChain>
 * is because it needs to remain serialization compatible with older versions.
 */
public class Node extends Pixel {

    private static final long serialVersionUID = 1L;
    private final Vector<PixelChain> pixelChains = new Vector<>();

    private Node(int pX, int pY) {
        super(pX, pY);
    }
}
