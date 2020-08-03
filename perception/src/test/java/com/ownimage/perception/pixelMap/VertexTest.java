package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.VertexService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class VertexTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private VertexService vertexService = context.getBean(VertexService.class);

    @Mock
    PixelMapData pixelMap;

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Before
    public void before() {
        when(pixelMap.height()).thenReturn(100);
        when(pixelMap.width()).thenReturn(100);
    }

    @Ignore
    @Test
    public void ctor_00() {
        // GIVEN
        int index = 6;
        Point point = new Point(0.2, 0.3);
        var mockPixel = new MockPixelBuilder().with_getUHVWMidPoint_returns(point).build();
        var mockPixelMap = new MockPixelMapBuilder().build();
        var mockPixelChain = new MockPixelChainBuilder(10)
                .with_getPixel_returns(index, mockPixel)
                .build();
        // WHEN
        IVertex underTest = vertexService.createVertex(pixelMap, mockPixelChain, 1, index);
        // THEN
        assertEquals(index, underTest.getPixelIndex());
        assertEquals(1, underTest.getVertexIndex());
        assertEquals(point, underTest.getPosition());
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getPixel(index);
        verify(mockPixel, times(1)).getUHVWMidPoint(mockPixelMap.height());
//        verifyNoMoreInteractions(mockPixelMap, mockPixelChain, mockPixel);
    }

    @Test
    public void ctor_01() {
        // GIVEN
        int index = 6;
        Point pointCtor = new Point(0.4, 0.5);
        var mockPixelMap = new MockPixelMapBuilder().build();
        var mockPixelChain = new MockPixelChainBuilder(10).build();
        // WHEN
        IVertex underTest = vertexService.createVertex(mockPixelChain, 1, index, pointCtor);
        // THEN
        assertEquals(index, underTest.getPixelIndex());
        assertEquals(1, underTest.getVertexIndex());
        assertEquals(pointCtor, underTest.getPosition());
        verify(mockPixelChain, times(1)).getPixelCount();
        verifyNoMoreInteractions(mockPixelMap, mockPixelChain);
    }

    @Ignore
    @Test
    public void equals_00() {
        // GIVEN
        var pixelChain1 = new MockPixelChainBuilder(10).build();
        var pixelChain2 = new MockPixelChainBuilder(11).build();
        IVertex underTest1 = vertexService.createVertex(pixelMap, pixelChain1, 1, 5);
        IVertex underTest2 = vertexService.createVertex(pixelMap, pixelChain2, 1, 5);
        // WHEN THEN
        assertTrue(underTest1.equals(underTest2));
        assertTrue(underTest2.equals(underTest1));
        verify(pixelChain1, times(1)).getPixelCount();
        verify(pixelChain2, times(1)).getPixelCount();
//        verifyNoMoreInteractions(pixelChain1, pixelChain2);
    }

    @Test
    public void equals_02() {
        // GIVEN
        var pixelChain1 = new MockPixelChainBuilder(10).build();
        var pixelChain2 = new MockPixelChainBuilder(11).build();
        IVertex underTest1 = vertexService.createVertex(pixelChain1, 1, 5, new Point(0.2, 0.3));
        IVertex underTest2 = vertexService.createVertex(pixelChain2, 1, 5, new Point(0.2, 0.3));
        // WHEN THEN
        assertTrue(underTest1.equals(underTest2));
        assertTrue(underTest2.equals(underTest1));
        verify(pixelChain1, times(1)).getPixelCount();
        verify(pixelChain2, times(1)).getPixelCount();
        verifyNoMoreInteractions(pixelChain1, pixelChain2);
    }

    @Test
    public void equals_03() {
        // GIVEN
        var pixelChain1 = new MockPixelChainBuilder(10).build();
        var pixelChain2 = new MockPixelChainBuilder(11).build();
        IVertex underTest1 = vertexService.createVertex(pixelChain1, 1, 5, new Point(0.2, 0.3));
        IVertex underTest2 = vertexService.createVertex(pixelChain2, 1, 5, new Point(0.4, 0.5));
        // WHEN THEN
        assertFalse(underTest1.equals(underTest2));
        assertFalse(underTest2.equals(underTest1));
        verify(pixelChain1, times(1)).getPixelCount();
        verify(pixelChain2, times(1)).getPixelCount();
        verifyNoMoreInteractions(pixelChain1, pixelChain2);
    }

    @Test
    public void test_toString_00() {
        // GIVEN
        int pixelIndex = 5;
        var pixelChain = new MockPixelChainBuilder(11).build();
        when(pixelChain.getUHVWPoint(pixelMap, 5)).thenReturn(new Point(1.2, 3.4));
        IVertex underTest1 = vertexService.createVertex(pixelMap, pixelChain, 1, pixelIndex);
        var expected = "Vertex { vertexIndex: 1, pixelIndex: 5, position: Point{ x : 1.2, y: 3.4 } }";
        // WHEN THEN
        assertEquals(expected, underTest1.toString());
        verify(pixelChain, times(1)).getPixelCount();
//        verifyNoMoreInteractions(pixelChain);
    }

    @Test
    public void test_toString_01() {
        // GIVEN
        int pixelIndex = 5;
        var pixelChain = new MockPixelChainBuilder(11).build();
        IVertex underTest1 = vertexService.createVertex(pixelChain, 1, pixelIndex, new Point(0.5, 0.3));
        var expected = "Vertex { vertexIndex: 1, pixelIndex: 5, position: Point{ x : 0.5, y: 0.3 } }";
        // WHEN THEN
        assertEquals(expected, underTest1.toString());
        verify(pixelChain, times(1)).getPixelCount();
        verifyNoMoreInteractions(pixelChain);
    }

    @Ignore
    @Test
    public void getPixel_00() {
        // GIVEN
        int pixelIndex = 5;
        var pixel = new MockPixelBuilder().build();
        var pixelChain = new MockPixelChainBuilder(11).with_getPixel_returns(pixelIndex, pixel).build();
        IVertex underTest1 = vertexService.createVertex(pixelMap, pixelChain, 1, pixelIndex);
        // WHEN THEN
        assertEquals(pixel, vertexService.getPixel(pixelChain, underTest1));
        verify(pixelChain, times(1)).getPixelCount();
        verify(pixelChain, times(1)).getPixel(eq(pixelIndex));
//        verifyNoMoreInteractions(pixelChain);
    }

    @Test
    @Ignore
    public void getStartSegment_00() {
        // GIVEN
        int pixelIndex = 5;
        int vertexIndex = 2;
        int segmentIndex = vertexIndex - 1;
        ISegment expected = mock(ISegment.class);
        var pixel = new Pixel(5, 3);
        var mockPixelChain = new MockPixelChainBuilder(11)
                .with_getSegment_returns(segmentIndex, expected)
                .with_getPixel_returns(pixelIndex, pixel)
                .build();
        IVertex underTest = vertexService.createVertex(pixelMap, mockPixelChain, vertexIndex, pixelIndex);
        // WHEN THEN
        assertEquals(expected, vertexService.getStartSegment(mockPixelChain, underTest));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(segmentIndex);
//        verifyNoMoreInteractions(mockPixelChain, expected);
    }

    @Ignore
    @Test
    public void getEndSegment_00() {
        // GIVEN
        int pixelIndex = 5;
        int vertexIndex = 2;
        int segmentIndex = vertexIndex;
        ISegment expected = mock(ISegment.class);
        var pixel = new Pixel(5, 3);
        var mockPixelChain = new MockPixelChainBuilder(11)
                .with_getSegment_returns(segmentIndex, expected)
                .with_getPixel_returns(pixelIndex, pixel)
                .build();
        IVertex underTest = vertexService.createVertex(pixelMap, mockPixelChain, vertexIndex, pixelIndex);
        // WHEN THEN
        assertEquals(expected, vertexService.getEndSegment(mockPixelChain, underTest));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(segmentIndex);
//        verifyNoMoreInteractions(mockPixelChain, expected);
    }

    private class MockPixelBuilder {
        Pixel mPixel = mock(Pixel.class);

        public MockPixelBuilder() {
        }

        public MockPixelBuilder with_getUHVWMidPoint_returns(Point pPoint) {
            when(mPixel.getUHVWMidPoint(any())).thenReturn(pPoint);
            return this;
        }

        public Pixel build() {
            return mPixel;
        }
    }

    private class MockPixelChainBuilder {
        PixelChain mPixelChain = mock(PixelChain.class);

        public MockPixelChainBuilder(int pLength) {
            when(mPixelChain.getPixelCount()).thenReturn(pLength);
        }

        MockPixelChainBuilder with_getPixel_returns(int pIndex, Pixel pPixel) {
            when(mPixelChain.getPixel(eq(pIndex))).thenReturn(pPixel);
            return this;
        }

        public MockPixelChainBuilder with_getSegment_returns(int pSegmentIndex, ISegment pSegment) {
            when(mPixelChain.getSegment(pSegmentIndex)).thenReturn(pSegment);
            return this;
        }

        public PixelChain build() {
            return mPixelChain;
        }

    }

    private class MockPixelMapBuilder {
        PixelMapData mPixelMap = mock(PixelMapData.class);

        public MockPixelMapBuilder() {
        }

        PixelMapData build() {
            return mPixelMap;
        }
    }

}
