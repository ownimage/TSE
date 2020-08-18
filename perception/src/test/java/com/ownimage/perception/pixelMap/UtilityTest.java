package com.ownimage.perception.pixelMap;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.view.javafx.FXViewFactory;
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
        FrameworkLogger.getInstance().init("logging.properties", "Perception.log");
        FXViewFactory.setAsViewFactory(false);
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
        pixelChain2 = pixelChainService.add(pixelChain2, new Pixel(5, 6));
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
        pixelChain1 = pixelChainService.add(pixelChain1, new Pixel(5, 6));
        var pixelChain2 = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        pixelChain2 = pixelChainService.add(pixelChain2, new Pixel(5, 6));
        // WHEN
        assertSamePixels(pixelMap, pixelChain1, pixelChain2);
    }

    @Test
    public void assertSamePixels_04() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);

        var startNode1 = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(5, 5);
        var pixelChain1 = pixelChainService.createStartingPixelChain(pixelMap, startNode1);
        pixelChain1 = pixelChainService.add(pixelChain1, new Pixel(5, 6));
        pixelChain1 = pixelChainService.add(pixelChain1, new Pixel(6, 7));
        pixelChain1 = pixelChainService.add(pixelChain1, new Pixel(8, 8));

        var startNode2 = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(8, 8);
        var pixelChain2 = pixelChainService.createStartingPixelChain(pixelMap, startNode2);
        pixelChain2 = pixelChainService.add(pixelChain2, new Pixel(6, 7));
        pixelChain2 = pixelChainService.add(pixelChain2, new Pixel(5, 6));
        pixelChain2 = pixelChainService.add(pixelChain2, new Pixel(5, 5));
        // WHEN
        assertSamePixels(pixelMap, pixelChain1, pixelChain2);
    }
}
