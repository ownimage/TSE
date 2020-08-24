package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.util.Counter;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.immutable.IXY;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Node;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ownimage.perception.pixelMap.PixelConstants.E;
import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.N;
import static com.ownimage.perception.pixelMap.PixelConstants.NE;
import static com.ownimage.perception.pixelMap.PixelConstants.NW;
import static com.ownimage.perception.pixelMap.PixelConstants.S;
import static com.ownimage.perception.pixelMap.PixelConstants.SE;
import static com.ownimage.perception.pixelMap.PixelConstants.SW;
import static com.ownimage.perception.pixelMap.PixelConstants.W;

@Service
public class PixelMapApproximationService {

    private final static Logger logger = Framework.getLogger();
    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    private PixelMapChainGenerationService pixelMapChainGenerationService;
    private PixelChainService pixelChainService;
    private PixelService pixelService;
    private PixelMapService pixelMapService;

    @Autowired
    public void setPixelMapChainGenerationService(PixelMapChainGenerationService pixelMapChainGenerationService) {
        this.pixelMapChainGenerationService = pixelMapChainGenerationService;
    }

    @Autowired
    public void setPixelChainService(PixelChainService pixelChainService) {
        this.pixelChainService = pixelChainService;
    }

    @Autowired
    public void setPixelService(PixelService pixelService) {
        this.pixelService = pixelService;
    }

    @Autowired
    public void setPixelMapService(PixelMapService pixelMapService) {
        this.pixelMapService = pixelMapService;
    }

    public ImmutablePixelMap actionProcess(
            @NotNull ImmutablePixelMap pixelMap,
            double tolerance,
            double lineCurvePreference,
            IProgressObserver progress) {
        var result = pixelMap.withAutoTrackChanges(false);
        result = process01_reset(result, progress);
        result = process02_thin(result, tolerance, lineCurvePreference, progress);
        result = process03_generateNodes(result, progress);
        result = process03b_removeEdgesBetweenTwoNodes(result, tolerance, lineCurvePreference, progress);
        result = process04b_removeBristles(result, tolerance, lineCurvePreference, progress);
        result = process04a_removeLoneNodes(result, tolerance, lineCurvePreference, progress);
        result = process05_generateChains(result, progress);
        result = process05a_findLoops(result, progress);
        result = process06_straightLinesRefineCorners(result, lineCurvePreference, progress);
        result = process07_mergeChains(result, progress);
        result = process08_refine(result, tolerance, lineCurvePreference, progress);
        result = process09_connectNodes(result, progress);
        result = result.withAutoTrackChanges(true);
        pixelMapService.validate(pixelMap);
        return result;
    }

    public ImmutablePixelMap process09_connectNodes(
            @NotNull ImmutablePixelMap pixelMap, IProgressObserver progress) {
        var nodes = StrongReference.of(pixelMap.nodes());
        pixelMap.pixelChains().stream()
                .forEach(pc -> {
                    var startNodeKey = pixelChainService.getStartNode(pixelMap, pc).orElseThrow().toImmutableIXY();
                    var endNodeKey = pixelChainService.getEndNode(pixelMap, pc).orElseThrow().toImmutableIXY();
                    nodes.update(n -> n.update(startNodeKey, (k, v) -> v.addPixelChain(pc)));
                    nodes.update(n -> n.update(endNodeKey, (k, v) -> v.addPixelChain(pc)));
                });
        return pixelMap.withNodes(nodes.get());
    }

    public @NotNull ImmutablePixelMap process01_reset(
            @NotNull ImmutablePixelMap pixelMap,
            IProgressObserver progress) {
        reportProgress(progress, "Resetting ...", 0);
        var result = pixelMap
                .withData(pixelMap.data().forEach(d -> (byte) (d & EDGE)))
                .withNodes(pixelMap.nodes().clear())
                .withPixelChains(pixelMap.pixelChains().clear())
                .withSegmentIndex(pixelMap.segmentIndex().clear());
        return result;
    }

    private void reportProgress(IProgressObserver progress, String pProgressString, int pPercent) {
        if (progress != null) {
            progress.setProgress(pProgressString, pPercent);
        }
    }

