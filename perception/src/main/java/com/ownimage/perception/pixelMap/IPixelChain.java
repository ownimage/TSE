package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.immutable.IImmutableVector;
import com.ownimage.perception.pixelMap.segment.ISegment;

import java.util.Optional;
import java.util.stream.Stream;

public interface IPixelChain {

    IImmutableVector<Pixel> getPixels();

    IImmutableVector<ISegment> getSegments();

    IImmutableVector<IVertex> getVertexes();

    default Stream<ISegment> streamSegments() {
        return getSegments().stream();
    }

    default int getPixelLength() {
        return getPixels().size();
    }


    default ISegment getSegment(final int i) {
        if (getSegments().size() <= i || i < 0) return null;
        return getSegments().get(i);
    }

    default IVertex getVertex(final int i) {
        if (getVertexes().size() <= i || i < 0) return null;
        return getVertexes().get(i);
    }

    @Deprecated
    default Pixel getPixel(final int pIndex) {
        if (pIndex < 0 || pIndex > getPixelLength()) {
            String msg = "pIndex, currently: %s, must be between 0 and the length of mPixels, currently: %s";
            throw new IllegalArgumentException(String.format(msg, pIndex, getPixelLength()));
        }

        return getPixels().get(pIndex);
    }

    default Optional<Pixel> getOptionalPixel(final int pIndex) {
        if (pIndex < 0 || pIndex > getPixelLength()) {
            return Optional.empty();
        }
        return Optional.of(getPixels().get(pIndex));
    }
}
