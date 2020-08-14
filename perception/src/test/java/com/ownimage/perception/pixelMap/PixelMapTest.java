package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapActionService;
import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelService;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.LogManager;

import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private PixelService pixelService = context.getBean(PixelService.class);
    private PixelMapActionService pixelMapActionService = context.getBean(PixelMapActionService.class);
    private PixelMapApproximationService pixelMapApproximationService = context.getBean(PixelMapApproximationService.class);
    private PixelChainService pixelChainService = context.getBean(PixelChainService.class);

    @BeforeClass
    public static void setupViewFactory() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory();
    }

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void process01_reset_01() {
        // GIVEN
        var underTest = Utility.createMap(2000, 1500);
        underTest = pixelMapService.setValue(underTest, 1, 1, (byte) (NODE));
        val start = Instant.now();
        // WHEN
        var result = pixelMapApproximationService.process01_reset(underTest, null);
        val end = Instant.now();
        // THEN
        Assert.assertEquals(new Byte((byte) 0), result.data().get(1, 1));
        val duration = Duration.between(start, end);
        Assert.assertEquals(1, result.data().size());
    }

    @Test
    public void process01_reset_02() {
        // GIVEN
        var underTest = Utility.createMap(2000, 1500);
        underTest = pixelMapService.setValue(underTest, 1, 1, (byte) (NODE | EDGE));
        val start = Instant.now();
        // WHEN
        var result = pixelMapApproximationService.process01_reset(underTest, null);
        val end = Instant.now();
        // THEN
        Assert.assertEquals(Byte.valueOf(EDGE), result.data().get(1, 1));
        val duration = Duration.between(start, end);
        Assert.assertEquals(1, result.data().size());
    }

    @Test
    public void setData_01() {
        // GIVEN
        Pixel pixel = new Pixel(1, 1);
        ImmutablePixelMap pixelMap = Utility.createMap(3, 3);
        // WHEN
        assertFalse(pixelMapService.getData(pixelMap, pixel, NODE));
        assertFalse(pixelMapService.getData(pixelMap, pixel, EDGE));
        // WHEN
        pixelMap = pixelMapService.setData(pixelMap, pixel, true, NODE);
        pixelMap = pixelMapService.setData(pixelMap, pixel, true, EDGE);
        // THEN
        assertTrue(pixelMapService.getData(pixelMap, pixel, NODE));
        assertTrue(pixelMapService.getData(pixelMap, pixel, EDGE));
        // WHEN
        pixelMap = pixelMapService.setData(pixelMap, pixel, false, NODE);
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
        var pixelMap = Utility.createMap(input, false);
        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.process02_thin(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        pixelMap = pixelMapApproximationService.process03_generateNodes(pixelMap, null);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.process04a_removeLoneNodes(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.process04a_removeLoneNodes(pixelMap, tolerance, lineCurvePreference, null);

        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        pixelMap = pixelMapApproximationService.process01_reset(pixelMap, null);
        pixelMap = pixelMapApproximationService.process02_thin(pixelMap, tolerance, lineCurvePreference, null);
        pixelMap = pixelMapApproximationService.process03_generateNodes(pixelMap, null);

        // WHEN
        pixelMap = pixelMapApproximationService.process04a_removeLoneNodes(pixelMap, tolerance, lineCurvePreference, null);

        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        // WHEN
        pixelMap = pixelMapApproximationService.process03_generateNodes(pixelMap, null);

        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        pixelMap = pixelMapApproximationService.process03_generateNodes(pixelMap, null);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.process04b_removeBristles(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        pixelMap = pixelMapApproximationService.process03_generateNodes(pixelMap, null);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.process04b_removeBristles(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        String[] actual = Utility.toStrings(pixelMap);
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
        var pixelMap = Utility.createMap(input, false);
        pixelMap = pixelMapApproximationService.process03_generateNodes(pixelMap, null);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.process04b_removeBristles(pixelMap, tolerance, lineCurvePreference, null);

        // THEN
        String[] actual = Utility.toStrings(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process03b_removeEdgesBetweenTwoNodes() {
        // GIVEN
        String[] input = {
                "    N      ",
                "    E      ",
                "    N      ",
                "   E EEEN  ",
                "  N        ",
        };
        String[] expected = {
                "    N      ",
                "           ",
                "    N      ",
                "     EEEN  ",
                "  N        ",
        };
        var pixelMap = Utility.createMap(input, false);
        // WHEN
        pixelMap = pixelMapApproximationService.process03b_removeEdgesBetweenTwoNodes(pixelMap, 1.2d / 1520, 1.2, null);

        // THEN
        String[] actual = Utility.toStrings(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process04b_deletePixelChain() {
        // GIVEN
        String[] input = {
                "    N      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "NEEN NEEN  ",
                "           ",
        };
        String[] expected = {
                "           ",
                "           ",
                "           ",
                "    E      ",
                "NEEE EEEN  ",
                "           ",
        };
        var immputablePixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        immputablePixelMap = pixelMapApproximationService.actionProcess(immputablePixelMap, tolerance, lineCurvePreference, null);
        assertEquals(3, immputablePixelMap.pixelChains().size());
        val deletePixels = new ArrayList<Pixel>();
        deletePixels.add(new Pixel(4, 1));
        // WHEN
        var result = pixelMapActionService.actionDeletePixelChain(immputablePixelMap, deletePixels, tolerance, lineCurvePreference);
        // THEN
        assertEquals(2, result.pixelChains().size());
        String[] actual = Utility.toStrings(result);
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toReadableString()));
        assertEquals("PixelChain[ Pixel(4, 3), Pixel(4, 2), Pixel(4, 1), Pixel(4, 0) ]\n", result.toString());
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toReadableString()));
        assertEquals("PixelChain[ Pixel(4, 4), Pixel(4, 3), Pixel(4, 2), Pixel(4, 1) ]\n", result.toString());
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toReadableString()));
        assertEquals("PixelChain[ Pixel(3, 4), Pixel(4, 3), Pixel(5, 2), Pixel(6, 2), Pixel(7, 1) ]\n", result.toString());
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toReadableString()));
        assertEquals("PixelChain[ Pixel(3, 4), Pixel(3, 3), Pixel(4, 2), Pixel(5, 1) ]\n", result.toString());
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
        var pixelMap = Utility.createMap(input, false);
        var transformSource = Utility.getDefaultTransformSource(input.length);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        // WHEN
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toReadableString()));
        assertEquals("PixelChain[ Pixel(5, 4), Pixel(4, 3), Pixel(3, 2), Pixel(3, 1) ]\n", result.toString());
        pixelMap.pixelChains().forEach(pc -> pixelChainService.validate(pc, false, "test"));
    }

    public ImmutablePixelMap addChain(@NonNull ImmutablePixelMap pixelMap, @NotNull IPixelMapTransformSource transformSource, @NotNull Pixel pStart, @NotNull List<Pixel> pChain) {
        var pixelMapRef = StrongReference.of(pixelMap);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        pChain.forEach(pixel -> pixelMapRef.update(pm -> pixelMapActionService.actionPixelOn(pm, List.of(new Pixel(pStart.add(pixel))), tolerance, lineCurvePreference)));
        return pixelMapRef.get();
    }

    @Test
    public void test_closeLoop() {
        int xMargin = 2;
        int yMargin = 2;
        Pixel offset = new Pixel(xMargin, yMargin);
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        var width = 11 + 2 * xMargin;
        var height = 14 + 2 * yMargin;
        ImmutablePixelMap pixelMap = ImmutablePixelMap.builder().width(width).height(height).is360(false).build();
        var transformSource = Utility.getDefaultTransformSource(height);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(new IntegerPoint(3, 11).add(offset))), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(4, 11).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(5, 12).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(6, 13).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(7, 13).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(8, 13).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(9, 12).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(9, 11).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 10).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 9).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 8).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 7).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 6).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 5).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 4).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(10, 3).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(9, 2).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(8, 1).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(7, 0).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(6, 0).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(5, 0).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(4, 0).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(3, 0).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(2, 1).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(1, 2).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(0, 3).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(0, 4).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(0, 5).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(0, 6).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(1, 7).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(1, 8).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(1, 9).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(1, 10).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(1, 11).add(offset)), tolerance, lineCurvePreference);
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, new Pixel(new IntegerPoint(2, 11).add(offset)), tolerance, lineCurvePreference);
        assertEquals(1, pixelMap.pixelChains().size());
    }

    @Test
    public void test_closeLoop_2() {
        int xMargin = 2;
        int yMargin = 2;
        Pixel offset = new Pixel(xMargin, yMargin);
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        var width = 11 + 2 * xMargin;
        var height = 14 + 2 * yMargin;
        var underTest = ImmutablePixelMap.builder().width(width).height(height).is360(false).build();
        var transformSource = Utility.getDefaultTransformSource(height);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        pixelMapApproximationService.actionProcess(underTest, tolerance, lineCurvePreference, null);
        List<Pixel> pixels = Arrays.asList(
                new Pixel(new IntegerPoint(3, 11).add(offset)),
                new Pixel(new IntegerPoint(4, 11).add(offset)),
                new Pixel(new IntegerPoint(5, 12).add(offset)),
                new Pixel(new IntegerPoint(6, 13).add(offset)),
                new Pixel(new IntegerPoint(7, 13).add(offset)),
                new Pixel(new IntegerPoint(8, 13).add(offset)),
                new Pixel(new IntegerPoint(9, 12).add(offset)),
                new Pixel(new IntegerPoint(9, 11).add(offset)),
                new Pixel(new IntegerPoint(10, 10).add(offset)),
                new Pixel(new IntegerPoint(10, 9).add(offset)),
                new Pixel(new IntegerPoint(10, 8).add(offset)),
                new Pixel(new IntegerPoint(10, 7).add(offset)),
                new Pixel(new IntegerPoint(10, 6).add(offset)),
                new Pixel(new IntegerPoint(10, 5).add(offset)),
                new Pixel(new IntegerPoint(10, 4).add(offset)),
                new Pixel(new IntegerPoint(10, 3).add(offset)),
                new Pixel(new IntegerPoint(9, 2).add(offset)),
                new Pixel(new IntegerPoint(8, 1).add(offset)),
                new Pixel(new IntegerPoint(7, 0).add(offset)),
                new Pixel(new IntegerPoint(6, 0).add(offset)),
                new Pixel(new IntegerPoint(5, 0).add(offset)),
                new Pixel(new IntegerPoint(4, 0).add(offset)),
                new Pixel(new IntegerPoint(3, 0).add(offset)),
                new Pixel(new IntegerPoint(2, 1).add(offset)),
                new Pixel(new IntegerPoint(1, 2).add(offset)),
                new Pixel(new IntegerPoint(0, 3).add(offset)),
                new Pixel(new IntegerPoint(0, 4).add(offset)),
                new Pixel(new IntegerPoint(0, 5).add(offset)),
                new Pixel(new IntegerPoint(0, 6).add(offset)),
                new Pixel(new IntegerPoint(1, 7).add(offset)),
                new Pixel(new IntegerPoint(1, 8).add(offset)),
                new Pixel(new IntegerPoint(1, 9).add(offset)),
                new Pixel(new IntegerPoint(1, 10).add(offset)),
                new Pixel(new IntegerPoint(1, 11).add(offset)),
                new Pixel(new IntegerPoint(2, 11).add(offset))
        );
        var actual = pixelMapActionService.actionPixelOn(underTest, pixels, tolerance, lineCurvePreference);
        assertEquals(1, actual.pixelChains().size());
    }

    @Test
    public void test_closeLoop_3() {
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        ImmutablePixelMap pixelMap = ImmutablePixelMap.builder().width(9).height(11).is360(false).build();
        var transformSource = Utility.getDefaultTransformSource(11);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
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
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, pixels, tolerance, lineCurvePreference);
        assertEquals(1, pixelMap.pixelChains().size());
    }

    @Test
    public void test_closeLoop_4() {
        IPixelMapTransformSource ts = new PixelMapTransformSource(2000, 1.2, 1.2);
        ImmutablePixelMap pixelMap = ImmutablePixelMap.builder().width(9).height(11).is360(false).build();
        var transformSource = Utility.getDefaultTransformSource(11);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        pixelMap = pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
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
        pixelMap = pixelMapActionService.actionPixelOn(pixelMap, pixels, tolerance, lineCurvePreference);
        assertEquals(1, pixelMap.pixelChains().size());
    }

    @Test
    public void testBuildChain_01() {
        // GIVEN WHEN
        var pixelMap = Utility.createMap(20, 20);
        pixelMap = addChain(pixelMap, Utility.getDefaultTransformSource(20), new Pixel(3, 4), chainS1);
        // THEN
        assertEquals(1, pixelMap.pixelChains().size());
        StringBuilder result = new StringBuilder();
        pixelMap.pixelChains().forEach(pc -> result.append(pc.toReadableString()));
        assertEquals("PixelChain[ Pixel(5, 1), Pixel(4, 2), Pixel(3, 3), Pixel(3, 4) ]\n", result.toString());
    }

    @Test
    public void actionThickness() {
        // GIVEN
        Pixel start1 = new Pixel(3, 4);
        Pixel start2 = new Pixel(10, 10);
        var pixelMap = StrongReference.of(Utility.createMap(20, 20));
        var source = Utility.getDefaultTransformSource(20);
        pixelMap.update(pm -> addChain(pm, source, start1, chainS1));
        pixelMap.update(pm -> addChain(pm, source, start2, chainNE));
        BiConsumer<ImmutablePixelMap, Thickness> test = (pPixelMap, pThickness) -> {
            assertEquals(2, pPixelMap.pixelChains().size());
            var chains1 = pixelMapService.getPixelChains(pPixelMap, start1);
            assertEquals(1, chains1.size());
            assertEquals(Thickness.Normal, chains1.get(0).getThickness());
            var chains2 = pixelMapService.getPixelChains(pPixelMap, start2);
            assertEquals(1, chains2.size());
            assertEquals(pThickness, chains2.get(0).getThickness());
        };
        test.accept(pixelMap.get(), Thickness.Normal);
        // WHEN
        var result = pixelMapActionService
                .actionSetPixelChainThickness(pixelMap.get(), Arrays.asList(start2), t -> Thickness.Thick);
        // THEN
        test.accept(result, Thickness.Thick);
    }

    @Test
    public void setPixelOnOff() {
        var underTest = ImmutablePixelMap.builder().width(10).height(10).is360(false).build();
        var transformSource = Utility.getDefaultTransformSource(10);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        Pixel pixel = new Pixel(5, 5);
        assertFalse(pixelService.isEdge(underTest, pixelMapService.getOptionalPixelAt(underTest, 5, 5).get()));
        underTest = pixelMapService.setEdge(underTest, pixel, true, tolerance, lineCurvePreference);
        assertTrue(pixelService.isEdge(underTest, pixelMapService.getOptionalPixelAt(underTest, 5, 5).get()));
        underTest = pixelMapService.setEdge(underTest, pixel, false, tolerance, lineCurvePreference);
        assertFalse(pixelService.isEdge(underTest, pixelMapService.getOptionalPixelAt(underTest, 5, 5).get()));
    }

    @Test
    public void actionPixelOnOff() {
        var underTest = ImmutablePixelMap.builder().width(10).height(10).is360(false).build();
        var transformSource = Utility.getDefaultTransformSource(10);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        Pixel pixel = new Pixel(5, 5);
        assertFalse(pixelService.isEdge(underTest, pixelMapService.getOptionalPixelAt(underTest, 5, 5).get()));
        var resultOn = pixelMapActionService.actionPixelOn(underTest, pixel, tolerance, lineCurvePreference);
        assertFalse(pixelService.isEdge(underTest, pixelMapService.getOptionalPixelAt(underTest, 5, 5).get()));
        assertTrue(pixelService.isEdge(resultOn, pixelMapService.getOptionalPixelAt(resultOn, 5, 5).get()));
        var resultOff = pixelMapActionService.actionPixelOff(underTest, pixel, 1, tolerance, lineCurvePreference);
        assertFalse(pixelService.isEdge(underTest, pixelMapService.getOptionalPixelAt(underTest, 5, 5).get()));
        assertTrue(pixelService.isEdge(resultOn, pixelMapService.getOptionalPixelAt(resultOn, 5, 5).get()));
        assertFalse(pixelService.isEdge(resultOff, pixelMapService.getOptionalPixelAt(resultOff, 5, 5).get()));
    }
}
