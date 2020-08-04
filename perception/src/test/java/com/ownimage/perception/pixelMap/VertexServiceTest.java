package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.immutable.VertexData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.VertexService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VertexServiceTest {

    //    @Mock
    PixelMap pixelMap;
    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private VertexService vertexService = context.getBean(VertexService.class);

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

//    @Test
//    public void ctor_00() {
//        // GIVEN
//        int index = 6;
//        Point point = new Point(0.2, 0.3);
//        var mockPixel = new MockPixelBuilder().with_getUHVWMidPoint_returns(point).build();
//        var mockPixelMap = new MockPixelMapBuilder().build();
//        var mockPixelChain = new MockPixelChainBuilder(10)
//                .with_getPixel_returns(index, mockPixel)
//                .build();
//        // WHEN
//        VertexData underTest = Vertex.createVertex(pixelMap, mockPixelChain, 1, index);
//        // THEN
//        assertEquals(index, underTest.getPixelIndex());
//        assertEquals(1, underTest.getVertexIndex());
//        assertEquals(point, underTest.getPosition(mockPixelMap, mockPixelChain));
//        verify(mockPixelChain, times(1)).getPixelCount();
//        verify(mockPixelChain, times(1)).getPixel(index);
//        verify(mockPixel, times(1)).getUHVWMidPoint(mockPixelMap);
//        verifyNoMoreInteractions(mockPixelMap, mockPixelChain, mockPixel);
//    }
//
//    @Test
//    public void ctor_01() {
//        // GIVEN
//        int index = 6;
//        Point pointCtor = new Point(0.4, 0.5);
//        var mockPixelMap = new MockPixelMapBuilder().build();
//        var mockPixelChain = new MockPixelChainBuilder(10).build();
//        // WHEN
//        VertexData underTest = Vertex.createVertex(mockPixelChain, 1, index, pointCtor);
//        // THEN
//        assertEquals(index, underTest.getPixelIndex());
//        assertEquals(1, underTest.getVertexIndex());
//        assertEquals(pointCtor, underTest.getPosition(mockPixelMap, mockPixelChain));
//        verify(mockPixelChain, times(1)).getPixelCount();
//        verifyNoMoreInteractions(mockPixelMap, mockPixelChain);
//    }
//
//    @Test
//    public void equals_00() {
//        // GIVEN
//        var pixelChain1 = new MockPixelChainBuilder(10).build();
//        var pixelChain2 = new MockPixelChainBuilder(11).build();
//        VertexData underTest1 = Vertex.createVertex(pixelMap, pixelChain1, 1, 5);
//        VertexData underTest2 = Vertex.createVertex(pixelMap, pixelChain2, 1, 5);
//        // WHEN THEN
//        assertTrue(underTest1.equals(underTest2));
//        assertTrue(underTest2.equals(underTest1));
//        verify(pixelChain1, times(1)).getPixelCount();
//        verify(pixelChain2, times(1)).getPixelCount();
//        verifyNoMoreInteractions(pixelChain1, pixelChain2);
//    }
//
//    @Test
//    public void equals_02() {
//        // GIVEN
//        var pixelChain1 = new MockPixelChainBuilder(10).build();
//        var pixelChain2 = new MockPixelChainBuilder(11).build();
//        VertexData underTest1 = Vertex.createVertex(pixelChain1, 1, 5, new Point(0.2, 0.3));
//        VertexData underTest2 = Vertex.createVertex(pixelChain2, 1, 5, new Point(0.2, 0.3));
//        // WHEN THEN
//        assertTrue(underTest1.equals(underTest2));
//        assertTrue(underTest2.equals(underTest1));
//        verify(pixelChain1, times(1)).getPixelCount();
//        verify(pixelChain2, times(1)).getPixelCount();
//        verifyNoMoreInteractions(pixelChain1, pixelChain2);
//    }
//
//    @Test
//    public void equals_03() {
//        // GIVEN
//        var pixelChain1 = new MockPixelChainBuilder(10).build();
//        var pixelChain2 = new MockPixelChainBuilder(11).build();
//        VertexData underTest1 = Vertex.createVertex(pixelChain1, 1, 5, new Point(0.2, 0.3));
//        VertexData underTest2 = Vertex.createVertex(pixelChain2, 1, 5, new Point(0.4, 0.5));
//        // WHEN THEN
//        assertFalse(underTest1.equals(underTest2));
//        assertFalse(underTest2.equals(underTest1));
//        verify(pixelChain1, times(1)).getPixelCount();
//        verify(pixelChain2, times(1)).getPixelCount();
//        verifyNoMoreInteractions(pixelChain1, pixelChain2);
//    }
//
//    @Test
//    public void test_toString_00() {
//        // GIVEN
//        int pixelIndex = 5;
//        var pixelChain = new MockPixelChainBuilder(11).build();
//        VertexData underTest1 = Vertex.createVertex(pixelMap, pixelChain, 1, pixelIndex);
//        var expected = "Vertex { vertexIndex: 1, pixelIndex: 5 }";
//        // WHEN THEN
//        assertEquals(expected, underTest1.toString());
//        verify(pixelChain, times(1)).getPixelCount();
//        verifyNoMoreInteractions(pixelChain);
//    }
//
//    @Test
//    public void test_toString_01() {
//        // GIVEN
//        int pixelIndex = 5;
//        var pixelChain = new MockPixelChainBuilder(11).build();
//        VertexData underTest1 = Vertex.createVertex(pixelChain, 1, pixelIndex, new Point(0.5, 0.3));
//        var expected = "Vertex { vertexIndex: 1, pixelIndex: 5, position: Point{ x : 0.5, y: 0.3 } }";
//        // WHEN THEN
//        assertEquals(expected, underTest1.toString());
//        verify(pixelChain, times(1)).getPixelCount();
//        verifyNoMoreInteractions(pixelChain);
//    }
//
//    @Test
//    public void getPixel_00() {
//        // GIVEN
//        int pixelIndex = 5;
//        var pixel = new MockPixelBuilder().build();
//        var pixelChain = new MockPixelChainBuilder(11).with_getPixel_returns(pixelIndex, pixel).build();
//        VertexData underTest1 = Vertex.createVertex(pixelMap, pixelChain, 1, pixelIndex);
//        // WHEN THEN
//        assertEquals(pixel, underTest1.getPixel(pixelChain));
//        verify(pixelChain, times(1)).getPixelCount();
//        verify(pixelChain, times(1)).getPixel(eq(pixelIndex));
//        verifyNoMoreInteractions(pixelChain);
//    }
//
//    @Test
//    public void getStartSegment_00() {
//        // GIVEN
//        int pixelIndex = 5;
//        int vertexIndex = 2;
//        int segmentIndex = vertexIndex - 1;
//        ISegment expected = mock(ISegment.class);
//        var mockPixelChain = new MockPixelChainBuilder(11)
//                .with_getSegment_returns(segmentIndex, expected)
//                .build();
//        VertexData underTest = Vertex.createVertex(pixelMap, mockPixelChain, vertexIndex, pixelIndex);
//        // WHEN THEN
//        assertEquals(expected, underTest.getStartSegment(mockPixelChain));
//        verify(mockPixelChain, times(1)).getPixelCount();
//        verify(mockPixelChain, times(1)).getSegment(segmentIndex);
//        verifyNoMoreInteractions(mockPixelChain, expected);
//    }
//
//    @Test
//    public void getEndSegment_00() {
//        // GIVEN
//        int pixelIndex = 5;
//        int vertexIndex = 2;
//        int segmentIndex = vertexIndex;
//        ISegment expected = mock(ISegment.class);
//        var mockPixelChain = new MockPixelChainBuilder(11)
//                .with_getSegment_returns(segmentIndex, expected)
//                .build();
//        VertexData underTest = Vertex.createVertex(pixelMap, mockPixelChain, vertexIndex, pixelIndex);
//        // WHEN THEN
//        assertEquals(expected, underTest.getEndSegment(mockPixelChain));
//        verify(mockPixelChain, times(1)).getPixelCount();
//        verify(mockPixelChain, times(1)).getSegment(segmentIndex);
//        verifyNoMoreInteractions(mockPixelChain, expected);
//    }
//
////    public Line calcTangent(final PixelChain mockPixelChain, final PixelMap pixelMap) {
////        Line tangent;
////        if (getStartSegment(mockPixelChain) == null && getEndSegment(mockPixelChain) == null) {
////            tangent = null;
////        } else if (getStartSegment(mockPixelChain) == null) {
////            tangent = getEndSegment(mockPixelChain).getStartTangent(pixelMap, mockPixelChain);
////            tangent = tangent.getReverse();
////        } else if (getEndSegment(mockPixelChain) == null) {
////            tangent = getStartSegment(mockPixelChain).getEndTangent(pixelMap, mockPixelChain);
////        } else {
////            final Point startTangentPoint = getStartSegment(mockPixelChain).getEndTangent(pixelMap, mockPixelChain).getPoint(1.0d);
////            final Point endTangentPoint = getEndSegment(mockPixelChain).getStartTangent(pixelMap, mockPixelChain).getPoint(1.0d);
////            final Vector tangentVector = startTangentPoint.minus(endTangentPoint).normalize();
////
////            tangent = new Line(getPosition(pixelMap, mockPixelChain), getPosition(pixelMap, mockPixelChain).add(tangentVector));
////        }
////        return tangent;
////    }

    @Test
    public void calcTangent_00() {
         // GIVEN start and end segments null
        int pixelIndex = 5;
        int vertexIndex = 2;
        int startSegmentIndex = vertexIndex - 1;
        int endSegmentIndex = vertexIndex;
        var mockPixelChain = new MockPixelChainBuilder(11)
                .with_getSegment_returns(startSegmentIndex, null)
                .with_getSegment_returns(endSegmentIndex, null)
                .build();
        var mockPixelMap = new MockPixelMapBuilder().build();
        VertexData vertex = vertexService.createVertex(pixelMap, mockPixelChain, vertexIndex, pixelIndex);
        var underTest = new VertexService();
        // WHEN THEN
        var actual = underTest.calcTangent(pixelMap, mockPixelChain, vertex);
        assertEquals(null, actual);
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(startSegmentIndex);
        verify(mockPixelChain, times(1)).getSegment(endSegmentIndex);
//        verifyNoMoreInteractions(mockPixelChain, mockPixelMap);
    }

    @Test
    public void calcTangent_01() {
        // GIVEN end segment null
        int pixelIndex = 5;
        int vertexIndex = 2;
        int startSegmentIndex = vertexIndex - 1;
        int endSegmentIndex = vertexIndex;
        var mockStartSegment = mock(ISegment.class);
        var mockPixelChain = new MockPixelChainBuilder(11)
                .with_getSegment_returns(startSegmentIndex, mockStartSegment)
                .with_getSegment_returns(endSegmentIndex, null)
                .build();
        var mockPixelMap = new MockPixelMapBuilder().build();

        Line expected = mock(Line.class);
        when(mockStartSegment.getEndTangent(pixelMap, mockPixelChain)).thenReturn(expected);

        VertexData vertex = vertexService.createVertex(pixelMap, mockPixelChain, vertexIndex, pixelIndex);
        var underTest = new VertexService();
        // WHEN THEN
        var actual = underTest.calcTangent(pixelMap, mockPixelChain, vertex);
        assertEquals(expected, actual);
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(startSegmentIndex);
        verify(mockPixelChain, times(1)).getSegment(endSegmentIndex);
        verify(mockStartSegment, times(1)).getEndTangent(pixelMap, mockPixelChain);
//        verifyNoMoreInteractions(mockPixelChain, mockPixelMap, mockStartSegment);
    }

    @Test
    public void calcTangent_02() {
        // GIVEN start segment null
        int pixelIndex = 5;
        int vertexIndex = 2;
        int startSegmentIndex = vertexIndex - 1;
        int endSegmentIndex = vertexIndex;
        var mockEndSegment = mock(ISegment.class);
        var mockPixelChain = new MockPixelChainBuilder(11)
                .with_getSegment_returns(startSegmentIndex, null)
                .with_getSegment_returns(endSegmentIndex, mockEndSegment)
                .build();
        var mockPixelMap = new MockPixelMapBuilder().build();

        Line expected = mock(Line.class);
        Line tangent = mock(Line.class);
        when(tangent.getReverse()).thenReturn(expected);
        when(mockEndSegment.getStartTangent(mockPixelMap, mockPixelChain)).thenReturn(tangent);

        VertexData vertex = vertexService.createVertex(pixelMap, mockPixelChain, vertexIndex, pixelIndex);
        var underTest = new VertexService();
        // WHEN THEN
        var actual = underTest.calcTangent(mockPixelMap, mockPixelChain, vertex);
        assertEquals(expected, actual);
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(startSegmentIndex);
        verify(mockPixelChain, times(1)).getSegment(endSegmentIndex);
        verify(mockEndSegment, times(1)).getStartTangent(mockPixelMap, mockPixelChain);
//        verifyNoMoreInteractions(mockPixelChain, mockPixelMap, mockEndSegment);
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
        PixelMap mPixelMap = mock(PixelMap.class);

        public MockPixelMapBuilder() {
        }

        PixelMap build() {
            return mPixelMap;
        }
    }

}
