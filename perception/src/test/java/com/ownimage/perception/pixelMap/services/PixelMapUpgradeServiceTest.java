package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.Vertex;
import com.ownimage.perception.pixelMap.immutable.ImmutableCurveSegment;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutableStraightSegment;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.Node;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import org.jetbrains.annotations.NotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Constructor;
import java.util.logging.LogManager;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PixelMapUpgradeServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapUpgradeService underTest = context.getBean(PixelMapUpgradeService.class);
    private PixelChainService pixelChainService = context.getBean(PixelChainService.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory();
        LogManager.getLogManager().reset();
    }

    @Test
    public void upgradePixelChain() {
        // GIVEN
        var pixelChain = generatePixelChain(10, 10);
        // WHEN
        var actual = underTest.upgradePixelChain(pixelChain, 10);
        // THEN
        validateVertexes(pixelChain, actual);
        validateSegments(pixelChain, actual);
    }

    private void validateSegments(PixelChain pixelChain, PixelChain actual) {
        assertEquals(5, actual.getSegments().size());
        for (int i = 0; i < actual.getSegments().size(); i++) {
            assertTrue(actual.getSegment(i) instanceof ImmutableStraightSegment
                    || actual.getSegment(i) instanceof ImmutableCurveSegment);
            var expected = pixelChain.getSegment(i).toImmutable();
            assertEquals(expected, actual.getSegment(i).toImmutable());
        }
    }

    private void validateVertexes(PixelChain pixelChain, PixelChain actual) {
        assertEquals(6, actual.getVertexes().size());
        for (int i = 0; i < actual.getVertexes().size(); i++) {
            assertTrue(actual.getVertex(i).sameValue(pixelChain.getVertex(i)));
            assertSame(ImmutableVertex.class, actual.getVertex(i).getClass());
        }
    }

    private CurveSegment generateCurveSegment(
            int segmentIndex, double startPosition, @NotNull Point a, @NotNull Point b, @NotNull Point p1) {
        try {
            Constructor<CurveSegment> ctor = CurveSegment.class
                    .getDeclaredConstructor(int.class, double.class, Point.class, Point.class, Point.class);
            ctor.setAccessible(true);
            return ctor.newInstance(segmentIndex, startPosition, a, b, p1);
        } catch (Exception e) {
            return null;
        }
    }

    private StraightSegment generateStraigntSegment(
            int segmentIndex, double startPosition, @NotNull LineSegment lineSegment) {
        try {
            Constructor<StraightSegment> ctor = StraightSegment.class
                    .getDeclaredConstructor(int.class, double.class, LineSegment.class);
            ctor.setAccessible(true);
            return ctor.newInstance(segmentIndex, startPosition, lineSegment);
        } catch (Exception e) {
            return null;
        }
    }

    private ImmutablePixelChain generatePixelChain(int mapWidth, int mapHeight) {
        var pixelMap = Utility.createMap(mapWidth, mapHeight);
        var pixelChain = StrongReference.of(pixelChainService.createStartingPixelChain(pixelMap, new Node(5, 5)));
        // create segments
        IntStream.range(4, 9).boxed()
                .map(i -> {
                            int index = 3 + 2 * i;
                            double position = i + 2.2d;
                            var a = new Point(i / 10.0d, (i + 3) / 10.0d);
                            var b = new Point(i + 3 / 10.0d, (i + 7) / 10.0d);
                            var p1 = new Point(i + 7 / 10.0d, (i + 11) / 10.0d);
                            var lineSegment = new LineSegment(a, b);
                            if ((i & 1) == 0) {
                                return generateCurveSegment(index, position, a, b, p1);
                            }
                            return generateStraigntSegment(index, position, lineSegment);
                        }
                )
                .forEach(s -> pixelChain.update(pc -> pc.changeSegments(segs -> segs.add(s))));
        // create vertexes
        IntStream.range(4, 9).boxed()
                .map(i -> {
                            var vertex = mock(Vertex.class);
                            when(vertex.getPixelIndex()).thenReturn(3 + 2 * i);
                            when(vertex.getVertexIndex()).thenReturn(i);
                            when(vertex.getPosition()).thenReturn(new Point(i / 10.0d, (i + 3) / 10.0d));
                            return vertex;
                        }
                )
                .forEach(v -> pixelChain.update(pc -> pc.changeVertexes(vs -> vs.add(v))));
        assertEquals(6, pixelChain.get().getVertexes().size());
        assertEquals(5, pixelChain.get().getSegments().size());
        return pixelChain.get();
    }

    @Test
    public void upgradeVertexes() {
        // GIVEN
        var pixelChain = generatePixelChain(10, 10);
        // WHEN
        var actual = underTest.upgradeVertexes(pixelChain, 10);
        // THEN
        validateVertexes(pixelChain, actual);
    }

    @Test
    public void upgradeSegments() {
        // GIVEN
        var pixelChain = generatePixelChain(10, 10);
        // WHEN
        var actual = underTest.upgradeSegments(pixelChain);
        // THEN
        validateSegments(pixelChain, actual);
    }

}