package com.ownimage.perception.pixelMap;

import com.ownimage.framework.view.javafx.FXViewFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PixelChainTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.setAsViewFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
        IPixelMapTransformSource ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getLineTolerance()).thenReturn(1.7d);
        when(ipmts.getLineCurvePreference()).thenReturn(1.2d);
        when(ipmts.getHeight()).thenReturn(input.length);

        PixelMap pixelMap = Utility.createMap(input, ipmts);
        pixelMap.process03_generateNodes(null);

        // WHEN
        pixelMap.process05_generateChains(null);
        assertEquals(1, pixelMap.getPixelChainCount());

        // THEN
        PixelChain chain = pixelMap.streamPixelChains().findFirst().get();
        chain = chain.approximate(ipmts, pixelMap);
        assertEquals(3, chain.getSegmentCount());
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void PixelChain_reverse_01() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 2000);
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
        IPixelMapTransformSource ipmts = mock(IPixelMapTransformSource.class);
        when(ipmts.getLineCurvePreference()).thenReturn(3d);
        when(ipmts.getLineTolerance()).thenReturn(2d);
        when(ipmts.getHeight()).thenReturn(2000);

        // WHEN
        var underTest = new PixelChain(new Node(3, 7));
        for (Pixel pixel : pixels) {
            underTest = underTest.add(pixel);
        }
        underTest = underTest.setEndNode(null, new Node(6, 7));

        final double tolerance = 2d / 2000;
        underTest.approximate01_straightLines(pixelMap, tolerance);
        var approx = underTest.approximate(ipmts, pixelMap);

        // AND WHEN
        var reverse = approx.reverse(pixelMap);
        var compare = reverse.reverse(pixelMap);
        assertEquals(underTest.toString(), compare.toString());
        assertEquals(dumpPixelChain(underTest), dumpPixelChain(compare));

    }

    public String dumpPixelChain(final PixelChain pPixelChain) {
        var sb = new StringBuilder();
        pPixelChain.streamSegments().forEach(s -> {
            sb.append(s);
            sb.append(pPixelChain.getVertex(s.getSegmentIndex()));
            sb.append(pPixelChain.getVertex(s.getSegmentIndex() + 1));
        });
        return sb.toString();
    }

}
