package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.persist.SortedProperties;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PixelMapServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapService underTest = context.getBean(PixelMapService.class);
    private PixelService pixelService = context.getBean(PixelService.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Test
    public void read() throws IOException {
        // GIVEN WHEN
        var actual = readPixelMap();
        // THEN
        assertEquals(1074, actual.width());
        assertEquals(1520, actual.height());
        assertEquals(3742, actual.pixelChains().size());
        assertEquals(88768, actual.data().size());
        validatePixels(actual);
    }

    private void validatePixels(ImmutablePixelMap actual) {
        var count = actual.pixelChains().stream()
                .flatMap(PixelChain::streamPixels)
                .filter(p -> !(p instanceof Pixel))
                .count();
        assertEquals(0, count);
    }

    private ImmutablePixelMap readPixelMap() throws IOException {
        var is = getClass().getResourceAsStream("NY2.transform");
        var db = new PersistDB(is);
        return underTest.read(db, "transform.2.cannyEdge");
    }

    @Test
    public void write() throws IOException {
        // GIVEN
        var pixelMap = readPixelMap();
        var db = new SortedProperties();
        var id = "test";
        // WHEN
        underTest.write(pixelMap, db, id);
        // THEN
        var actual = underTest.read(db, id);
        assertEquals(1074, actual.width());
        assertEquals(1520, actual.height());
        assertEquals(3742, actual.pixelChains().size());
        assertEquals(88768, actual.data().size());
    }

    @Test
    public void nodeAdd_00() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);
        var position = ImmutableIXY.of(5, 5);
        assertTrue(underTest.getNode(pixelMap, position).isEmpty());
        // WHEN
        var actual = underTest.nodeAdd(pixelMap, position);
        // THEN
        assertTrue(underTest.getNode(actual, position).isPresent());
        assertTrue(pixelService.isEdge(actual, position));
        assertTrue(pixelService.isNode(actual, position));
    }

    @Test
    public void nodeAdd_01() {
        // GIVEN
        var position = ImmutableIXY.of(5, 5);
        var pixelMap = Utility.createMap(10, 10);
        pixelMap = underTest.nodeAdd(pixelMap, position);
        var expectedNode = underTest.getNode(pixelMap, position).orElseThrow();
        // WHEN
        var actual = underTest.nodeAdd(pixelMap, position);
        // THEN
        assertSame(pixelMap,  actual);
        assertSame(expectedNode, underTest.getNode(actual, position).orElseThrow());
        assertTrue(pixelService.isEdge(actual, position));
        assertTrue(pixelService.isNode(actual, position));
    }

    @Test
    public void nodeRemove_00() {
        // GIVEN
        var pixelMap = Utility.createMap(10, 10);
        var position = ImmutableIXY.of(5, 5);
        // WHEN
        var actual = underTest.nodeRemove(pixelMap, position);
        // THEN
        assertTrue(underTest.getNode(actual, position).isEmpty());
        assertFalse(pixelService.isEdge(actual, position));
        assertFalse(pixelService.isNode(actual, position));
    }

    @Test
    public void nodeRemove_01() {
        // GIVEN
        var position = ImmutableIXY.of(5, 5);
        var pixelMap = Utility.createMap(10, 10);
        pixelMap = underTest.nodeAdd(pixelMap, position);
        // WHEN
        var actual = underTest.nodeRemove(pixelMap, position);
        // THEN
        assertTrue(underTest.getNode(actual, position).isEmpty());
        assertTrue(pixelService.isEdge(actual, position));
        assertFalse(pixelService.isNode(actual, position));
    }

    @Test
    public void nodeRemove_02() {
        // GIVEN
        var position = ImmutableIXY.of(5, 5);
        var pixelMap = Utility.createMap(10, 10);
        pixelMap = underTest.nodeAdd(pixelMap, position);
        pixelMap = underTest.nodeRemove(pixelMap, position);
        // WHEN
        var actual = underTest.nodeRemove(pixelMap, position);
        // THEN
        assertSame(pixelMap, actual);
        assertTrue(underTest.getNode(actual, position).isEmpty());
        assertTrue(pixelService.isEdge(actual, position));
        assertFalse(pixelService.isNode(actual, position));
    }
}
