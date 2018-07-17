package com.ownimage.perception.pixelMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PixelMapTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
                "E E       ",
                "   EE EE  ",
                "E    E    "
        };
        PixelMap pixelMap = Utility.createMap(input);

        // WHEN
        pixelMap.process02_thin();

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
        pixelMap.process03_generateNodes();

        // WHEN
        pixelMap.process04a_removeLoneNodes();

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
        pixelMap.process04a_removeLoneNodes();

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
        pixelMap.process01_reset();
        pixelMap.process02_thin();
        pixelMap.process03_generateNodes();

        // WHEN
        pixelMap.process04a_removeLoneNodes();

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
        pixelMap.process03_generateNodes();

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
        pixelMap.process03_generateNodes();

        // WHEN
        pixelMap.process04b_removeBristles();

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
        pixelMap.process03_generateNodes();

        // WHEN
        pixelMap.process04b_removeBristles();

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
        pixelMap.process03_generateNodes();

        // WHEN
        pixelMap.process04b_removeBristles();

        // THEN
        String[] actual = Utility.getMap(pixelMap);
        Utility.assertMapEquals(expected, actual);
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
