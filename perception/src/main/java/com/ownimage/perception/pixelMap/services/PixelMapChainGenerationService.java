package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import io.vavr.Tuple2;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PixelMapChainGenerationService {

    private final static Logger logger = Framework.getLogger();

    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();
    private static PixelMapService pixelMapService = Services.getDefaultServices().getPixelMapService();



    public Tuple2<ImmutablePixelMapData, PixelChain> generateChain(
            PixelMapData pixelMap, Node startNode, Pixel pixel, PixelChain pixelChain) {
        try {
            Framework.logEntry(logger);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("startNode: " + startNode);
                logger.finest("pixel: " + pixel);
                logger.finest("pixelChain: " + pixelChain);
            }
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

            var pixelMapResult = ImmutablePixelMapData.copyOf(pixelMap);
            PixelChain copy = pixelChainService.add(pixelChain, pixel);
            pixelMapResult = pixelMapService.setInChain(pixelMapResult, pixel, true);
            pixelMapResult = pixelMapService.setVisited(pixelMapResult, pixel, true);
            // try to end quickly at a node to prevent bypassing
            for (Pixel nodalNeighbour : pixel.getNodeNeighbours(pixelMapResult)) {
                // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
                // going back to the staring node.
                // if ((nodalNeighbour.isUnVisitedEdge() || nodalNeighbour.isNode()) && (pChain.count() != 2 ||
                // !nodalNeighbour.isNeighbour(pChain.firstPixel()))) {
                if ((nodalNeighbour.isUnVisitedEdge(pixelMapResult) || nodalNeighbour.isNode(pixelMapResult)) && !(copy.getPixelCount() == 2 &&
                        nodalNeighbour.samePosition(pixelChainService.firstPixel(copy)))) {
                    return  generateChain(pixelMapResult, startNode, nodalNeighbour, copy);
                }
            }
            // otherwise go to the next pixel normally
            for (Pixel neighbour : pixel.getNeighbours()) {
                // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
                // going back to the staring node.
                // if ((neighbour.isUnVisitedEdge() || neighbour.isNode()) && (pChain.count() != 2 ||
                // !neighbour.isNeighbour(pChain.firstPixel()))) {
                if ((neighbour.isUnVisitedEdge(pixelMapResult) || neighbour.isNode(pixelMapResult))
                        && !(copy.getPixelCount() == 2 && pixelChainService.getStartNode(pixelMapResult, copy).isPresent()
                        && neighbour.samePosition(pixelChainService.getStartNode(pixelMapResult, copy).get()))) {
                    return  generateChain(pixelMapResult, startNode, neighbour, copy);
                }
            }
            return new Tuple2<>(pixelMapResult, copy);
        } catch (StackOverflowError soe) {
            logger.severe("Stack Overflow Error");
            throw new RuntimeException("oops");
        }
    }
}
