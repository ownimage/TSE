package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.KColor;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.render.ITransformResult;
import io.vavr.Tuple2;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class PixelMapTransformService {

    private  PixelMapService pixelMapService;
    private  PixelService pixelService;

    private final static Logger mLogger = Framework.getLogger();

    @Autowired
    public void setPixelMapService(PixelMapService pixelMapService) {
        this.pixelMapService = pixelMapService;
    }

    @Autowired
    public void setPixelService(PixelService pixelService) {
        this.pixelService = pixelService;
    }

    public void transform(
            @NotNull ImmutablePixelMap pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NotNull ITransformResult renderResult) {
        var point = renderResult.getPoint();
        var color = transformGetPixelColor(pixelMap, transformSource, point, renderResult.getColor());
        color = transformGetLineColor(pixelMap, transformSource, point, color, false);
        color = getMaxiLineShadowColor(pixelMap, transformSource, point, color);
        renderResult.setColor(color);
    }

    public Color transformGetPixelColor(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Point point,
            @NotNull Color color) {
        StrongReference<Color> result = StrongReference.of(color);
        if (transformSource.getShowPixels()) {
            pixelMapService.getOptionalPixelAt(pixelMap, point)
                    .filter(p -> pixelService.isEdge(pixelMap, p))
                    .ifPresent(p -> result.set(transformSource.getPixelColor()));
        }
        return result.get();
    }

    public Color transformGetLineColor(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Point point,
            @NotNull Color color
            , boolean thickOnly) {
        var lineColor = transformSource.getLineColor();
        var lineOpacity = transformSource.getLineOpacity();
        return transformSource.getShowLines() ?
                transformGetLineColor(pixelMap, transformSource, point, color, lineColor, lineOpacity, 1.0d, thickOnly) :
                color;
    }

    public Color transformGetLineColor(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Point point,
            @NotNull Color color,
            @NotNull Color lineColor,
            double opacity,
            double thicknessMultiplier,
            boolean pThickOnly) {
        double shortThickness = transformSource.getMediumLineThickness() * thicknessMultiplier / 1000d;
        double normalThickness = transformSource.getShortLineThickness() * thicknessMultiplier / 1000d;
        double longThickness = transformSource.getLongLineThickness() * thicknessMultiplier / 1000d;
        if (isAnyLineCloserThan(pixelMap, transformSource, point, shortThickness, normalThickness, longThickness, thicknessMultiplier, pThickOnly)) {
            return KColor.fade(color, lineColor, opacity);
        }
        return color;
    }

    private boolean isAnyLineCloserThan(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Point point,
            double pThinWidth,
            double pNormalWidth,
            double pThickWidth,
            double pMultiplier,
            boolean pThickOnly) {
        double maxThickness = KMath.max(pThinWidth, pNormalWidth, pThickWidth) * pMultiplier;
        Point uhvw = pixelMapService.toUHVW(pixelMap, point);
        var width = pixelMap.width();
        var height = pixelMap.height();
        var aspectRatio = pixelMapService.aspectRatio(pixelMap);
        // to prevent the expensive closerThanActual being run against the same segment more than once they
        // are condensed into a set.
        var candidateSegments = new HashSet<Tuple2<PixelChain, ISegment>>();
        for (int x = (int) Math.floor((uhvw.getX() - maxThickness) * width / aspectRatio) - 1; x <= Math.ceil((uhvw.getX() + maxThickness) * width / aspectRatio) + 1; x++) {
            for (int y = (int) (Math.floor((uhvw.getY() - maxThickness) * height)) - 1; y <= Math.ceil((uhvw.getY() + maxThickness) * height) + 1; y++) {
                if (0 <= x && x < width && 0 <= y && y < height) {
                    getSegments(pixelMap, x, y).ifPresent(set -> set.stream()
                            .filter(tuple -> tuple._1().getThickness() != IPixelChain.Thickness.None)
                            .filter(tuple -> !pThickOnly || tuple._1().getThickness() == IPixelChain.Thickness.Thick)
                            .forEach(candidateSegments::add));
                }
            }
        }
        StrongReference<Boolean> result = new StrongReference<>(false);
        candidateSegments.stream()
                .filter(tuple -> tuple._2().closerThanActual(pixelMap, tuple._1(), transformSource, uhvw, pMultiplier))
                .findAny()
                .ifPresent(tuple -> result.set(true));
        return result.get();
    }

    public Optional<ImmutableSet<Tuple2<PixelChain, ISegment>>> getSegments(
            @NotNull ImmutablePixelMap pixelMap, int x, int y) {
        Framework.checkParameterGreaterThanEqual(mLogger, x, 0, "x");
        Framework.checkParameterLessThan(mLogger, x, pixelMap.width(), "x");
        Framework.checkParameterGreaterThanEqual(mLogger, y, 0, "y");
        Framework.checkParameterLessThan(mLogger, y, pixelMap.height(), "y");
        val segmentIndex = pixelMap.segmentIndex().get(x, y);
        return Optional.ofNullable(segmentIndex);
    }

    public Color getMaxiLineShadowColor(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Point point,
            @NotNull Color pColor) {
        if (transformSource.getShowShadow()) {
            double x = point.getX() - transformSource.getShadowXOffset() / 1000d;
            x = x < 0 ? 0 : x > pixelMap.width() - 1 ? x - pixelMap.width() : x;
            double y = point.getY() - transformSource.getShadowYOffset() / 1000d;
            y = y < 0 ? 0 : y > pixelMap.height() - 1 ? y - (pixelMap.height() - 1) : y;
            Point uhvw = new Point(x, y);
            return transformGetLineColor(pixelMap, transformSource, uhvw, pColor, transformSource.getShadowColor(), transformSource.getShadowOpacity(), transformSource.getShadowThickness(), true);
        }
        return pColor;
    }
}
