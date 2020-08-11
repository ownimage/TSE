/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.immutable.Segment;
import com.ownimage.perception.pixelMap.immutable.Vertex;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.VertexService;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;

public class PixelChain implements Serializable, Cloneable, com.ownimage.perception.pixelMap.immutable.PixelChain {

    private final static long serialVersionUID = 2L;
    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private static VertexService vertexService = context.getBean(VertexService.class);

    @Getter
    private final ImmutableVectorClone<Pixel> mPixels;

    @Getter
    private final ImmutableVectorClone<Segment> mSegments;

    @Getter
    private final ImmutableVectorClone<com.ownimage.perception.pixelMap.immutable.Vertex> mVertexes;

    @Getter
    private final double mLength;

    @Getter
    private final Thickness mThickness;

    public PixelChain(@NotNull PixelMap pPixelMap, @NotNull Node pStartNode) {
        mPixels = new ImmutableVectorClone<Pixel>().add(pStartNode);
        mSegments = new ImmutableVectorClone<>();
        mVertexes = new ImmutableVectorClone<com.ownimage.perception.pixelMap.immutable.Vertex>(); //.add(vertexService.createVertex(pPixelMap, this, 0, 0));
        mLength = 0.0d;
        mThickness = Thickness.Normal;
    }

    public PixelChain(
            @NonNull ImmutableVectorClone<Pixel> pPixels,
            @NonNull ImmutableVectorClone<Segment> pSegments,
            @NonNull ImmutableVectorClone<Vertex> pVertexes,
            double pLength,
            @NonNull Thickness pThickness
    ) {
        mPixels = pPixels;
        mSegments = pSegments;
        mVertexes = pVertexes;
        mLength = pLength;
        mThickness = pThickness;
    }

    public PixelChain(@NonNull PixelChain pixelChain) {
        mPixels = pixelChain.getPixels();
        mSegments = pixelChain.getSegments();
        mVertexes = pixelChain.getVertexes();
        mLength = pixelChain.getLength();
        mThickness = pixelChain.getThickness();
    }

    public static PixelChain of(@NonNull PixelChain pixelChain) {
        if (pixelChain instanceof PixelChain) {
            return (PixelChain) pixelChain;
        }
        return new PixelChain(pixelChain);
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
                mLength == that.mLength &&
                mThickness == that.mThickness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPixels, mSegments, mVertexes, mLength, mThickness);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");
        sb.append(mPixels.stream().map(Pixel::toString).collect(Collectors.joining(", ")));
        sb.append(" ]\n");
        return sb.toString();
    }

}

