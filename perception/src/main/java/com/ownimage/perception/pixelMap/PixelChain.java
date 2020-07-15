/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.Services;
import com.ownimage.perception.pixelMap.services.VertexService;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The Class PixelChain. The following shows how a PixelChain would be constructed, populated with Pixels and ISegments
 * <p>
 * <code>
 * <br/>PixelChain chain = new PixelChain(this, pStartNode);
 * <br/>
 * <br/>for (Pixel pixel : ...) {
 * <br/>	chain.add(pixel);
 * <br/>}
 * <br/>
 * <br/>// then at the end
 * <br/>chain.setEndNode(getNode(endPixel));
 * <br/>
 * <br/>// then set up the segments
 * <br/>
 * <br/>mSegments = new Vector<ISegment>(); // mSegments is a private member of PixelChain
 * <br/>for each segment
 * <br/>// note that the first segment needs to be attached to getStartVertex()
 * <br/>// then each segment needs to be attached to the previous endVertex
 * <br/>{
 * <br/>    segment.attachToVertexes(false);
 * <br/>	mSegments.add(segment);
 * <br/>}
 * </code>
 */
public class PixelChain implements Serializable, Cloneable, IPixelChain {

    private final static Logger mLogger = Framework.getLogger();
    private final static long serialVersionUID = 2L;
    static private PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();

    @Getter
    private final ImmutableVectorClone<Pixel> mPixels;

    @Getter
    private final ImmutableVectorClone<ISegment> mSegments;

    @Getter
    private final ImmutableVectorClone<IVertex> mVertexes;

    @Getter
    transient private double mLength;

    @Getter
    private Thickness mThickness;


    @Setter
    transient private @NotNull VertexService vertexService = Services.getDefaultServices().getVertexService();

    /**
     * Instantiates a new pixel chain.
     *
     * @param pPixelMap
     * @param pStartNode the start node
     */
    public PixelChain(PixelMapData pPixelMap, Node pStartNode) {
        if (pStartNode == null) {
            throw new IllegalArgumentException("pStartNode must not be null");
        }
        mPixels = new ImmutableVectorClone<Pixel>().add(pStartNode);
        mSegments = new ImmutableVectorClone<>();
        mVertexes = new ImmutableVectorClone<IVertex>().add(vertexService.createVertex(pPixelMap, this, 0, 0));
        mThickness = IPixelChain.Thickness.Normal;
    }

    public PixelChain(
            @NonNull ImmutableVectorClone<Pixel> pPixels,
            @NonNull ImmutableVectorClone<ISegment> pSegments,
            @NonNull ImmutableVectorClone<IVertex> pVertexes,
            double pLength,
            @NonNull Thickness pThickness
    ) {
        mPixels = pPixels;
        mSegments = pSegments;
        mVertexes = pVertexes;
        mLength = pLength;
        mThickness = pThickness;

//        validate(pPixelMap, false, "PixelChain");
    }


    @Override
    public boolean equals(Object pO) {
        if (this == pO) {
            return true;
        }
        if (pO == null || getClass() != pO.getClass()) {
            return false;
        }
        PixelChain that = (PixelChain) pO;
        return Double.compare(that.mLength, mLength) == 0 &&
                mPixels.equals(that.mPixels) &&
                mSegments.equals(that.mSegments) &&
                mVertexes.equals(that.mVertexes) &&
                mThickness == that.mThickness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPixels, mSegments, mVertexes, mLength, mThickness);
    }


    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");
        sb.append(mPixels.stream().map(Pixel::toString).collect(Collectors.joining(", ")));
        sb.append(" ]\n");

        return sb.toString();
    }


}

