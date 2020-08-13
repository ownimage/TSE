/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.perception.pixelMap.Pixel;

import java.util.Vector;
import java.util.stream.Stream;

/**
 * This class should be immutable.  The reason that has a Vector<PixelChain> rather than an ImmutableVectorClone<ImmutablePixelChain>
 * is because it needs to remain serialization compatible with older versions.
 */
public class Node {

    private static final long serialVersionUID = 1L;

    private final Vector<PixelChain> pixelChains = new Vector<>();
    private final int x;
    private final int y;

    public Node(IntegerPoint integerPoint) {
        this.x = integerPoint.getX();
        this.y = integerPoint.getY();
    }

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private Node(Node node) {
        this(node.x, node.y);
        pixelChains.addAll(node.pixelChains);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Node addPixelChain(ImmutablePixelChain pixelChain) {
        if (pixelChains.contains(pixelChain)) {
            return this;
        }

        var clone = new Node(this);
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
        var clone = new Node(this);
        clone.pixelChains.remove(pixelChain);
        return clone;
    }

    public boolean containsPixelChain(ImmutablePixelChain pixelChain) {
        return pixelChains.contains(pixelChain);
    }

    public IntegerPoint toIntegerPoint() {
        return new IntegerPoint(x, y);
    }

    public Pixel toPixel() {
        return new Pixel(x, y);
    }

    @Override
    public String toString() {
        return "Node(" + x + ", " + y + ")";
    }
}
