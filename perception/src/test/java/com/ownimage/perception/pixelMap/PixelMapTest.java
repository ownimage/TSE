package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapMappingService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.Services;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.IN_CHAIN;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static com.ownimage.perception.pixelMap.PixelConstants.VISITED;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PixelMapTest {


    private static List<Pixel> chainS1 = Arrays.asList(
            new Pixel(0, 0),
            new Pixel(0, -1),
            new Pixel(1, -2),
            new Pixel(2, -3)
    );
    private static List<Pixel> chainNE = Arrays.asList(
            new Pixel(0, 0),
            new Pixel(1, 1),
            new Pixel(2, 2),
            new Pixel(3, 3)
    );

    private PixelMapService pixelMapService = Services.getDefaultServices().getPixelMapService();
    private PixelMapApproximationService pixelMapApproximationService = Services.getDefaultServices().getPixelMapApproximationService();
    private PixelMapMappingService pixelMapMappingService = Services.getDefaultServices().getPixelMapMappingService();
    private PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void process01_reset_01() {
        // GIVEN
        val underTest = Utility.createMap(2000, 1500);
        underTest.setValuesFrom(pixelMapService.setValue(underTest, 1, 1, (byte) (VISITED | IN_CHAIN | NODE)));
        val start = Instant.now();
        // WHEN
        var result = pixelMapApproximationService.process01_reset(pixelMapMappingService.toImmutablePixelMapData(underTest), null);
        val end = Instant.now();
        // THEN
        Assert.assertEquals(new Byte((byte) 0), result.data().get(1, 1));
        val duration = Duration.between(start, end);
        System.out.println("Duration = " + duration.toMillis());
        Assert.assertEquals(1, result.data().getSize());
    }

    @Test
    public void process01_reset_02() {
        // GIVEN
        val underTest = Utility.createMap(2000, 1500);
        underTest.setValuesFrom(pixelMapService.setValue(underTest, 1, 1, (byte) (VISITED | IN_CHAIN | NODE | EDGE)));
        val start = Instant.now();
        // WHEN
        var result = pixelMapApproximationService.process01_reset(pixelMapMappingService.toImmutablePixelMapData(underTest), null);
        val end = Instant.now();
        // THEN
        Assert.assertEquals(Byte.valueOf(EDGE), result.data().get(1, 1));
        val duration = Duration.between(start, end);
        System.out.println("Duration = " + duration.toMillis());
        Assert.assertEquals(1, result.data().getSize());
    }

    @Test
    public void setData_01() {
        // GIVEN
        Pixel pixel = new Pixel(1, 1);
        PixelMap pixelMap = Utility.createMap(3, 3);
        // WHEN
        assertFalse(pixelMapService.getData(pixelMap, pixel, NODE));
        assertFalse(pixelMapService.getData(pixelMap, pixel, EDGE));
        // WHEN
        pixelMap.setData_FOR_TESTING_PURPOSES_ONLY(pixel, true, NODE);
        pixelMap.setData_FOR_TESTING_PURPOSES_ONLY(pixel, true, EDGE);
        // THEN
        assertTrue(pixelMapService.getData(pixelMap, pixel, NODE));
        assertTrue(pixelMapService.getData(pixelMap, pixel, EDGE));
        // WHEN
        pixelMap.setData_FOR_TESTING_PURPOSES_ONLY(pixel, false, NODE);
        // THEN
        assertFalse(pixelMapService.getData(pixelMap, pixel, NODE));
        assertTrue(pixelMapService.getData(pixelMap, pixel, EDGE));
    }

    @Test
    public void genericNullTest() {
        // GIVEN
        String[] input = {
                "N E       ",
                "  NEEEEN  ",
                "E    N    "
        };
        // WHEN
        PixelMap pixelMap = Utility.createMap(input);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(actual, input);
    }

    @Test
    public void process02_thin_01() {
        // GIVEN
        String[] input = {
                "E E       ",
                "  EEEEEE  ",
                "E    E    "
        };
        String[] expected = {
                "E N       ",
                "   EE EE  ",
                "E    E    "
        };
        PixelMap pixelMap = Utility.createMap(input);
        System.out.println(pixelMap.getData().get(2, 0));
        // WHEN
        pixelMap.process02_thin(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void process04a_removeLoneNodes_01() {
        // GIVEN
        String[] input = {
                " N  ",
                "    ",
                "N N "
        };
        String[] expected = {
                "    ",
                "    ",
                "    "
        };
        PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);
        // WHEN
        pixelMap.process04a_removeLoneNodes(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process04a_removeLoneNodes_02() {
        // GIVEN
        String[] input = {
                "N E       ",
                "  NEEEEN  ",
                "E    N    "
        };
        String[] expected = {
                "  E       ",
                "  NEEEEN  ",
                "E    N    "
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMap.process04a_removeLoneNodes(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void process04a_removeLoneNodes_03() {
        // GIVEN
        String[] input = {
                "    ",
                " N  ",
                "    "
        };
        String[] expected = {
                "    ",
                "    ",
                "    "
        };
        PixelMap pixelMap = Utility.createMap(input);
        pixelMap.setValuesFrom(pixelMapApproximationService.process01_reset(pixelMap, null));
        pixelMap.process02_thin(null);
        pixelMap.process03_generateNodes(null);
        // WHEN
        pixelMap.process04a_removeLoneNodes(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process03_generateNodes() {
        // GIVEN
        String[] input = {
                "E E       ",
                "   EE EE  ",
                "E E  E    "
        };
        String[] expected = {
                "N N       ",
                "   NE EN  ",
                "N N  E    "
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMap.process03_generateNodes(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void process04b_removeBristles_01() {
        // GIVEN
        String[] input = {
                "E E       ",
                "   EE EE  ",
                "E E  E    "
        };
        String[] expected = {
                "N         ",
                "   NE EN  ",
                "N    E    "
        };
        PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);
        // WHEN
        pixelMap.process04b_removeBristles(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void process04b_removeBristles_02() {
        // GIVEN
        String[] input = {
                "           ",
                " E E       ",
                "    EE EE  ",
                " E E  E    ",
                "           ",
        };
        String[] expected = {
                "           ",
                " N         ",
                "    NE EN  ",
                " N    E    ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);
        // WHEN
        pixelMap.process04b_removeBristles(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process04b_removeBristles_03() {
        // GIVEN
        String[] input = {
                "    N      ",
                "    E      ",
                "    N      ",
                "   N EEEN  ",
                "           ",
        };
        String[] expected = {
                "    N      ",
                "    E      ",
                "    E      ",
                "     EEEN  ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);
        // WHEN
        pixelMap.process04b_removeBristles(null);
        // THEN
        String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process04b_deletePixelChain() {
        // GIVEN
        String[] input = {
                "    N      ",
                "    E      ",
                "    N      ",
                "NEEN NEEN  ",
                "           ",
        };
        String[] expected = {
                "           ",
                "           ",
                "    E      ",
                "NEEE EEEN  ",
                "           ",
        };
        var pixelMap = Utility.createMap(input);
        var immputablePixelMap = pixelMapMappingService.toImmutablePixelMapData(pixelMap);
        immputablePixelMap = pixelMapApproximationService.actionProcess(immputablePixelMap, Utility.getTransformSource(input), null);
        assertEquals(3, immputablePixelMap.pixelChains().size());
        val deletePixels = new ArrayList<Pixel>();
        deletePixels.add(new Pixel(4, 1));
        // WHEN
        var result = pixelMapService.actionDeletePixelChain(immputablePixelMap, pixelMap.mTransformSource, deletePixels);
        // THEN
        assertEquals(2, result.pixelChains().size());
        String[] actual = Utility.getMap(pixelMapMappingService.toPixelMap(result, null));
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process05a_findLoops() {
        // GIVEN
        String[] input = {
                "           ",
                "    E      ",
                "   E E     ",
                "   E E     ",
                "   E E     ",
                "    E      ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMapApproximationService.actionProcess(pixelMap, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
    }

    @Test
    public void process04b_generatePixelChain_00() {
        // GIVEN
        String[] input = {
                "    N      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMapApproximationService.actionProcess(pixelMap, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(4, 3), Pixel(4, 2), Pixel(4, 1), Node(4, 0) ]\n", result.toString());
        pixelMap.pixelChains().forEach(pc -> pixelChainService.validate(pc, false, "test"));
    }

    @Test
    public void process04b_generatePixelChain_01() {
        // GIVEN
        String[] input = {
                "           ",
                "    N      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMapApproximationService.actionProcess(pixelMap, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(4, 4), Pixel(4, 3), Pixel(4, 2), Node(4, 1) ]\n", result.toString());
        pixelMap.pixelChains().forEach(pc -> pixelChainService.validate(pc, false, "test"));
    }

    @Test
    public void process04b_generatePixelChain_02() {
        // GIVEN
        String[] input = {
                "           ",
                "       N   ",
                "     EE    ",
                "    E      ",
                "   N       ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMapApproximationService.actionProcess(pixelMap, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(3, 4), Pixel(4, 3), Pixel(5, 2), Pixel(6, 2), Node(7, 1) ]\n", result.toString());
        pixelMap.pixelChains().forEach(pc -> pixelChainService.validate(pc, false, "test"));
    }

    @Test
    public void process04b_generatePixelChain_03() {
        // GIVEN
        String[] input = {
                "           ",
                "     N     ",
                "    E      ",
                "   E       ",
                "   N       ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMapApproximationService.actionProcess(pixelMap, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(3, 4), Pixel(3, 3), Pixel(4, 2), Node(5, 1) ]\n", result.toString());
        pixelMap.pixelChains().forEach(pc -> pixelChainService.validate(pc, false, "test"));
    }

    @Test
    public void process04b_generatePixelChain_04() {
        // GIVEN
        String[] input = {
                "           ",
                "   N       ",
                "   E       ",
                "    E      ",
                "     N     ",
                "           ",
        };
        PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMapApproximationService.actionProcess(pixelMap, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(5, 4), Pixel(4, 3), Pixel(3, 2), Node(3, 1) ]\n", result.toString());
        pixelMap.pixelChains().forEach(pc -> pixelChainService.validate(pc, false, "test"));
    }

    public PixelMap addChain(@NonNull PixelMapData pixelMap, @NotNull IPixelMapTransformSource ts, @NotNull Pixel pStart, @NotNull List<Pixel> pChain) {
        var pixelMapRef =  StrongReference.of(pixelMap);
        pChain.forEach(pixel -> pixelMapRef.update(pm -> pixelMapService.actionPixelOn(pixelMapMappingService.toImmutablePixelMapData(pm), ts, List.of(pStart.add(pixel)))));
        return pixelMapMappingService.toPixelMap(pixelMapRef.get(), ts);
    }

    @Test
    public void test_closeLoop() {
        int xMargin = 2;
        int yMargin = 2;
        Pixel offset = new Pixel(xMargin, yMargin);
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        PixelMap pixelMap = new PixelMap(11 + 2 * xMargin, 14 + 2 * yMargin, false, ts);
        pixelMap.setValuesFrom(pixelMapApproximationService.actionProcess(pixelMap, null));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(3, 11).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(4, 11).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(5, 12).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(6, 13).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(7, 13).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(8, 13).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(9, 12).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(9, 11).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 10).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 9).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 8).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 7).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 6).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 5).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 4).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(10, 3).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(9, 2).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(8, 1).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(7, 0).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(6, 0).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(5, 0).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(4, 0).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(3, 0).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(2, 1).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(1, 2).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(0, 3).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(0, 4).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(0, 5).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(0, 6).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(1, 7).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(1, 8).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(1, 9).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(1, 10).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(1, 11).add(offset)));
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), new Pixel(2, 11).add(offset)));
        assertEquals(1, pixelMap.pixelChains().size());
    }

    @Test
    public void test_closeLoop_2() {
        int xMargin = 2;
        int yMargin = 2;
        Pixel offset = new Pixel(xMargin, yMargin);
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        PixelMap pixelMap = new PixelMap(11 + 2 * xMargin, 14 + 2 * yMargin, false, ts);
        var underTest = pixelMapMappingService.toImmutablePixelMapData(pixelMap);
        pixelMapApproximationService.actionProcess(pixelMap, null);
        List<Pixel> pixels = Arrays.asList(
                new Pixel(3, 11).add(offset),
                new Pixel(4, 11).add(offset),
                new Pixel(5, 12).add(offset),
                new Pixel(6, 13).add(offset),
                new Pixel(7, 13).add(offset),
                new Pixel(8, 13).add(offset),
                new Pixel(9, 12).add(offset),
                new Pixel(9, 11).add(offset),
                new Pixel(10, 10).add(offset),
                new Pixel(10, 9).add(offset),
                new Pixel(10, 8).add(offset),
                new Pixel(10, 7).add(offset),
                new Pixel(10, 6).add(offset),
                new Pixel(10, 5).add(offset),
                new Pixel(10, 4).add(offset),
                new Pixel(10, 3).add(offset),
                new Pixel(9, 2).add(offset),
                new Pixel(8, 1).add(offset),
                new Pixel(7, 0).add(offset),
                new Pixel(6, 0).add(offset),
                new Pixel(5, 0).add(offset),
                new Pixel(4, 0).add(offset),
                new Pixel(3, 0).add(offset),
                new Pixel(2, 1).add(offset),
                new Pixel(1, 2).add(offset),
                new Pixel(0, 3).add(offset),
                new Pixel(0, 4).add(offset),
                new Pixel(0, 5).add(offset),
                new Pixel(0, 6).add(offset),
                new Pixel(1, 7).add(offset),
                new Pixel(1, 8).add(offset),
                new Pixel(1, 9).add(offset),
                new Pixel(1, 10).add(offset),
                new Pixel(1, 11).add(offset),
                new Pixel(2, 11).add(offset)
        );
        var actual = pixelMapService.actionPixelOn(underTest, ts, pixels);
        assertEquals(1, actual.pixelChains().size());
    }

    @Test
    public void test_closeLoop_3() {
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        PixelMap pixelMap = new PixelMap(9, 11, false, ts);
        pixelMapApproximationService.actionProcess(pixelMap, null);
        List<Pixel> pixels = Arrays.asList(
                new Pixel(5, 9),
                new Pixel(4, 9),
                new Pixel(3, 8),
                new Pixel(2, 7),
                new Pixel(2, 6),
                new Pixel(2, 5),
                new Pixel(2, 4),
                new Pixel(3, 3),
                new Pixel(4, 2),
                new Pixel(5, 3),
                new Pixel(6, 4),
                new Pixel(6, 5),
                new Pixel(6, 6),
                new Pixel(6, 7),
                new Pixel(6, 8)
        );
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), pixels));
        assertEquals(1, pixelMap.pixelChains().size());
    }

    @Test
    public void test_closeLoop_4() {
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        PixelMap pixelMap = new PixelMap(9, 11, false, ts);
        pixelMapApproximationService.actionProcess(pixelMap, null);
        List<Pixel> pixels = Arrays.asList(
                new Pixel(5, 9),
                new Pixel(4, 9),
                new Pixel(3, 8),
                new Pixel(2, 7),
                new Pixel(2, 6),
                new Pixel(2, 5),
                new Pixel(2, 4),
                new Pixel(3, 3),
                new Pixel(4, 2),
                new Pixel(5, 3),
                new Pixel(6, 4),
                new Pixel(6, 5),
                new Pixel(6, 6),
                new Pixel(6, 7),
                new Pixel(6, 8),
                new Pixel(6, 9)
        );
        pixelMap.setValuesFrom(pixelMapService.actionPixelOn(pixelMap, pixelMap.getTransformSource(), pixels));
        assertEquals(1, pixelMap.pixelChains().size());
    }

    @Test
    public void testBuildChain_01() {
        // GIVEN WHEN
        PixelMap pixelMap = Utility.createMap(20, 20);
        pixelMap = addChain(pixelMap, Utility.getDefaultTransformSource(20), new Pixel(3, 4), chainS1);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(3, 4), Pixel(3, 3), Pixel(4, 2), Node(5, 1) ]\n", result.toString());
    }

    @Test
    public void actionThickness() {
        // GIVEN
        Pixel start1 = new Pixel(3, 4);
        Pixel start2 = new Pixel(10, 10);
        var pixelMap = StrongReference.of((PixelMapData)Utility.createMap(20, 20));
        var source = Utility.getDefaultTransformSource(20);
        pixelMap.update(pm -> addChain(pm, source, start1, chainS1));
        pixelMap.update(pm ->  addChain(pm, source, start2, chainNE));
        BiConsumer<PixelMapData, PixelChain.Thickness> test = (pPixelMap, pThickness) -> {
            assertEquals(2, pPixelMap.pixelChains().size());
            List<PixelChain> chains1 = pixelMapService.getPixelChains(pPixelMap, start1);
            assertEquals(1, chains1.size());
            assertEquals(Thickness.Normal, chains1.get(0).getThickness());
            List<PixelChain> chains2 = pixelMapService.getPixelChains(pPixelMap, start2);
            assertEquals(1, chains2.size());
            assertEquals(pThickness, chains2.get(0).getThickness());
        };
        test.accept(pixelMap.get(), Thickness.Normal);
        // WHEN
        var result = pixelMapService
                .actionSetPixelChainThickness(pixelMapMappingService.toImmutablePixelMapData(pixelMap.get()), Arrays.asList(start2), t -> Thickness.Thick);
        // THEN
        test.accept(result, Thickness.Thick);
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setPixelOnOff() {
        PixelMap underTest = new PixelMap(10, 10, false, null);
        Pixel pixel = new Pixel(5, 5);
        assertFalse(pixelMapService.getOptionalPixelAt(underTest,5, 5).get().isEdge(underTest));
        pixel.setEdge(underTest, true);
        assertTrue(pixelMapService.getOptionalPixelAt(underTest,5, 5).get().isEdge(underTest));
        pixel.setEdge(underTest, false);
        assertFalse(pixelMapService.getOptionalPixelAt(underTest,5, 5).get().isEdge(underTest));
    }

    @Test
    public void actionPixelOnOff() {
        PixelMap underTest = new PixelMap(10, 10, false, Utility.getDefaultTransformSource(10));
        Pixel pixel = new Pixel(5, 5);
        assertFalse(pixelMapService.getOptionalPixelAt(underTest,5, 5).get().isEdge(underTest));
        var resultOn = pixelMapService.actionPixelOn(underTest, underTest.getTransformSource(), pixel);
        assertFalse(pixelMapService.getOptionalPixelAt(underTest,5, 5).get().isEdge(underTest));
        assertTrue(pixelMapService.getOptionalPixelAt(resultOn,5, 5).get().isEdge(resultOn));
        var resultOff = pixelMapService.actionPixelOff(underTest, underTest.mTransformSource, pixel, 1);
        assertFalse(pixelMapService.getOptionalPixelAt(underTest,5, 5).get().isEdge(underTest));
        assertTrue(pixelMapService.getOptionalPixelAt(resultOn,5, 5).get().isEdge(resultOn));
        assertFalse(pixelMapService.getOptionalPixelAt(resultOff,5, 5).get().isEdge(resultOff));
    }
}
