package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.XY;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PixelMapActionServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapActionService underTest = context.getBean(PixelMapActionService.class);
    private PixelMapService pixelMapService = context.getBean(PixelMapService.class);

    private PixelMapTestSupport pixelMapTestSupport = new PixelMapTestSupport();

    @BeforeAll
    public static void turnLoggingOff() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void actionPixelOn_00() {
        // GIVEN empty pixel map and set of pixels
        var pixelMap = Utility.createMap(10, 10);
        Collection<XY> hoizontal = IntStream.range(2, 9).boxed()
                .map(i -> ImmutableIXY.of(i, 5))
                .collect(Collectors.toList());
        // WHEN
        var actual = underTest.actionPixelOn(pixelMap, hoizontal, 1.2d / pixelMap.height(), 1.2);
        // THEN
        assertEquals(1, actual.pixelChains().size());
        assertEquals(1, actual.segmentCount());
    }

    @Test
    public void actionPixelOn_01() {
        // GIVEN empty pixel map and set of pixels
        var pixelMap = Utility.createMap(10, 10);
        Collection<XY> cross = new ArrayList<>();
        IntStream.range(2, 9).boxed().map(i -> ImmutableIXY.of(i, 5)).forEach(cross::add);
        IntStream.range(2, 9).boxed().map(i -> ImmutableIXY.of(5, i)).forEach(cross::add);
        // WHEN
        var actual = underTest.actionPixelOn(pixelMap, cross, 1.2d / pixelMap.height(), 1.2);
        // THEN
        assertEquals(4, actual.pixelChains().size());
        assertEquals(4, actual.segmentCount());
    }

    @Test
    public void actionPixelOff_01() {
        // GIVEN a pixel map with a horizontal line
        var pixelMap = Utility.createMap(10, 10);
        Collection<XY> horiz = IntStream.range(2, 9).boxed().map(i -> ImmutableIXY.of(i, 5))
                .collect(Collectors.toList());
        Collection<XY> vert = IntStream.range(2, 9).boxed().map(i -> ImmutableIXY.of(5, i))
                .collect(Collectors.toList());
        var tolerance = 1.2d / pixelMap.height();
        var lineCurvePreference = 1.2d;
        pixelMap = underTest.actionPixelOn(pixelMap, horiz, tolerance, lineCurvePreference);
        assertEquals(1, pixelMap.pixelChains().size());
        assertEquals(1, pixelMap.segmentCount());
        var actual = StrongReference.of(pixelMap);
        // WHEN
        vert.forEach(ixy -> actual.update(pm -> underTest.actionPixelOff(pm, ixy, 1, tolerance, lineCurvePreference)));
        // THEN
        assertEquals(new Tuple2<>(2, 2), pixelMapTestSupport.countUniquePixelChainsAndSegmentsInIndex(actual.get()));
    }

    private ImmutablePixelMap createMap() {
        String[] input = {
                "                ",
                "       N        ",
                "       E        ",
                "       E        ",
                "       E        ",
                "       N        ",
                "  NEEEE EEEEN   ",
                "                ",
                "   NEEEEEN      ",
        };
        var pixelMap = Utility.createMap(input, true);
        assertEquals(4, pixelMap.pixelChains().size());
        // three chains that share a common node
        assertEquals(3, pixelMapService.getPixelChains(pixelMap, XY.of(7, 5)).size());
        assertEquals(1, pixelMapService.getPixelChains(pixelMap, XY.of(7, 1)).size());
        assertEquals(1, pixelMapService.getPixelChains(pixelMap, XY.of(2, 6)).size());
        assertEquals(1, pixelMapService.getPixelChains(pixelMap, XY.of(12, 6)).size());
        // and the separate chain
        assertEquals(1, pixelMapService.getPixelChains(pixelMap, XY.of(3, 8)).size());
        assertEquals(1, pixelMapService.getPixelChains(pixelMap, XY.of(9, 8)).size());
        return pixelMap;
    }

    @ParameterizedTest
    @MethodSource("com.ownimage.perception.pixelMap.Utility#testColors")
    public void actionSetPixelChainChangeColor_00(@NotNull Color color) {
        // GIVEN
        var pixelMap = createMap();
        var pixel = XY.of(7, 1); // in a chain
        var pixels = List.of(pixel);
        // WHEN pixel hits a chain
        var actual = underTest.actionSetPixelChainChangeColor(pixelMap, pixels, color);
        // THEN
        assertNotSame(pixelMap, actual);
        assertEquals(1, pixelMapService.getPixelChains(actual, pixel).size());
        assertEquals(color, pixelMapService.getPixelChains(actual, pixel).get(0).color().orElseThrow());
    }

    @ParameterizedTest
    @MethodSource("com.ownimage.perception.pixelMap.Utility#testColors")
    public void actionSetPixelChainChangeColor_01(@NotNull Color color) {
        // GIVEN
        var pixelMap = createMap();
        var pixel = XY.of(1, 1);
        var pixels = List.of(pixel);
        // WHEN pixel misses all chains
        var actual = underTest.actionSetPixelChainChangeColor(pixelMap, pixels, color);
        // THEN
        assertSame(pixelMap, actual);
    }

    @ParameterizedTest
    @MethodSource("com.ownimage.perception.pixelMap.Utility#testColors")
    public void actionSetPixelChainChangeColor_02(@NotNull Color color) {
        var pixel = XY.of(7, 1);
        var pixels = List.of(pixel);
        var pixelMap = createMap();
        pixelMap = underTest.actionSetPixelChainChangeColor(pixelMap, pixels, color);
        // WHEN pixel hits chain but there is no color change
        var actual = underTest.actionSetPixelChainChangeColor(pixelMap, pixels, color);
        // THEN
        assertSame(pixelMap, actual);
        var affectedPixelChains = pixelMapService.getPixelChains(actual, pixel);
        var count = affectedPixelChains.stream()
                .peek(pc -> assertEquals(color, pc.color().orElseThrow()))
                .count();
        assertEquals(1, count);
        pixelMap.pixelChains().stream()
                .filter(pc -> !affectedPixelChains.contains(pc))
                .forEach((pc -> assertTrue(pc.color().isEmpty())));
    }
}