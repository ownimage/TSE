package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.javafx.FXViewFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class PixelTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory(false);
    }

    @Test
    public void isEdge_00() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                "   "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertFalse(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_01() {
        // GIVEN
        final String[] input = {
                "   ",
                " E ",
                "   "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_02() {
        // GIVEN
        final String[] input = {
                "E  ",
                "   ",
                "   "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(0, 0);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_03() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                " E "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 2);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_04() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                " E "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(-1, 2);

        // WHEN

        // THEN
        assertFalse(underTest.isEdge(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_00() {
        // GIVEN
        final String[] input = {
                "   ",
                " E ",
                "   "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertSame(0, underTest.countEdgeNeighbours(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_01() {
        // GIVEN
        final String[] input = {
                "   ",
                "EEE",
                "   "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertSame(2, underTest.countEdgeNeighbours(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_02() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                "EEE"
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 2);

        // WHEN

        // THEN
        assertSame(2, underTest.countEdgeNeighbours(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_03() {
        // GIVEN
        final String[] input = {
                " N  ",
                "    ",
                "N N "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN
        final int actual = underTest.countEdgeNeighbours(pixelMap);

        // THEN
        assertEquals(0, actual);
    }

    @Test
    public void countEdgeNeighboursTransitions_01() {
        // GIVEN
        final String[] input = {
                "   ",
                "EEE",
                "   "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 1);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void countEdgeNeighboursTransitions_02() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                "EEE"
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 2);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void countEdgeNeighboursTransitions_03() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                "E  "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN

        // THEN
        assertSame(0, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void countEdgeNeighboursTransitions_04() {
        // GIVEN
        final String[] input = {
                "   ",
                "E  ",
                "EE "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void calcIsNode_01() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                "E  "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(0, 2);

        // WHEN
        final boolean n = underTest.calcIsNode(pixelMap);

        // THEN
        assertTrue(n);
        assertTrue(underTest.isNode(pixelMap));
    }

    @Test
    public void getNeighbours_01() {
        // GIVEN
        final String[] input = {
                "   ",
                "   ",
                "E  "
        };
        final PixelMap pixelMap = Utility.createMap(input);
        final Pixel underTest = pixelMap.getPixelAt(1, 1);
        StrongReference<Integer> count = new StrongReference<>(new Integer(0));

        // WHEN
        underTest.getNeighbours().forEach(p -> count.set(count.get() + 1));

        // THEN
        assertSame(8, count.get());
    }

    @Test
    public void samePosition_00() {
        // GIVEN
        Pixel underTest = new Pixel(5, 5);
        Pixel sameAsTest = null;
        // THEN
        assertFalse(underTest.samePosition(sameAsTest));
    }

    @Test
    public void samePosition_01() {
        // GIVEN
        Pixel underTest = new Pixel(5, 5);
        Pixel sameAsTest = new Pixel(5, 5);
        // THEN
        assertTrue(underTest.samePosition(sameAsTest));
    }

    @Test
    public void samePosition_02() {
        // GIVEN
        Pixel underTest = new Pixel(5, 5);
        Pixel sameAsTest = new Pixel(4, 5);
        // THEN
        assertFalse(underTest.samePosition(sameAsTest));
    }

    @Test
    public void samePosition_03() {
        // GIVEN
        Pixel underTest = new Pixel(5, 5);
        Pixel sameAsTest = new Pixel(5, 4);
        // THEN
        assertFalse(underTest.samePosition(sameAsTest));
    }

    @Test
    public void samePosition_04() {
        // GIVEN
        Pixel underTest = new Pixel(5, 5);
        Pixel sameAsTest = new Pixel(4, 4);
        // THEN
        assertFalse(underTest.samePosition(sameAsTest));
    }

    @Test
    public void samePosition_05() {
        // GIVEN
        Pixel underTest = new Pixel(5, 5);
        Node sameAsTest = new Node(new Pixel(5, 5));
        // THEN
        assertTrue(underTest.samePosition(sameAsTest));
    }

    @Test
    public void samePosition_06() {
        // GIVEN
        Pixel underTest = new Pixel(5, 5);
        Node sameAsTest = new Node(new Pixel(4, 4));
        // THEN
        assertFalse(underTest.samePosition(sameAsTest));
    }
}