    // TODO need to work out how to have a progress bar
    public ImmutablePixelMap process02_thin(
            @NotNull ImmutablePixelMap pixelMap,
            double tolerance, double lineCurvePreference, IProgressObserver progress) {
        reportProgress(progress, "thinning ...", 0);
        var result = StrongReference.of(pixelMap);
        new Range2D(pixelMap.width(), pixelMap.height())
                .forEach((x, y) -> result.update(r -> thin(r, pixelMapService.getPixelOptionalAt(r, x, y).orElseThrow(), tolerance, lineCurvePreference)));
        return result.get();
    }

    public ImmutablePixelMap process03_generateNodes(
            @NotNull ImmutablePixelMap pixelMap, IProgressObserver pProgressObserver) {
        var result = StrongReference.of(pixelMap);
        reportProgress(pProgressObserver, "Generating Nodes ...", 0);
        pixelMapService.forEachPixel(result.get(), pixel -> {
            result.update(r -> pixelMapService.calcIsNode(result.get(), pixel));
        });
        return result.get();
    }

    public ImmutablePixelMap process03b_removeEdgesBetweenTwoNodes(
            @NotNull ImmutablePixelMap pixelMap,
            double tolerance,
            double lineCurvePreference,
            IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Remove Edges between 2 Nodes ...", 0);
        var result = StrongReference.of(pixelMap);
        var removedPixels = new HashSet<Pixel>();
        pixelMapService.forEachPixel(result.get(), pixel -> {
            if (!pixelService.isNode(result.get(), pixel)
                    && pixelService.isEdge(result.get(), pixel)
                    && pixelService.getNodeNeighbours(result.get(), pixel).stream().count() >= 2) {
                result.update(r -> r.withData(r.data().set(pixel.getX(), pixel.getY(), (byte) 0)));
                removedPixels.add(pixel);
            }
        });
        removedPixels.stream()
                .flatMap(p -> pixelMapService.stream8Neighbours(result.get(), p))
                .forEach(p -> result.update(r -> thin(r, p, tolerance, lineCurvePreference)));
        removedPixels.stream()
                .flatMap(p -> pixelMapService.stream8Neighbours(result.get(), p))
                .forEach(p -> result.update(r -> pixelMapService.calcIsNode(r, p)));
        return result.get();
    }

    public ImmutablePixelMap process04a_removeLoneNodes(
            @NotNull ImmutablePixelMap pixelMap,
            double tolerance, double lineCurvePreference, IProgressObserver pProgressObserver) {
        var result = StrongReference.of(pixelMap);
        reportProgress(pProgressObserver, "Removing Lone Nodes ...", 0);
        pixelMapService.forEachPixel(result.get(), pixel -> {
            if (pixelService.isNode(result.get(), pixel)) {
                Node node = pixelMapService.getNode(result.get(), pixel).get();
                if (pixelService.countEdgeNeighbours(result.get(), node) == 0) {
                    result.update(r -> pixelMapService.setEdge(r, pixel, false, tolerance, lineCurvePreference));
                    result.update(r -> pixelMapService.setNode(r, pixel, false));
                }
            }
        });
        return result.get();
    }

    public ImmutablePixelMap process04b_removeBristles(
            @NotNull ImmutablePixelMap pixelMap,
            double tolerance,
            double lineCurvePreference,
            IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Removing Bristles ...", 0);
        var toBeRemoved = new Vector<ImmutableIXY>();
        var result = StrongReference.of(pixelMap);
        result.get().nodes().values().stream()
                .map(ImmutableIXY::copyOf)
                .forEach(node -> pixelService.getNodeNeighbours(result.get(), node)
                        .forEach(other -> {
                            var nodeSet = pixelService.allEdgeNeighbours(result.get(), node);
                            var otherSet = pixelService.allEdgeNeighbours(result.get(), other);
                            nodeSet.remove(other);
                            nodeSet.removeAll(otherSet);
                            otherSet.remove(node);
                            otherSet.removeAll(nodeSet);
                            if (nodeSet.isEmpty() && !toBeRemoved.contains(other)) {
                                // TODO should be a better check here to see whether it is better to remove the other node
                                toBeRemoved.add(node);
                            }
                })
        );
        result.update(r -> pixelMapService.nodesRemoveAll(r, toBeRemoved));
        toBeRemoved
                .forEach(pixel -> {
                    result.update(r -> pixelMapService.setEdge(r, pixel, false, tolerance, lineCurvePreference));
                    pixelService.allEdgeNeighbours(result.get(), pixel)
                            .forEach(pPixel -> result.update(r -> pixelMapService.calcIsNode(r, pPixel)));
                });
        return result.get();
    }

