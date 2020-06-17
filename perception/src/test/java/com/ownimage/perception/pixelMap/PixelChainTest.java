package com.ownimage.perception.pixelMap;

import com.ownimage.framework.view.javafx.FXViewFactory;
import lombok.val;
import org.junit.*;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PixelChainTest {

    @Mock
    PixelMap pixelMap;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory(false);
    }

    @Before
    public void before() {
        when(pixelMap.getHeight()).thenReturn(100);
        when(pixelMap.getWidth()).thenReturn(100);
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

        PixelMap pixelMap = Utility.createMap(input, ipmts);
        pixelMap.process03_generateNodes(null);

        // WHEN
        pixelMap.process05_generateChains(null);
        assertEquals(1, pixelMap.getPixelChainCount());

        // THEN
        PixelChain chain = pixelMap.streamPixelChains().findFirst().orElseThrow();
        chain = chain.approximate(pixelMap, tolerance);
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
        var approx = underTest.approximate(pixelMap, tolerance);

        // AND WHEN
        var reverse = approx.reverse(pixelMap);
        var compare = reverse.reverse(pixelMap);
        assertEquals(underTest.toString(), compare.toString());
        assertEquals(dumpPixelChain(approx), dumpPixelChain(compare));

    }

    @Test
    public void getWidth_00() {
        // GIVEN a thick line
        var longThickness = 77d;
        var height = 1000;
        var underTest = createPixelChain();
        var length = underTest.getPixelCount();
        underTest = underTest.setThickness(1, length - 2, length - 1);
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
        underTest = underTest.setThickness(1, length - 2, length + 1);
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
        underTest = underTest.setThickness(1, length + 2, length + 4);
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
        underTest = underTest.setThickness(length + 1, length + 2, length + 4);
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
        Assert.assertEquals(new Pixel(3, 5), actual);
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
        Assert.assertEquals(new Pixel(3, 5), actual);
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

    private PixelChain createPixelChain() {
        Pixel[] pixels = new Pixel[]{
                new Pixel(4, 6),
                new Pixel(3, 5),
                new Pixel(3, 4),
                new Pixel(4, 3),
                new Pixel(4, 2),
                new Pixel(5, 1),
                new Pixel(6, 1),
                new Pixel(7, 1),
                new Pixel(8, 2),
                new Pixel(9, 2),
                new Pixel(10, 3),
                new Pixel(11, 4),
                new Pixel(10, 5),
                new Pixel(9, 6),
                new Pixel(8, 7),
                new Pixel(7, 7)
        };
        var pixelMap = Utility.createMap(10, 2000);

        var pixelChain = new PixelChain(pixelMap, new Node(3, 7));
        for (Pixel pixel : pixels) {
            pixelChain = pixelChain.add(pixelMap, pixel);
        }
        pixelChain = pixelChain.setEndNode(null, new Node(6, 7));
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
