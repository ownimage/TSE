package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.immutable.Segment;
import com.ownimage.perception.pixelMap.immutable.Vertex;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.util.Optional;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PixelChainServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelChainService underTest = context.getBean(PixelChainService.class);

    @BeforeAll
    public static void turnLoggingOff() {
        LogManager.getLogManager().reset();
    }

    public ImmutablePixelChain createPixelChain() {
        String[] input = {
                "           ",
                "    N      ",
                "    E      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "           ",
        };
        var pixelMap = Utility.createMap(input, true);
        assertEquals(1, pixelMap.pixelChains().size());
        var pixelChain = pixelMap.pixelChains().stream().findFirst().orElseThrow();
        assertEquals(5, pixelChain.pixelCount());
        assertEquals(1, pixelChain.segmentCount());
        return pixelChain;
    }

    @ParameterizedTest
    @MethodSource("com.ownimage.perception.pixelMap.Utility#testColors")
    public void changeColor_00(@NotNull Color color) {
        // GIVEN
        var pixelChain = createPixelChain();
        assertEquals(Optional.empty(), pixelChain.color());
        // WHEN
        var actual = underTest.changeColor(pixelChain, color);
        // THEN
        assertEquals(color, actual.color().get());
        Utility.assertIdenticalPixels(pixelChain, actual);
        Utility.assertIdenticalSegments(pixelChain, actual);
    }

    @ParameterizedTest
    @MethodSource("com.ownimage.perception.pixelMap.Utility#testColors")
    public void changeColor_01(@NotNull Color color) {
        // GIVEN
        var pixelChain = createPixelChain().withColor(color);
        assertEquals(color, pixelChain.color().get());
        // WHEN
        var actual = underTest.changeColor(pixelChain, color);
        // THEN
        assertTrue(pixelChain == actual);
    }

    @Test
    public void resequence() {
        // GIVEN a PixelChain with the Segments and Vertexes messed up
        var pixelMap = mock(PixelMap.class);
        var vertexes = new ImmutableVectorClone<Vertex>()
                .add(ImmutableVertex.of(0, 0, new Point(0, 3)))
                .add(ImmutableVertex.of(0, 10, new Point(1, 4)))
                .add(ImmutableVertex.of(0, 20, new Point(2, 5)));
        var pixelChainIn = ImmutablePixelChain.of(new ImmutableVectorClone<Pixel>(), vertexes, new ImmutableVectorClone<Segment>(), 0.0d, Thickness.Normal);
        var segments = new ImmutableVectorClone<Segment>()
                .add(SegmentFactory.createTempStraightSegment(pixelChainIn, 0))
                .add(SegmentFactory.createTempStraightSegment(pixelChainIn, 0));
        pixelChainIn = pixelChainIn.changeSegments(s -> segments);
        // WHEN
        var pixelChainOut = underTest.resequence(pixelMap, pixelChainIn);
        // THEN sequence is good
        assertEquals(3, pixelChainOut.vertexes().size());
        for (int i = 0; i < pixelChainIn.vertexes().size(); i++) {
            assertEquals(pixelChainIn.getVertex(i).getPixelIndex(), pixelChainOut.getVertex(i).getPixelIndex());
            assertEquals(pixelChainIn.getVertex(i).getPosition(), pixelChainOut.getVertex(i).getPosition());
            assertEquals(i, pixelChainOut.getVertex(i).getVertexIndex());
        }
        assertEquals(2, pixelChainOut.segments().size());
        double startPosition = 0.0d;
        for (int i = 0; i < pixelChainIn.segments().size(); i++) {
            assertEquals(i, pixelChainOut.getSegment(i).getSegmentIndex());
            assertEquals(pixelChainIn.getSegment(i).getClass(), pixelChainOut.getSegment(i).getClass());
            assertEquals(startPosition, pixelChainOut.getSegment(i).getStartPosition(), 0.0d);
            startPosition += pixelChainOut.getSegment(i).getLength(pixelMap, pixelChainOut);
        }
    }
}