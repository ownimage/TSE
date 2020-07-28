package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.Vertex;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class PixelChainServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelChainService underTest = context.getBean(PixelChainService.class);

    @Test
    public void resequence() {
        // GIVEN a PixelChain with the Segments and Vertexes messed up
        var pixelMap = mock(PixelMapData.class);
        var vertexes = new ImmutableVectorClone<IVertex>()
                .add(new Vertex(0, 0, new Point(0, 3)))
                .add(new Vertex(0, 10, new Point(1, 4)))
                .add(new Vertex(0, 20, new Point(2, 5)));
        var pixelChainIn = new PixelChain(new ImmutableVectorClone<Pixel>(), new ImmutableVectorClone<ISegment>(), vertexes, 0.0d, IPixelChain.Thickness.Normal);
        var segments = new ImmutableVectorClone<ISegment>()
                .add(SegmentFactory.createTempStraightSegment(pixelMap, pixelChainIn, 0))
                .add(SegmentFactory.createTempStraightSegment(pixelMap, pixelChainIn, 0));
        pixelChainIn = pixelChainIn.changeSegments(s -> segments);
        // WHEN
        var pixelChainOut = underTest.resequence(pixelMap, pixelChainIn);
        // THEN sequence is good
        assertEquals(3, pixelChainOut.getVertexes().size());
        for (int i = 0; i < pixelChainIn.getVertexes().size(); i++) {
            assertEquals(pixelChainIn.getVertex(i).getPixelIndex(), pixelChainOut.getVertex(i).getPixelIndex());
            assertEquals(pixelChainIn.getVertex(i).getPosition(), pixelChainOut.getVertex(i).getPosition());
            assertEquals(i, pixelChainOut.getVertex(i).getVertexIndex());
        }
        assertEquals(2, pixelChainOut.getSegments().size());
        double startPosition = 0.0d;
        for (int i = 0; i < pixelChainIn.getSegments().size(); i++) {
            assertEquals(i, pixelChainOut.getSegment(i).getSegmentIndex());
            assertEquals(pixelChainIn.getSegment(i).getClass(), pixelChainOut.getSegment(i).getClass());
            assertEquals(startPosition, pixelChainOut.getSegment(i).getStartPosition(), 0.0d);
            startPosition += pixelChainOut.getSegment(i).getLength(pixelMap, pixelChainOut);
        }
    }
}