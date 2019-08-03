package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.segment.ISegment;
import org.junit.Test;

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

public class VertexTest {

//
//    @Override
//    public Line calcTangent(final PixelChain pPixelChain, final PixelMap pPixelMap) {
//    public int compareTo(final IVertex pOther) {
//        return mPixelIndex - pOther.getPixelIndex();
//    }
//

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
        IVertex underTest = Vertex.createVertex(mockPixelChain, 1, index);
        // THEN
        assertEquals(index, underTest.getPixelIndex());
        assertEquals(1, underTest.getVertexIndex());
        assertEquals(point, underTest.getUHVWPoint(mockPixelMap, mockPixelChain));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getPixel(index);
        verify(mockPixel, times(1)).getUHVWMidPoint(mockPixelMap);
        verifyNoMoreInteractions(mockPixelMap, mockPixelChain, mockPixel);
    }

    @Test
    public void ctor_01() {
        // GIVEN
        int index = 6;
        Point pointCtor = new Point(0.4, 0.5);
        var mockPixelMap = new MockPixelMapBuilder().build();
        var mockPixelChain = new MockPixelChainBuilder(10).build();
        // WHEN
        IVertex underTest = Vertex.createVertex(mockPixelChain, 1, index, pointCtor);
        // THEN
        assertEquals(index, underTest.getPixelIndex());
        assertEquals(1, underTest.getVertexIndex());
        assertEquals(pointCtor, underTest.getUHVWPoint(mockPixelMap, mockPixelChain));
        verify(mockPixelChain, times(1)).getPixelCount();
        verifyNoMoreInteractions(mockPixelMap, mockPixelChain);
    }

    @Test
    public void equals_00() {
        // GIVEN
        var pixelChain1 = new MockPixelChainBuilder(10).build();
        var pixelChain2 = new MockPixelChainBuilder(11).build();
        IVertex underTest1 = Vertex.createVertex(pixelChain1, 1, 5);
        IVertex underTest2 = Vertex.createVertex(pixelChain2, 1, 5);
        // WHEN THEN
        assertTrue(underTest1.equals(underTest2));
        assertTrue(underTest2.equals(underTest1));
        verify(pixelChain1, times(1)).getPixelCount();
        verify(pixelChain2, times(1)).getPixelCount();
        verifyNoMoreInteractions(pixelChain1, pixelChain2);
    }

    @Test
    public void equals_02() {
        // GIVEN
        var pixelChain1 = new MockPixelChainBuilder(10).build();
        var pixelChain2 = new MockPixelChainBuilder(11).build();
        IVertex underTest1 = Vertex.createVertex(pixelChain1, 1, 5, new Point(0.2, 0.3));
        IVertex underTest2 = Vertex.createVertex(pixelChain2, 1, 5, new Point(0.2, 0.3));
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
        IVertex underTest1 = Vertex.createVertex(pixelChain1, 1, 5, new Point(0.2, 0.3));
        IVertex underTest2 = Vertex.createVertex(pixelChain2, 1, 5, new Point(0.4, 0.5));
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
        IVertex underTest1 = Vertex.createVertex(pixelChain, 1, pixelIndex);
        var expected = "Vertex { vertexIndex: 1, pixelIndex: 5 }";
        // WHEN THEN
        assertEquals(expected, underTest1.toString());
        verify(pixelChain, times(1)).getPixelCount();
        verifyNoMoreInteractions(pixelChain);
    }

    @Test
    public void test_toString_01() {
        // GIVEN
        int pixelIndex = 5;
        var pixelChain = new MockPixelChainBuilder(11).build();
        IVertex underTest1 = Vertex.createVertex(pixelChain, 1, pixelIndex, new Point(0.5, 0.3));
        var expected = "Vertex { vertexIndex: 1, pixelIndex: 5, position: Point{ x : 0.5, y: 0.3 } }";
        // WHEN THEN
        assertEquals(expected, underTest1.toString());
        verify(pixelChain, times(1)).getPixelCount();
        verifyNoMoreInteractions(pixelChain);
    }

    @Test
    public void getPixel_00() {
        // GIVEN
        int pixelIndex = 5;
        var pixel = new MockPixelBuilder().build();
        var pixelChain = new MockPixelChainBuilder(11).with_getPixel_returns(pixelIndex, pixel).build();
        IVertex underTest1 = Vertex.createVertex(pixelChain, 1, pixelIndex);
        // WHEN THEN
        assertEquals(pixel, underTest1.getPixel(pixelChain));
        verify(pixelChain, times(1)).getPixelCount();
        verify(pixelChain, times(1)).getPixel(eq(pixelIndex));
        verifyNoMoreInteractions(pixelChain);
    }

    @Test
    public void getStartSegment_00() {
        // GIVEN
        int pixelIndex = 5;
        int vertexIndex = 2;
        int segmentIndex = vertexIndex - 1;
        ISegment expected = mock(ISegment.class);
        var mockPixelChain = new MockPixelChainBuilder(11)
                .with_getSegment_returns(segmentIndex, expected)
                .build();
        IVertex underTest = Vertex.createVertex(mockPixelChain, vertexIndex, pixelIndex);
        // WHEN THEN
        assertEquals(expected, underTest.getStartSegment(mockPixelChain));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(segmentIndex);
        verifyNoMoreInteractions(mockPixelChain, expected);
    }

    @Test
    public void getEndSegment_00() {
        // GIVEN
        int pixelIndex = 5;
        int vertexIndex = 2;
        int segmentIndex = vertexIndex;
        ISegment expected = mock(ISegment.class);
        var mockPixelChain = new MockPixelChainBuilder(11)
                .with_getSegment_returns(segmentIndex, expected)
                .build();
        IVertex underTest = Vertex.createVertex(mockPixelChain, vertexIndex, pixelIndex);
        // WHEN THEN
        assertEquals(expected, underTest.getEndSegment(mockPixelChain));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(segmentIndex);
        verifyNoMoreInteractions(mockPixelChain, expected);
    }

