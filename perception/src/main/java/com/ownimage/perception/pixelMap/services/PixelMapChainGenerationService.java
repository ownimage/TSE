package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Logger;

@Service
public class PixelMapChainGenerationService {

    private final static Logger logger = Framework.getLogger();
    private PixelChainService pixelChainService;
    private PixelMapService pixelMapService;

    @Autowired
    public void setPixelChainService(PixelChainService pixelChainService) {
        this.pixelChainService = pixelChainService;
    }

    @Autowired
    public void setPixelMapService(PixelMapService pixelMapService) {
        this.pixelMapService = pixelMapService;
    }

    public Tuple2<ImmutablePixelMapData, PixelChain> generateChain(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            @NotNull Pixel pixel) {
        Optional<Node> node = pixelMapService.getNode(pixelMap, pixel);
        if (node.isPresent()) {
            return pixelChainService.setEndNode(pixelMap, pixelChain, node.get());
        }

        if (pixelChain.getPixels().lastElement().orElseThrow() == pixel) {
            logger.severe("SHOULD NOT BE ADDING THE SAME PIXEL LASTPIXEL");
        }

        if (pixelChain.getPixels().contains(pixel)) {
            logger.severe("SHOULD NOT BE ADDING A PIXEL THAT IT ALREADY CONTAINS");
        }

        var result = pixelMap;
        PixelChain copy = pixelChainService.add(pixelChain, pixel);
        result = pixelMapService.setInChain(result, pixel, true);
        result = pixelMapService.setVisited(result, pixel, true);
        // try to end quickly at a node
        for (Pixel nodalNeighbour : pixel.getNodeNeighbours(result)) {
            // there is a check here to stop you IMMEDIATELY going back to the staring node.
            if (!(copy.getPixelCount() == 2 && nodalNeighbour.samePosition(pixelChainService.firstPixel(copy)))) {
                return generateChain(result, copy, nodalNeighbour);
            }
        }
        // otherwise go to the next pixel normally
        for (Pixel neighbour : pixel.getNeighbours()) {
            if (!neighbour.isNode(result) && neighbour.isEdge(result) && !copy.getPixels().contains(neighbour)
                    && !(copy.getPixelCount() == 2 && neighbour.samePosition(pixelChainService.firstPixel(copy)))) {
                return generateChain(result, copy, neighbour);
            }
        }
        return new Tuple2<>(result, copy);
    }

    public Tuple2<ImmutablePixelMapData, Collection<PixelChain>> generateChains(
            @NotNull ImmutablePixelMapData pixelMap, @NotNull Node pStartNode) {
        var pixelMapResult = StrongReference.of(ImmutablePixelMapData.copyOf(pixelMap));

        Vector<PixelChain> chains = new Vector<>();
        pixelMapResult.update(pmr -> pixelMapService.setVisited(pmr, pStartNode, true));
        pixelMapResult.update(pmr -> pixelMapService.setInChain(pmr, pStartNode, true));
        pStartNode.getNeighbours().forEach(neighbour -> {
            if (neighbour.isNode(pixelMapResult.get())
                    || neighbour.isEdge(pixelMapResult.get())
                    && !neighbour.isVisited(pixelMapResult.get())) {
                PixelChain chain = new PixelChain(pixelMap, pStartNode);
                var generatedChain = generateChain(pixelMap, chain, neighbour);
                pixelMapResult.set(generatedChain._1);
                chain = generatedChain._2;
                if (pixelChainService.pixelLength(chain) > 2) {
                    chains.add(chain);
                }
            }
        });
        return new Tuple2<>(pixelMapResult.get(), chains);
    }
}
