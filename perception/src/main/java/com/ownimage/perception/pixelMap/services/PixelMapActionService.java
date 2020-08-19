package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.EqualizeValues;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.immutable.IXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.transform.CannyEdgeTransform;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class PixelMapActionService {

    private final static Logger logger = Framework.getLogger();
    private PixelMapService pixelMapService;
    private PixelMapChainGenerationService pixelMapChainGenerationService;
    private PixelMapApproximationService pixelMapApproximationService;
    private PixelChainService pixelChainService;
    private PixelService pixelService;

    @Autowired
    public void setPixelMapService(PixelMapService pixelMapService) {
        this.pixelMapService = pixelMapService;
    }

    @Autowired
    public void setPixelMapChainGenerationService(PixelMapChainGenerationService pixelMapChainGenerationService) {
        this.pixelMapChainGenerationService = pixelMapChainGenerationService;
    }

    @Autowired
    public void setPixelMapApproximationService(PixelMapApproximationService pixelMapApproximationService) {
        this.pixelMapApproximationService = pixelMapApproximationService;
    }

    @Autowired
    public void setPixelChainService(PixelChainService pixelChainService) {
        this.pixelChainService = pixelChainService;
    }

    @Autowired
    public void setPixelService(PixelService pixelService) {
        this.pixelService = pixelService;
    }

    public ImmutablePixelMap actionPixelOn(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IXY pixel,
            double tolerance,
            double lineCurvePreference) {
        var pixels = Collections.singletonList(pixel);
        return actionPixelOn(pixelMap, pixels, tolerance, lineCurvePreference);
    }

    public ImmutablePixelMap actionPixelOn(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Collection<IXY> pixels,
            double tolerance,
            double lineCurvePreference) {
        var result = StrongReference.of(pixelMap);
        result.update(r -> r.withAutoTrackChanges(false));
        pixels.forEach(pixel ->
                result.update(r -> pixelMapApproximationService.setEdge(r, pixel, true, tolerance, lineCurvePreference)));
        result.update(r -> r.withAutoTrackChanges(true));
        result.update(r -> pixelMapApproximationService.trackPixelOn(r, pixels, tolerance, lineCurvePreference));
        return result.get();
    }

    public ImmutablePixelMap actionPixelOff(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IXY pixel,
            int cursorSize,
            double tolerance,
            double lineCurvePreference) {
        var result = StrongReference.of(pixelMap);
        double radius = (double) cursorSize / result.get().height();
        new Range2D(pixel.getX() - cursorSize, pixel.getX() + cursorSize, pixel.getY() - cursorSize, pixel.getY() + cursorSize)
                .forEach((x, y) ->
                        pixelMapService.getOptionalPixelAt(result.get(), x, y)
                                .filter(p -> pixelService.isEdge(result.get(), p))
                                .filter(p -> pixel
                                        .getUHVWMidPoint(result.get().height())
                                        .distance(p.getUHVWMidPoint(result.get().height())) < radius)
                                .ifPresent(p -> result.update(r -> pixelMapService.setEdge(r, p, false, tolerance, lineCurvePreference)))
                );
        return result.get();
    }

    public ImmutablePixelMap actionDeletePixelChain(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Collection<IXY> pixels,
            double tolerance,
            double lineCurvePreference) {
        var clone = StrongReference.of(pixelMap.withAutoTrackChanges(false));
        pixels.stream()
                .filter(p -> pixelService.isEdge(clone.get(), p))
                .forEach(p -> pixelMapService.getPixelChains(clone.get(), p)
                        .forEach(pc -> {
                            // TODO in the implementation of the method below make the parameter immutable
                            clone.update(c -> pixelMapService.setEdge(c, pc, tolerance, lineCurvePreference));
                            pixelChainService.getStartNode(clone.get(), pc)
                                    .ifPresent(n -> clone.update(c -> pixelMapService.nodeRemove(c, n)));
                            pixelChainService.getEndNode(clone.get(), pc)
                                    .ifPresent(n -> clone.update(c -> pixelMapService.nodeRemove(c, n)));
                            clone.update(c -> c.withPixelChains(c.pixelChains().remove(pc)));
                            clone.update(c -> pixelMapService.indexSegments(c, pc, false));
                            clone.update(c -> pixelMapService.indexSegments(c, pc, false));
                        }));
        return clone.get().withAutoTrackChanges(true);
    }

    public ImmutablePixelMap actionEqualizeValues(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull EqualizeValues values) {
        if (pixelMap.pixelChains().size() == 0) {
            return pixelMap;
        }
        // TODO do not like this mutable parameter
        var totalLength = new StrongReference<>(0);
        pixelMap.pixelChains().forEach(chain -> totalLength.update(len -> len + chain.getPixelCount()));
        var sortedChains = pixelMapService.getPixelChainsSortedByLength(pixelMap);
        int shortThreshold = (int) (totalLength.get() * values.getIgnoreFraction());
        int mediumThreshold = (int) (totalLength.get() * (values.getIgnoreFraction() + values.getShortFraction()));
        int longThreshold = (int) (totalLength.get() * (values.getIgnoreFraction() + values.getShortFraction() +
                values.getMediumFraction()));
        Integer shortLength = null;
        Integer mediumLength = null;
        Integer longLength = null;
        int currentLength = 0;
        for (PixelChain chain : sortedChains) {
            currentLength += chain.getPixelCount();
            if (shortLength == null && currentLength > shortThreshold) {
                shortLength = chain.getPixelCount();
            }
            if (mediumLength == null && currentLength > mediumThreshold) {
                mediumLength = chain.getPixelCount();
            }
            if (longLength == null && currentLength > longThreshold) {
                longLength = chain.getPixelCount();
                break;
            }
        }
        values.setReturnValues(shortLength, mediumLength, longLength);
        return pixelMap;
    }

    public ImmutablePixelMap actionSetPixelChainThickness(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Collection<IXY> pixels,
            @NotNull Function<PixelChain, Thickness> mapper) {
        var result = StrongReference.of(pixelMap);
        pixels.stream()
                .filter(p -> pixelService.isEdge(pixelMap, p))
                .flatMap(p -> pixelMapService.getPixelChains(pixelMap, p).stream())
                .distinct()
                .forEach(pc -> {
                    var currentThickness = pc.getThickness();
                    var newThickness = mapper.apply(pc);
                    if (newThickness != currentThickness) {
                        result.update(r -> pixelMapService.pixelChainRemove(r, pc));
                        result.update(r -> pixelMapService.pixelChainAdd(r, pixelChainService.withThickness(pc, newThickness)));
                    }
                });
        return result.get();
    }

    public ImmutablePixelMap actionPixelToggle(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IXY pixel,
            double tolerance,
            double lineCurvePreference) {
        var newValue = !pixelService.isEdge(pixelMap, pixel);
        return pixelMapService.setEdge(pixelMap, pixel, newValue, tolerance, lineCurvePreference);
    }

    public ImmutablePixelMap actionPixelChainDeleteAllButThis(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Pixel pixel) {
        var pixelChains = pixelMapService.getPixelChains(pixelMap, pixel);
        if (pixelChains.size() != 1) {
            return pixelMap;
        }

        var result = pixelMapService.clearAllPixelChains(pixelMap);
        result = pixelMapService.pixelChainsAddAll(result, pixelChains);
        return result;
    }


    public ImmutablePixelMap actionPixelChainApproximateCurvesOnly(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel) {
        if (pixelMapService.getPixelChains(pixelMap, pixel).isEmpty()) {
            return pixelMap;
        }
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        var clone = StrongReference.of(pixelMap);
        pixelMapService.getPixelChains(clone.get(), pixel).forEach(pc -> {
            clone.update(c -> pixelMapService.pixelChainRemove(c, pc));
            var pc2 = pixelChainService.approximateCurvesOnly(clone.get(), pc, tolerance, lineCurvePreference);
            clone.update(c -> pixelMapService.pixelChainAdd(c, pc2));
        });
        //copy.indexSegments();
        return clone.get();
    }

    public ImmutablePixelMap actionReapproximate(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IPixelMapTransformSource transformSource) {
        SplitTimer.split("PixelMap actionReapproximate() start");
        var result = StrongReference.of(pixelMap);
        var updates = new Vector<ImmutablePixelChain>();
        var tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        var lineCurvePreference = transformSource.getLineCurvePreference();
        result.get().pixelChains().stream()
                .parallel()
                .map(pc -> pixelChainService.approximate(result.get(), pc, tolerance))
                .map(pc -> pixelChainService.refine(result.get(), pc, lineCurvePreference))
                //.map(pc -> pc.indexSegments(this, true))
                .forEach(updates::add);
        result.update(r -> pixelMapService.pixelChainsClear(r));
        result.update(r -> pixelMapService.pixelChainsAddAll(r, updates));
        SplitTimer.split("PixelMap actionReapproximate() end");
        return result.get();
    }

    public ImmutablePixelMap actionRerefine(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull CannyEdgeTransform transformSource) {
        var result = StrongReference.of(pixelMap);
        var updates = new Vector<ImmutablePixelChain>();
        var tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        var lineCurvePreference = transformSource.getLineCurvePreference();
        result.get().pixelChains().stream()
                .parallel()
                .map(pc -> pixelChainService.refine(result.get(), pc, lineCurvePreference))
                .forEach(updates::add);
        result.update(r -> pixelMapService.pixelChainsClear(r));
        result.update(r -> pixelMapService.pixelChainsAddAll(r, updates));
        return result.get();
    }

    public ImmutablePixelMap actionSetPixelChainDefaultThickness(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull CannyEdgeTransform transform) {
        int shortLength = transform.getShortLineLength();
        int mediumLength = transform.getMediumLineLength();
        int longLength = transform.getLongLineLength();
        var updates = new Vector<ImmutablePixelChain>();
        pixelMap.pixelChains().forEach(chain -> updates.add(pixelChainService.withThickness(chain, shortLength, mediumLength, longLength)));
        var result = pixelMapService.clearAllPixelChains(pixelMap);
        result = pixelMapService.pixelChainsAddAll(result, updates);
        return result;
    }

    public ImmutablePixelMap actionVertexAdd(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Pixel pixel,
            double lineCurvePreference) {
        var pixelChains = pixelMapService.getPixelChains(pixelMap, pixel);
        if (pixelChains.size() != 1) {
            return pixelMap;
        }
        var pixelChain = pixelChains.get(0);
        var pixelIndex = pixelChain.getPixels().indexOf(pixel);
        var optionalNextVertex = pixelChain.getVertexes().stream()
                .filter(v -> v.getPixelIndex() >= pixelIndex)
                .findFirst();
        if (optionalNextVertex.isEmpty() || optionalNextVertex.get().getPixelIndex() == pixelIndex) {
            return pixelMap;
        }
        var vertexIndex = optionalNextVertex.get().getVertexIndex();
        var position = pixel.getUHVWMidPoint(pixelMap.height());
        var newVertex = ImmutableVertex.of(0, pixelIndex, position);
        var updatedPixelChain = StrongReference.of(pixelChain.changeVertexes(v -> v.add(optionalNextVertex.get().getVertexIndex(), newVertex)));
        updatedPixelChain.update(upc -> upc.changeSegments(s -> s.set(vertexIndex - 1, SegmentFactory.createTempStraightSegment(upc, vertexIndex - 1))));
        updatedPixelChain.update(upc -> upc.changeSegments(s -> s.add(vertexIndex, SegmentFactory.createTempStraightSegment(upc, vertexIndex))));
        updatedPixelChain.update(upc -> pixelChainService.resequence(pixelMap, upc));
        updatedPixelChain.update(upc -> pixelChainService.refine(pixelMap, upc, lineCurvePreference));


        var result = StrongReference.of(pixelMap);
        result.update(r -> pixelMapService.removePixelChain(r, pixelChain));
        result.update(r -> pixelMapService.addPixelChain(r, updatedPixelChain.get()));
        return result.get();
    }

    public ImmutablePixelMap actionVertexRemove(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Pixel pixel,
            double lineCurvePreference) {
        var pixelChains = pixelMapService.getPixelChains(pixelMap, pixel);
        if (pixelChains.size() != 1) {
            return pixelMap;
        }
        var pixelChain = pixelChains.get(0);
        var optionalVertex = pixelChain.getVertexes().stream()
                .filter(v -> pixelChain.getPixels().get(v.getPixelIndex()).equals(pixel))
                .findFirst();
        if (optionalVertex.isEmpty()) {
            return pixelMap;
        }

        var vertex = optionalVertex.get();
        int vertexIndex = vertex.getVertexIndex();
        if (vertexIndex == 0 || vertexIndex == pixelChain.getVertexCount()) {
            return pixelMap;
        }

        var updatedPixelChain = StrongReference.of(pixelChain
                .changeSegments(s -> s.remove(vertexIndex))
                .changeVertexes(v -> v.remove(vertexIndex)));
        updatedPixelChain.update(upc -> upc.changeSegments(s -> s.set(vertexIndex - 1, SegmentFactory.createTempStraightSegment(upc, vertexIndex - 1))));
        updatedPixelChain.update(upc -> pixelChainService.resequence(pixelMap, upc));
        updatedPixelChain.update(upc -> pixelChainService.refine(pixelMap, upc, lineCurvePreference));

        var result = StrongReference.of(pixelMap);
        result.update(r -> pixelMapService.removePixelChain(r, pixelChain));
        result.update(r -> pixelMapService.addPixelChain(r, updatedPixelChain.get()));
        return result.get();
    }

}
