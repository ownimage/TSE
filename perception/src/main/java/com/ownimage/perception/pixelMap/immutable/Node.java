/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Value.Immutable(prehash = true)
public interface Node extends IXY {

    @Override
    @Value.Parameter(order = 1)
    int getX();

    @Override
    @Value.Parameter(order = 2)
    int getY();

    @Value.Default
    default ImmutableVectorClone<PixelChain> pixelChains() {
        return new ImmutableVectorClone<PixelChain>();
    }

    Node withPixelChains(@NotNull ImmutableVectorClone<PixelChain> pixelChains);

    static ImmutableNode ofIXY(@NotNull IXY pixel) {
        return ImmutableNode.of(pixel.getX(), pixel.getY());
    }

    default Node addPixelChain(@NotNull ImmutablePixelChain pixelChain) {
        if (pixelChains().contains(pixelChain)) {
            return this;
        }
        return withPixelChains(pixelChains().add(pixelChain));
    }

    default Stream<ImmutablePixelChain> streamPixelChains() {
        return pixelChains().stream().map(ImmutablePixelChain::copyOf);
    }

    default int countPixelChains() {
        return pixelChains().size();
    }

    default ImmutablePixelChain getPixelChain(int n) {
        if (n > countPixelChains()) {
            throw new IllegalArgumentException("Cannot return item: " + n + ". There are only " +
                    countPixelChains() + " chains.");
        }

        return ImmutablePixelChain.copyOf(pixelChains().get(n));
    }

    default Node removePixelChain(ImmutablePixelChain pixelChain) {
        return withPixelChains(pixelChains().remove(pixelChain));
    }

    default boolean containsPixelChain(ImmutablePixelChain pixelChain) {
        return pixelChains().contains(pixelChain);
    }

    default ImmutableIXY toImmutableIXY() {
        return ImmutableIXY.of(getX(), getY());
    }

    default Pixel toPixel(int height) {
        return Pixel.of(getX(), getY(), height);
    }

    static ImmutableNode of(@NotNull IXY ixy) {
        return ImmutableNode.of(ixy.getX(), ixy.getY());
    }

}
