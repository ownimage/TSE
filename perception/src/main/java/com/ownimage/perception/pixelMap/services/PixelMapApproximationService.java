package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
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

public class PixelMapApproximationService {

    private final static Logger logger = Framework.getLogger();

    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    private static PixelMapChainGenerationService pixelMapChainGenerationService = Services.getDefaultServices().pixelMapChainGenerationService();
    private static PixelMapMappingService pixelMapMappingService = Services.getDefaultServices().getPixelMapMappingService();
    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();
    private static PixelService pixelService = Services.getDefaultServices().getPixelService();
    private static PixelMapService pixelMapService = Services.getDefaultServices().getPixelMapService();

    public ImmutablePixelMapData actionProcess(
            @NotNull PixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            IProgressObserver progress) {
        var result = pixelMap.withAutoTrackChanges(false);
        result = process01_reset(result, progress);
        result = process02_thin(result, transformSource, progress);
        result = process03_generateNodes(result, progress);
        result = process04b_removeBristles(result, transformSource, progress);
        result = process04a_removeLoneNodes(result, transformSource, progress);
        result = process05_generateChains(result, progress);
        result = process05a_findLoops(result, progress);
        result = process06_straightLinesRefineCorners(result, transformSource, progress);
        var mutable = pixelMapMappingService.toPixelMap(result, transformSource);
        return actionProcess(mutable, progress);
    }

    public @NotNull ImmutablePixelMapData process01_reset(
            @NotNull PixelMapData pixelMap,
            IProgressObserver progress) {
        reportProgress(progress, "Resetting ...", 0);
        var result = pixelMap
                .withData(pixelMap.data().forEach(d -> (byte) (d & EDGE)))
                .withNodes(pixelMap.nodes().clear())
                .withPixelChains(pixelMap.pixelChains().clear())
                .withSegmentIndex(pixelMap.segmentIndex().clear());
        logger.info("############## resetting done");
        return result;
    }

    private void reportProgress(IProgressObserver progress, String pProgressString, int pPercent) {
        if (progress != null) {
            progress.setProgress(pProgressString, pPercent);
        }
    }

    // TODO need to work out how to have a progress bar
    public ImmutablePixelMapData process02_thin(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            IProgressObserver progress) {
        reportProgress(progress, "thinning ...", 0);
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        new Range2D(pixelMap.width(), pixelMap.height())
                .forEach((x, y) -> result.update(r -> thin(r, transformSource, pixelMapService.getPixelOptionalAt(r, x, y).orElseThrow())));
        return result.get();
    }

    public ImmutablePixelMapData process03_generateNodes(
            @NotNull PixelMapData pixelMap, IProgressObserver pProgressObserver) {
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        reportProgress(pProgressObserver, "Generating Nodes ...", 0);
        pixelMapService.forEachPixel(result.get(), pixel -> {
            var calsIsNodeResult = pixelMapService.calcIsNode(result.get(), pixel);
            result.set(calsIsNodeResult._1);
            if (calsIsNodeResult._2) {
                result.update(r -> pixelMapService.nodeAdd(r, pixel));
            }
        });
        return result.get();
    }

    public ImmutablePixelMapData process04a_removeLoneNodes(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            IProgressObserver pProgressObserver) {
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        reportProgress(pProgressObserver, "Removing Lone Nodes ...", 0);
        pixelMapService.forEachPixel(result.get(), pixel -> {
            if (pixelService.isNode(result.get(), pixel)) {
                Node node = pixelMapService.getNode(result.get(), pixel).get();
                if (node.countEdgeNeighbours(result.get()) == 0) {
                    result.update(r -> pixelMapService.setEdge(r, transformSource, pixel, false));
                    result.update(r -> pixelMapService.setNode(r, pixel, false));
                    result.update(r -> pixelMapService.setVisited(r, pixel, false));
                }
            }
        });
        return result.get();
    }