    public ImmutablePixelMap process05_generateChains(
            @NotNull ImmutablePixelMap pixelMap,
            IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating chains ...", 0);
        var result = StrongReference.of(pixelMap);
        var counter = Counter.createMaxCounter(pixelMap.nodes().size() + 1);
        var chains = Collections.synchronizedCollection(new ArrayList<ImmutablePixelChain>());
        pixelMap.nodes().values().parallelStream().forEach(node -> {
            counter.increase();
            reportProgress(pProgressObserver, "Generating chains ...", counter.getPercentInt());
            pixelMapChainGenerationService.generateChains(result.get(), node).stream()
                    .filter(pc -> 0 >= pc.getPixels().firstElement().get().compareTo(pc.getPixels().lastElement().get()))
                    .forEach(chains::add);
        });
        result.update(r -> pixelMapService.pixelChainsAddAll(r, chains));
        logger.info(() -> "Number of chains: " + result.get().pixelChains().size());
        return result.get();
    }

    public ImmutablePixelMap process05a_findLoops(
            @NotNull ImmutablePixelMap pixelMap, IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Finding loops ...", 0);
        var result = StrongReference.of(pixelMap);
        var pixelsInChains = Collections.synchronizedSet(new HashSet<ImmutableIXY>());
        result.get().pixelChains().stream().parallel()
                .flatMap(pc -> pc.getPixels().stream())
                .map(ImmutableIXY::copyOf)
                .forEach(pixelsInChains::add);
        var edges = pixelMap.data().entrySet().stream().parallel()
                .filter(e -> (e.getValue() | EDGE) != 0)
                .map(Map.Entry::getKey)
                .map(IXY::of)
                .collect(Collectors.toList());
        var counter = Counter.createMaxCounter(edges.size() + 1);
        edges.forEach(pixel -> {
            counter.increase();
            if (pixelService.isEdge(result.get(), pixel) && !pixelsInChains.contains(pixel)) {
                reportProgress(pProgressObserver, "Finding loops ...", counter.getPercentInt());
                result.update(r -> pixelMapService.setNode(r, pixel, true));
                pixelMapService.getNode(result.get(), pixel).ifPresent(node -> {
                    var chains = pixelMapChainGenerationService.generateChains(result.get(), node);
                    result.update(r -> pixelMapService.pixelChainsAddAll(r, chains));
                    chains.stream()
                            .flatMap(pc -> pc.getPixels().stream())
                            .map(ImmutableIXY::copyOf)
                            .forEach(pixelsInChains::add);
                });
            }
        });
        return result.get();
    }


    public ImmutablePixelMap process06_straightLinesRefineCorners(
            @NotNull ImmutablePixelMap pixelMap,
            double tolerance, IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Straight Lines ...", 0);
        var pegs = new Object[]{
                PixelChain.PegCounters.RefineCornersAttempted,
                PixelChain.PegCounters.RefineCornersSuccessful
        };
        var pegCounter = com.ownimage.perception.app.Services.getServices().getPegCounter();
        pegCounter.clear(pegs);
        logger.info(() -> "process06_straightLinesRefineCorners " + tolerance);
        var result = StrongReference.of(pixelMap);
        var refined = new Vector<ImmutablePixelChain>();
        result.get().pixelChains().forEach(pixelChain ->
                refined.add(pixelChainService.approximate(result.get(), pixelChain, tolerance)));
        result.update(r -> pixelMapService.pixelChainsClear(r));
        result.update(r -> pixelMapService.pixelChainsAddAll(r, refined));
        logger.info(pegCounter.getString(pegs));
        logger.info("approximate - done");
        return result.get();
    }

