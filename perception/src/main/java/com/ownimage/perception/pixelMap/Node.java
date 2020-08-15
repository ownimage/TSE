/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.perception.pixelMap.immutable.IXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.PixelChain;

import java.util.Vector;
import java.util.stream.Stream;

/**
 * This class should be immutable.  The reason that has a Vector<PixelChain> rather than an ImmutableVectorClone<ImmutablePixelChain>
 * is because it needs to remain serialization compatible with older versions.
 */
public class Node extends Pixel {


    private static final long serialVersionUID = 1L;

    private final Vector<PixelChain> pixelChains = new Vector<>();

    public Node(IXY integerPoint) {
        super(integerPoint);
    }

    public Node(int pX, int pY) {
        super(pX, pY);
    }

    public Node addPixelChain(ImmutablePixelChain pixelChain) {
        if (pixelChains.contains(pixelChain)) {
            return this;
        }

        Node clone = copy();
        clone.pixelChains.add(pixelChain);
        return clone;
    }

    public Stream<ImmutablePixelChain> streamPixelChains() {
        return pixelChains.stream().map(ImmutablePixelChain::copyOf);
    }

    public int countPixelChains() {
        return pixelChains.size();
    }

    public ImmutablePixelChain getPixelChain(int n) {
        if (n > countPixelChains()) {
            throw new IllegalArgumentException("Cannot return item: " + n + ". There are only " +
                    countPixelChains() + " chains.");
        }

        return ImmutablePixelChain.copyOf(pixelChains.get(n));
    }

    public Node removePixelChain(ImmutablePixelChain pixelChain) {
        Node clone = copy();
        clone.pixelChains.remove(pixelChain);
        return clone;
    }

    public boolean containsPixelChain(ImmutablePixelChain pixelChain) {
        return pixelChains.contains(pixelChain);
    }

    private Node copy() {
        Node clone = new Node(this);
        clone.pixelChains.addAll(pixelChains);
        return clone;
    }

    @Override
    public String toString() {
        return "Node(" + getX() + ", " + getY() + ")";
    }
}
