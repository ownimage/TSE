package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.javafx.FXViewFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.setAsViewFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void setData_01() {
        // GIVEN
        Pixel pixel = new Pixel(1, 1);
        final PixelMap pixelMap = Utility.createMap(3, 3);
        // WHEN
        assertEquals(false, pixelMap.getData(pixel, NODE));
        assertEquals(false, pixelMap.getData(pixel, EDGE));
        // WHEN
        pixelMap.setData_FOR_TESTING_PURPOSES_ONLY(pixel, true, NODE);
        pixelMap.setData_FOR_TESTING_PURPOSES_ONLY(pixel, true, EDGE);
        // THEN
        assertEquals(true, pixelMap.getData(pixel, NODE));
        assertEquals(true, pixelMap.getData(pixel, EDGE));
        // WHEN
        pixelMap.setData_FOR_TESTING_PURPOSES_ONLY(pixel, false, NODE);
        // THEN
        assertEquals(false, pixelMap.getData(pixel, NODE));
        assertEquals(true, pixelMap.getData(pixel, EDGE));
    }

    @Test
    public void genericNullTest() {
        // GIVEN
        final String[] input = {
                "N E       ",
                "  NEEEEN  ",
                "E    N    "
        };

        // WHEN
        final PixelMap pixelMap = Utility.createMap(input);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(actual, input);
    }

    @Test
    public void process02_thin_01() {
        // GIVEN
        final String[] input = {
                "E E       ",
                "  EEEEEE  ",
                "E    E    "
        };
        final String[] expected = {
                "E N       ",
                "   EE EE  ",
                "E    E    "
        };
        final PixelMap pixelMap = Utility.createMap(input);

        // WHEN
        pixelMap.process02_thin(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void process04a_removeLoneNodes_01() {
        // GIVEN
        final String[] input = {
                " N  ",
                "    ",
                "N N "
        };
        final String[] expected = {
                "    ",
                "    ",
                "    "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);

        // WHEN
        pixelMap.process04a_removeLoneNodes(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process04a_removeLoneNodes_02() {
        // GIVEN
        final String[] input = {
                "N E       ",
                "  NEEEEN  ",
                "E    N    "
        };
        final String[] expected = {
                "  E       ",
                "  NEEEEN  ",
                "E    N    "
        };
        final PixelMap pixelMap = Utility.createMap(input);

        // WHEN
        pixelMap.process04a_removeLoneNodes(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void process04a_removeLoneNodes_03() {
        // GIVEN
        final String[] input = {
                "    ",
                " N  ",
                "    "
        };
        final String[] expected = {
                "    ",
                "    ",
                "    "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process01_reset(null);
        pixelMap.process02_thin(null);
        pixelMap.process03_generateNodes(null);

        // WHEN
        pixelMap.process04a_removeLoneNodes(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }


    @Test
    public void process03_generateNodes() {
        // GIVEN
        final String[] input = {
                "E E       ",
                "   EE EE  ",
                "E E  E    "
        };
        final String[] expected = {
                "N N       ",
                "   NE EN  ",
                "N N  E    "
        };
        final PixelMap pixelMap = Utility.createMap(input);

        // WHEN
        pixelMap.process03_generateNodes(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }


    @Test
    public void process04b_removeBristles_01() {
        // GIVEN
        final String[] input = {
                "E E       ",
                "   EE EE  ",
                "E E  E    "
        };
        final String[] expected = {
                "N         ",
                "   NE EN  ",
                "N    E    "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);

        // WHEN
        pixelMap.process04b_removeBristles(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void process04b_removeBristles_02() {
        // GIVEN
        final String[] input = {
                "           ",
                " E E       ",
                "    EE EE  ",
                " E E  E    ",
                "           ",
        };
        final String[] expected = {
                "           ",
                " N         ",
                "    NE EN  ",
                " N    E    ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);

        // WHEN
        pixelMap.process04b_removeBristles(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process04b_removeBristles_03() {
        // GIVEN
        final String[] input = {
                "    N      ",
                "    E      ",
                "    N      ",
                "   N EEEN  ",
                "           ",
        };
        final String[] expected = {
                "    N      ",
                "    E      ",
                "    E      ",
                "     EEEN  ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        pixelMap.process03_generateNodes(null);

        // WHEN
        pixelMap.process04b_removeBristles(null);

        // THEN
        final String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process04b_deletePixelChain() {
        // GIVEN
        final String[] input = {
                "    N      ",
                "    E      ",
                "    N      ",
                "NEEN NEEN  ",
                "           ",
        };
        final String[] expected = {
                "           ",
                "           ",
                "    E      ",
                "NEEE EEEN  ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        pixelMap.actionProcess(null);
        assertEquals(3, pixelMap.getPixelChainCount());

        // WHEN
        final PixelMap result = pixelMap.actionDeletePixelChain(pixelMap.getPixelAt(4, 1), 1);

        // THEN
        result.calcSegmentIndex();
        assertEquals(2, result.getPixelChainCount());
        final String[] actual = Utility.getMap(result);
        Utility.assertMapEquals(expected, actual);
    }

    @Test
    public void process05a_findLoops() {
        // GIVEN
        final String[] input = {
                "           ",
                "    E      ",
                "   E E     ",
                "   E E     ",
                "   E E     ",
                "    E      ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);

        // WHEN
        pixelMap.actionProcess(null);

        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
    }

    @Test
    public void process04b_generatePixelChain_00() {
        // GIVEN
        final String[] input = {
                "    N      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMap.actionProcess(null);
        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
        StringBuilder result = new StringBuilder();
        pixelMap.forEachPixelChain(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(4, 3), Pixel(4, 2), Pixel(4, 1), Node(4, 0) ]\n", result.toString());
        pixelMap.forEachPixelChain(pc -> pc.validate("test"));
    }

    @Test
    public void process04b_generatePixelChain_01() {
        // GIVEN
        final String[] input = {
                "           ",
                "    N      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMap.actionProcess(null);
        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
        StringBuilder result = new StringBuilder();
        pixelMap.forEachPixelChain(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(4, 4), Pixel(4, 3), Pixel(4, 2), Node(4, 1) ]\n", result.toString());
        pixelMap.forEachPixelChain(pc -> pc.validate("test"));
    }

    @Test
    public void process04b_generatePixelChain_02() {
        // GIVEN
        final String[] input = {
                "           ",
                "       N   ",
                "     EE    ",
                "    E      ",
                "   N       ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMap.actionProcess(null);
        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
        StringBuilder result = new StringBuilder();
        pixelMap.forEachPixelChain(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(3, 4), Pixel(4, 3), Pixel(5, 2), Pixel(6, 2), Node(7, 1) ]\n", result.toString());
        pixelMap.forEachPixelChain(pc -> pc.validate("test"));
    }

    @Test
    public void process04b_generatePixelChain_03() {
        // GIVEN
        final String[] input = {
                "           ",
                "     N     ",
                "    E      ",
                "   E       ",
                "   N       ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMap.actionProcess(null);
        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
        StringBuilder result = new StringBuilder();
        pixelMap.forEachPixelChain(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(5, 1), Pixel(4, 2), Pixel(3, 3), Node(3, 4) ]\n", result.toString());
        pixelMap.forEachPixelChain(pc -> pc.validate("test"));
    }

    @Test
    public void process04b_generatePixelChain_04() {
        // GIVEN
        final String[] input = {
                "           ",
                "   N       ",
                "   E       ",
                "    E      ",
                "     N     ",
                "           ",
        };
        final PixelMap pixelMap = Utility.createMap(input);
        // WHEN
        pixelMap.actionProcess(null);
        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
        StringBuilder result = new StringBuilder();
        pixelMap.forEachPixelChain(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(5, 4), Pixel(4, 3), Pixel(3, 2), Node(3, 1) ]\n", result.toString());
        pixelMap.forEachPixelChain(pc -> pc.validate("test"));
    }

    public PixelMap addChain(@NotNull PixelMap pPixelMap, @NotNull Pixel pStart, @NotNull List<Pixel> pChain) {
        StrongReference<PixelMap> pixelMapRef = new StrongReference<>(pPixelMap);
        pChain.forEach(pixel -> pixelMapRef.set(pixelMapRef.get().actionPixelOn(pStart.add(pixel))));
        return pixelMapRef.get();
    }

    @Test
    public void testBuildChain_01() {
        // GIVEN WHEN
        PixelMap pixelMap = Utility.createMap(20, 20);
        pixelMap = addChain(pixelMap, new Pixel(3, 4), chainS1);
        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
        StringBuilder result = new StringBuilder();
        pixelMap.forEachPixelChain(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(3, 4), Pixel(3, 3), Pixel(4, 2), Node(5, 1) ]\n", result.toString());
    }

    @Test
    public void actionThickness() {
        // GIVEN
        final Pixel start1 = new Pixel(3, 4);
        final Pixel start2 = new Pixel(10, 10);
        PixelMap pixelMap = Utility.createMap(20, 20);
        pixelMap = addChain(pixelMap, start1, chainS1);
        pixelMap = addChain(pixelMap, start2, chainNE);
        BiConsumer<PixelMap, PixelChain.Thickness> test = (pPixelMap, pThickness) -> {
            assertEquals(2, pPixelMap.getPixelChainCount());
            List<PixelChain> chains1 = pPixelMap.getPixelChains(start1);
            assertEquals(1, chains1.size());
            assertEquals(PixelChain.Thickness.Normal, chains1.get(0).getThickness());
            List<PixelChain> chains2 = pPixelMap.getPixelChains(start2);
            assertEquals(1, chains2.size());
            assertEquals(pThickness, chains2.get(0).getThickness());
        };
        test.accept(pixelMap, PixelChain.Thickness.Normal);
        // WHEN
        pixelMap = pixelMap.actionSetPixelChainThickness(start2, PixelChain.Thickness.Thick);
        // THEN
        test.accept(pixelMap, PixelChain.Thickness.Thick);
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
        assertTrue(!underTest.getOptionalPixelAt(5, 5).get().isEdge(underTest));

        pixel.setEdge(underTest, true);
        assertTrue(underTest.getOptionalPixelAt(5, 5).get().isEdge(underTest));

        pixel.setEdge(underTest, false);
        assertTrue(!underTest.getOptionalPixelAt(5, 5).get().isEdge(underTest));
    }

    @Test
    public void actionPixelOnOff() {
        PixelMap underTest = new PixelMap(10, 10, false, null);
        Pixel pixel = new Pixel(5, 5);
        assertTrue(!underTest.getOptionalPixelAt(5, 5).get().isEdge(underTest));

        PixelMap resultOn = underTest.actionPixelOn(pixel);
        assertTrue(!underTest.getOptionalPixelAt(5, 5).get().isEdge(underTest));
        assertTrue(resultOn.getOptionalPixelAt(5, 5).get().isEdge(resultOn));

        PixelMap resultOff = underTest.actionPixelOff(pixel, 1);
        assertTrue(!underTest.getOptionalPixelAt(5, 5).get().isEdge(underTest));
        assertTrue(resultOn.getOptionalPixelAt(5, 5).get().isEdge(resultOn));
        assertTrue(!resultOff.getOptionalPixelAt(5, 5).get().isEdge(resultOff));
    }


}