    public ImmutablePixelMap process07_mergeChains(
            @NotNull ImmutablePixelMap pixelMap, IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Merging Chains ...", 0);
        var result = StrongReference.of(pixelMap);
        logger.info(() -> "number of PixelChains: " + result.get().pixelChains().size());
        result.get().nodes().values().forEach(node ->
                result.update(r -> pixelMapService.mergePixelChains(r, node)));
        logger.info(() -> "number of PixelChains: " + result.get().pixelChains().size());
        return result.get();
    }

    public ImmutablePixelMap process08_refine(
            @NotNull ImmutablePixelMap pixelMap,
            double tolerance,
            double lineCurvePreference,
            IProgressObserver pProgressObserver) {
        var result = StrongReference.of(pixelMap);
        var pegs = new Object[]{
                PixelChain.PegCounters.StartSegmentStraightToCurveAttempted,
                PixelChain.PegCounters.StartSegmentStraightToCurveSuccessful,
                PixelChain.PegCounters.MidSegmentEatForwardAttempted,
                PixelChain.PegCounters.MidSegmentEatForwardSuccessful,
                PixelChain.PegCounters.refine01FirstSegmentAttempted,
                PixelChain.PegCounters.refine01FirstSegmentSuccessful
        };
        var pegCounter = com.ownimage.perception.app.Services.getServices().getPegCounter();
        pegCounter.clear(pegs);
        var pixelChainCount = result.get().pixelChains().size();
        if (pixelChainCount > 0) {
            var counter = Counter.createMaxCounter(pixelChainCount);
            reportProgress(pProgressObserver, "Refining ...", 0);
            var refined = new Vector<ImmutablePixelChain>();
            result.get().pixelChains().stream().parallel().forEach(pc -> {
                var refinedPC = pixelChainService.approximateCurvesOnly(result.get(), pc, tolerance, lineCurvePreference);
                refined.add(refinedPC);
                counter.increase();
                reportProgress(pProgressObserver, "Refining ...", counter.getPercentInt());
            });
            result.update(r -> pixelMapService.pixelChainsClear(r));
            result.update(r -> pixelMapService.pixelChainsAddAll(r, refined));
        }
        logger.info(pegCounter.getString(pegs));
        return result.get();
    }

    public @NotNull ImmutablePixelMap thin(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IXY pixel,
            double tolerance,
            double lineCurvePreference) {
        if (!pixelService.isEdge(pixelMap, pixel)) {
            return pixelMap;
        }
        var result = pixelMap;
        boolean canEliminate = false;
        for (int[] set : eliminate) {
            canEliminate |= pixelService.isEdge(pixelMap, pixelService.getNeighbour(pixel, set[0]))
                    && pixelService.isEdge(pixelMap, pixelService.getNeighbour(pixel, set[1]))
                    && !pixelService.isEdge(pixelMap, pixelService.getNeighbour(pixel, set[2]));
        }
        if (canEliminate) {
            result = setEdge(result, pixel, false, tolerance, lineCurvePreference);
            result = pixelMapService.nodeRemove(result, pixel);
        }
        return result;
    }

    public @NotNull ImmutablePixelMap setEdge(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull IXY pixel,
            boolean isEdge,
            double tolerance,
            double lineCurvePreference) {
        if (pixelService.isEdge(pixelMap, pixel) == isEdge) {
            return pixelMap;
        }
        var result = StrongReference.of(pixelMap);
        if (pixelService.isNode(pixelMap, pixel) && !isEdge) {
            result.update(r -> pixelMapService.setNode(r, pixel, false));
        }
        result.update(r -> pixelMapService.setData(r, pixel, isEdge, EDGE));
        result.update(r -> calcIsNode(r, pixel));
        pixelService.getNeighbours(pixel)
                .forEach(p -> {
                    result.update(r -> thin(r, p, tolerance, lineCurvePreference));
                    result.update(r -> calcIsNode(r, p));
                });
        result.update(r -> thin(r, pixel, tolerance, lineCurvePreference));
        if (result.get().autoTrackChanges()) {
            if (isEdge) { // turning pixel on
                trackPixelOn(pixelMap, pixel, tolerance, lineCurvePreference);
            } else { // turning pixel off
                trackPixelOff(pixelMap, pixel, tolerance, lineCurvePreference);
            }
        }
        return result.get();
    }

