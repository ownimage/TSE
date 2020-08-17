/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.Pixel;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Value.Immutable
public interface Node extends IXY {

//    private final ImmutableVectorClone<PixelChain> pixelChains;
//    private final int x;
//    private final int y;

    //    public Node(@NotNull IXY integerPoint) {
//        this(integerPoint.getX(), integerPoint.getY());
//    }
//
//    Node(int x, int y) {
//        this(x, y, new ImmutableVectorClone<>());
//    }
//
//    Node(int x, int y, @NotNull ImmutableVectorClone<PixelChain> pixelChains) {
//        this.x = x;
//        this.y = y;
//        this.pixelChains = pixelChains;
//    }
//

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

    default ImmutableIXY toPixelMapGridPosition() {
        return ImmutableIXY.of(getX(), getY());
    }

    default Pixel toPixel() {
        return new Pixel(getX(), getY());
    }

}
