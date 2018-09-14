//package com.ownimage.perception.pixelMap;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertSame;
//import static org.junit.Assert.assertTrue;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import com.ownimage.framework.view.javafx.FXViewFactory;
//
//public class PixelTest {
//
//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception {
//        FXViewFactory.setAsViewFactory();
//    }
//
//    @Test
//    public void isEdge_00() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "   ",
//                "   "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 1);
//
//        // WHEN
//
//        // THEN
//        assertFalse(underTest.isEdge(pPixelMap));
//    }
//
//    @Test
//    public void isEdge_01() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                " E ",
//                "   "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 1);
//
//        // WHEN
//
//        // THEN
//        assertTrue(underTest.isEdge(pPixelMap));
//    }
//
//    @Test
//    public void isEdge_02() {
//        // GIVEN
//        final String[] input = {
//                "E  ",
//                "   ",
//                "   "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(0, 0);
//
//        // WHEN
//
//        // THEN
//        assertTrue(underTest.isEdge(pPixelMap));
//    }
//
//    @Test
//    public void isEdge_03() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "   ",
//                " E "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 2);
//
//        // WHEN
//
//        // THEN
//        assertTrue(underTest.isEdge(pPixelMap));
//    }
//
//    @Test
//    public void countEdgeNeighbours_00() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                " E ",
//                "   "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 1);
//
//        // WHEN
//
//        // THEN
//        assertSame(0, underTest.countEdgeNeighbours(pPixelMap));
//    }
//
//    @Test
//    public void countEdgeNeighbours_01() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "EEE",
//                "   "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 1);
//
//        // WHEN
//
//        // THEN
//        assertSame(2, underTest.countEdgeNeighbours(pPixelMap));
//    }
//
//    @Test
//    public void countEdgeNeighbours_02() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "   ",
//                "EEE"
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 2);
//
//        // WHEN
//
//        // THEN
//        assertSame(2, underTest.countEdgeNeighbours(pPixelMap));
//    }
//
//    @Test
//    public void countEdgeNeighbours_03() {
//        // GIVEN
//        final String[] input = {
//                " N  ",
//                "    ",
//                "N N "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(0, 2);
//
//        // WHEN
//        final int actual = underTest.countEdgeNeighbours(pPixelMap);
//
//        // THEN
//        assertEquals(0, actual);
//    }
//
//    @Test
//    public void countEdgeNeighboursTransitions_01() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "EEE",
//                "   "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 1);
//
//        // WHEN
//
//        // THEN
//        assertSame(4, underTest.countEdgeNeighboursTransitions(pPixelMap));
//    }
//
//    @Test
//    public void countEdgeNeighboursTransitions_02() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "   ",
//                "EEE"
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(1, 2);
//
//        // WHEN
//
//        // THEN
//        assertSame(4, underTest.countEdgeNeighboursTransitions(pPixelMap));
//    }
//
//    @Test
//    public void countEdgeNeighboursTransitions_03() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "   ",
//                "E  "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(0, 2);
//
//        // WHEN
//
//        // THEN
//        assertSame(0, underTest.countEdgeNeighboursTransitions(pPixelMap));
//    }
//
//    @Test
//    public void countEdgeNeighboursTransitions_04() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "E  ",
//                "EE "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(0, 2);
//
//        // WHEN
//
//        // THEN
//        assertSame(4, underTest.countEdgeNeighboursTransitions(pPixelMap));
//    }
//
//    @Test
//    public void calcIsNode_01() {
//        // GIVEN
//        final String[] input = {
//                "   ",
//                "   ",
//                "E  "
//        };
//        final PixelMap pixelMap = Utility.createMap(input);
//        final Pixel underTest = pixelMap.getPixelAt(0, 2);
//
//        // WHEN
//        final boolean n = underTest.calcIsNode(pPixelMap);
//
//        // THEN
//        assertTrue(n);
//        assertTrue(underTest.isNode(pPixelMap));
//    }
//
//
//}
