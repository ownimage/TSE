package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.EqualizeValues;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.transform.CannyEdgeTransform;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.function.Function;
import java.util.logging.Logger;

public class PixelMapActionService {

    private final static Logger logger = Framework.getLogger();

    private static PixelMapService pixelMapService = Services.getDefaultServices().getPixelMapService();
    private static PixelMapChainGenerationService pixelMapChainGenerationService = Services.getDefaultServices().getPixelMapGenerationService();
    private static PixelMapApproximationService pixelMapApproximationService = Services.getDefaultServices().getPixelMapApproximationService();
    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();
    private static PixelService pixelService = Services.getDefaultServices().getPixelService();

    public ImmutablePixelMapData actionPixelOn(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel) {
        var pixels = Collections.singletonList(pixel);
        return actionPixelOn(pixelMap, transformSource, pixels);
    }

    public ImmutablePixelMapData actionPixelOn(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Collection<Pixel> pixels) {
        var result = StrongReference.of(pixelMap);
        result.update(r -> r.withAutoTrackChanges(false));
        pixels.forEach(pixel ->
                result.update(r -> pixelMapApproximationService.setEdge(r, transformSource, pixel, true)));
        result.update(r -> r.withAutoTrackChanges(true));
        result.update(r -> pixelMapApproximationService.trackPixelOn(r, transformSource, pixels));
        return result.get();
    }

    public ImmutablePixelMapData actionPixelOff(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel,
            int cursorSize) {
        val result = StrongReference.of(pixelMap);
        double radius = (double) cursorSize / result.get().height();
        new Range2D(pixel.getX() - cursorSize, pixel.getX() + cursorSize, pixel.getY() - cursorSize, pixel.getY() + cursorSize)
                .forEach((x, y) ->
                        pixelMapService.getOptionalPixelAt(result.get(), x, y)
                                .filter(p -> pixelService.isEdge(result.get(), p))
                                .filter(p -> pixel
                                        .getUHVWMidPoint(result.get().height())
                                        .distance(p.getUHVWMidPoint(result.get().height())) < radius)
                                .ifPresent(p -> result.update(r -> pixelMapService.setEdge(r, transformSource, p, false)))
                );
        return result.get();
    }

    public ImmutablePixelMapData actionDeletePixelChain(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Collection<Pixel> pixels) {
        var clone = StrongReference.of(pixelMap.withAutoTrackChanges(false));
        pixels.stream()
                .filter(p -> pixelService.isEdge(clone.get(), p))
                .forEach(p -> pixelMapService.getPixelChains(clone.get(), p)
                        .forEach(pc -> {
                            // TODO in the implementation of the method below make the parameter immutable
                            clone.update(c -> pixelMapService.clearInChainAndVisitedThenSetEdge(c, transformSource, pc));
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

    public ImmutablePixelMapData actionEqualizeValues(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull EqualizeValues values) {
        if (pixelMap.pixelChains().size() == 0) {
            return pixelMap;
        }
        // TODO do not like this mutable parameter
        var totalLength = new StrongReference<>(0);
        pixelMap.pixelChains().forEach(chain -> totalLength.update(len -> len + chain.getPixelCount()));
        Vector<PixelChain> sortedChains = pixelMapService.getPixelChainsSortedByLength(pixelMap);
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

    public ImmutablePixelMapData actionSetPixelChainThickness(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<Pixel> pixels,
            @NotNull Function<PixelChain, IPixelChain.Thickness> mapper) {
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

    public ImmutablePixelMapData actionPixelToggle(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel) {
        var newValue = !pixelService.isEdge(pixelMap, pixel);
        return pixelMapService.setEdge(pixelMap, transformSource, pixel, newValue);
    }

    public ImmutablePixelMapData actionPixelChainDeleteAllButThis(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel) {
        val pixelChains = pixelMapService.getPixelChains(pixelMap, pixel);
        if (pixelChains.size() != 1) {
            return pixelMap;
        }

        var result = pixelMapService.clearAllPixelChains(pixelMap);
        result = pixelMapService.pixelChainsAddAll(result, pixelChains);
        return result;
    }


    public ImmutablePixelMapData actionPixelChainApproximateCurvesOnly(
            @NotNull ImmutablePixelMapData pixelMap,
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
            val pc2 = pixelChainService.approximateCurvesOnly(clone.get(), pc, tolerance, lineCurvePreference);
            clone.update(c -> pixelMapService.pixelChainAdd(c, pc2));
        });
        //copy.indexSegments();
        return clone.get();
    }

    public ImmutablePixelMapData actionReapproximate(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource) {
        SplitTimer.split("PixelMap actionReapproximate() start");
        var result = StrongReference.of(pixelMap);
        Vector<PixelChain> updates = new Vector<>();
        val tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        val lineCurvePreference = transformSource.getLineCurvePreference();
        result.get().pixelChains().stream()
                .parallel()
                .map(pc -> pixelChainService.approximate(result.get(), pc, tolerance))
                .map(pc -> pixelChainService.refine(result.get(), pc, tolerance, lineCurvePreference))
                //.map(pc -> pc.indexSegments(this, true))
                .forEach(updates::add);
        result.update(r -> pixelMapService.pixelChainsClear(r));
        result.update(r -> pixelMapService.pixelChainsAddAll(r, updates));
        SplitTimer.split("PixelMap actionReapproximate() end");
        return result.get();
    }

    public ImmutablePixelMapData actionRerefine(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull CannyEdgeTransform transformSource) {
        var result = StrongReference.of(pixelMap);
        Vector<PixelChain> updates = new Vector<>();
        val tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        val lineCurvePreference = transformSource.getLineCurvePreference();
        result.get().pixelChains().stream()
                .parallel()
                .map(pc -> pixelChainService.refine(result.get(), pc, tolerance, lineCurvePreference))
                .forEach(updates::add);
        result.update(r -> pixelMapService.pixelChainsClear(r));
        result.update(r -> pixelMapService.pixelChainsAddAll(r, updates));
        return result.get();
    }

    public ImmutablePixelMapData actionSetPixelChainDefaultThickness(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull CannyEdgeTransform transform) {
        int shortLength = transform.getShortLineLength();
        int mediumLength = transform.getMediumLineLength();
        int longLength = transform.getLongLineLength();
        Vector<PixelChain> updates = new Vector<>();
        pixelMap.pixelChains().forEach(chain -> updates.add(pixelChainService.withThickness(chain, shortLength, mediumLength, longLength)));
        var result = pixelMapService.clearAllPixelChains(pixelMap);
        result = pixelMapService.pixelChainsAddAll(result, updates);
        return result;
    }

}
