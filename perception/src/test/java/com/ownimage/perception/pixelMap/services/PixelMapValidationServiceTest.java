package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PixelMapValidationServiceTest {

    private static ImmutablePixelMapData pixelMap;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelChainService pixelChainService = context.getBean(PixelChainService.class);
    private PixelMapValidationService underTest = context.getBean(PixelMapValidationService.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory();

        String[] input = {
                "    N      ",
                "    E      ",
                "    E      ",
                "    E      ",
                "    N      ",
                "NEEN NEEN  ",
                "           ",
        };
        pixelMap = Utility.createMap(input, true);
    }

    @Test
    public void validate() {
        // GIVEN
        // WHEN
        underTest.validate(pixelMap);
        // THEN no exceptions
        assertEquals(3, pixelMap.pixelChains().size());
    }

    @Test
    public void checkAllPixelMapNodesReferencePixelChainsInPixelMap() {
        // GIVEN
        var broken = pixelMap.withPixelChains(pixelMap.pixelChains().clear());
        thrown.expectMessage("checkAllPixelMapNodesReferencePixelChainsInPixelMap");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void checkPixelChainEndsReferenceNodesThatReferenceThePixelChain() {
        // GIVEN
        IntegerPoint point = new IntegerPoint(4, 0);
        var broken = pixelMap.withNodes(
                pixelMap.nodes()
                        .remove(point)
                        .put(point, new Node(point)));
        thrown.expectMessage("checkPixelChainEndsReferenceNodesThatReferenceThePixelChain");
        // WHEN
        underTest.checkPixelChainEndsReferenceNodesThatReferenceThePixelChain(broken);
        // THEN
    }

    @Test
    public void checkAllPixelsChainsHaveValidNodeEnds() {
        // GIVEN
        var pixelChain = new PixelChain(pixelMap, new Node(4, 0));
        pixelChain = pixelChainService.add(pixelChain, new Pixel(3, 0));
        pixelChain = pixelChainService.add(pixelChain, new Node(2, 0));
        var broken = pixelMap.withPixelChains(pixelMap.pixelChains().add(pixelChain));
        thrown.expectMessage("checkAllPixelsChainsHaveValidNodeEnds");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void checkAllDataEdgesHave2Neighbours() {
        // GIVEN
        var broken = pixelMap.withData(pixelMap.data().set(3, 2, EDGE));
        thrown.expectMessage("checkAllDataEdgesHave2Neighbours");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void checkAllDataNodesShouldBeNodes() {
        // GIVEN
        var broken = pixelMap.withData(pixelMap.data().set(4, 1, (byte) (EDGE | NODE)));
        thrown.expectMessage("checkAllDataNodesShouldBeNodes");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void isSingleton_01() {
        // GIVEN
        IntegerPoint singletonPoint = new IntegerPoint(5, 5);
        var pixelMap = Utility.createMap(10, 10);
        pixelMap = pixelMap.withData(pixelMap.data().set(5, 5, (byte) (EDGE | NODE)));
        pixelMap = pixelMap.withNodes(pixelMap.nodes().put(singletonPoint, new Node(singletonPoint)));
        // WHEN THEN
        assertTrue(underTest.isSingleton(pixelMap, singletonPoint));
    }

    @Test
    public void isSingleton_02() {
        // GIVEN
        IntegerPoint singletonPoint = new IntegerPoint(5, 5);
        var pixelMap = Utility.createMap(10, 10);
        // WHEN THEN
        assertFalse(underTest.isSingleton(pixelMap, singletonPoint));
    }

    @Test
    public void isSingleton_03() {
        // GIVEN
        IntegerPoint point = new IntegerPoint(0, 3);
        // WHEN THEN
        assertFalse(underTest.isSingleton(pixelMap, point));
    }

    @Test
    public void isInBounds_01() {
        // GIVEN
        IntegerPoint point = new IntegerPoint(0, 3);
        // WHEN THEN
        assertTrue(underTest.isInBounds(pixelMap, point));
    }

    @Test
    public void isInBounds_02() {
        // GIVEN
        IntegerPoint point = new IntegerPoint(20, 20);
        // WHEN THEN
        assertFalse(underTest.isInBounds(pixelMap, point));
    }

    @Test
    public void checkNoPixelMapNodesAreSingletons() {
        // GIVEN
        var singleton = new IntegerPoint(1, 1);
        var broken = pixelMap.withData(pixelMap.data()
                .set(singleton.getX(), singleton.getY(), (byte) (NODE | EDGE))
        );
        broken = broken.withNodes(broken.nodes().put(singleton, new Node(singleton)));
        thrown.expectMessage("checkNoPixelMapNodesAreSingletons");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void checkAllDataNodesArePixelMapNodesOrSingletons() {
        // GIVEN
        var broken = pixelMap.withData(pixelMap.data()
                .set(1, 1, (byte) (NODE | EDGE))
                .set(1, 2, (byte) (NODE | EDGE))
        );
        thrown.expectMessage("checkAllDataNodesArePixelMapNodesOrSingletons");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void checkPixelMapNodesKeyMatchesValue() {
        // GIVEN
        var broken = pixelMap.withNodes(pixelMap.nodes()
                .put(new IntegerPoint(1, 1), new Node(1, 2))
        );
        thrown.expectMessage("checkPixelMapNodesKeyMatchesValue");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void checkAllPixelMapNodesAreDataNodes() {
        // GIVEN
        IntegerPoint singletonPoint = new IntegerPoint(5, 5);
        var broken = pixelMap.withNodes(pixelMap.nodes().put(singletonPoint, new Node(singletonPoint)));
        thrown.expectMessage("checkAllPixelMapNodesAreDataNodes");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

    @Test
    public void checkAllDataNodesAreDataEdges() {
        // GIVEN
        var broken = pixelMap.withData(pixelMap.data().set(4, 0, NODE));
        thrown.expectMessage("checkAllDataNodesAreDataEdges");
        // WHEN
        underTest.validate(broken);
        // THEN
    }

}