    public ImmutablePixelMapData process04b_removeBristles(
            @NotNull PixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Removing Bristles ...", 0);
        var toBeRemoved = new Vector<Pixel>();
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        result.get().nodes().values().forEach(node -> node.getNodeNeighbours(result.get()).forEach(other -> {
                    Set<Pixel> nodeSet = node.allEdgeNeighbours(result.get());
                    Set<Pixel> otherSet = other.allEdgeNeighbours(result.get());
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
                    result.update(r -> pixelMapService.setEdge(r, transformSource, pixel, false));
                    pixel.allEdgeNeighbours(result.get())
                            .forEach(pPixel -> result.update(r -> pixelMapService.calcIsNode(r, pPixel)._1));
                });
        return result.get();
    }

    public ImmutablePixelMapData process05_generateChains(
            @NotNull PixelMapData pixelMap,
            IProgressObserver pProgressObserver) {
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        pixelMap.nodes().values().forEach(node -> {
            var chains = pixelMapChainGenerationService.generateChains(result.get(), node);
            result.set(pixelMapService.pixelChainsAddAll(chains._1, chains._2));
        });
        pixelMapService.forEachPixel(result.get(), pixel -> {
            if (pixel.isUnVisitedEdge(result.get())) {
                pixelMapService.getNode(result.get(), pixel).ifPresent(node -> {
                    var chains = pixelMapChainGenerationService.generateChains(result.get(), node);
                    result.set(pixelMapService.pixelChainsAddAll(chains._1, chains._2));
                });
            }
        });
        logger.info(() -> "Number of chains: " + result.get().pixelChains().size());
        return result.get();
    }

    public ImmutablePixelMapData process05a_findLoops(
            @NotNull PixelMapData pixelMap, IProgressObserver pProgressObserver) {
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        pixelMapService.forEachPixel(result.get(), pixel -> {
            if (pixelService.isEdge(result.get(), pixel) && !pixelService.isInChain(result.get(), pixel)) {
                result.update(r -> pixelMapService.setNode(r, pixel, true));
                pixelMapService.getNode(result.get(), pixel).ifPresent(node -> {
                    var chains = pixelMapChainGenerationService.generateChains(result.get(), node);
                    result.update(r -> pixelMapService.pixelChainsAddAll(chains._1, chains._2));
                });
            }
        });
        return result.get();
    }

    public ImmutablePixelMapData  process06_straightLinesRefineCorners(
            @NotNull PixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            IProgressObserver pProgressObserver
    ) {
        reportProgress(pProgressObserver, "Generating Straight Lines ...", 0);
        double tolerance = transformSource.getLineTolerance() / pixelMap.height();
        logger.info(() -> "process06_straightLinesRefineCorners " + tolerance);
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        var refined = new Vector<PixelChain>();
        result.get().pixelChains().forEach(pixelChain ->
                refined.add(pixelChainService.approximate(result.get(), pixelChain, tolerance)));
        result.update(r -> pixelMapService.pixelChainsClear(r));
        result.update(r -> pixelMapService.pixelChainsAddAll(r, refined));
        logger.info("approximate - done");
        return result.get();
    }


    public @NotNull ImmutablePixelMapData thin(
            @NotNull ImmutablePixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel) {
        if (!pixelService.isEdge(pixelMap, pixel)) {
            return pixelMap;
        }
        var result = pixelMap;
        boolean canEliminate = false;
        for (int[] set : eliminate) {
            canEliminate |= pixelService.isEdge(pixelMap, pixel.getNeighbour(set[0]))
                    && pixelService.isEdge(pixelMap, pixel.getNeighbour(set[1]))
                    && !pixelService.isEdge(pixelMap, pixel.getNeighbour(set[2]));
        }
        if (canEliminate) {
            result = setEdge(result, transformSource, pixel, false);
            result = pixelMapService.nodeRemove(result, pixel);
        }
        logger.info("############## thinning done");
        return result;
    }

    public @NotNull ImmutablePixelMapData setEdge(
            @NotNull ImmutablePixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NonNull Pixel pixel,
            boolean isEdge) {
        if (pixelService.isEdge(pixelMap, pixel) == isEdge) {
            return pixelMap;
        }
        var result = StrongReference.of(pixelMap);
        if (pixelService.isNode(pixelMap, pixel) && !isEdge) {
            result.update(r -> pixelMapService.setNode(r, pixel, false));
        }
        result.update(r -> pixelMapService.setData(r, pixel, isEdge, EDGE));
        result.update(r -> calcIsNode(r, pixel));
        pixel.getNeighbours().forEach(p -> {
            result.update(r -> thin(r, transformSource, p));
            result.update(r -> calcIsNode(r, p));
        });
        result.update(r -> thin(r, transformSource, pixel));
        if (result.get().autoTrackChanges()) {
            if (isEdge) { // turning pixel on
                trackPixelOn(pixelMap, transformSource, pixel);
            } else { // turning pixel off
                trackPixelOff(pixelMap, transformSource, pixel);
            }
        }
        return result.get();
    }

    public @NotNull ImmutablePixelMapData trackPixelOn(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NonNull Pixel pPixel) {
        List<Pixel> pixels = Collections.singletonList(pPixel);
        return trackPixelOn(pixelMap, transformSource, pixels);
    }

    public @NotNull ImmutablePixelMapData trackPixelOff(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NonNull Pixel pPixel) {
        List<Pixel> pixels = Collections.singletonList(pPixel);
        return trackPixelOff(pixelMap, transformSource, pixels);
    }

    public @NotNull ImmutablePixelMapData trackPixelOff(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NonNull List<Pixel> pixels) {
        double tolerance = transformSource.getLineTolerance() / pixelMap.height();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        pixels.forEach(pixel -> pixelMapService.getPixelChains(result.get(), pixel).forEach(pc -> {
            result.update(r -> pixelMapService.pixelChainRemove(r, pc));
            pc.getPixels().stream().forEach(p -> {
                result.update(r -> pixelMapService.setInChain(r, p, false));
                result.update(r -> pixelMapService.setVisited(r, p, false));
            });
            pc.streamPixels()
                    .filter(pPixel1 -> pixelService.isNode(result.get(), pPixel1))
                    .forEach(chainPixel -> pixelMapService.getNode(result.get(), chainPixel)
                            .ifPresent(node -> {
                                var generatedChains = pixelMapChainGenerationService.generateChains(result.get(), node);
                                result.update(r -> generatedChains._1);
                                var chains = generatedChains._2
                                        .parallelStream()
                                        .map(pc2 -> pixelChainService.approximateCurvesOnly(result.get(), pc2, tolerance, lineCurvePreference))
                                        .collect(Collectors.toList());
                                result.update(r -> pixelMapService.addPixelChains(r, chains));
                            })
                    );
        }));
        return result.get();
    }

    public @NotNull ImmutablePixelMapData trackPixelOn(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NonNull Collection<Pixel> pixels) {
        if (pixels.isEmpty()) {
            return pixelMapMappingService.toImmutablePixelMapData(pixelMap);
        }

        var result = StrongReference.of(pixelMapService.resetInChain(pixelMap));
        result.update(r -> pixelMapService.resetVisited(r));

        var nodes = new HashSet<Node>();
        pixels.forEach(pixel -> {
            pixelMapService.getNode(result.get(), pixel).ifPresent(nodes::add);
            pixel.getNeighbours()
                    .forEach(neighbour -> {
                        pixelMapService.getPixelChains(result.get(), neighbour)
                                .forEach(pc -> {
                                    pixelChainService.getStartNode(result.get(), pc).ifPresent(nodes::add);
                                    pixelChainService.getEndNode(result.get(), pc).ifPresent(nodes::add);
                                    result.update(r -> pixelMapService.removePixelChain(r, pc));
                                });
                        neighbour.getNode(result.get()).ifPresent(nodes::add); // this is the case where is is not in a chain
                    });
        });

        nodes.stream()
                .map(n -> pixelMapService.generateChainsAndApproximate(result.get(), transformSource, n))
                .peek(gca -> result.update(r -> gca._1))
                .flatMap(r -> r._2)
                .forEach(pc -> result.update(r -> pixelMapService.addPixelChain(r, pc)));

        // if there is a loop then this ensures that it is closed and converted to pixel chain
        pixels.stream()
                .filter(p -> pixelService.isEdge(result.get(), p))
                .findFirst()
                .filter(p -> !pixelService.isNode(result.get(), p))
                .filter(p -> pixelMapService.getPixelChains(result.get(), p).isEmpty())
                .stream()
                .peek(p -> result.update(r -> pixelMapService.setNode(r, p, true)))
                .map(p -> pixelMapService.generateChainsAndApproximate(result.get(), transformSource, new Node(p)))
                .peek(gca -> result.update(r -> gca._1))
                .flatMap(r -> r._2)
                .forEach(pc -> result.update(r -> pixelMapService.addPixelChain(r, pc)));

        return result.get();
    }


    public @NotNull ImmutablePixelMapData calcIsNode (
            @NotNull ImmutablePixelMapData pixelMap,
            @NonNull Pixel pixel){
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

    public ImmutablePixelMapData actionProcess (@NotNull PixelMap pixelMap, IProgressObserver pProgressObserver){
        try {
            SplitTimer.split("PixelMap actionProcess() start");

            logger.info("############## findLoops done");

            var pegs = new Object[]{
                    IPixelChain.PegCounters.RefineCornersAttempted,
                    IPixelChain.PegCounters.RefineCornersSuccessful
            };
            pixelMap.getPegCounter().clear(pegs);
            logger.info(pixelMap.getPegCounter().getString(pegs));
            pixelMap.process07_mergeChains(pProgressObserver);
            pixelMapService.validate(pixelMap);
            logger.info("############## process07_mergeChains done");

            pegs = new Object[]{
                    IPixelChain.PegCounters.StartSegmentStraightToCurveAttempted,
                    IPixelChain.PegCounters.StartSegmentStraightToCurveSuccessful,
                    IPixelChain.PegCounters.MidSegmentEatForwardAttempted,
                    IPixelChain.PegCounters.MidSegmentEatForwardSuccessful,
                    IPixelChain.PegCounters.refine01FirstSegmentAttempted,
                    IPixelChain.PegCounters.refine01FirstSegmentSuccessful
            };
            pixelMap.getPegCounter().clear(pegs);
            pixelMap.process08_refine(pProgressObserver);
            logger.info(pixelMap.getPegCounter().getString(pegs));
            pixelMapService.validate(pixelMap);
            logger.info("############## process08_refine done");
            // // reapproximate(null, mTransformSource.getLineTolerance());
//                pixelMap.validate();
//                logger.info("############## validate done");
            //process04a_removeLoneNodes();
            pixelMap.indexSegments();
            logger.info("############## indexSegments done");
            pixelMapService.validate(pixelMap);
            //
        } catch (Exception pEx) {
            logger.info(() -> "pEx");
            Framework.logThrowable(logger, Level.INFO, pEx);
        } finally {
            // pProgress.hideProgressBar();
            SplitTimer.split("PixelMap actionProcess() end");
        }
        return pixelMap.withAutoTrackChanges(true);
    }
}
