package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PixelTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private PixelService pixelService = context.getBean(PixelService.class);

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Test
    public void isEdge_00() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "   "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 1);

        // WHEN

        // THEN
        assertFalse(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_01() {
        // GIVEN
        String[] input = {
                "   ",
                " E ",
                "   "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 1);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_02() {
        // GIVEN
        String[] input = {
                "E  ",
                "   ",
                "   "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 0, 0);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_03() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                " E "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 2);

        // WHEN

        // THEN
        assertTrue(underTest.isEdge(pixelMap));
    }

    @Test
    public void isEdge_04() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                " E "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, -1, 2);

        // WHEN

        // THEN
        assertFalse(underTest.isEdge(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_00() {
        // GIVEN
        String[] input = {
                "   ",
                " E ",
                "   "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 1);

        // WHEN

        // THEN
        assertSame(0, underTest.countEdgeNeighbours(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_01() {
        // GIVEN
        String[] input = {
                "   ",
                "EEE",
                "   "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 1);

        // WHEN

        // THEN
        assertSame(2, underTest.countEdgeNeighbours(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_02() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "EEE"
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 2);

        // WHEN

        // THEN
        assertSame(2, underTest.countEdgeNeighbours(pixelMap));
    }

    @Test
    public void countEdgeNeighbours_03() {
        // GIVEN
        String[] input = {
                " N  ",
                "    ",
                "N N "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 0, 2);

        // WHEN
        int actual = underTest.countEdgeNeighbours(pixelMap);

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
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 1);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void countEdgeNeighboursTransitions_02() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "EEE"
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 2);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void countEdgeNeighboursTransitions_03() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "E  "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 0, 2);

        // WHEN

        // THEN
        assertSame(0, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void countEdgeNeighboursTransitions_04() {
        // GIVEN
        String[] input = {
                "   ",
                "E  ",
                "EE "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 0, 2);

        // WHEN

        // THEN
        assertSame(4, underTest.countEdgeNeighboursTransitions(pixelMap));
    }

    @Test
    public void calcIsNode_01() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "E  "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 0, 2);

        // WHEN
        var actual = pixelMapService.calcIsNode(pixelMap, underTest);
        boolean n = actual._2;
        var finalPM = actual._1;

        // THEN
        assertTrue(n);
        assertTrue(pixelService.isNode(finalPM, underTest));
    }

    @Test
    public void getNeighbours_01() {
        // GIVEN
        String[] input = {
                "   ",
                "   ",
                "E  "
        };
        var pixelMap = Utility.createMap(input, false);
        Pixel underTest = pixelMapService.getPixelAt(pixelMap, 1, 1);
        StrongReference<Integer> count = new StrongReference<>(0);

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
