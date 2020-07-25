package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.Services;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

public class PixelChainBuilder implements IPixelChain {

    private final static Logger mLogger = Framework.getLogger();
    private static Services services = Services.getDefaultServices();
    private static PixelChainService pixelChainService = services.getPixelChainService();

    @Getter
    private ImmutableVectorClone<Pixel> mPixels;
    @Getter
    private ImmutableVectorClone<ISegment> mSegments;
    @Getter
    private ImmutableVectorClone<IVertex> mVertexes;
    @Getter
    private double mLength;
    @Getter
    private Thickness mThickness;

    public PixelChainBuilder(
            Collection<Pixel> pPixels,
            Collection<IVertex> pVertexes,
            Collection<ISegment> pSegments,
            double pLength,
            Thickness pThickness
    ) {
        mPixels = new ImmutableVectorClone<Pixel>().addAll(pPixels);
        mVertexes = new ImmutableVectorClone<IVertex>().addAll(pVertexes);
        mSegments = new ImmutableVectorClone<ISegment>().addAll(pSegments);
        mLength = pLength;
        mThickness = pThickness;
    }

    public PixelChainBuilder(@NotNull IPixelChain pixelChain) {
        mPixels = pixelChain.getPixels();
        mVertexes = pixelChain.getVertexes();
        mSegments = pixelChain.getSegments();
        mLength = pixelChain.getLength();
        mThickness = pixelChain.getThickness();
    }

     public PixelChain build() {
        return new PixelChain(
                mPixels,
                mSegments,
                mVertexes,
                mLength,
                mThickness
        );
    }

    private void setValuesFrom(PixelChain pixelChain) {
        mPixels = pixelChain.getPixels();
        mSegments = pixelChain.getSegments();
        mVertexes = pixelChain.getVertexes();
        mLength = pixelChain.getLength();
        mThickness = pixelChain.getThickness();
    }





}
