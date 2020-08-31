package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Node;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.XY;
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

    public ImmutablePixelChain generateChain(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull PixelChain pixelChain,
            @NotNull Pixel pixel) {
        Optional<Node> node = pixelMapService.getNode(pixelMap, pixel);
        if (node.isPresent()) {
            return pixelChainService.setEndNode(pixelMap, pixelChain, node.get());
        }

        if (pixelChain.pixels().lastElement().orElseThrow() == pixel) {
            logger.severe("SHOULD NOT BE ADDING THE SAME PIXEL LASTPIXEL");
        }

        if (pixelChain.pixels().contains(pixel)) {
            logger.severe("SHOULD NOT BE ADDING A PIXEL THAT IT ALREADY CONTAINS");
        }

        var result = pixelChainService.add(pixelChain, pixel);
        // try to end quickly at a node
        for (XY nodalNeighbour : pixelService.getNodeNeighbours(pixelMap, pixel)) {
            // there is a check here to stop you IMMEDIATELY going back to the staring node.
            if (!(result.pixelCount() == 2 && nodalNeighbour.samePosition(pixelChainService.firstPixel(result)))) {
                return generateChain(pixelMap, result, Pixel.of(nodalNeighbour, pixelMap.height()));
            }
        }
        // otherwise go to the next pixel normally
        var nextNormal =  pixelService.getNeighbours(pixel)
                .filter(neighbour -> !pixelService.isNode(pixelMap, neighbour)
                        && pixelService.isEdge(pixelMap, neighbour) && !result.pixels().contains(Pixel.of(neighbour, pixelMap.height()))
                        && !(result.pixelCount() == 2 && neighbour.samePosition(pixelChainService.firstPixel(result)))
                        // below stops you making a loop of 4 back to yourself
                        && !(result.pixelCount() == 2 && pixelService.isNeighbour(neighbour, result.pixels().get(0)))
                )
                .findFirst();
        if (nextNormal.isPresent()) {
            return generateChain(pixelMap, result, Pixel.of(nextNormal.get(), pixelMap.height()));
        }

        return result;
    }

    public Collection<ImmutablePixelChain> generateChains(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Node pStartNode) {
        Vector<ImmutablePixelChain> chains = new Vector<>();
        pixelService.getNeighbours(pStartNode.toImmutableIXY()).forEach(neighbour -> {
            if (pixelService.isNode(pixelMap, neighbour)
                    || pixelService.isEdge(pixelMap, neighbour)
                    && (
                    pixelMapService.getPixelChains(pixelMap, Pixel.of(neighbour, pixelMap.height())).isEmpty()
                            && chains.stream().filter(pc -> pc.pixels().contains(Pixel.of(neighbour, pixelMap.height()))).findFirst().isEmpty())
            ) {
                var startingChain = pixelChainService.createStartingPixelChain(pixelMap, pStartNode);
                var generatedChain = generateChain(pixelMap, startingChain, Pixel.of(neighbour, pixelMap.height()));
                if (pixelChainService.pixelLength(generatedChain) > 2) {
                    chains.add(generatedChain);
                }
            }
        });
        return chains;
    }
}
