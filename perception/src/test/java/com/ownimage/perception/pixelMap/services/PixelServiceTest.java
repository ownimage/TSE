package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.logging.LogManager;

import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static com.ownimage.perception.pixelMap.PixelConstants.NONE;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PixelServiceTest extends TestCase {

    private final int x = 3;
    private final int y = 4;
    private final ImmutableIXY integerPoint = ImmutableIXY.of(x, y);
    private PixelService underTest = new PixelService();
    @Mock
    private PixelMap pixelMap;
    @Mock
    private ImmutableMap2D<Byte> data;

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }


    @Test
    public void test_isNode_01() {
        // GIVEN a pixelMap that returns NONE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NONE);
        // WHEN
        var actual = underTest.isNode(pixelMap, x, y);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isNode_02() {
        // GIVEN a pixelMap that returns EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(EDGE);
        // WHEN
        var actual = underTest.isNode(pixelMap, x, y);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isNode_03() {
        // GIVEN a pixelMap that returns NODE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NODE);
        // WHEN
        var actual = underTest.isNode(pixelMap, x, y);
        // THEN
        assertEquals(true, actual);
    }

    @Test
    public void test_isNode_04() {
        // GIVEN a pixelMap that returns NODE + EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn((byte) (NODE + EDGE));
        // WHEN
        var actual = underTest.isNode(pixelMap, x, y);
        // THEN
        assertEquals(true, actual);
    }

    @Test
    public void test_isEdge_01() {
        // GIVEN a pixelMap that returns NONE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NONE);
        // WHEN
        var actual = underTest.isEdge(pixelMap, x, y);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isEdge_02() {
        // GIVEN a pixelMap that returns EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(EDGE);
        // WHEN
        var actual = underTest.isEdge(pixelMap, x, y);
        // THEN
        assertEquals(true, actual);
    }

    @Test
    public void test_isEdge_03() {
        // GIVEN a pixelMap that returns NODE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NODE);
        // WHEN
        var actual = underTest.isEdge(pixelMap, x, y);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isEdge_04() {
        // GIVEN a pixelMap that returns NODE + EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn((byte) (NODE + EDGE));
        // WHEN
        var actual = underTest.isEdge(pixelMap, x, y);
        // THEN
        assertEquals(true, actual);
    }

    @Test
    public void test_isNode_05() {
        // GIVEN a pixelMap that returns NONE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NONE);
        // WHEN
        var actual = underTest.isNode(pixelMap, integerPoint);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isNode_06() {
        // GIVEN a pixelMap that returns EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(EDGE);
        // WHEN
        var actual = underTest.isNode(pixelMap, integerPoint);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isNode_07() {
        // GIVEN a pixelMap that returns NODE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NODE);
        // WHEN
        var actual = underTest.isNode(pixelMap, integerPoint);
        // THEN
        assertEquals(true, actual);
    }

    @Test
    public void test_isNode_08() {
        // GIVEN a pixelMap that returns NODE + EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn((byte) (NODE + EDGE));
        // WHEN
        var actual = underTest.isNode(pixelMap, integerPoint);
        // THEN
        assertEquals(true, actual);
    }

    @Test
    public void test_isEdge_05() {
        // GIVEN a pixelMap that returns NONE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NONE);
        // WHEN
        var actual = underTest.isEdge(pixelMap, integerPoint);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isEdge_06() {
        // GIVEN a pixelMap that returns EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(EDGE);
        // WHEN
        var actual = underTest.isEdge(pixelMap, integerPoint);
        // THEN
        assertEquals(true, actual);
    }

    @Test
    public void test_isEdge_07() {
        // GIVEN a pixelMap that returns NODE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn(NODE);
        // WHEN
        var actual = underTest.isEdge(pixelMap, integerPoint);
        // THEN
        assertEquals(false, actual);
    }

    @Test
    public void test_isEdge_08() {
        // GIVEN a pixelMap that returns NODE + EDGE
        when(pixelMap.data()).thenReturn(data);
        when(pixelMap.height()).thenReturn(10);
        when(pixelMap.width()).thenReturn(10);
        when(data.get(x, y)).thenReturn((byte) (NODE + EDGE));
        // WHEN
        var actual = underTest.isEdge(pixelMap, integerPoint);
        // THEN
        assertEquals(true, actual);
    }
}