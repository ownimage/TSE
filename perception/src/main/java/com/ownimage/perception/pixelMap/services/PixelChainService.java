package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelChainBuilder;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

public class PixelChainService {

    private static VertexService vertexService = Services.getDefaultServices().getVertexService();

    public PixelChain fixNullPositionVertexes(PixelMap pixelMap, PixelChain pixelChain) {
        var mappedVertexes = pixelChain.getVertexes().stream()
                .map(v -> {
                    var p = v.getPosition();
                    if (p == null) {
                        p = vertexService.getPixel(pixelChain, v).getUHVWMidPoint(pixelMap);
                        return vertexService.createVertex(pixelChain, v.getVertexIndex(), v.getPixelIndex(), p);
                    }
                    return v;
                })
                .collect(Collectors.toList());
        var vertexes = new ImmutableVectorClone<IVertex>().addAll(mappedVertexes);
        return new PixelChain(pixelChain.getPixels(), pixelChain.getSegments(), vertexes, pixelChain.length(), pixelChain.getThickness());
    }

    private PixelChainBuilder builder(PixelChain pixelChain) {
        return new PixelChainBuilder(
                pixelChain.getPixels().toVector(),
                pixelChain.getVertexes().toVector(),
                pixelChain.getSegments().toVector(),
                pixelChain.length(),
                pixelChain.getThickness()
        );
    }

    public PixelChain add(PixelChain pixelChain, Pixel pPixel) {
        val builder = builder(pixelChain);
        builder.changePixels(p -> p.add(pPixel));
        return builder.build();
    }


    public PixelChain approximate(
            @NotNull PixelMap pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance
    ) {
        val builder = builder(pixelChain);
        builder.approximate(pixelMap, tolerance);
        return builder.build();
    }

    public PixelChain approximateCurvesOnly(
            @NotNull PixelMap pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference
    ) {
        val builder = builder(pixelChain);
        builder.approximateCurvesOnly(pixelMap, tolerance, lineCurvePreference);
        return builder.build();
    }

    /**
     * Creates a copy of this PixelChain with the order of the pixels in the pixel chain.
     * The original PixelChain is not altered.
     * This means reversing the start and end nodes.
     * All of the segments in the line are also reversed and replaced with new straight line
     * segments.
     *
     * @param pixelMap the PixelMap this chain belongs to
     * @return a new PixelChain with the elements reversed
     */
    public PixelChain reverse(PixelMap pixelMap, PixelChain pixelChain) {
        // note that this uses direct access to the data members as the public setters have other side effects
        //validate("reverse");
        val builder = builder(pixelChain);

        // reverse pixels
        Vector<Pixel> pixels = builder.getPixels().toVector();
        Collections.reverse(pixels);
        builder.changePixels(p -> p.clear().addAll(pixels));

        // reverse vertexes
        int maxPixelIndex = builder.getPixels().size() - 1;
        Vector<IVertex> vertexes = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            IVertex vertex = builder.getVertexes().get(i);
            IVertex v = vertexService.createVertex(pixelMap, builder, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
        }
        builder.changeVertexes(v -> v.clear().addAll(vertexes));

        // reverse segments
        Vector<ISegment> segments = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            if (i != pixelChain.getVertexes().size() - 1) {
                StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pixelMap, builder.build(), segments.size());
                segments.add(newSegment);
            }
        }
        builder.changeSegments(s -> s.clear().addAll(segments));
        return builder.build();
    }

    public PixelChain refine(PixelMap pixelMap, PixelChain pixelChain, double tolerance, double lineCurvePreference) {
        var builder = builder(pixelChain);
        // builder.refine(pixelMap, pSource);
        builder.approximateCurvesOnly(pixelMap, tolerance, lineCurvePreference);
        return builder.build();
    }

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    public Pixel firstPixel(PixelChain pixelChain) {
        return pixelChain.getPixels().firstElement().orElseThrow();
    }

    public Optional<Node> getEndNode(PixelMap pPixelMap, PixelChain pixelChain) {
        return pixelChain.getPixels().lastElement().flatMap(pPixelMap::getNode);
    }


    public Optional<Node> getStartNode(PixelMap pPixelMap, PixelChain pixelChain) {
        return pPixelMap.getNode(pixelChain.getPixels().firstElement().orElseThrow());
    }

    public IVertex getStartVertex(PixelChain pixelChain) {
        return pixelChain.getVertexes().firstElement().orElse(null);
    }
}
