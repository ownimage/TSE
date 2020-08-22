package com.ownimage.framework.util.immutable;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class ImmutableMapTest {

    @Test
    public void clear_01() {
        // GIVEN
        var key = 1;
        var value = "one";
        var underTest = new ImmutableMap<Integer, String>().put(key, value);
        assertEquals(underTest.get(key), value);
        // WHEN
        var actual = underTest.clear();
        // THEN
        assertEquals(underTest.get(key), value);
        assertEquals(actual.get(key), null);
        assertEquals(underTest.get(key), value);
        assertEquals(actual.get(key), null);
    }

    @Test
    public void put_01() {
        // GIVEN
        var key = 1;
        var value1 = "one";
        var value2 = "two";
        var underTest = new ImmutableMap<Integer, String>().put(key, value1);
        assertEquals(underTest.get(key), value1);
        // WHEN
        var actual = underTest.put(key, value2);
        // THEN
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), value2);
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), value2);
    }

    @Test
    public void put_02() {
        // GIVEN
        var underTest = new ImmutableMap<Integer, String>();
        underTest = underTest.put(1, "one");
        underTest = underTest.put(2, "two");
        assertEquals(underTest.get(1), "one");
        assertEquals(underTest.get(2), "two");
        // WHEN
        var actual = underTest.put(1, "ONE");
        // THEN
        assertEquals(underTest.get(1), "one");
        assertEquals(underTest.get(2), "two");

        assertEquals(actual.get(1), "ONE");
        assertEquals(actual.get(2), "two");

        assertEquals(underTest.get(1), "one");
        assertEquals(underTest.get(2), "two");

        assertEquals(actual.get(1), "ONE");
        assertEquals(actual.get(2), "two");
    }

    @Test
    public void update_01() {
        // GIVEN
        var key = "one";
        var value1 = 1;
        var value2 = 2;
        var underTest = new ImmutableMap<String, Integer>().put(key, value1);
        assertEquals((long) underTest.get(key), value1);
        // WHEN
        var actual = underTest.update(key, (k, v) -> value2);
        // THEN
        assertEquals((long) underTest.get(key), value1);
        assertEquals((long) actual.get(key), value2);
        assertEquals((long) underTest.get(key), value1);
        assertEquals((long) actual.get(key), value2);
    }

    @Test
    public void remove_01() {
        // GIVEN
        var key = 1;
        var value1 = "one";
        var underTest = new ImmutableMap<Integer, String>().put(key, value1);
        assertEquals(underTest.get(key), value1);
        // WHEN
        var actual = underTest.remove(key);
        // THEN
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), null);
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), null);
    }

    @Test
    public void remove_02() {
        // GIVEN
        var key = 1;
        var key2 = 2;
        var value1 = "one";
        var underTest = new ImmutableMap<Integer, String>().put(key, value1);
        assertEquals(underTest.get(key), value1);
        // WHEN
        var actual = underTest.remove(key2);
        // THEN
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), "one");
        assertEquals(underTest.get(key2), null);
        assertEquals(actual.get(key2), null);

        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), "one");
        assertEquals(underTest.get(key2), null);
        assertEquals(actual.get(key2), null);
    }

    @Test
    public void size_01() {
        // GIVEN
        var underTest = new ImmutableMap<Integer, String>();
        // WHEN
        var actual = underTest.size();
        // THEN
        assertEquals(actual, 0);
    }

    @Test
    public void size_02() {
        // GIVEN
        var underTest = new ImmutableMap<Integer, String>()
                .put(1, "one");
        // WHEN
        var actual = underTest.size();
        // THEN
        assertEquals(actual, 1);
    }

    @Test
    public void size_03() {
        // GIVEN
        var underTest = new ImmutableMap<Integer, String>()
                .put(1, "one")
                .put(1, "two");
        // WHEN
        var actual = underTest.size();
        // THEN
        assertEquals(actual, 1);
    }

    @Test
    public void size_04() {
        // GIVEN
        var underTest = new ImmutableMap<Integer, String>()
                .put(1, "one")
                .put(2, "two");
        // WHEN
        var actual = underTest.size();
        // THEN
        assertEquals(actual, 2);
    }

    @Test
    public void size_05() {
        // GIVEN
        var underTest = new ImmutableMap<Integer, String>()
                .put(1, "one")
                .put(2, "two")
                .clear();
        // WHEN
        var actual = underTest.size();
        // THEN
        assertEquals(actual, 0);
    }

    @Test
    public void mapConstructor() {
        // GIVEN an existing map
        var existingMap = new HashMap<Integer, String>();
        existingMap.put(1, "one");
        existingMap.put(2, "two");
        existingMap.put(3, "three");
        // WHEN a new ImmutableMap is constructed
        var underTest = new ImmutableMap(existingMap);
        // THEN the mappings should exist
        assertEquals("one", underTest.get(1));
        assertEquals("two", underTest.get(2));
        assertEquals("three", underTest.get(3));
        assertEquals(3, underTest.size());
        // AND WHEN the original map is modified
        existingMap.put(1, "other");
        existingMap.remove(2);
        // THEN the ImmutableMap should be unchanged
        assertEquals("one", underTest.get(1));
        assertEquals("two", underTest.get(2));
        assertEquals("three", underTest.get(3));
        assertEquals(3, underTest.size());
    }

    @Test
    public void toHashMap() {
        // GIVEN an ImmutableMap
        var originalMap = new HashMap<Integer, String>();
        originalMap.put(1, "one");
        originalMap.put(2, "two");
        originalMap.put(3, "three");
        var underTest = new ImmutableMap(originalMap);
        // WHEN converted to HashMap
        var actual = underTest.toHashMap();
        // THEN the mappings should exist
        assertEquals("one", actual.get(1));
        assertEquals("two", actual.get(2));
        assertEquals("three", actual.get(3));
        assertEquals(3, actual.size());
        // AND WHEN the originalMap and the ImmutableMap are modified
        originalMap.put(1, "other");
        originalMap.remove(2);
        underTest = underTest.put(1, "other");
        underTest = underTest.remove(2);
        // THEN the returned HashMap should be unchanged
        assertEquals("one", actual.get(1));
        assertEquals("two", actual.get(2));
        assertEquals("three", actual.get(3));
        assertEquals(3, actual.size());
    }
}
