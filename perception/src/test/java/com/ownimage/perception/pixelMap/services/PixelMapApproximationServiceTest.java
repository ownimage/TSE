package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import org.jetbrains.annotations.NotNull;
import org.junit.BeforeClass;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PixelMapApproximationServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private PixelMapApproximationService underTest = context.getBean(PixelMapApproximationService.class);
    private PixelMapValidationService pixelMapValidationService = context.getBean(PixelMapValidationService.class);
    private PixelChainService pixelChainService = context.getBean(PixelChainService.class);

    @BeforeClass
    public static void setViewFactory() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory();
    }

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Test
    public void actionProcess_00() throws IOException {
        // GIVEN
        var is = getClass().getResourceAsStream("NY2.transform");
        var db = new PersistDB(is);
        var pixelMap = pixelMapService.read(db, "transform.2.cannyEdge");
        var tolerance = 1.2d / 1520;
        var lineCurvePreference = 1.2d;
        // WHEN
        var actual = underTest.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        assertEquals(1074, actual.width());
        assertEquals(1520, actual.height());
        assertTrue(3100 < actual.pixelChains().size());
        assertTrue(87000 < actual.data().size());
        pixelMapValidationService.validate(actual);
    }

    @Test
    public void actionProcess_01() {
        // GIVEN
        String[] input = {
                "           ",
                "NEEE N     ",
                "    N E    ",
                "     E     ",
                "           ",
        };
        // WHEN
        var actual = Utility.createMap(input, true);
        // THEN
        pixelMapValidationService.validate(actual);
    }

    @Test
    public void checkAllDataEdgesHave2Neighbours() {
        // GIVEN pixel map with valid usage of 3 neighbours
        String[] input = {
                "  E     ",
                " N NEE  ",
                "   E  E ",
                "   E    ",
                "   E    ",
                "  E     ",
        };
        String[] expected = {
                "        ",
                "    EE  ",
                "   E  N ",
                "   E    ",
                "   E    ",
                "  N     ",
        };        // WHEN
        var actual = Utility.createMap(input, true);
        // THEN
        Utility.assertMapEquals(expected, Utility.toStrings(actual));
        pixelMapValidationService.validate(actual);
    }

    @Test
    public void generateChains() {
        // GIVEN pixel map with valid usage of 3 neighbours
        String[] input = {
                "     N        ",
                "     E        ",
                "     E        ",
                "      NEEN    ",
                "      E       ",
                "      E       ",
                "      N       ",
        };
        var pixelMap = Utility.createMap(input, false);
        pixelMap = pixelMap.withAutoTrackChanges(false);
        pixelMap = underTest.process01_reset(pixelMap, null);
        pixelMap = underTest.process03_generateNodes(pixelMap, null);
        // WHEN
        var actual = underTest.process05_generateChains(pixelMap, null);
        // THEN
        assertNoLoops(actual);
        assertEquals(3, actual.pixelChains().size());
    }

    private void assertNoLoops(@NotNull ImmutablePixelMap actual) {
        var loops = actual.pixelChains().stream()
                .filter(pixelChainService::isLoop)
                .collect(Collectors.toList());
        if (loops.isEmpty()) {
            return;
        }
        var chains = loops.stream().map(PixelChain::toReadableString).collect(Collectors.joining(System.lineSeparator()));
        throw new ComparisonFailure("Some chains have loops", null, chains);
    }
}