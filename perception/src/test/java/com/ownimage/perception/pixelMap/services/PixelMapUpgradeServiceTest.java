package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.Vertex;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory();
        LogManager.getLogManager().reset();
    }

    @Test
    public void upgradeVertex() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);
        var pixelChain = StrongReference.of(new PixelChain(pixelMap, new Node(5, 5)));
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
        // WHEN
        var actual = underTest.upgradeVertex(pixelChain.get());
        // THEN
        assertEquals(6, actual.getVertexes().size());
        for (int i = 0; i < actual.getVertexes().size(); i++) {
            assertTrue(actual.getVertex(i).sameValue(pixelChain.get().getVertex(i)));
            assertSame(ImmutableVertex.class, actual.getVertex(i).getClass());
        }
    }
}