package com.ownimage.perception.pixelMap;

import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PixelChainTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private PixelMapApproximationService pixelMapApproximationService = context.getBean(PixelMapApproximationService.class);
    private PixelChainService pixelChainService = context.getBean(PixelChainService.class);

    @Mock
    PixelMap pixelMap;

    @BeforeClass
    public static void setupViewFactory() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory(false);
    }

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Before
    public void before() {
        pixelMap = mock(PixelMap.class);
        when(pixelMap.height()).thenReturn(100);
        when(pixelMap.width()).thenReturn(100);
    }

    @Test
    public void pixelChain_approximate() {
        // GIVEN
        String[] input = {
                "                          ",
                "     EEE                  ",
                "    E   EE                ",
                "    E     E               ",
                "   E       E              ",
                "   E      E               ",
                "    E    E                ",
                "   N  NEE                 ",
                "                          ",
        };
        var ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getHeight()).thenReturn(input.length);
        when(ipmts.getLineTolerance()).thenReturn(1.2d);
        when(ipmts.getLineCurvePreference()).thenReturn(1.7d);
        double tolerance = ipmts.getLineTolerance() / ipmts.getHeight();

        var pixelMap = Utility.createMap(input, ipmts, false);
        pixelMap = pixelMapApproximationService.process03_generateNodes(pixelMap, null);

        // WHEN
        pixelMap = pixelMapApproximationService.process05_generateChains(pixelMap, null);
        assertEquals(1, pixelMap.pixelChains().size());

        // THEN
        var chain = pixelMapService.streamPixelChains(pixelMap).findFirst().orElseThrow();
        chain = pixelChainService.approximate(pixelMap, chain, tolerance);
        assertEquals(3, chain.getSegmentCount());
    }

    @Test
    public void PixelChain_reverse_01() {
        // GIVEN
        var lineTolerance = 2.0d;
        var pixelMap = Utility.createMap(10, 2000);
        var ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getHeight()).thenReturn(2000);
        when(ipmts.getLineTolerance()).thenReturn(lineTolerance);
        when(ipmts.getLineCurvePreference()).thenReturn(3.0d);
        double tolerance = ipmts.getLineTolerance() / ipmts.getHeight();

        // WHEN
        var underTest = createPixelChain();
        var approx = pixelChainService.approximate(pixelMap, underTest, tolerance);

        // AND WHEN
        var reverse = pixelChainService.reverse(pixelMap, approx);
        var compare = pixelChainService.reverse(pixelMap, reverse);
        assertEquals(underTest.toReadableString(), compare.toReadableString());
        assertEquals(dumpPixelChain(approx), dumpPixelChain(compare));

    }

    @Test
    public void getWidth_00() {
        // GIVEN a thick line
        var longThickness = 77d;
        var height = 1000;
        var underTest = createPixelChain();
        var length = underTest.getPixelCount();
        underTest = pixelChainService.withThickness(underTest, 1, length - 2, length - 1);
        var ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getHeight()).thenReturn(height);
        when(ipmts.getLongLineThickness()).thenReturn(longThickness);
        var expected = longThickness / height;
        // WHEN
        var actual = underTest.getWidth(ipmts);
        // THEN
        assertEquals(expected, actual, 0.0d);
    }

    @Test
    public void getWidth_01() {
        // GIVEN a medium line
        var longThickness = 77d;
        var mediumThickness = 55d;
        var height = 1000;
        var underTest = createPixelChain();
        var length = underTest.getPixelCount();
        underTest = pixelChainService.withThickness(underTest, 1, length - 2, length + 1);
        var ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getHeight()).thenReturn(height);
        when(ipmts.getLongLineThickness()).thenReturn(longThickness);
        when(ipmts.getMediumLineThickness()).thenReturn(mediumThickness);
        var expected = mediumThickness / height;
        // WHEN
        var actual = underTest.getWidth(ipmts);
        // THEN
        assertEquals(expected, actual, 0.0d);
    }

    @Test
    public void getWidth_02() {
        // GIVEN a short line
        var longThickness = 77d;
        var mediumThickness = 55d;
        var shortThickness = 33d;
        var height = 1000;
        var underTest = createPixelChain();
        var length = underTest.getPixelCount();
        underTest = pixelChainService.withThickness(underTest, 1, length + 2, length + 4);
        var ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getHeight()).thenReturn(height);
        when(ipmts.getLongLineThickness()).thenReturn(longThickness);
        when(ipmts.getMediumLineThickness()).thenReturn(mediumThickness);
        when(ipmts.getShortLineThickness()).thenReturn(shortThickness);
        var expected = shortThickness / height;
        // WHEN
        var actual = underTest.getWidth(ipmts);
        // THEN
        assertEquals(expected, actual, 0.0d);
    }

    @Test
    public void getWidth_03() {
        // GIVEN a very short line
        var longThickness = 77d;
        var mediumThickness = 55d;
        var shortThickness = 33d;
        var height = 1000;
        var underTest = createPixelChain();
        var length = underTest.getPixelCount();
        underTest = pixelChainService.withThickness(underTest, length + 1, length + 2, length + 4);
        var ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getHeight()).thenReturn(height);
        when(ipmts.getLongLineThickness()).thenReturn(longThickness);
        when(ipmts.getMediumLineThickness()).thenReturn(mediumThickness);
        when(ipmts.getShortLineThickness()).thenReturn(shortThickness);
        var expected = 0d;
        // WHEN
        var actual = underTest.getWidth(ipmts);
        // THEN
        assertEquals(expected, actual, 0.0d);
    }

    @Test
    public void getOptionalPixel_01() {
        // GIVEN
        val underTest = createPixelChain();
        // WHEN
        val actual = underTest.getOptionalPixel(-1);
        // THEN
        Assert.assertEquals(Optional.empty(), actual);
    }

    @Test
    public void getOptionalPixel_02() {
        // GIVEN
        val underTest = createPixelChain();
        // WHEN
        val actual = underTest.getOptionalPixel(25);
        // THEN
        Assert.assertEquals(Optional.empty(), actual);
    }

    @Test
    public void getOptionalPixel_03() {
        // GIVEN
        val underTest = createPixelChain();
        // WHEN
        val actual = underTest.getOptionalPixel(2).orElseThrow();
        // THEN
        Assert.assertEquals(Pixel.of(3, 5, pixelMap.height()), actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPixel_01() {
        // GIVEN
        val underTest = createPixelChain();
        // WHEN
        underTest.getPixel(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPixel_02() {
        // GIVEN
        val underTest = createPixelChain();
        // WHEN
        underTest.getPixel(25);
    }

    @Test
    public void getlPixel_03() {
        // GIVEN
        val underTest = createPixelChain();
        // WHEN
        val actual = underTest.getPixel(2);
        // THEN
        Assert.assertEquals(Pixel.of(3, 5, pixelMap.height()), actual);
    }

    @Test
    public void getMaxPixelIndex_01() {
        // GIVEN
        val underTest = createPixelChain();
        val expected = 17;
        // WHEN
        val actual = underTest.getMaxPixelIndex();
        // THEN
        Assert.assertEquals(expected, actual);
    }

    private ImmutablePixelChain createPixelChain() {
        var height = pixelMap.height();
        var pixelChain = Utility.createPixelChain(pixelMap,
                Pixel.of(3, 7, height),
                Pixel.of(4, 6, height),
                Pixel.of(3, 5, height),
                Pixel.of(3, 4, height),
                Pixel.of(4, 3, height),
                Pixel.of(4, 2, height),
                Pixel.of(5, 1, height),
                Pixel.of(6, 1, height),
                Pixel.of(7, 1, height),
                Pixel.of(8, 2, height),
                Pixel.of(9, 2, height),
                Pixel.of(10, 3, height),
                Pixel.of(11, 4, height),
                Pixel.of(10, 5, height),
                Pixel.of(9, 6, height),
                Pixel.of(8, 7, height),
                Pixel.of(7, 7, height),
                Pixel.of(6, 7, height));
        return pixelChain;
    }

    private String dumpPixelChain(PixelChain pPixelChain) {
        var sb = new StringBuilder();
        pPixelChain.streamSegments().forEach(s -> {
            sb.append(s);
            sb.append(pPixelChain.getVertex(s.getSegmentIndex()));
            sb.append(pPixelChain.getVertex(s.getSegmentIndex() + 1));
        });
        return sb.toString();
    }

}