//    public Line calcTangent(final PixelChain pPixelChain, final PixelMap pPixelMap) {
//        Line tangent;
//        if (getStartSegment(pPixelChain) == null && getEndSegment(pPixelChain) == null) {
//            tangent = null;
//        } else if (getStartSegment(pPixelChain) == null) {
//            tangent = getEndSegment(pPixelChain).getStartTangent(pPixelMap, pPixelChain);
//            tangent = tangent.getReverse();
//        } else if (getEndSegment(pPixelChain) == null) {
//            tangent = getStartSegment(pPixelChain).getEndTangent(pPixelMap, pPixelChain);
//        } else {
//            final Point startTangentPoint = getStartSegment(pPixelChain).getEndTangent(pPixelMap, pPixelChain).getPoint(1.0d);
//            final Point endTangentPoint = getEndSegment(pPixelChain).getStartTangent(pPixelMap, pPixelChain).getPoint(1.0d);
//            final Vector tangentVector = startTangentPoint.minus(endTangentPoint).normalize();
//
//            tangent = new Line(getUHVWPoint(pPixelMap, pPixelChain), getUHVWPoint(pPixelMap, pPixelChain).add(tangentVector));
//        }
//        return tangent;
//    }

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
        IVertex underTest = Vertex.createVertex(mockPixelChain, vertexIndex, pixelIndex);
        // WHEN THEN
        assertEquals(null, underTest.calcTangent(mockPixelChain, mockPixelMap));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(startSegmentIndex);
        verify(mockPixelChain, times(1)).getSegment(endSegmentIndex);
        verifyNoMoreInteractions(mockPixelChain, mockPixelMap);
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
        when(mockStartSegment.getEndTangent(mockPixelMap, mockPixelChain)).thenReturn(expected);

        IVertex underTest = Vertex.createVertex(mockPixelChain, vertexIndex, pixelIndex);
        // WHEN THEN
        assertEquals(expected, underTest.calcTangent(mockPixelChain, mockPixelMap));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(startSegmentIndex);
        verify(mockPixelChain, times(1)).getSegment(endSegmentIndex);
        verify(mockStartSegment, times(1)).getEndTangent(mockPixelMap, mockPixelChain);
        verifyNoMoreInteractions(mockPixelChain, mockPixelMap, mockStartSegment);
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

        IVertex underTest = Vertex.createVertex(mockPixelChain, vertexIndex, pixelIndex);
        // WHEN THEN
        assertEquals(expected, underTest.calcTangent(mockPixelChain, mockPixelMap));
        verify(mockPixelChain, times(1)).getPixelCount();
        verify(mockPixelChain, times(1)).getSegment(startSegmentIndex);
        verify(mockPixelChain, times(1)).getSegment(endSegmentIndex);
        verify(mockEndSegment, times(1)).getStartTangent(mockPixelMap, mockPixelChain);
        verifyNoMoreInteractions(mockPixelChain, mockPixelMap, mockEndSegment);
    }

    @Test
    public void calcTangent_03() {
//        neither null
//            complex calculation
        //fail();
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
