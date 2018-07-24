package com.ownimage.perception.pixelMap;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.*;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.transform.CannyEdgeTransform;

public class PixelChainTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.setAsViewFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void PixelChain_refine_01() {
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
        when(ipmts.getLineCurvePreference()).thenReturn(1.2d);

        PixelMap pixelMap = Utility.createMap(input, ipmts);
        pixelMap.process03_generateNodes();

        // WHEN
        final Vector<PixelChain> chains = pixelMap.process05_generateChains();

        // THEN
        assertEquals(1, chains.size());

        // WHEN
        final PixelChain chain = chains.get(0);
        chain.approximate01_straightLines(1.7d /input.length );

        // THEN
        assertEquals(3,  chain.getSegmentCount());

        // WHEN
        chain.approximate02_refineCorners();
        chain.refine();

        // THEN
        assertEquals(3,  chain.getSegmentCount());
        chain.getAllSegments().forEach( s -> System.out.println(s.getClass().getName()));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
