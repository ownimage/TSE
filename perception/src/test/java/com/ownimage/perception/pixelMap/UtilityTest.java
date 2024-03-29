package com.ownimage.perception.pixelMap;

import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.LogManager;

import static com.ownimage.perception.pixelMap.Utility.assertSamePixels;
import static org.junit.Assert.assertArrayEquals;

public class UtilityTest {

    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private static PixelChainService pixelChainService = context.getBean(PixelChainService.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setViewFactory() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void toStringsTest() {
        // GIVEN
        String[] input = {
                "    N      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "NEEN NEEN  ",
                "           ",
        };
        var immputablePixelMap = Utility.createMap(input, false);
        // WHEN
        var actual = Utility.toStrings(immputablePixelMap);
        // THEN
        assertArrayEquals(input, actual);
    }

    @Test
    public void assertSamePixels_01() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);
        var startNode = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(5, 5);
        var pixelChain1 = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        var pixelChain2 = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        // WHEN
        assertSamePixels(pixelMap, pixelChain1, pixelChain2);
    }

    @Test
    public void assertSamePixels_02() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);
        var startNode = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(5, 5);
        var pixelChain1 = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        var pixelChain2 = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        pixelChain2 = pixelChainService.add(pixelChain2, Pixel.of(5, 6, pixelMap.height()));
        thrown.expectMessage("PixelChains do not contain same pixels");
        // WHEN
        assertSamePixels(pixelMap, pixelChain1, pixelChain2);
    }

    @Test
    public void assertSamePixels_03() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);
        var startNode = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(5, 5);
        var pixelChain1 = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        pixelChain1 = pixelChainService.add(pixelChain1, Pixel.of(5, 6, pixelMap.height()));
        var pixelChain2 = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        pixelChain2 = pixelChainService.add(pixelChain2, Pixel.of(5, 6, pixelMap.height()));
        // WHEN
        assertSamePixels(pixelMap, pixelChain1, pixelChain2);
    }

    @Test
    public void assertSamePixels_04() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);

        var startNode1 = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(5, 5);
        var pixelChain1 = pixelChainService.createStartingPixelChain(pixelMap, startNode1);
        pixelChain1 = pixelChainService.add(pixelChain1, Pixel.of(5, 6, pixelMap.height()));
        pixelChain1 = pixelChainService.add(pixelChain1, Pixel.of(6, 7, pixelMap.height()));
        pixelChain1 = pixelChainService.add(pixelChain1, Pixel.of(8, 8, pixelMap.height()));

        var startNode2 = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(8, 8);
        var pixelChain2 = pixelChainService.createStartingPixelChain(pixelMap, startNode2);
        pixelChain2 = pixelChainService.add(pixelChain2, Pixel.of(6, 7, pixelMap.height()));
        pixelChain2 = pixelChainService.add(pixelChain2, Pixel.of(5, 6, pixelMap.height()));
        pixelChain2 = pixelChainService.add(pixelChain2, Pixel.of(5, 5, pixelMap.height()));
        // WHEN
        assertSamePixels(pixelMap, pixelChain1, pixelChain2);
    }
}