    public @NotNull ImmutablePixelMap trackPixelOn(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull IXY pPixel,
            double tolerance,
            double lineCurvePreference) {
        List<IXY> pixels = Collections.singletonList(pPixel);
        return trackPixelOn(pixelMap, pixels, tolerance, lineCurvePreference);
    }

    public @NotNull ImmutablePixelMap trackPixelOff(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull IXY pPixel,
            double tolerance,
            double lineCurvePreference) {
        List<IXY> pixels = Collections.singletonList(pPixel);
        return trackPixelOff(pixelMap, pixels, tolerance, lineCurvePreference);
    }

    public @NotNull ImmutablePixelMap trackPixelOff(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull List<IXY> pixels,
            double tolerance,
            double lineCurvePreference) {
        var result = StrongReference.of(pixelMap);
        pixels.forEach(pixel -> pixelMapService.getPixelChains(result.get(), pixel).forEach(pc -> {
            result.update(r -> pixelMapService.pixelChainRemove(r, pc));
            pc.streamPixels()
                    .filter(pPixel1 -> pixelService.isNode(result.get(), pPixel1))
                    .forEach(chainPixel -> pixelMapService.getNode(result.get(), chainPixel)
                            .ifPresent(node -> {
                                var generatedChains = pixelMapChainGenerationService.generateChains(result.get(), node);
                                var chains = generatedChains
                                        .parallelStream()
                                        .map(pc2 -> pixelChainService.approximateCurvesOnly(result.get(), pc2, tolerance, lineCurvePreference))
                                        .collect(Collectors.toList());
                                result.update(r -> pixelMapService.addPixelChains(r, chains));
                            })
                    );
        }));
        return result.get();
    }

    public @NotNull ImmutablePixelMap trackPixelOn(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull Collection<IXY> pixels,
            double tolerance,
            double lineCurvePreference) {
        if (pixels.isEmpty()) {
            return pixelMap;
        }

        var result = StrongReference.of(pixelMap);

        var nodes = new HashSet<Node>();
        pixels.forEach(pixel -> {
            pixelMapService.getNode(result.get(), pixel).ifPresent(nodes::add);
            pixelService.getNeighbours(pixel)
                    .forEach(neighbour -> {
                        pixelMapService.getPixelChains(result.get(), neighbour)
                                .forEach(pc -> {
                                    pixelChainService.getStartNode(result.get(), pc).ifPresent(nodes::add);
                                    pixelChainService.getEndNode(result.get(), pc).ifPresent(nodes::add);
                                    result.update(r -> pixelMapService.removePixelChain(r, pc));
                                });
                        pixelMapService.getNode(result.get(), neighbour).ifPresent(nodes::add); // this is the case where is is not in a chain
                    });
        });

        nodes.stream()
                .flatMap(n -> pixelMapService.generateChainsAndApproximate(result.get(), n, tolerance, lineCurvePreference))
                .forEach(pc -> result.update(r -> pixelMapService.addPixelChain(r, pc)));

        // if there is a loop then this ensures that it is closed and converted to pixel chain
        pixels.stream()
                .filter(p -> pixelService.isEdge(result.get(), p))
                .findFirst()
                .filter(p -> !pixelService.isNode(result.get(), p))
                .filter(p -> pixelMapService.getPixelChains(result.get(), p).isEmpty())
                .stream()
                .peek(p -> result.update(r -> pixelMapService.setNode(r, p, true)))
                .flatMap(p -> pixelMapService.generateChainsAndApproximate(result.get(), Node.ofIXY(p), tolerance, lineCurvePreference))
                .forEach(pc -> result.update(r -> pixelMapService.addPixelChain(r, pc)));

        return result.get();
    }


    public @NotNull ImmutablePixelMap calcIsNode(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull IXY pixel) {
        boolean shouldBeNode = false;
        if (pixelService.isEdge(pixelMap, pixel)) {
            // here we use transitions to eliminate double counting connected neighbours
            // also note the the number of transitions is twice the number of neighbours
            int transitionCount = pixelMapService.countEdgeNeighboursTransitions(pixelMap, pixel);
            if (transitionCount != 4) {
                shouldBeNode = true;
            }
        }
        return pixelMapService.setNode(pixelMap, pixel, shouldBeNode);
    }

}
