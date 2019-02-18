package com.ownimage.perception.pixelMap;

import com.ownimage.framework.view.javafx.FXViewFactory;
import org.junit.*;

import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static org.junit.Assert.*;

public class PixelMapTest {

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

    @Test
    public void buildChain_01() {
        // GIVEN
        PixelMap pixelMap = Utility.createMap(10, 10);
        pixelMap.actionProcess(null);
        // WHEN
        pixelMap = pixelMap.actionPixelOn(new Pixel(3, 4));
        pixelMap = pixelMap.actionPixelOn(new Pixel(3, 3));
        pixelMap = pixelMap.actionPixelOn(new Pixel(4, 2));
        pixelMap = pixelMap.actionPixelOn(new Pixel(5, 1));
        // THEN
        assertEquals(1, pixelMap.getPixelChainCount());
        StringBuilder result = new StringBuilder();
        pixelMap.forEachPixelChain(pc -> result.append(pc.toString()));
        assertEquals("PixelChain[ Node(3, 4), Pixel(3, 3), Pixel(4, 2), Node(5, 1) ]\n", result.toString());
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
