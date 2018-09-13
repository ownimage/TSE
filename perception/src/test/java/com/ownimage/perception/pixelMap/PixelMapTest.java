package com.ownimage.perception.pixelMap;

import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.view.javafx.FXViewFactory;

public class PixelMapTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.setAsViewFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
