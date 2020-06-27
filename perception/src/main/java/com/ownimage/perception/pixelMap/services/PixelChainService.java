package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelChainBuilder;
import com.ownimage.perception.pixelMap.PixelMap;
import lombok.val;

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

//    public PixelChain add(PixelChain pixelChain, Pixel pPixel) {
//        val builder = builder(pixelChain);
//        builder.changePixels(p -> p.add(pPixel));
//        return builder.build();
//    }
}
