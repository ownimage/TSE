package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.IXY;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import io.vavr.Tuple2;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class PixelMapActionServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapActionService underTest = context.getBean(PixelMapActionService.class);

    private PixelMapTestSupport pixelMapTestSupport = new PixelMapTestSupport();

    @BeforeClass
    public static void turnLoggingOff() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void actionPixelOn_00() {
        // GIVEN empty pixel map and set of pixels
        var pixelMap = Utility.createMap(10, 10);
        Collection<IXY> hoizontal = IntStream.range(2, 9).mapToObj(Integer::valueOf)
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
        Collection<IXY> cross = new ArrayList<>();
        IntStream.range(2, 9).mapToObj(Integer::valueOf).map(i -> ImmutableIXY.of(i, 5)).forEach(cross::add);
        IntStream.range(2, 9).mapToObj(Integer::valueOf).map(i -> ImmutableIXY.of(5, i)).forEach(cross::add);
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
        Collection<IXY> horiz = IntStream.range(2, 9).mapToObj(Integer::valueOf).map(i -> ImmutableIXY.of(i, 5))
                .collect(Collectors.toList());
        Collection<IXY> vert = IntStream.range(2, 9).mapToObj(Integer::valueOf).map(i -> ImmutableIXY.of(5, i))
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
}