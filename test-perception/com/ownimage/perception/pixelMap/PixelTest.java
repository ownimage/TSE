package com.ownimage.perception.pixelMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.view.javafx.FXViewFactory;

public class PixelTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.setAsViewFactory();
    }

    @Test
    public void isEdge_00() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "   "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertFalse(underTest.isEdge());
    }

    @Test
    public void isEdge_01() {
        // GIVEN
        String[] input = {
                "   ",
                " E ",
                "   "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge());
    }

    @Test
    public void isEdge_02() {
        // GIVEN
        String[] input = {
                "E  ",
                "   ",
                "   "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(0, 0);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge());
    }

    @Test
    public void isEdge_03() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                " E "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 2);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge());
    }

    @Test
    public void countEdgeNeighbours_00() {
        // GIVEN
        String[] input = {
                "   ",
                " E ",
                "   "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertSame(0, underTest.countEdgeNeighbours());
    }

    @Test
    public void countEdgeNeighbours_01() {
        // GIVEN
        String[] input = {
                "   ",
                "EEE",
                "   "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertSame(2, underTest.countEdgeNeighbours());
    }

    @Test
    public void countEdgeNeighbours_02() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "EEE"
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 2);

        // WHEN

        // THEN
        assertSame(2, underTest.countEdgeNeighbours());
    }

    @Test
    public void countEdgeNeighbours_03() {
        // GIVEN
        String[] input = {
                " N  ",
                "    ",
                "N N "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN
        int actual = underTest.countEdgeNeighbours();

        // THEN
        assertEquals(0, actual);
    }

    @Test
    public void countEdgeNeighboursTransitions_01() {
        // GIVEN
        String[] input = {
                "   ",
                "EEE",
                "   "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions());
    }

    @Test
    public void countEdgeNeighboursTransitions_02() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "EEE"
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(1, 2);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions());
    }

    @Test
    public void countEdgeNeighboursTransitions_03() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "E  "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN

        // THEN
        assertSame(0, underTest.countEdgeNeighboursTransitions());
    }

    @Test
    public void countEdgeNeighboursTransitions_04() {
        // GIVEN
        String[] input = {
                "   ",
                "E  ",
                "EE "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions());
    }

    @Test
    public void calcIsNode_01() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "E  "
        };
        PixelMap pixelMap = Utility.createMap(input);
        Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN
        boolean n = underTest.calcIsNode();

        // THEN
        assertTrue(n);
        assertTrue(underTest.isNode());
    }


}
