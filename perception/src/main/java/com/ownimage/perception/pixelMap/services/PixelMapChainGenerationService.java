package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Node;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
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
    private PixelService pixelService;

    @Autowired
    public void setPixelChainService(PixelChainService pixelChainService) {
        this.pixelChainService = pixelChainService;
    }

    @Autowired
    public void setPixelMapService(PixelMapService pixelMapService) {
        this.pixelMapService = pixelMapService;
    }

    @Autowired
    public void setPixelService(PixelService pixelService) {
        this.pixelService = pixelService;
    }

    public Tuple2<ImmutablePixelMap, ImmutablePixelChain> generateChain(
            @NotNull ImmutablePixelMap pixelMap,
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
        var copy = pixelChainService.add(pixelChain, pixel);
        // try to end quickly at a node
        for (Pixel nodalNeighbour : pixelService.getNodeNeighbours(result, pixel)) {
            // there is a check here to stop you IMMEDIATELY going back to the staring node.
            if (!(copy.getPixelCount() == 2 && nodalNeighbour.samePosition(pixelChainService.firstPixel(copy)))) {
                return generateChain(result, copy, nodalNeighbour);
            }
        }
        // otherwise go to the next pixel normally
        var nextNormal =  pixel.getNeighbours()
                .filter(neighbour -> !pixelService.isNode(result, neighbour)
                        && pixelService.isEdge(result, neighbour) && !copy.getPixels().contains(neighbour)
                        && !(copy.getPixelCount() == 2 && neighbour.samePosition(pixelChainService.firstPixel(copy))))
                .findFirst();
        if (nextNormal.isPresent()) {
            return generateChain(result, copy, nextNormal.get());
        }

        return new Tuple2<>(result, copy);
    }

    public Tuple2<ImmutablePixelMap, Collection<ImmutablePixelChain>> generateChains(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Node pStartNode) {
        var result = StrongReference.of(pixelMap);

        Vector<PixelChain> chains = new Vector<>();
        pStartNode.toPixel().getNeighbours().forEach(neighbour -> {
            if (pixelService.isNode(result.get(), neighbour)
                    || pixelService.isEdge(result.get(), neighbour)
                    && (
                    pixelMapService.getPixelChains(result.get(), neighbour).isEmpty()
                            && chains.stream().filter(pc -> pc.getPixels().contains(neighbour)).findFirst().isEmpty())
            ) {
                var chain = pixelChainService.createStartingPixelChain(pixelMap, pStartNode);
                var generatedChain = generateChain(pixelMap, chain, neighbour);
                result.set(generatedChain._1);
                chain = generatedChain._2;
                if (pixelChainService.pixelLength(chain) > 2) {
                    chains.add(chain);
                }
            }
        });
        return new Tuple2(result.get(), chains);
    }
}
