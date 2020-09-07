package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.persist.PersistDB;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Pixel;
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
    public static void turnLoggingOff() {
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

    @Test
    public void process08_refine() {
        // This is to test the problem where process08_refine on the refine subsequent segments was short cutting to the end of the pixel chain
        // if there was no good alternative.  This meant that some shortcuts were a long way from pixels.  What it should do is to shortcut to
        // the next vertex position and try again.  The assertion values of 8 and 6 are not hard values ... but before they were 8 and 3 which
        // meant that many pixels where not well approximated (visually there was a diagonal line across a rectangle).
        // GIVEN
        var pixelMap = Utility.createMap(1500, 1000);
        var height = pixelMap.height();
        var tolerance = 1.2d / height;
        var pixelChain = Utility.createPixelChain(pixelMap
                , Pixel.of(437, 166, height), Pixel.of(436, 167, height), Pixel.of(435, 166, height), Pixel.of(434, 166, height)
                , Pixel.of(433, 166, height), Pixel.of(432, 166, height), Pixel.of(431, 166, height), Pixel.of(430, 165, height)
                , Pixel.of(430, 164, height), Pixel.of(429, 163, height), Pixel.of(428, 162, height), Pixel.of(428, 161, height)
                , Pixel.of(428, 160, height), Pixel.of(428, 159, height), Pixel.of(428, 158, height), Pixel.of(428, 157, height)
                , Pixel.of(428, 156, height), Pixel.of(428, 155, height), Pixel.of(428, 154, height), Pixel.of(428, 153, height)
                , Pixel.of(428, 152, height), Pixel.of(428, 151, height), Pixel.of(428, 150, height), Pixel.of(428, 149, height)
                , Pixel.of(428, 148, height), Pixel.of(428, 147, height), Pixel.of(428, 146, height), Pixel.of(428, 145, height)
                , Pixel.of(428, 144, height), Pixel.of(428, 143, height), Pixel.of(428, 142, height), Pixel.of(428, 141, height)
                , Pixel.of(428, 140, height), Pixel.of(428, 139, height), Pixel.of(428, 138, height), Pixel.of(428, 137, height)
                , Pixel.of(428, 136, height), Pixel.of(428, 135, height), Pixel.of(428, 134, height), Pixel.of(427, 133, height)
                , Pixel.of(427, 132, height), Pixel.of(427, 131, height), Pixel.of(427, 130, height), Pixel.of(427, 129, height)
                , Pixel.of(427, 128, height), Pixel.of(427, 127, height), Pixel.of(427, 126, height), Pixel.of(427, 125, height)
                , Pixel.of(427, 124, height), Pixel.of(427, 123, height), Pixel.of(427, 122, height), Pixel.of(427, 121, height)
                , Pixel.of(427, 120, height), Pixel.of(427, 119, height), Pixel.of(427, 118, height), Pixel.of(427, 117, height)
                , Pixel.of(427, 116, height), Pixel.of(427, 115, height), Pixel.of(427, 114, height), Pixel.of(427, 113, height)
                , Pixel.of(427, 112, height), Pixel.of(428, 111, height), Pixel.of(429, 111, height), Pixel.of(430, 111, height)
                , Pixel.of(431, 111, height), Pixel.of(432, 111, height), Pixel.of(433, 111, height), Pixel.of(434, 112, height)
                , Pixel.of(435, 112, height), Pixel.of(436, 112, height), Pixel.of(437, 112, height), Pixel.of(438, 112, height)
                , Pixel.of(439, 112, height), Pixel.of(440, 113, height), Pixel.of(441, 113, height), Pixel.of(442, 113, height)
                , Pixel.of(443, 114, height), Pixel.of(444, 114, height), Pixel.of(445, 114, height), Pixel.of(446, 114, height)
                , Pixel.of(447, 115, height), Pixel.of(448, 115, height), Pixel.of(449, 115, height), Pixel.of(450, 115, height)
                , Pixel.of(451, 116, height), Pixel.of(452, 116, height), Pixel.of(453, 116, height), Pixel.of(454, 116, height)
                , Pixel.of(455, 116, height), Pixel.of(456, 116, height), Pixel.of(457, 116, height), Pixel.of(458, 116, height)
                , Pixel.of(459, 117, height), Pixel.of(460, 117, height), Pixel.of(461, 118, height), Pixel.of(462, 118, height)
                , Pixel.of(463, 118, height), Pixel.of(464, 119, height), Pixel.of(465, 119, height), Pixel.of(466, 119, height)
                , Pixel.of(467, 120, height), Pixel.of(468, 121, height), Pixel.of(468, 122, height), Pixel.of(468, 123, height)
                , Pixel.of(468, 124, height), Pixel.of(468, 125, height), Pixel.of(468, 126, height), Pixel.of(468, 127, height)
                , Pixel.of(468, 128, height), Pixel.of(468, 129, height), Pixel.of(468, 130, height), Pixel.of(468, 131, height)
                , Pixel.of(468, 132, height), Pixel.of(468, 133, height), Pixel.of(468, 134, height), Pixel.of(468, 135, height)
                , Pixel.of(468, 136, height), Pixel.of(468, 137, height), Pixel.of(468, 138, height), Pixel.of(468, 139, height)
                , Pixel.of(468, 140, height), Pixel.of(468, 141, height), Pixel.of(468, 142, height), Pixel.of(468, 143, height)
                , Pixel.of(468, 144, height), Pixel.of(468, 145, height), Pixel.of(468, 146, height), Pixel.of(468, 147, height)
                , Pixel.of(468, 148, height), Pixel.of(468, 149, height), Pixel.of(468, 150, height), Pixel.of(468, 151, height)
                , Pixel.of(468, 152, height), Pixel.of(469, 153, height), Pixel.of(469, 154, height), Pixel.of(469, 155, height)
                , Pixel.of(469, 156, height), Pixel.of(468, 157, height), Pixel.of(467, 157, height), Pixel.of(466, 157, height)
                , Pixel.of(465, 157, height), Pixel.of(464, 157, height));

        // do the pre-processing
        pixelMap = pixelMapService.addPixelChain(pixelMap, pixelChain);
        pixelMap = underTest.process06_straightLinesRefineCorners(pixelMap, tolerance, null);
        pixelMap = underTest.process07_mergeChains(pixelMap, null);
        assertEquals(8, pixelMap.pixelChains().stream().findFirst().orElseThrow().segments().size());

        // WHEN
        pixelMap = underTest.process08_refine(pixelMap, tolerance, 1.2d, null);

        // THEN
       assertEquals(6, pixelMap.pixelChains().stream().findFirst().orElseThrow().segments().size());
    }

}