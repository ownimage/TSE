/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.Pixel;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class Node implements IXY{

    private static final long serialVersionUID = 1L;

    private final ImmutableVectorClone<PixelChain> pixelChains;
    private final int x;
    private final int y;

    public Node(@NotNull IXY integerPoint) {
        this(integerPoint.getX(), integerPoint.getY());
    }

    public Node(int x, int y) {
        this(x, y, new ImmutableVectorClone<>());
    }

    public Node(int x, int y, @NotNull ImmutableVectorClone<PixelChain> pixelChains) {
        this.x = x;
        this.y = y;
        this.pixelChains = pixelChains;
    }

    public Node withPixelChains(@NotNull ImmutableVectorClone<PixelChain> pixelChains) {
        if (this.pixelChains == pixelChains) {
            return this;
        }
        return new Node(x, y, pixelChains);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public Node addPixelChain(ImmutablePixelChain pixelChain) {
        if (pixelChains.contains(pixelChain)) {
            return this;
        }
        return withPixelChains(pixelChains.add(pixelChain));
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
        return withPixelChains(pixelChains.remove(pixelChain));
    }

    public boolean containsPixelChain(ImmutablePixelChain pixelChain) {
        return pixelChains.contains(pixelChain);
    }

    public ImmutableIXY toPixelMapGridPosition() {
        return ImmutableIXY.of(x, y);
    }

    public Pixel toPixel() {
        return new Pixel(x, y);
    }

    @Override
    public String toString() {
        return "Node(" + x + ", " + y + ")";
    }
}
