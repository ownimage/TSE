package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.segment.ISegment;

import java.util.function.Function;
import java.util.logging.Logger;

public class PixelChainBuilder implements IPixelChain {

    public final static Logger mLogger = Framework.getLogger();

    private ImmutableVectorClone<Pixel> mPixels;
    private ImmutableVectorClone<ISegment> mSegments;
    private ImmutableVectorClone<IVertex> mVertexes;
    private double mLength;
    private PixelChain.Thickness mThickness;

    public PixelChainBuilder(
            ImmutableVectorClone<Pixel> pPixels,
            ImmutableVectorClone<IVertex> pVertexes,
            ImmutableVectorClone<ISegment> pSegments,
            double pLength,
            PixelChain.Thickness pThickness
    ) {
        mPixels = pPixels;
        mVertexes = pVertexes;
        mSegments = pSegments;
        mLength = pLength;
        mThickness = pThickness;
    }

    public PixelChainBuilder changePixels(Function<ImmutableVectorClone<Pixel>, ImmutableVectorClone<Pixel>> pFn) {
        mPixels = pFn.apply(mPixels);
        return this;
    }

    public PixelChainBuilder changeVertexes(Function<ImmutableVectorClone<IVertex>, ImmutableVectorClone<IVertex>> pFn) {
        mVertexes = pFn.apply(mVertexes);
        return this;
    }

    public PixelChainBuilder changeSegments(Function<ImmutableVectorClone<ISegment>, ImmutableVectorClone<ISegment>> pFn) {
        mSegments = pFn.apply(mSegments);
        return this;
    }

    public PixelChainBuilder setLength(double pLength) {
        mLength = pLength;
        return this;
    }

    public PixelChainBuilder setThickness(PixelChain.Thickness pThickness) {
        Framework.checkParameterNotNull(mLogger, pThickness, "pThickness");
        mThickness = pThickness;
        return this;
    }

    public ImmutableVectorClone<Pixel> getPixels() {
        return mPixels;
    }

    public ImmutableVectorClone<ISegment> getSegments() {
        return mSegments;
    }

    public ImmutableVectorClone<IVertex> getVertexes() {
        return mVertexes;
    }

    public PixelChain build(final PixelMap pPixelMap) {
        return new PixelChain(pPixelMap, mPixels, mSegments, mVertexes, mLength, mThickness);
    }

}

